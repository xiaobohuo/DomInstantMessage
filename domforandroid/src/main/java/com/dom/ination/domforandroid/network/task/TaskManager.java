package com.dom.ination.domforandroid.network.task;

import android.os.Bundle;
import android.text.TextUtils;

import com.dom.ination.domforandroid.common.utils.Logger;

import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Set;

/**
 * Created by huoxiaobo on 16/8/24.
 */
public class TaskManager implements ITaskManager {

    private final static String TAG = "TaskManager";

    private LinkedHashMap<String, WeakReference<WorkTask>> taskCacheMap;
    private HashMap<String, Integer> taskCountMap;

    public TaskManager() {
        taskCacheMap = new LinkedHashMap<>();
        taskCountMap = new HashMap<>();
    }

    @Override
    public void addTask(WorkTask task) {
        if (task != null && !TextUtils.isEmpty(task.getTaskId())) {
            int count = taskCountMap.keySet().contains(task.getTaskId()) ? taskCountMap.get(task.getTaskId()) : 0;
            taskCountMap.put(task.getTaskId(), ++count);
            cancelExistTask(task.getTaskId(), true);
            taskCacheMap.put(task.getTaskId(), new WeakReference<WorkTask>(task));

            Logger.d(TAG, String.format("addTask ---> %s", task.getTaskId()));
        }
    }

    private void cancelExistTask(String taskId, boolean cancelIfRunning) {
        WorkTask existTask = getExistTask(taskId);
        if (existTask != null) {
            Logger.d(TAG, String.format("interrupt exist task ---> %s", taskId));
            existTask.cancel(cancelIfRunning);
        }
        taskCacheMap.remove(taskId);
    }

    private WorkTask getExistTask(String taskId) {
        if (TextUtils.isEmpty(taskId)) {
            return null;
        }
        WeakReference<WorkTask> workTaskRef = taskCacheMap.get(taskId);
        if (workTaskRef == null) {
            return null;
        }
        return workTaskRef.get();
    }

    @Override
    public void removeTask(String taskId, boolean cancelIfRunning) {
        cancelExistTask(taskId, cancelIfRunning);
    }

    @Override
    public void removeAllTask(boolean cancelIfRunning) {
        Set<String> keySet = taskCacheMap.keySet();
        for (String key : keySet) {
            WorkTask task = getExistTask(key);
            if (task != null) {
                task.cancel(cancelIfRunning);
            }
        }
        taskCacheMap.clear();
    }

    @Override
    public int getTaskCount(String taskId) {
        if (TextUtils.isEmpty(taskId)) {
            return 0;
        }
        return taskCountMap.keySet().contains(taskId) ? taskCountMap.get(taskId) : 0;
    }

    public void clearTaskCount(String taskId) {
        if (!TextUtils.isEmpty(taskId)) {
            taskCountMap.remove(taskId);
        }
    }

    public void save(Bundle outState) {
        outState.putSerializable("map", taskCountMap);
    }

    public void restore(Bundle savedInstanceState) {
        if (savedInstanceState.getSerializable("map") != null) {
            taskCountMap = (HashMap<String, Integer>) savedInstanceState.getSerializable("map");
        }
    }
}
