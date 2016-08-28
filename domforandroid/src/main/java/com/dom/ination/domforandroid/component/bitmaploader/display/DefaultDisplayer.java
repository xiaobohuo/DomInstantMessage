package com.dom.ination.domforandroid.component.bitmaploader.display;

import android.graphics.drawable.BitmapDrawable;
import android.widget.ImageView;

/**
 * Created by huoxiaobo on 16/8/28.
 */
public class DefaultDisplayer implements Displayer{
    @Override
    public void loadCompletedDisplay(ImageView imageView, BitmapDrawable drawable) {
        imageView.setImageDrawable(drawable);
    }

    @Override
    public void loadFailedDisplay(ImageView imageView, BitmapDrawable drawable) {
        imageView.setImageDrawable(drawable);
    }
}
