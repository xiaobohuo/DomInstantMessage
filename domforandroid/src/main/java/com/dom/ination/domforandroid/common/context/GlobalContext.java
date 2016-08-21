package com.dom.ination.domforandroid.common.context;

import android.app.Application;
import android.os.Handler;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;

/**
 * Created by huoxiaobo on 16/8/21.
 */
public class GlobalContext extends Application {
    private static GlobalContext _context;

    private final static int CONNECT_TIMEOUT = 3000;
    private final static int READ_TIMEOUT = 3000;

    private static OkHttpClient mOkHttpClient = null;

    static {
        configOkHttpClient();
    }

    private static void configOkHttpClient() {
        mOkHttpClient = new OkHttpClient.Builder().connectTimeout(CONNECT_TIMEOUT, TimeUnit.MILLISECONDS).readTimeout(READ_TIMEOUT, TimeUnit.MILLISECONDS).build();
    }

    public static OkHttpClient getOkHttpClient() {
        return mOkHttpClient;
    }

    public static GlobalContext getInstance() {
        return _context;
    }

    Handler handler = new Handler();

    public Handler getHandler() {
        return handler;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        _context = this;
    }
}
