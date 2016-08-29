package com.dom.ination.domforandroid.ui.fragment.itemview;

import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.AdapterView;

import com.dom.ination.domforandroid.R;
import com.dom.ination.domforandroid.support.inject.ViewInject;
import com.dom.ination.domforandroid.ui.fragment.APagingFragment;
import com.dom.ination.domforandroid.ui.fragment.adpter.BasicRecyclerViewAdapter;
import com.dom.ination.domforandroid.ui.fragment.adpter.IPagingAdapter;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * Created by huoxiaobo on 16/8/29.
 */
public abstract class ARecyclerViewFragment<T extends Serializable,Ts extends Serializable> extends APagingFragment<T,Ts,RecyclerView> implements AdapterView.OnItemClickListener{

    @ViewInject(idStr = "recyclerview")
    RecyclerView mRecyclerView;

    @Override
    public int inflateContentView() {
        return R.layout.comm_ui_recyclerview;
    }

    @Override
    public RecyclerView getRefreshView() {
        return mRecyclerView;
    }

    @Override
    protected void setupRefreshConfig(RefreshConfig config) {
        super.setupRefreshConfig(config);
        mRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener(){
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                ARecyclerViewFragment.this.onScrollStateChanged(newState);
            }

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
            }
        });
        mRecyclerView.setLayoutManager(configLayoutManager());
    }

    @Override
    protected IPagingAdapter<T> newAdapter(ArrayList<T> datas) {
        return new BasicRecyclerViewAdapter<>(this,configItemViewCreator(),datas);
    }

    protected RecyclerView.LayoutManager configLayoutManager() {
        return new LinearLayoutManager(getActivity());
    }

    @Override
    protected void bindAdapter(IPagingAdapter adapter) {
        if (mRecyclerView.getAdapter()==null){
            mRecyclerView.setAdapter((BasicRecyclerViewAdapter)adapter);
        }
        if (((BasicRecyclerViewAdapter) getAdapter()).getOnItemClickListener() != this) {
            ((BasicRecyclerViewAdapter) getAdapter()).setOnItemClickListener(this);
        }
    }

    @Override
    protected void addFooterViewToRefreshView(AFooterItemView<?> footerItemView) {
        ((BasicRecyclerViewAdapter) getAdapter()).addFooterView(footerItemView);
    }

    @Override
    protected void addHeaderViewToRefreshView(AHeaderItemViewCreator<?> headerItemViewCreator) {
        ((BasicRecyclerViewAdapter) getAdapter()).setHeaderItemViewCreator(headerItemViewCreator);
    }

    @Override
    protected void setupRefreshView(Bundle savedInstanceSate) {
        super.setupRefreshView(savedInstanceSate);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

    }
}
