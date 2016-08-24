package com.dom.ination.domforandroid.network.cache;

import com.dom.ination.domforandroid.common.setting.Setting;
import com.dom.ination.domforandroid.network.biz.IResult;
import com.dom.ination.domforandroid.network.http.Params;

/**
 * Created by huoxiaobo on 16/8/25.
 */
public interface ICacheUtility {
    IResult findCacheData(Setting action, Params params);

    void addCacheData(Setting action, Params params, IResult result);
}
