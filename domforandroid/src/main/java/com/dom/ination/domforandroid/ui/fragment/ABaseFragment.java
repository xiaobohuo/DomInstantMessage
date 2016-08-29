package com.dom.ination.domforandroid.ui.fragment;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.dom.ination.domforandroid.R;
import com.dom.ination.domforandroid.common.utils.Logger;
import com.dom.ination.domforandroid.component.bitmaploader.BitmapLoader;
import com.dom.ination.domforandroid.component.bitmaploader.core.BitmapOwner;
import com.dom.ination.domforandroid.network.biz.IResult;
import com.dom.ination.domforandroid.network.task.ITaskManager;
import com.dom.ination.domforandroid.network.task.TaskException;
import com.dom.ination.domforandroid.network.task.TaskManager;
import com.dom.ination.domforandroid.network.task.WorkTask;
import com.dom.ination.domforandroid.support.inject.InjectUtility;
import com.dom.ination.domforandroid.support.inject.ViewInject;
import com.dom.ination.domforandroid.ui.activity.BaseActivity;

import java.text.SimpleDateFormat;

/**
 * Created by huoxiaobo on 16/8/23.
 */
public abstract class ABaseFragment extends Fragment implements ITaskManager, BitmapOwner {

    static final String TAG = "ABaseFragment";

    public enum ABaseTaskState {
        none, prepare, failed, success, finished, canceled
    }

    private TaskManager taskManager;

    ViewGroup rootView; //根视图
    @ViewInject(idStr = "layoutLoading")
    View loadingLayout; //加载中视图
    @ViewInject(idStr = "layoutLoadFailed")
    View loadFailedLayout; //加载失败视图
    @ViewInject(idStr = "layoutContent")
    View contentLayout;  //内容视图
    @ViewInject(idStr = "layoutEmpty")
    View emptyLayout; //空视图

    //标志是否contentLayout为空
    private boolean contentEmpty = true;

    protected long lastResultGetTime = 0; //最后一次非缓存数据获取时间

    private boolean destroy = false;

