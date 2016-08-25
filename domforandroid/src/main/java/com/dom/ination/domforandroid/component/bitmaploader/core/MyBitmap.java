package com.dom.ination.domforandroid.component.bitmaploader.core;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.dom.ination.domforandroid.common.utils.Logger;

import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by huoxiaobo on 16/8/26.
 */
public class MyBitmap {
    static final String TAG = "MyBitmap";

    static int createdCount = 0;

    private String id;

    private String url;

    private Bitmap bitmap;

    private static Map<String, WeakReference<Bitmap>> cacheMap;

    static {
        cacheMap = new HashMap<String, WeakReference<Bitmap>>();
    }

    static Bitmap getCacheBitmap(Context context, int resId) {
        String key = String.valueOf(resId);
        Bitmap bitmap = null;

        if (cacheMap.containsKey(key)) {
            bitmap = cacheMap.get(key).get();
        }

        if (bitmap == null) {
            try {
                bitmap = BitmapFactory.decodeResource(context.getResources(), resId);
                cacheMap.put(key, new WeakReference<Bitmap>(bitmap));
            } catch (Error e) {
                return Bitmap.createBitmap(0, 0, Bitmap.Config.ARGB_4444);
            }
        }

        return bitmap;
    }

    @Override
    protected void finalize() throws Throwable {
        super.finalize();
        createdCount--;
        Logger.v(TAG, createdCount + "");
    }

    public MyBitmap(Context context, int resId) {
        this();
        this.bitmap = getCacheBitmap(context, resId);
    }

    public MyBitmap(Context context, int resId, String url) {
        this();
        this.bitmap = getCacheBitmap(context, resId);
        this.url = url;
    }

    public MyBitmap(Bitmap bitmap, String url) {
        this();
        this.url = url;
        this.bitmap = bitmap;
    }

    private MyBitmap() {
        createdCount++;
        Logger.v(TAG, createdCount + "");
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Bitmap getBitmap() {
        return bitmap;
    }

    public void setBitmap(Bitmap bitmap) {
        this.bitmap = bitmap;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }
}
