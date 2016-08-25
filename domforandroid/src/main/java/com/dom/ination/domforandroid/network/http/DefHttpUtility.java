package com.dom.ination.domforandroid.network.http;

import android.text.TextUtils;
import android.util.Log;

import com.alibaba.fastjson.JSON;
import com.dom.ination.domforandroid.common.context.GlobalContext;
import com.dom.ination.domforandroid.common.setting.Setting;
import com.dom.ination.domforandroid.common.setting.SettingUtility;
import com.dom.ination.domforandroid.common.utils.Logger;
import com.dom.ination.domforandroid.common.utils.SystemUtils;
import com.dom.ination.domforandroid.network.biz.ABizLogic;
import com.dom.ination.domforandroid.network.task.TaskException;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.SocketTimeoutException;
import java.util.Set;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * Created by huoxiaobo on 16/8/25.
 */
public class DefHttpUtility implements IHttpUtility {

    static String getTag(Setting action, String append) {
        return ABizLogic.getTag(action, append);
    }

    @Override
    public <T> T doGet(HttpConfig config, Setting action, Params urlParams, Class<T> responseCls) throws TaskException {
        Request.Builder builder = createRequestBuilder(config, action, urlParams, "GET");
        Request request = builder.build();
        return executeRequest(request, responseCls, action, "GET");
    }

    private Request.Builder createRequestBuilder(HttpConfig config, Setting action, Params urlParams, String method) throws TaskException {
        if (GlobalContext.getInstance() != null && SystemUtils.getNetworkType(GlobalContext.getInstance()) == SystemUtils.NetWorkType.none) {
            Logger.w(getTag(action, method), "没有网络连接");
            throw new TaskException(TaskException.TaskError.noneNetwork.toString());
        }
        String url = (config.baseUrl + action.getValue() + (urlParams == null ? "" : "?" + ParamsUtil.encodeToURLParams(urlParams))).replace(" ", "");
        Logger.d(getTag(action, method), url);

        Request.Builder builder = new Request.Builder();
        builder.url(url);

        //add Cookie
        if (!TextUtils.isEmpty(config.cookie)) {
            builder.header("Cookie", config.cookie);
            Logger.d(getTag(action, method), "Cookie = " + config.cookie);
        }

        //add header
        if (config.headerMap.size() > 0) {
            Set<String> keySet = config.headerMap.keySet();
            for (String key : keySet) {
                builder.addHeader(key, config.headerMap.get(key));
                Logger.d(getTag(action, method), "Header[%s,%s]", key, config.headerMap.get(key));
            }
        }
        return builder;
    }

    @Override
    public <T> T doPost(HttpConfig config, Setting action, Params urlParams, Params bodyParams, Object requestObj, Class<T> responseCls) throws TaskException {
        Request.Builder builder = createRequestBuilder(config, action, urlParams, "POST");
        if (bodyParams != null) {
            String requestBodyStr = ParamsUtil.encodeToURLParams(bodyParams);
            Logger.d(getTag(action, "Post"), requestBodyStr);
            builder.post(RequestBody.create(MediaType.parse("application/x-www-form-urlencoded;charset=UTF-8"), requestBodyStr));
        } else if (requestObj != null) {
            String requestBodyStr = JSON.toJSONString(requestObj);
            Logger.d(getTag(action, "Post"), requestBodyStr);
            builder.post(RequestBody.create(MediaType.parse("application/json; charset=UTF-8"), requestBodyStr));
        }
        return executeRequest(builder.build(), responseCls, action, "POST");
    }

    @Override
    public <T> T doPostFiles(HttpConfig config, Setting action, Params urlParams, Params bodyParams, MultipartFile[] files, Class<T> responseCls) throws TaskException {
        String method = "doPostFiles";

        Request.Builder builder = createRequestBuilder(config, action, urlParams, method);
        MultipartBody.Builder bodyBuilder = new MultipartBody.Builder();
        bodyBuilder.setType(MultipartBody.FORM);
        //处理body参数
        if (bodyParams != null && bodyParams.getKeys().size() > 0) {
            for (String key : bodyParams.getKeys()) {
                String value = bodyParams.getParameter(key);
                bodyBuilder.addFormDataPart(key,value);
                Logger.d(getTag(action, method), "BodyParam[%s, %s]", key, value);
            }
        }
        //处理文件数据
        if (files!=null && files.length >0){
            for (MultipartFile file:files) {
                if (file.getBytes()!=null){  //字节流
                    bodyBuilder.addFormDataPart(file.getKey(),file.getKey(),RequestBody.create(MediaType.parse("application/octet-stream"), file.getBytes()));
                    Logger.d(getTag(action, method), "Multipart bytes, length = " + file.getBytes().length);
                }else if(file.getFile()!=null){ // 文件
                    bodyBuilder.addFormDataPart(file.getKey(), file.getFile().getName(), RequestBody.create(MediaType.parse(file.getContentType()), file.getFile()));

                    Logger.d(getTag(action, method), "Multipart file, name = %s, path = %s", file.getFile().getName(), file.getFile().getAbsolutePath());
                }
            }
        }
        RequestBody requestBody = bodyBuilder.build();
        builder.post(requestBody);
        return executeRequest(builder.build(),responseCls,action,method);
    }

    private <T> T executeRequest(Request request, Class<T> responseCls, Setting action, String method) throws TaskException {
        try {
            if (SettingUtility.getPermanentSettingAsInt("http_delay") > 0) {
                Thread.sleep(SettingUtility.getPermanentSettingAsInt("http_delay"));
            }
        } catch (Exception e) {
        }

        try {
            Response response = getOkHttpClient().newCall(request).execute();
            Logger.w(getTag(action, method), "Http-code = %d", response.code());
            if (!(response.code() == HttpURLConnection.HTTP_OK || response.code() == HttpURLConnection.HTTP_PARTIAL)) {
                String responseStr = response.body().string();
                if (Logger.DEBUG) {
                    Logger.w(getTag(action, method), responseStr);
                }
                TaskException.checkResponse(responseStr);
                throw new TaskException(TaskException.TaskError.timeout.toString());
            } else {
                String responseStr = response.body().string();
                Logger.v(getTag(action, method), "Response = %s", responseStr);
                return parResponse(responseStr, responseCls);
            }
        } catch (SocketTimeoutException e) {
            Logger.printExc(DefHttpUtility.class, e);
            Logger.w(getTag(action, method), e + "");

            throw new TaskException(TaskException.TaskError.timeout.toString());
        } catch (IOException e) {
            Logger.printExc(DefHttpUtility.class, e);
            Logger.w(getTag(action, method), e + "");

            throw new TaskException(TaskException.TaskError.timeout.toString());
        } catch (TaskException e) {
            Logger.printExc(DefHttpUtility.class, e);
            Logger.w(getTag(action, method), e + "");

            throw e;
        } catch (Exception e) {
            Logger.printExc(DefHttpUtility.class, e);
            Logger.w(getTag(action, method), e + "");

            throw new TaskException(TaskException.TaskError.resultIllegal.toString());
        }
    }

    protected <T> T parResponse(String resultStr, Class<T> responseCls) throws TaskException {
        if (responseCls.getSimpleName().equals("String")) {
            return (T) resultStr;
        }
        T result = JSON.parseObject(resultStr, responseCls);
        return result;
    }

    public OkHttpClient getOkHttpClient() {
        return GlobalContext.getOkHttpClient();
    }
}
