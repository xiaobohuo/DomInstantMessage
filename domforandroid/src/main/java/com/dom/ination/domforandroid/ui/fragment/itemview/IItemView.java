package com.dom.ination.domforandroid.ui.fragment.itemview;

import android.view.View;

import java.io.Serializable;

/**
 * Created by 10174987 on 2016/8/29.
 */

public interface IItemView<T extends Serializable> {

    //将View绑定到属性
    void onBindView(View convertView);

    //将Data绑定到View
    void onBindData(View convertView, T data, int position);

    //Item的Position
    int itemPosition();

    // 重置数据
    void reset(int size, int position);

    //Item的数据size
    int itemSize();

    //Item的ConvertView
    View getConvertView();
}
