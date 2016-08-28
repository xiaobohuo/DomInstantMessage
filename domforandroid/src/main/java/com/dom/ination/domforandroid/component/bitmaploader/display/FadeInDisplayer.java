package com.dom.ination.domforandroid.component.bitmaploader.display;

import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.TransitionDrawable;
import android.widget.ImageView;

/**
 * Created by huoxiaobo on 16/8/28.
 */
public class FadeInDisplayer implements Displayer {
    @Override
    public void loadCompletedDisplay(ImageView imageView, BitmapDrawable drawable) {
        //PhotoView 不设置
        if (imageView.getClass().getSimpleName().indexOf("PhotoView") != -1) {
            return;
        }
        if (imageView.getDrawable() != null) {
            final TransitionDrawable td = new TransitionDrawable(new Drawable[]{imageView.getDrawable(), drawable});
            imageView.setImageDrawable(td);
            td.startTransition(300);
        } else {
            imageView.setImageDrawable(drawable);
        }
    }

    @Override
    public void loadFailedDisplay(ImageView imageView, BitmapDrawable drawable) {
        imageView.setImageDrawable(drawable);
    }
}
