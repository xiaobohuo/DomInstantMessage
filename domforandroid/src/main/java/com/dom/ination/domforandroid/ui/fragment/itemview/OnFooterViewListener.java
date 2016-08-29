package com.dom.ination.domforandroid.ui.fragment.itemview;

import com.dom.ination.domforandroid.network.task.TaskException;
import com.dom.ination.domforandroid.ui.fragment.ABaseFragment;
import com.dom.ination.domforandroid.ui.fragment.APagingFragment;

/**
 * Created by 10174987 on 2016/8/29.
 */

public interface OnFooterViewListener {
    void onTaskStateChanged(AFooterItemView<?> footerItemView, ABaseFragment.ABaseTaskState state, TaskException exception, APagingFragment.RefreshMode mode);

    void setFooterViewToRefreshing();
}
