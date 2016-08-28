package com.dom.ination.domforandroid.component.bitmaploader.core;

import android.content.Context;
import android.graphics.BitmapFactory;
import android.text.TextUtils;

import com.dom.ination.domforandroid.common.utils.KeyGenerator;
import com.dom.ination.domforandroid.component.bitmaploader.BitmapLoader;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Created by huoxiaobo on 16/8/29.
 */
public class BitmapProcess {
    private static final String TAG = "BitmapProcess";

    private FileDisk compFileDisk;  //保存压缩或者缩放后的图片
    private FileDisk origFileDisk;  //保存原始下载

    public BitmapProcess(String imageCache) {
        compFileDisk = new FileDisk(imageCache + File.separator + "compression");
        origFileDisk = new FileDisk(imageCache + File.separator + "originate");
    }

    private byte[] getBitmapFromDiskCache(String url, String key, FileDisk fileDisk, ImageConfig config) throws Exception {
        InputStream inputStream = fileDisk.getInputStream(url, key);
        if (inputStream == null) {
            return null;
        }
        if (config.getProgress() != null) {
            config.getProgress().sendLength(inputStream.available());
        }
        byte[] buffer = new byte[8 * 1024];
        int readLen = -1;
        int readBytes = 0;
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        while ((readLen = inputStream.read(buffer)) != -1) {
            readBytes += readLen;
            if (config.getProgress() != null) {
                config.getProgress().sendProgress(readBytes);
            }
            outputStream.write(buffer, 0, readLen);
        }
        return outputStream.toByteArray();
    }

    public File getOrigFile(String url) {
        String key = KeyGenerator.generateMD5(BitmapLoader.getKeyByConfig(url, null));
        return origFileDisk.getFile(url, key);
    }

    public File getCompressFile(String url, String imageId) {
        ImageConfig config = null;
        if (!TextUtils.isEmpty(imageId)) {
            config = new ImageConfig();
            config.setId(imageId);
        }
        String key = KeyGenerator.generateMD5(BitmapLoader.getKeyByConfig(url, config));
        return compFileDisk.getFile(url, key);
    }

    public void deleteFile(String url, ImageConfig config) {
        String key = KeyGenerator.generateMD5(BitmapLoader.getKeyByConfig(url, config));

        compFileDisk.deleteFile(url, key);
        origFileDisk.deleteFile(url, key);
    }

    public void writeBytesToOrigDisk(byte[] bs, String url) throws Exception {
        String key = KeyGenerator.generateMD5(BitmapLoader.getKeyByConfig(url, null));
        OutputStream out = origFileDisk.getOutputStream(url, key);

        ByteArrayInputStream in = new ByteArrayInputStream(bs);
        byte[] buffer = new byte[8 * 1024];
        int len = -1;
        while ((len = in.read(buffer)) != -1)
            out.write(buffer, 0, len);

        out.flush();
        in.close();
        out.close();
        origFileDisk.renameFile(url, key);
    }

    public void writeBytesToCompressDisk(String url, String key, byte[] bytes) throws Exception {
        OutputStream out = compFileDisk.getOutputStream(url, key);

        ByteArrayInputStream in = new ByteArrayInputStream(bytes);
        byte[] buffer = new byte[8 * 1024];
        int len = -1;
        while ((len = in.read(buffer)) != -1)
            out.write(buffer, 0, len);

        out.flush();
        in.close();
        out.close();
        compFileDisk.renameFile(url, key);
    }

    public byte[] getBitmapFromCompDiskCache(String url, ImageConfig config) throws Exception {
        String key = KeyGenerator.generateMD5(BitmapLoader.getKeyByConfig(url, config));

        return getBitmapFromDiskCache(url, key, compFileDisk, config);
    }

    public byte[] getBitmapFromOrigDiskCache(String url, ImageConfig config) throws Exception {
        String key = KeyGenerator.generateMD5(BitmapLoader.getKeyByConfig(url, null));

        return getBitmapFromDiskCache(url, key, origFileDisk, config);
    }

    public MyBitmap compressBitmap(Context context, byte[] bitmapBytes, String url, int flag, ImageConfig config) throws Exception {
        boolean writeToComp = config.getCorner() > 0;

        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeByteArray(bitmapBytes, 0, bitmapBytes.length, options);
        BitmapType bitmapType = BitmapUtil.getType(bitmapBytes);
    }
}
