package com.dom.ination.domforandroid.ui.fragment.itemview;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.io.Serializable;

/**
 * Created by 10174987 on 2016/8/29.
 */

public interface IItemViewCreator<T extends Serializable> {
    View newContentView(LayoutInflater inflater, ViewGroup parent, int viewType);

    IItemView<T> newItemView(View convertView, int viewType);
}
