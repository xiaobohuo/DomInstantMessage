package com.dom.ination.domforandroid.ui.fragment.itemview;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.io.Serializable;

/**
 * Created by 10174987 on 2016/8/29.
 */

public abstract class AHeaderItemViewCreator<T extends Serializable> implements IItemViewCreator<T> {
    @Override
    public View newContentView(LayoutInflater inflater, ViewGroup parent, int viewType) {
        for (int[] headerLayoutRes : setHeaders()) {
            if (viewType == headerLayoutRes[1]) {
                return inflater.inflate(headerLayoutRes[0], parent, false);
            }
        }
        return null;
    }

    abstract public int[][] setHeaders();
}
