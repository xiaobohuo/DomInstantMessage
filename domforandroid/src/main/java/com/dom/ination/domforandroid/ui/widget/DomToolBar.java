package com.dom.ination.domforandroid.ui.widget;

import android.content.Context;
import android.support.annotation.Nullable;
import android.support.v7.widget.Toolbar;
import android.util.AttributeSet;
import android.view.MotionEvent;

import com.dom.ination.domforandroid.ui.activity.BaseActivity;

/**
 * Created by 10174987 on 2016/8/29.
 */

public class DomToolBar extends Toolbar {
    static final String TAG = "DomToolbar";

    public DomToolBar(Context context) {
        super(context);
    }

    public DomToolBar(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public DomToolBar(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    private long lastClickTime = 0;

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        boolean ret = super.onTouchEvent(ev);
        if (ev.getAction() == MotionEvent.ACTION_UP) {
            if (lastClickTime != 0) {
                if (System.currentTimeMillis() - lastClickTime <= 500) {
                    BaseActivity activity = BaseActivity.getRunningActivity();
                    if (activity != null && activity instanceof OnToolbarDoubleClick)
                        ((OnToolbarDoubleClick) activity).onToolbarDoubleClick();
                }
            }

            lastClickTime = System.currentTimeMillis();
        }
        return ret;
    }

    public void performDoublcClick() {
        BaseActivity activity = BaseActivity.getRunningActivity();
        if (activity != null && activity instanceof OnToolbarDoubleClick)
            ((OnToolbarDoubleClick) activity).onToolbarDoubleClick();
    }

    public interface OnToolbarDoubleClick {

        boolean onToolbarDoubleClick();

    }
}
