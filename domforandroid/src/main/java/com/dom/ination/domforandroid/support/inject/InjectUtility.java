package com.dom.ination.domforandroid.support.inject;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.text.TextUtils;
import android.view.View;
import android.widget.AbsListView;

import com.dom.ination.domforandroid.common.utils.Logger;

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
                    if (viewInject != null) {
                        int viewId = viewInject.id();
                        if (viewId == 0) {
                            String idStr = viewInject.idStr();
                            if (!TextUtils.isEmpty(idStr)) {
                                try {
                                    String packageName = context.getPackageName();
                                    Resources resources = context.getPackageManager().getResourcesForApplication(packageName);
//                                    context.getResources(); //here to test the difference
                                    viewId = resources.getIdentifier(idStr, "id", packageName);
                                    if (viewId == 0) {
                                        throw new RuntimeException(String.format("%s 的属性%s关联了id=%s,但其无效", clazz.getSimpleName(), field.getName(), idStr));
                                    }
                                } catch (Exception e) {

                                }
                            }
                        }
                        if (viewId != 0) {
                            try {
                                field.setAccessible(true);
                                if (field.get(injectedSource) == null) {
                                    field.set(injectedSource, sourceView.findViewById(viewId));
                                    if (Logger.DEBUG) {
                                        Logger.v(TAG, "id = %d,view = %s", viewId, field.get(injectedSource) + "");
                                    }
                                } else {
                                    continue;
                                }
                            } catch (Exception e) {
                                Logger.printExc(InjectUtility.class, e);
                            }
                        }
                        String clickMethod = viewInject.click();
                        if (!TextUtils.isEmpty(clickMethod)) {
                            setViewClickListener(injectedSource, field, clickMethod);
                        }
                        String longClickMethod = viewInject.longClick();
                        if (!TextUtils.isEmpty(longClickMethod)) {
                            setViewLongClickListener(injectedSource, field, longClickMethod);
                        }
                        String itemClickMethod = viewInject.itemClick();
                        if (!TextUtils.isEmpty(itemClickMethod)) {
                            setItemClickListener(injectedSource, field, itemClickMethod);
                        }
                        String itemLongClickMethod = viewInject.itemLongClick();
                        if (!TextUtils.isEmpty(itemLongClickMethod)) {
                            setItemLongClickListener(injectedSource, field, itemLongClickMethod);
                        }
                        Select select = viewInject.select();
                        if (!TextUtils.isEmpty(select.selected())) {
                            setViewSelectListener(injectedSource, field, select.selected(), select.noSelected());
                        }
                    }
                }
            }
        }
    }

    public static void setViewSelectListener(Object injectedSource, Field field, String selected, String noSelect) {
        try {
            Object obj = field.get(injectedSource);
            if (obj instanceof AbsListView) {
                ((AbsListView) obj).setOnItemSelectedListener(new EventListener(injectedSource).select(selected).noSelect(noSelect));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void setItemLongClickListener(Object injectedSource, Field field, String itemLongClickMethod) {
        try {
            Object obj = field.get(injectedSource);
            if (obj instanceof View) {
                ((View) obj).setOnLongClickListener(new EventListener(injectedSource).longClick(itemLongClickMethod));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void setItemClickListener(Object injectedSource, Field field, String itemClickMethod) {
        try {
            Object obj = field.get(injectedSource);
            if (obj instanceof AbsListView) {
                ((AbsListView) obj).setOnItemClickListener(new EventListener(injectedSource).itemClick(itemClickMethod));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void setViewLongClickListener(Object injectedSource, Field field, String longClickMethod) {
        try {
            Object obj = field.get(injectedSource);
            if (obj instanceof View) {
                ((View) obj).setOnLongClickListener(new EventListener(injectedSource).longClick(longClickMethod));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void setViewClickListener(Object injectedSource, Field field, String clickMethod) {
        try {
            Object obj = field.get(injectedSource);
            if (obj instanceof View) {
                ((View) obj).setOnClickListener(new EventListener(injectedSource).click(clickMethod));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
