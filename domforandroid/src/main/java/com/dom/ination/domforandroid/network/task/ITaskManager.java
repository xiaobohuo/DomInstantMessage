package com.dom.ination.domforandroid.network.task;

/**
 * Created by huoxiaobo on 16/8/23.
 */
public interface ITaskManager {
    void addTask(WorkTask task);
    void removeTask(String taskId,boolean cancelIfRunning);
    void removeAllTask(boolean cancelIfRunning);
    int  getTaskCount(String taskId);
}
