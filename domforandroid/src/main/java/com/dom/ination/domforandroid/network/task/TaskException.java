package com.dom.ination.domforandroid.network.task;

import android.content.res.Resources;
import android.text.TextUtils;

import com.dom.ination.domforandroid.R;
import com.dom.ination.domforandroid.common.context.GlobalContext;

import org.w3c.dom.Text;

/**
 * Created by huoxiaobo on 16/8/23.
 */
public class TaskException extends Exception {

    private static final long serialVersionUID = -6262214243381380676L;

    public enum TaskError {

        //网络错误
        failIOError,
        //无网络连接
        noneNetwork,
        //连接超时
        timeout,
        //响应超时
        socketTimeout,
        //返回数据不合法
        resultIllegal
    }

    private String code;

    private String msg = "";

    private static IExceptionDeclare exceptionDeclare;

    public TaskException(String code) {
        this.code = code;
    }

    public TaskException(String code, String msg) {
        this(code);
        this.msg = msg;
    }

    public String getCode() {
        return code;
    }

    @Override
    public String getMessage() {
        if (!TextUtils.isEmpty(msg)) {
            return msg + "";
        }
        if (!TextUtils.isEmpty(code) && exceptionDeclare != null) {
            String msg = exceptionDeclare.checkCode(code);
            if (!TextUtils.isEmpty(msg)) {
                return msg + "";
            }
        }

        if (GlobalContext.getInstance() != null) {
            Resources resources = GlobalContext.getInstance().getResources();
            TaskError error = TaskError.valueOf(code);
            if (error == TaskError.noneNetwork || error == TaskError.failIOError) {
                msg = resources.getString(R.string.comm_error_noneNetwork);
            } else if (error == TaskError.socketTimeout || error == TaskError.timeout) {
                msg = resources.getString(R.string.comm_error_timeout);
            } else if (error == TaskError.resultIllegal) {
                msg = resources.getString(R.string.comm_error_resultIllegal);
            }
            if (!TextUtils.isEmpty(msg)) {
                return msg + "";
            }
        }

        return super.getMessage() + "";
    }

    public static void config(IExceptionDeclare declare) {
        TaskException.exceptionDeclare = declare;
    }

    public static void checkResponse(String response) throws TaskException {
        if (TaskException.exceptionDeclare != null) {
            TaskException.exceptionDeclare.checkResponse(response);
        }
    }
}
