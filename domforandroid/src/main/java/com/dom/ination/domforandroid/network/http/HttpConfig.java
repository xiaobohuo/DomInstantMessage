package com.dom.ination.domforandroid.network.http;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by huoxiaobo on 16/8/25.
 */
public class HttpConfig {
    public String cookie;
    public String baseUrl;
    public Map<String, String> headerMap = new HashMap<>();

    @Override
    protected Object clone() throws CloneNotSupportedException {
        super.clone();
        HttpConfig httpConfig = new HttpConfig();
        httpConfig.cookie = cookie;
        httpConfig.baseUrl = baseUrl;
        httpConfig.headerMap = headerMap;
        return httpConfig;
    }

    public void addHeader(String key, String value) {
        headerMap.put(key, value);
    }
}
