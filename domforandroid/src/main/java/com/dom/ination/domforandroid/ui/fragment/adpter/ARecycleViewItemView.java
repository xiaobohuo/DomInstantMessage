package com.dom.ination.domforandroid.ui.fragment.adpter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.dom.ination.domforandroid.support.inject.InjectUtility;
import com.dom.ination.domforandroid.ui.fragment.itemview.IItemView;

import java.io.Serializable;

/**
 * Created by 10174987 on 2016/8/29.
 */

public abstract class ARecycleViewItemView<T extends Serializable> extends RecyclerView.ViewHolder implements IItemView<T> {
    private int size;
    private int position;
    private View convertView;
    private Context context;

    public ARecycleViewItemView(Context context, View itemView) {
        super(itemView);
        this.context = context;
        this.convertView = itemView;
    }


    @Override
    public void onBindView(View convertView) {
        InjectUtility.initInjectedView(getContext(), this, convertView);
    }

    @Override
    public void onBindData(View convertView, T data, int position) {

    }

    @Override
    public int itemPosition() {
        return position;
    }

    @Override
    public void reset(int size, int position) {
        this.size = size;
        this.position = position;
    }

    @Override
    public int itemSize() {
        return size;
    }

    @Override
    public View getConvertView() {
        return convertView;
    }

    public Context getContext() {
        return context;
    }
}
