package com.dom.ination.domforandroid.component.bitmaploader;

import android.app.ActivityManager;
import android.content.Context;
import android.text.TextUtils;
import android.widget.ImageView;

import com.dom.ination.domforandroid.common.utils.KeyGenerator;
import com.dom.ination.domforandroid.common.utils.Logger;
import com.dom.ination.domforandroid.common.utils.SystemUtils;
import com.dom.ination.domforandroid.component.bitmaploader.core.BitmapCache;
import com.dom.ination.domforandroid.component.bitmaploader.core.BitmapOwner;
import com.dom.ination.domforandroid.component.bitmaploader.core.BitmapTask;
import com.dom.ination.domforandroid.component.bitmaploader.core.ImageConfig;
import com.dom.ination.domforandroid.component.bitmaploader.core.MyBitmap;

import java.io.File;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by huoxiaobo on 16/8/24.
 */
public class BitmapLoader {

    public static final String TAG = "BitmapLoader";

    private Map<WeakReference<BitmapOwner>, List<WeakReference<MyBitmapLoaderTask>>> ownerMap;
    private Map<String, WeakReference<MyBitmapLoaderTask>> taskCache;

    private String imageCachePath;  //图片缓存路径

    private BtimapProcess bitmapProcess;

    private BtimapCache mImageCache; //图片缓存

    private Context context;

    private BitmapLoader(Context context) {
        this.context = context;
    }

    private static BitmapLoader imageLoader;

    static BitmapLoader newInstance(Context context) {
        imageLoader = new BitmapLoader(context);
        return imageLoader;
    }

    public static BitmapLoader newInstance(Context context, String imageCachePath) {
        BitmapLoader loader = newInstance(context);
        if (TextUtils.isEmpty(imageCachePath)) {
            imageCachePath = SystemUtils.getSdcardPath() + File.separator + "domImage" + File.separator;
        }
        loader.imageCachePath = imageCachePath;
        loader.init();

        return loader;
    }

    public void destroy() {
    }

    BitmapLoader init() {
        ownerMap = new HashMap<>();
        taskCache = new HashMap<>();
        int memCacheSize = 1024 * 1024 * ((ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE)).getMemoryClass();
        memCacheSize = memCacheSize / 3;
        Logger.i(TAG, "memCacheSize = " + (memCacheSize / 1024 / 1024) + "MB");
        bitmapProcess = new BitmapProcess(imageCachePath);
        mImageCache = new BitmapCache(memCacheSize);
        return this;
    }

    public static BitmapLoader getInstance() {
        return imageLoader;
    }

    public static String getKeyByConfig(String url, ImageConfig config) {
        String path = url;

        if (config == null || TextUtils.isEmpty(config.getId())) {
            return path;
        }

        return path + config.getId();
    }

    public class MyBitmapLoaderTask extends BitmapTask<Void, Void, MyBitmap> {

        private final String imageUrl;
        private List<WeakReference<ImageView>> imageViewRef;
        private final ImageConfig config;
        boolean isCompleted = false;

        public String getKey() {
            return KeyGenerator.generateMD5(getKeyByConfig(imageUrl, config));
        }

        public MyBitmapLoaderTask(String imageUrl, ImageView imageView, BitmapLoader bitmapLoader, ImageConfig config) {
            this.imageUrl = imageUrl;
            imageViewRef = new ArrayList<>();
            if (imageView != null) {
                imageViewRef.add(new WeakReference<ImageView>(imageView));
            }
            this.config = config;
        }

        @Override
        public MyBitmap workInBackground(Void... parmas) throws Exception {
            try {
                BitmapBytesAndFlag bitmapBytesAndFlag = doDownload(imageUrl, config);
            } catch (OutOfMemoryError e) {
                e.printStackTrace();
            }
            throw new Exception("task canceled or failed,bitmap is null,url = " + imageUrl);
        }
    }

    // 非异步
    public BitmapBytesAndFlag doDownload(String imageUrl, final ImageConfig config) throws Exception {
        byte[] bitmapBytes = null;
        int flag = 0x00;
        //判断二级缓存数据
        bitmapBytes = bitmapProcess.getBitmapFromCompDiskCache(imageUrl, config);

    }

    public static class BitmapBytesAndFlag {
        public byte[] bitmapBytes;
        public int flag;
    }
}
