package com.dom.ination.domforandroid.network.task;

/**
 * Created by huoxiaobo on 16/8/23.
 */
public interface IExceptionDeclare {
    void checkResponse(String response) throws TaskException;

    String checkCode(String code);
}
