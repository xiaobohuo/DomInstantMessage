package com.dom.ination.domforandroid.component.bitmaploader.download;

import android.content.Context;

import com.dom.ination.domforandroid.component.bitmaploader.core.ImageConfig;

/**
 * Created by huoxiaobo on 16/8/28.
 */
public interface Downloader {
    byte[] downloadBitmap(Context context, String url, ImageConfig config) throws Exception;
}
