package com.dom.ination.domforandroid.network.http;

import com.dom.ination.domforandroid.common.context.GlobalContext;
import com.dom.ination.domforandroid.common.setting.Setting;
import com.dom.ination.domforandroid.network.biz.ABizLogic;
import com.dom.ination.domforandroid.network.task.TaskException;

import okhttp3.OkHttpClient;

/**
 * Created by huoxiaobo on 16/8/25.
 */
public class DefHttpUtility implements IHttpUtility {

    static String getTag(Setting action, String append) {
        return ABizLogic.getTag(action, append);
    }

    @Override
    public <T> T doGet(HttpConfig config, Setting action, Params urlParams, Class<T> responseCls) throws TaskException {
        return null;
    }

    @Override
    public <T> T doPost(HttpConfig config, Setting action, Params urlParams, Params bodyParams, Object requestObj, Class<T> responseCls) throws TaskException {
        return null;
    }

    @Override
    public <T> T doPostFiles(HttpConfig config, Setting action, Params urlParams, Params bodyParams, MultipartFile[] files, Class<T> responseCls) throws TaskException {
        return null;
    }

    public OkHttpClient getOkHttpClient() {
        return GlobalContext.getOkHttpClient();
    }
}
