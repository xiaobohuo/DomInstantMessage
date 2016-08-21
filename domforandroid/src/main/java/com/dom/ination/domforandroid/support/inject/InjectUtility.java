package com.dom.ination.domforandroid.support.inject;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.text.TextUtils;
import android.view.View;

import java.lang.reflect.Field;

/**
 * Created by huoxiaobo on 16/8/22.
 */
public class InjectUtility {
    static final String TAG = "InjectUtility";

    public static void initInjectedView(Activity sourceActivity) {
        initInjectedView(sourceActivity, sourceActivity, sourceActivity.getWindow().getDecorView());
    }

    public static void initInjectedView(Context context, Object injectedSource, View sourceView) {
        Class<?> clazz = injectedSource.getClass();
        for (; clazz.getSuperclass() != Object.class; clazz = clazz.getSuperclass()) {
            Field[] fields = clazz.getDeclaredFields();
            if (fields != null && fields.length > 0) {
                for (Field field : fields) {
                    ViewInject viewInject = field.getAnnotation(ViewInject.class);
                    if (viewInject != null){
                        int viewId = viewInject.id();
                        if (viewId == 0){
                            String idStr = viewInject.idStr();
                            if (!TextUtils.isEmpty(idStr)){
                                try {
                                    String packageName = context.getPackageName();
                                    Resources resources = context.getPackageManager().getResourcesForApplication(packageName);
//                                    context.getResources(); //here to test the difference
                                    viewId = resources.getIdentifier(idStr,"id",packageName);
                                    if (viewId == 0 ){
                                        throw new RuntimeException(String.format("%s 的属性%s关联了id=%s,但其无效",clazz.getSimpleName(),field.getName(),idStr));
                                    }
                                }catch (Exception e){

                                }
                            }
                        }
                        if (viewId != 0){

                        }
                    }
                }
            }
        }
    }
}
