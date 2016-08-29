package com.dom.ination.domforandroid.ui.fragment.itemview;

import android.content.Context;
import android.view.View;

import com.dom.ination.domforandroid.ui.fragment.adpter.ARecycleViewItemView;

import java.io.Serializable;

/**
 * Created by 10174987 on 2016/8/29.
 */

public abstract class AFooterItemView<T extends Serializable> extends ARecycleViewItemView<T>
        implements OnFooterViewListener {

    private OnFooterViewCallback onFooterViewCallback;

    public AFooterItemView(Context context, View itemView, OnFooterViewCallback callback) {
        super(context, itemView);

        this.onFooterViewCallback = callback;
    }

    protected OnFooterViewCallback getCallback() {
        return onFooterViewCallback;
    }

    public interface OnFooterViewCallback {
        void onLoadMore();
        boolean canLoadMore();
    }
}
