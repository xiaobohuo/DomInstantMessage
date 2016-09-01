package com.dom.ination.domforandroid.ui.fragment;

import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;

import com.dom.ination.domforandroid.R;
import com.dom.ination.domforandroid.support.inject.ViewInject;
import com.dom.ination.domforandroid.ui.fragment.itemview.ARecyclerViewFragment;

import java.io.Serializable;

/**
 * Created by huoxiaobo on 16/8/29.
 */
public abstract class ARecyclerViewSwipeRefreshFragment<T extends Serializable, Ts extends Serializable> extends ARecyclerViewFragment<T, Ts> implements SwipeRefreshLayout.OnRefreshListener {

    @ViewInject(
            idStr = "swipeRefreshLayout"
    )
    SwipeRefreshLayout swipeRefreshLayout;

    public ARecyclerViewSwipeRefreshFragment() {
    }

    public int inflateContentView() {
        return R.layout.comm_ui_recyclerview_swiperefresh;
    }

    protected void setupRefreshView(Bundle savedInstanceSate) {
        super.setupRefreshView(savedInstanceSate);
        this.setupSwipeRefreshLayout();
    }

    protected void setupSwipeRefreshLayout() {
        this.swipeRefreshLayout.setOnRefreshListener(this);
        this.swipeRefreshLayout.setColorSchemeResources(new int[]{17170459, 17170452, 17170456, 17170454});
    }

    public void onRefresh() {
        this.onPullDownToRefresh();
    }

    public boolean setRefreshViewToLoading() {
        this.swipeRefreshLayout.setRefreshing(true);
        return false;
    }

    public void onRefreshViewFinished(APagingFragment.RefreshMode mode) {
        if(mode != RefreshMode.update && this.swipeRefreshLayout.isRefreshing()) {
            this.swipeRefreshLayout.setRefreshing(false);
        }

    }

    public SwipeRefreshLayout getSwipeRefreshLayout() {
        return this.swipeRefreshLayout;
    }
}
