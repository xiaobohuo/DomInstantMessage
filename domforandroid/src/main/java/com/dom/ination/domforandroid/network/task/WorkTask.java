package com.dom.ination.domforandroid.network.task;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.Process;
import android.text.TextUtils;

import com.dom.ination.domforandroid.common.utils.Logger;

import java.util.ArrayDeque;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.FutureTask;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by huoxiaobo on 16/8/23.
 */
public abstract class WorkTask<Params, Progress, Result> {
    private final static String TAG = "WorkTask";

    //加载图片默认10个进程
    private final static int CORE_IMAGE_POOL_SIZE = 10;

    //默认核心进程5个
    private final static int CORE_POOL_SIZE = 5;

    //默认执行最大进程128个
    private final static int MAXIMUM_POOL_SIZE = 128;
    private final static int KEEP_ALIVE = 1;

    private TaskException exception;

    private boolean cancelByUser;

    private static final ThreadFactory sThreadFactory = new ThreadFactory() {
        private final AtomicInteger count = new AtomicInteger(1);

        @Override
        public Thread newThread(Runnable r) {
            return new Thread(r, "AsyncTask #" + count.getAndIncrement());
        }
    };

    //执行队列,默认10个,超过10个后开启新线程,如果大于MAXIMUM_POOL_SIZE,执行异常策略
    private static final BlockingQueue<Runnable> sPoolWorkQueue = new LinkedBlockingQueue<>(10);

    //默认线程池,最大执行CORE_POOL_SIZE + MAXIMUM_POOL_SIZE个线程
    public static final Executor THREAD_POOL_EXECUTOR = new ThreadPoolExecutor(CORE_POOL_SIZE, MAXIMUM_POOL_SIZE, KEEP_ALIVE, TimeUnit.SECONDS, sPoolWorkQueue, sThreadFactory);

    //固定大小为CORE_IMAGE_POOL_SIZE的线程池
    public static final Executor IMAGE_POOL_EXECUTOR = Executors.newFixedThreadPool(CORE_IMAGE_POOL_SIZE, sThreadFactory);

    //execute tasks one at a time in serial order
    public static final Executor SERIAL_EXECUTOR = new SerialExecutor();

    private final static int MESSAGE_POST_RESUTL = 0x01;
    private final static int MESSAGE_POST_PROGRESS = 0x02;

    private final static InternalHandler sHandler = new InternalHandler();

    private static volatile Executor sDefaultExecutor = SERIAL_EXECUTOR;
    private final WorkerRunnable<Params, Result> mWorker;
    private final FutureTask<Result> mFuture;

    private final AtomicBoolean mTaskInvoked = new AtomicBoolean();

    public static void init() {
        sHandler.getLooper();
    }

    private static void setsDefaultExecutor(Executor exec) {
        sDefaultExecutor = exec;
    }

    private volatile Status mStatus = Status.PENDING;

    public enum Status {
        PENDING, RUNNING, FINISHED
    }

    private String taskId;

    public String getTaskId() {
        return taskId;
    }

    public void setTaskId(String taskId) {
        this.taskId = taskId;
    }

    public WorkTask(String taskId, ITaskManager taskManager) {
        this();
        this.taskId = taskId;
        taskManager.addTask(this);
    }

    public WorkTask() {
        mWorker = new WorkerRunnable<Params, Result>() {
            @Override
            public Result call() throws Exception {
                mTaskInvoked.set(true);
                Process.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND);
                return postResult(doInBackground(mParams));
            }
        };

        mFuture = new FutureTask<Result>(mWorker) {
            @Override
            protected void done() {
                try {
                    final Result result = get();
                    postResultIfNotInvoked(result);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (ExecutionException e) {
                    e.printStackTrace();
                }
            }
        };
    }

    private void postResultIfNotInvoked(Result result) {
        final boolean wasTaskInvoked = mTaskInvoked.get();
        if (!wasTaskInvoked) {
            postResult(result);
        }
    }

    private Result postResult(Result result) {
        Message message = sHandler.obtainMessage(MESSAGE_POST_RESUTL, new AsyncTaskResult<Result>(this, result));
        message.sendToTarget();
        return result;
    }

    public final Status getStatus() {
        return mStatus;
    }

    protected void onPrepare() {

    }

    protected void onFailure(TaskException exception) {

    }

    protected void onSuccess(Result result) {

    }

    protected void onFinished() {
        Logger.d(TAG, String.format("%s --> onFinished()", TextUtils.isEmpty(taskId) ? "run" : (taskId + "run")));

    }

    abstract public Result workInBackground(Params... params) throws TaskException;

    private Result doInBackground(Params... params) {
        Logger.d(TAG, String.format("%s --> doInBackground()", TextUtils.isEmpty(taskId) ? "run" : (taskId + "run")));
        try {
            return workInBackground(params);
        } catch (TaskException exception) {
            exception.printStackTrace();
            this.exception = exception;
        }
        return null;
    }

