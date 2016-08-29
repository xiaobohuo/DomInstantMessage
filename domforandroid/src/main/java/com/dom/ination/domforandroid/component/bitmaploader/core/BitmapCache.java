package com.dom.ination.domforandroid.component.bitmaploader.core;

import android.text.TextUtils;

import com.dom.ination.domforandroid.common.utils.KeyGenerator;
import com.dom.ination.domforandroid.component.bitmaploader.BitmapLoader;

/**
 * Created by huoxiaobo on 16/8/29.
 */
public class BitmapCache {
    private LruMemoryCache<String, MyBitmap> mMemoryCache;

    public BitmapCache(int memCacheSize) {
        init(memCacheSize);
    }

    private void init(int memCacheSize) {
        mMemoryCache = new LruMemoryCache<String, MyBitmap>(memCacheSize) {
            @Override
            protected int sizeOf(String key, MyBitmap value) {
                return BitmapCommonUtils.getBitmapSize(value.getBitmap()) * 4;
            }
        };
    }

    public void addBitmapToMemCache(String url, ImageConfig config, MyBitmap bitmap) {
        if (TextUtils.isEmpty(url) || bitmap == null) return;
        if (mMemoryCache != null) {
            mMemoryCache.put(KeyGenerator.generateMD5(BitmapLoader.getKeyByConfig(url, config)), bitmap);
        }
    }

    public MyBitmap getBitmapFromMemCache(String url, ImageConfig config) {
        if (mMemoryCache != null) {
            MyBitmap myBitmap = mMemoryCache.get(KeyGenerator.generateMD5(BitmapLoader.getKeyByConfig(url, config)));
            if (myBitmap != null) {
                return myBitmap;
            }
        }
        return null;
    }

    public void clearMemCache() {
        if (mMemoryCache != null) {
            mMemoryCache.evictAll();
        }
    }

    public void clearHalfMemCache() {
        if (mMemoryCache != null) {
            mMemoryCache.evictHalf();
        }
    }
}
