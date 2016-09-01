package com.dom.ination.dominstantmessage.base;

import android.app.Application;

import com.dom.ination.domforandroid.common.setting.SettingUtility;

import java.util.Random;

/**
 * Created by huoxiaobo on 16/9/1.
 */
public class MyApplication extends Application{

    @Override
    public void onCreate() {
        super.onCreate();
        // 添加一些配置项
        SettingUtility.addSettings(this, "actions");
        SettingUtility.addSettings(this, "settings");

        // 初始化一个颜色主题
        setupTheme();
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
