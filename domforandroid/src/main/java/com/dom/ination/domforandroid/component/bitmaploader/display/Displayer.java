package com.dom.ination.domforandroid.component.bitmaploader.display;

import android.graphics.drawable.BitmapDrawable;
import android.widget.ImageView;

/**
 * Created by huoxiaobo on 16/8/28.
 */
public interface Displayer {
    void loadCompletedDisplay(ImageView imageView, BitmapDrawable drawable);

    void loadFailedDisplay(ImageView imageView, BitmapDrawable drawable);
}