    final protected void onPreExecute() {
        Logger.d(TAG, String.format("%s --->onTaskStarted()", TextUtils.isEmpty(taskId) ? "run " : (taskId + " run ")));
        onPrepare();
    }

    final protected void onPostExecute(Result result) {
        if (exception == null) {
            Logger.d(TAG, String.format("%s --->onTaskSuccess()", TextUtils.isEmpty(taskId) ? "run " : (taskId + " run ")));
            onSuccess(result);
        } else if (exception != null) {
            Logger.d(TAG, String.format("%s --->onFailure(), \nError msg --->", TextUtils.isEmpty(taskId) ? "run " : (taskId + " run "), exception.getMessage()));
            onFailure(exception);
        }

        onFinished();
    }

    protected void onProgressUpdate(Progress... values) {
    }

    protected void onCancelled(Result result) {
        _onCancelled();
    }

    private void _onCancelled() {
        onCancelled();

        onFinished();
    }

    protected void onCancelled() {
        Logger.d(TAG, String.format("%s --->onCancelled()", TextUtils.isEmpty(taskId) ? "run " : (taskId + " run ")));
    }

    public final boolean isCancelled() {
        return mFuture.isCancelled();
    }

    public boolean isCancelByUser() {
        return cancelByUser;
    }

    public boolean cancel(boolean mayInterruptIfRunning) {
        cancelByUser = true;

        return mFuture.cancel(mayInterruptIfRunning);
    }

    public final Result get() throws InterruptedException, ExecutionException {
        return mFuture.get();
    }

    public final Result get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
        return mFuture.get(timeout, unit);
    }

    public final WorkTask<Params, Progress, Result> executeOnSerialExecutor(Params... params) {
        return executeOnExecutor(SERIAL_EXECUTOR, params);
    }

    public final WorkTask<Params, Progress, Result> executrOnImageExecutor(Params... params) {
        return executeOnExecutor(IMAGE_POOL_EXECUTOR, params);
    }

    public final WorkTask<Params, Progress, Result> execute(Params... params) {
        return executeOnExecutor(THREAD_POOL_EXECUTOR, params);
    }

    public final WorkTask<Params, Progress, Result> executeOnExecutor(Executor exec, Params... params) {
        if (mStatus != Status.PENDING) {
            switch (mStatus) {
                case RUNNING:
                    throw new IllegalStateException("Cannot execute task:" + " the task is already running.");
                case FINISHED:
                    throw new IllegalStateException("Cannot execute task:" + " the task has already been executed "
                            + "(a task can be executed only once)");
            }
        }

        mStatus = Status.RUNNING;

        if (Looper.myLooper() == Looper.getMainLooper()) {
            onPreExecute();
        } else {
            sHandler.post(new Runnable() {

                @Override
                public void run() {
                    onPreExecute();
                }

            });
        }

        mWorker.mParams = params;
        exec.execute(mFuture);

        return this;
    }

    public static void execute(Runnable runnable) {
        sDefaultExecutor.execute(runnable);
    }

    protected final void publishProgress(Progress... values) {
        if (!isCancelled()) {
            sHandler.obtainMessage(MESSAGE_POST_PROGRESS, new AsyncTaskResult<Progress>(this, values)).sendToTarget();
        }
    }

    private void finish(Result result) {
        if (isCancelled()) {
            onCancelled(result);
        } else {
            onPostExecute(result);
        }
        mStatus = Status.FINISHED;
    }

    protected Params[] getParams() {
        return mWorker.mParams;
    }

    private static abstract class WorkerRunnable<Params, Result> implements Callable<Result> {
        Params[] mParams;
    }

    private static class AsyncTaskResult<Data> {
        final WorkTask mTask;
        final Data[] mData;

        AsyncTaskResult(WorkTask task, Data... data) {
            mTask = task;
            mData = data;
        }
    }

    private static class InternalHandler extends Handler {
        InternalHandler() {
            super(Looper.getMainLooper());
        }

        @Override
        public void handleMessage(Message msg) {
            AsyncTaskResult result = (AsyncTaskResult) msg.obj;
            switch (msg.what) {
                case MESSAGE_POST_RESUTL:
                    result.mTask.finish(result.mData[0]);
                    break;
                case MESSAGE_POST_PROGRESS:
                    result.mTask.onProgressUpdate(result.mData);
                    break;
            }
        }
    }

    private static class SerialExecutor implements Executor {
        final ArrayDeque<Runnable> mTasks = new ArrayDeque<>();
        Runnable mActive;

        @Override
        public synchronized void execute(final Runnable r) {
            mTasks.offer(new Runnable() {
                @Override
                public void run() {
                    try {
                        r.run();
                    } finally {
                        schedualNext();
                    }
                }
            });
        }

        protected synchronized void schedualNext() {
            if ((mActive = mTasks.poll()) != null) {
                THREAD_POOL_EXECUTOR.execute(mActive);
            }
        }
    }


}
