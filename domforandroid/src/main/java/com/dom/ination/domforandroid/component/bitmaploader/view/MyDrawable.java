package com.dom.ination.domforandroid.component.bitmaploader.view;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;

import com.dom.ination.domforandroid.component.bitmaploader.BitmapLoader;
import com.dom.ination.domforandroid.component.bitmaploader.core.ImageConfig;
import com.dom.ination.domforandroid.component.bitmaploader.core.MyBitmap;

import java.lang.ref.WeakReference;

/**
 * Created by 10174987 on 2016/8/29.
 */

public class MyDrawable extends BitmapDrawable{
    private MyBitmap bitmap;
    private ImageConfig config;
    private WeakReference<BitmapLoader.MyBitmapLoaderTask> task;

    public MyDrawable(Resources res, Bitmap bitmap) {
        super(res, bitmap);
    }

    public MyDrawable(Resources res, MyBitmap myBitmap, ImageConfig config, WeakReference<BitmapLoader.MyBitmapLoaderTask> task) {
        this(res, myBitmap.getBitmap());
        this.bitmap = myBitmap;
        this.config = config;
        this.task = task;
    }

    public MyBitmap getMyBitmap() {
        return bitmap;
    }

    public void setMyBitmap(MyBitmap myBitmap) {
        this.bitmap = myBitmap;
    }

    public ImageConfig getConfig() {
        return config;
    }

    public void setConfig(ImageConfig config) {
        this.config = config;
    }

    public WeakReference<BitmapLoader.MyBitmapLoaderTask> getTask() {
        return task;
    }

    public void setTask(WeakReference<BitmapLoader.MyBitmapLoaderTask> task) {
        this.task = task;
    }
}