    //UI线程Handler
    Handler mHandler = new Handler(Looper.getMainLooper());

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        if (activity instanceof BaseActivity) {
//            ((BaseActivity)activity).addFragment(toString(),this);
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        taskManager = new TaskManager();
        if (savedInstanceState != null) {
            taskManager.restore(savedInstanceState);
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (inflateContentView() > 0) {
            ViewGroup contentView = (ViewGroup) inflater.inflate(inflateContentView(), null);
            contentView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
            setUpContentView(inflater, contentView, savedInstanceState);
            return getContentView();
        }

        return super.onCreateView(inflater, container, savedInstanceState);
    }

    protected void setUpContentView(LayoutInflater inflater, ViewGroup contentView, Bundle savedInstanceState) {
        setContentView(contentView);
        _layoutInit(inflater, savedInstanceState);
        layoutInit(inflater, savedInstanceState);
    }

    private void _layoutInit(LayoutInflater inflater, Bundle savedInstanceState) {
        InjectUtility.initInjectedView(getActivity(), this, getContentView());
        if (emptyLayout != null) {
            View reloadView = emptyLayout.findViewById(R.id.layoutReload);
            if (reloadView != null) {
                setViewOnClick(reloadView);
            }
        }
        if (loadFailedLayout != null) {
            View reloadView = loadFailedLayout.findViewById(R.id.layoutReload);
            if (reloadView != null) {
                setViewOnClick(reloadView);
            }
        }
        setViewVisiable(loadingLayout, View.GONE);
        setViewVisiable(loadFailedLayout, View.GONE);
        setViewVisiable(emptyLayout, View.GONE);
        if (isContentEmpty()) {
            if (savedInstanceState != null) {
                requestData();
            } else {
                setViewVisiable(emptyLayout, View.VISIBLE);
                setViewVisiable(contentLayout, View.GONE);
            }
        } else {
            setViewVisiable(contentLayout, View.VISIBLE);
        }
    }

    public View findViewById(int viewId) {
        if (getContentView() == null) {
            return null;
        }
        return getContentView().findViewById(viewId);
    }

    public void setContentEmpty(boolean empty) {
        this.contentEmpty = empty;
    }


    public boolean isContentEmpty() {
        return contentEmpty;
    }

    protected void setViewVisiable(View v, int visibility) {
        if (v != null && v.getVisibility() != visibility)
            v.setVisibility(visibility);
    }

    protected void setViewOnClick(View v) {
        if (v == null) {
            return;
        }
        v.setOnClickListener(innerOnClickListener);
    }

    View.OnClickListener innerOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            onViewClicked(v);
        }
    };

    private void onViewClicked(View v) {
        if (v.getId() == R.id.layoutReload) {
            requestData();
        } else if (v.getId() == R.id.layoutRefresh) {
            requestData();
        }
    }

    public void requestData() {

    }

    public ViewGroup getContentView() {
        return rootView;
    }

    public void setContentView(ViewGroup view) {
        this.rootView = view;
    }

    abstract public int inflateContentView(); //指定Fragment的layoutId

    protected void onTaskStateChanged(ABaseTaskState state, TaskException exception) {
        // 开始task
        if (state == ABaseTaskState.prepare) {
            if (isContentEmpty()) {
                setViewVisiable(loadingLayout, View.VISIBLE);
                setViewVisiable(contentLayout, View.GONE);
            } else {
                setViewVisiable(loadingLayout, View.GONE);
                setViewVisiable(contentLayout, View.VISIBLE);
            }
            setViewVisiable(emptyLayout, View.GONE);
            if (isContentEmpty() && loadingLayout == null) {
                setViewVisiable(contentLayout, View.VISIBLE);
            }
            setViewVisiable(loadFailedLayout, View.GONE);
        }
        // task成功
        else if (state == ABaseTaskState.success) {
            setViewVisiable(loadingLayout, View.GONE);
            if (isContentEmpty()) {
                setViewVisiable(emptyLayout, View.VISIBLE);
                setViewVisiable(contentLayout, View.GONE);
            } else {
                setViewVisiable(contentLayout, View.VISIBLE);
                setViewVisiable(emptyLayout, View.GONE);
            }
        }
        // task取消
        else if (state == ABaseTaskState.canceled) {
            if (isContentEmpty()) {
                setViewVisiable(loadingLayout, View.GONE);
                setViewVisiable(emptyLayout, View.VISIBLE);
            }
        }
        // task失败
        else if (state == ABaseTaskState.failed) {
            if (isContentEmpty()) {
                if (loadFailedLayout != null) {
                    setViewVisiable(loadFailedLayout, View.VISIBLE);
                    if (exception != null) {
                        TextView txtLoadFailed = (TextView) loadFailedLayout.findViewById(R.id.txtLoadFailed);
                        if (txtLoadFailed != null) {
                            txtLoadFailed.setText(exception.getMessage());
                        }
                    }
                    setViewVisiable(emptyLayout, View.GONE);
                } else {
                    setViewVisiable(emptyLayout, View.VISIBLE);
                }
                setViewVisiable(loadingLayout, View.GONE);
            }
        }
        // task结束
        else if (state == ABaseTaskState.finished) {

        }
    }

    public void showMessage(CharSequence msg) {
        if (!TextUtils.isEmpty(msg) && getActivity() != null) {
//            ViewUtils.showMessage(getActivity(), msg.toString());
        }
    }

    public void showMessage(int msgId) {
        if (getActivity() != null)
            showMessage(getString(msgId));
    }

    // Fragment任务线程,耦合各状态下视图刷新
    protected abstract class ABaseTask<Params, Progress, Result> extends WorkTask<Params, Progress, Result> {
        public ABaseTask(String taskId) {
            super(taskId, ABaseFragment.this);
        }

        @Override
        protected void onPrepare() {
            super.onPrepare();
            onTaskStateChanged(ABaseTaskState.prepare, null);
        }

        @Override
        protected void onSuccess(Result result) {
            super.onSuccess(result);
            // 默认加载数据成功,且ContentView有数据展示
            ABaseFragment.this.setContentEmpty(result == null ? true : false);

            onTaskStateChanged(ABaseTaskState.success, null);

            if (Logger.DEBUG) {
                Logger.d(TAG, "Result 获取时间 %s", new SimpleDateFormat("HH:mm:ss").format(lastResultGetTime));
            }

            if (result instanceof IResult) {
                IResult iResult = (IResult) result;
                //是否是缓存数据
                if (iResult.fromCache()) {
                    //判断是否过期
                    if (iResult.outofdate()) {
                        runUIRunnable(new Runnable() {
                            @Override
                            public void run() {
                                Logger.d(TAG, "数据过期,开始刷新," + toString());
                                requestDataOutofdate();
                            }
                        }, configRequestDelay());
                    }
                } else {
                    lastResultGetTime = System.currentTimeMillis();
                }
            } else {
                lastResultGetTime = System.currentTimeMillis();
            }
        }

        @Override
        protected void onFailure(TaskException exception) {
            super.onFailure(exception);
            onTaskStateChanged(ABaseTaskState.failed, exception);
        }

        @Override
        protected void onCancelled() {
            super.onCancelled();
            onTaskStateChanged(ABaseTaskState.canceled, null);
        }

        @Override
        protected void onFinished() {
            super.onFinished();
            onTaskStateChanged(ABaseTaskState.finished, null);
        }
    }

    public void runUIRunnable(Runnable runnable) {
        runUIRunnable(runnable, 0);
    }

    public void runUIRunnable(Runnable runnable, long delay) {
        if (delay > 0) {
            mHandler.removeCallbacks(runnable);
            mHandler.postDelayed(runnable, delay);
        } else {
            mHandler.post(runnable);
        }
    }

    public int configRequestDelay() {
        return 500;
    }

    public void requestDataOutofdate() {
        requestData();
    }

    @Override
    public void onDestroy() {
        destroy = true;
        try {
            super.onDestroy();
        } catch (Exception e) {
            Logger.printExc(getClass(), e);
        }

        removeAllTask(true);

        if (BitmapLoader.getInstance() != null) {
//            BitmapLoader.getInstance().cancelPotentialTask(this);
        }
    }

    public boolean isDestory() {
        return destroy;
    }

    public boolean isActivityRunning() {
        return getActivity() != null;
    }

    @Override
    public void onDetach() {
        super.onDetach();

        if (getActivity() != null && getActivity() instanceof BaseActivity) {
            // ((BaseActivity) getActivity()).removeFragment(this.toString());
        }
    }

    @Override
    public void addTask(WorkTask task) {
        taskManager.addTask(task);
    }

    @Override
    public void removeTask(String taskId, boolean cancelIfRunning) {
        taskManager.removeTask(taskId, cancelIfRunning);
    }

    @Override
    public void removeAllTask(boolean cancelIfRunning) {
        taskManager.removeAllTask(cancelIfRunning);
    }

    @Override
    public int getTaskCount(String taskId) {
        return taskManager.getTaskCount(taskId);
    }

    //task执行BizLogic方法,第一次创建时拉取缓存,其他都只拉取网络
//    final protected CacheMode getTaskCacheMode(WorkTask task) {
//        if (task == null || TextUtils.isEmpty(task.getTaskId())) {
//            return getTaskCount(task.getTaskId()) == 1 ? CacheMode.auto : CacheMode.disable;
//        }
//        return CacheMode.disable;
//    }

    public void cleatTaskCount(String taskId) {
        taskManager.clearTaskCount(taskId);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        if (taskManager != null)
            taskManager.save(outState);
    }

    protected ITaskManager getTaskManager() {
        return taskManager;
    }

    //子类重写,初始化视图
    protected void layoutInit(LayoutInflater inflater, Bundle savedInstanceState) {

    }

    @Override
    public boolean canDisplay() {
        return true;
    }

    public int inflateActivityContentView() {
        return -1;
    }

    public int setActivityTheme() {
        return -1;
    }

    public View getLoadingLayout() {
        return loadingLayout;
    }

    public View getLoadFailureLayout() {
        return loadFailedLayout;
    }

    public View getContentLayout() {
        return contentLayout;
    }

    public View getEmptyLayout() {
        return emptyLayout;
    }
}
