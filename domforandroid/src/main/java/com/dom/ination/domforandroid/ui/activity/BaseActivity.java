package com.dom.ination.domforandroid.ui.activity;

import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.support.annotation.LayoutRes;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;

import com.dom.ination.domforandroid.R;
import com.dom.ination.domforandroid.common.setting.SettingUtility;

import java.lang.reflect.Field;
import java.util.Locale;

public class BaseActivity extends AppCompatActivity {

    static final String TAG = "BaseActivity";
    private BaseActivityHelper baseActivityHelper;
    private int theme = 0;
    private Locale locale = null;
    //    private TaskManager taskManager;
    private boolean isDestory = false;
    private static BaseActivity runningActivity = null;
//    private Map<String, WeakReference<ABaseFragent>> framgentRefs = null;
    private Toolbar mToolbar;
    private View rootView;

    public static BaseActivity getRunningActivity() {
        return runningActivity;
    }

    public static void setRunningActivity(BaseActivity activity) {
        runningActivity = activity;
    }

    protected int configTheme() {
        if (baseActivityHelper != null) {
            int theme = baseActivityHelper.configTheme();
            if (theme > 0) {
                return theme;
            }
        }

        return -1;
    }

    public void setLanguage(Locale locale) {
        Resources res = getResources();
        Configuration configuration = res.getConfiguration();
        configuration.locale = locale;
        DisplayMetrics metrics = res.getDisplayMetrics();
        res.updateConfiguration(configuration, metrics);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        if (baseActivityHelper == null) {
            try {
                if (SettingUtility.getStringSetting("activity_helper") != null) {
                    baseActivityHelper = (BaseActivityHelper) Class.forName(SettingUtility.getStringSetting("activity_helper")).newInstance();
                    baseActivityHelper.bindActivity(this);
                }
            } catch (Exception e) {

            }
        }

        if (baseActivityHelper != null) {
            baseActivityHelper.onCreate(savedInstanceState);
        }

//        framgentRefs = new HashMap<>();

        if (savedInstanceState == null) {
            theme = configTheme();
            locale = new Locale(SettingUtility.getPermanentSettingAsStr("language", Locale.getDefault().getLanguage()),
                    SettingUtility.getPermanentSettingAsStr("language-country", Locale.getDefault().getCountry()));
        } else {
            theme = savedInstanceState.getInt("theme");
            locale = new Locale(savedInstanceState.getString("language"), savedInstanceState.getString("language-country"));
        }

        if (theme > 0) {
            setTheme(theme);
        }
        setLanguage(locale);

//        taskManager = new TaskManager();

        //有实体键就不显示overflow菜单
        ViewConfiguration viewConfiguration = ViewConfiguration.get(this);
        if (viewConfiguration.hasPermanentMenuKey()) {
            try {
                Field menuField = ViewConfiguration.class.getDeclaredField("sHasPermanentMenuKey");
                menuField.setAccessible(true);
                menuField.setBoolean(viewConfiguration, false);
            } catch (Exception e) {

            }
        }
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onPostCreate(Bundle savedInstanceState, PersistableBundle persistentState) {
        super.onPostCreate(savedInstanceState, persistentState);
        if (baseActivityHelper != null) {
            baseActivityHelper.onPostCreate(savedInstanceState);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();

        if (baseActivityHelper != null)
            baseActivityHelper.onStart();
    }

    @Override
    protected void onRestart() {
        super.onRestart();

        if (baseActivityHelper != null)
            baseActivityHelper.onRestart();
    }

    public Toolbar getToolbar(){
        return mToolbar;
    }

    @Override
    public void setContentView(@LayoutRes int layoutResID) {
        setContentView(View.inflate(this,layoutResID,null));
    }

    public View getRootView(){
        return rootView;
    }

    @Override
    public void setContentView(View view, ViewGroup.LayoutParams params) {
        super.setContentView(view, params);
        rootView = view;
        InjectUtility.initInjectedView(this);
    }

    @Override
    public void setContentView(View view) {
        super.setContentView(view,new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        rootView = view;
        InjectUtility.initInjectView(this);
        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        if (mToolbar!=null){
            setSupportActionBar(mToolbar);
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (baseActivityHelper != null){
            baseActivityHelper.onSaveInstanceState(outState);
        }
        outState.putInt("theme",theme);
        outState.putString("language",locale.getLanguage());
        outState.putString("language-country",locale.getCountry());
    }

//    public void addFragment(String tag,ABaseFragment fragment){
//
//    }

//    public void removeFragment(String tag){
//
//    }


}
