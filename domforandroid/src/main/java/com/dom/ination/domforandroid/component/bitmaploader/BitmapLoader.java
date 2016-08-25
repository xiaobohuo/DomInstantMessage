package com.dom.ination.domforandroid.component.bitmaploader;

import com.dom.ination.domforandroid.component.bitmaploader.core.BitmapOwner;
import com.dom.ination.domforandroid.component.bitmaploader.core.BitmapTask;
import com.dom.ination.domforandroid.component.bitmaploader.core.MyBitmap;

import java.lang.ref.WeakReference;
import java.util.List;
import java.util.Map;

/**
 * Created by huoxiaobo on 16/8/24.
 */
public class BitmapLoader {

    public static final String TAG = "BitmapLoader";

    private Map<WeakReference<BitmapOwner>,List<WeakReference<MyBitmapLoaderTask>>> ownerMap;

    private static BitmapLoader imageLoader;

    public static BitmapLoader getInstance() {
        return imageLoader;
    }

    public class MyBitmapLoaderTask extends BitmapTask<Void,Void,MyBitmap> {

        @Override
        public MyBitmap workInBackground(Void... parmas) throws Exception {
            return null;
        }
    }
}
