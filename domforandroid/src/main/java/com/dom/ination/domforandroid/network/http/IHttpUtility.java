package com.dom.ination.domforandroid.network.http;

import com.dom.ination.domforandroid.common.setting.Setting;
import com.dom.ination.domforandroid.network.task.TaskException;

import java.io.File;

/**
 * Created by huoxiaobo on 16/8/25.
 */
public interface IHttpUtility {
    <T> T doGet(HttpConfig config, Setting action,Params urlParams,Class<T> responseCls) throws TaskException;

    <T> T doPost(HttpConfig config, Setting action, Params urlParams, Params bodyParams, Object requestObj, Class<T> responseCls) throws TaskException;

    <T> T doPostFiles(HttpConfig config, Setting action, Params urlParams, Params bodyParams, MultipartFile[] files, Class<T> responseCls) throws TaskException;

    class MultipartFile {
        private String contentType;
        private File file;
        private String key;
        private byte[] bytes;

        public MultipartFile(String contentType, String key, File file) {
            this.key = key;
            this.contentType = contentType;
            this.file = file;
        }

        public MultipartFile(String contentType, String key, byte[] bytes) {
            this.key = key;
            this.contentType = contentType;
            this.bytes = bytes;
        }

        public String getContentType() {
            return contentType;
        }

        public void setContentType(String contentType) {
            this.contentType = contentType;
        }

        public File getFile() {
            return file;
        }

        public void setFile(File file) {
            this.file = file;
        }

        public byte[] getBytes() {
            return bytes;
        }

        public void setBytes(byte[] bytes) {
            this.bytes = bytes;
        }

        public String getKey() {
            return key;
        }

        public void setKey(String key) {
            this.key = key;
        }
    }
}
