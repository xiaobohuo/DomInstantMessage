package com.dom.ination.domforandroid.ui.fragment;

import android.app.Fragment;

import com.dom.ination.domforandroid.component.bitmaploader.core.BitmapOwner;
import com.dom.ination.domforandroid.network.task.ITaskManager;

/**
 * Created by huoxiaobo on 16/8/23.
 */
public abstract class ABaseFragment extends Fragment implements ITaskManager,BitmapOwner {

    static final String TAG = "ABaseFragment";

    public enum ABaseTaskState{
        none,prepare,failed,success,finished,canceled
    }

    private TaskManager taskManager;

}
