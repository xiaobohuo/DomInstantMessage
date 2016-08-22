package com.dom.ination.domforandroid.support.inject;

import android.view.View;
import android.widget.AdapterView;

import java.lang.reflect.Method;

/**
 * Created by huoxiaobo on 16/8/22.
 */
public class EventListener implements View.OnClickListener, View.OnLongClickListener, AdapterView.OnItemClickListener, AdapterView.OnItemLongClickListener, AdapterView.OnItemSelectedListener {

    private Object handler;

    private String clickMethod;
    private String longClickMethod;
    private String itemClickMethod;
    private String itemLongClickMethod;
    private String itemSelectMethod;
    private String nothingSelectedMethod;

    public EventListener(Object handler) {
        this.handler = handler;
    }

    public EventListener click(String method) {
        this.clickMethod = method;
        return this;
    }

    public EventListener longClick(String method) {
        this.longClickMethod = method;
        return this;
    }

    public EventListener itemClick(String method) {
        this.itemClickMethod = method;
        return this;
    }

    public EventListener itemLongClick(String method) {
        this.itemLongClickMethod = method;
        return this;
    }

    public EventListener select(String method) {
        this.itemSelectMethod = method;
        return this;
    }

    public EventListener noSelect(String method) {
        this.nothingSelectedMethod = method;
        return this;
    }

    @Override
    public void onClick(View v) {
        invokeClickMethod(handler, clickMethod, v);
    }

    public static Object invokeClickMethod(Object handler, String clickMethod, Object... params) {
        if (handler == null) {
            return null;
        }
        Method method = null;
        Class<?> clazz = handler.getClass();
        for (; clazz != Object.class; clazz = clazz.getSuperclass()) {
            Method[] methods = clazz.getDeclaredMethods();
            boolean breakFlag = false;
            for (Method med : methods) {
                if (med.getName() == clickMethod) {
                    method = med;
                    breakFlag = true;
                    break;
                }
            }
            if (breakFlag) {
                break;
            }
        }
        if (method != null) {
            try {
                method.setAccessible(true);
                return method.invoke(handler, params);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        invokeItemClickMethod(handler, itemClickMethod, parent, view, position, id);
    }

    public static Object invokeItemClickMethod(Object handler, String itemClickMethod, Object... params) {
        if (handler == null) {
            return null;
        }
        Method method = null;
        try {
            method = handler.getClass().getDeclaredMethod(itemClickMethod, AdapterView.class, View.class, int.class, long.class);
            if (method != null) {
                method.setAccessible(true);
                return method.invoke(handler, params);
            } else {
                throw new RuntimeException("no such method:" + itemClickMethod);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
        return invokeItemLongClickMethod(handler, itemLongClickMethod, parent, view, position, id);
    }

    public static boolean invokeItemLongClickMethod(Object handler, String itemLongClickMethod, Object... params) {
        if (handler == null) {
            return false;
        }
        Method method = null;
        try {
            method = handler.getClass().getDeclaredMethod(itemLongClickMethod, AdapterView.class, View.class, int.class, long.class);
            if (method != null) {
                method.setAccessible(true);
                Object obj = method.invoke(handler, params);
                if (obj == null) {
                    return false;
                } else {
                    return Boolean.valueOf(obj.toString());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        invokeItemSelectMethod(handler, itemSelectMethod, parent, view, position, id);
    }

    public static Object invokeItemSelectMethod(Object handler, String methodName, Object... params) {
        if (handler == null)
            return null;
        Method method = null;
        try {
            method = handler.getClass().getDeclaredMethod(methodName, AdapterView.class, View.class, int.class, long.class);
            if (method != null)
                return method.invoke(handler, params);
            else
                throw new RuntimeException("no such method:" + methodName);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {
        invokeNoSelectMethod(handler, nothingSelectedMethod, parent);
    }

    public static Object invokeNoSelectMethod(Object handler, String methodName, Object... params) {
        if (handler == null)
            return null;
        Method method = null;
        try {
            method = handler.getClass().getDeclaredMethod(methodName, AdapterView.class);
            if (method != null)
                return method.invoke(handler, params);
            else
                throw new RuntimeException("no such method:" + methodName);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    @Override
    public boolean onLongClick(View v) {
        return invokeLongClickMethod(handler, longClickMethod, v);
    }

    public static boolean invokeLongClickMethod(Object handler, String longClickMethod, Object... params) {
        if (handler == null) {
            return false;
        }
        Method method = null;
        try {
            method = handler.getClass().getDeclaredMethod(longClickMethod, View.class);
            if (method != null) {
                method.setAccessible(true);
                Object obj = method.invoke(handler, params);
                if (obj == null) {
                    return false;
                } else {
                    return Boolean.valueOf(obj.toString());
                }
            } else {
                throw new RuntimeException("no such method");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }
}
