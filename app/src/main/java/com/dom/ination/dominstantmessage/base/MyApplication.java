package com.dom.ination.dominstantmessage.base;

import android.app.Application;
import android.os.Environment;

import com.dom.ination.domforandroid.common.context.GlobalContext;
import com.dom.ination.domforandroid.common.setting.SettingUtility;
import com.dom.ination.domforandroid.component.bitmaploader.BitmapLoader;

import java.io.File;
import java.util.Random;

/**
 * Created by huoxiaobo on 16/9/1.
 */
public class MyApplication extends GlobalContext{

    @Override
    public void onCreate() {
        super.onCreate();
        // 添加一些配置项
        SettingUtility.addSettings(this, "actions");
        SettingUtility.addSettings(this, "settings");

        // 初始化一个颜色主题
        setupTheme();
        // 打开Debug日志
        setupCrash();
        //初始化图片加载
        BitmapLoader.newInstance(this,getImagePath());
    }

    public static String getImagePath() {
        return GlobalContext.getInstance().getExternalFilesDir(Environment.DIRECTORY_PICTURES).getAbsolutePath() + File.separator;
    }

    public void setupCrash() {
//        if (BuildConfig.LOG_DEBUG) {
//            CrashHandler.setupCrashHandler(this);
//        }
//        // UMENG统计设置
//        MobclickAgent.setDebugMode(Logger.DEBUG);
//        MobclickAgent.setCatchUncaughtExceptions(false);
//        MobclickAgent.openActivityDurationTrack(false);
//        if (BuildConfig.LOG_DEBUG) {
//            Logger.d("Device_info", UMengUtil.getDeviceInfo(this));
//        }
//        // BUGLY日志上报
//        CrashReport.initCrashReport(this, BuildConfig.BUGLY_APP_ID, Logger.DEBUG);
    }

    private void setupTheme() {
        int position = AppSettings.getThemeColor();
        if (position == -1) {
            // 一些我喜欢的颜色
            int[] initIndex = new int[]{ 0, 1, 4, 8, 15, 16, 18 };
            position = initIndex[new Random().nextInt(initIndex.length)];

            AppSettings.setThemeColor(position);
        }
    }
}
