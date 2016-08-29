package com.dom.ination.domforandroid.component.bitmaploader.core;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.text.TextUtils;

import com.dom.ination.domforandroid.common.utils.BitmapUtil;
import com.dom.ination.domforandroid.common.utils.KeyGenerator;
import com.dom.ination.domforandroid.common.utils.Logger;
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
        BitmapUtil.BitmapType bitmapType = BitmapUtil.getType(bitmapBytes);
        MyBitmap myBitmap = null;

        // 如果图片取自压缩目录，则不再对图片做压缩或者其他处理，直接返回
        if ((flag & 0x01) != 0) {
            myBitmap = new MyBitmap(BitmapDecoder.decodeSampledBitmapFromByte(context, bitmapBytes), url);
            return myBitmap;
        }

        // 判断是否需要压缩图片
        IBitmapCompress bitmapCompress = config.getBitmapCompress().newInstance();
        myBitmap = bitmapCompress.compress(bitmapBytes, getOrigFile(url), url, config, options.outWidth, options.outHeight);
        Bitmap bitmap = myBitmap.getBitmap();
        if (bitmap == null) {
            // 如果没压缩，就原始解析图片
            bitmap = BitmapDecoder.decodeSampledBitmapFromByte(context, bitmapBytes);
        } else {
            // 如果图片做了压缩处理，则需要写入二级缓存
            writeToComp = true;
        }

        // 对图片做圆角处理
        if (bitmapType != BitmapUtil.BitmapType.gif && config.getCorner() > 0) {
            bitmap = BitmapUtil.setImageCorner(bitmap, config.getCorner());
            bitmapType = BitmapUtil.BitmapType.png;
        }

        // GIF图片，进行压缩
        if (bitmapType == BitmapUtil.BitmapType.gif)
            writeToComp = true;

        // 当图片做了圆角、压缩处理后，将图片放置二级缓存
        if (writeToComp && config.isCompressCacheEnable()) {
            String key = KeyGenerator.generateMD5(BitmapLoader.getKeyByConfig(url, config));

            // PNG以外其他格式，都压缩成JPG格式
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            bitmap.compress(BitmapUtil.BitmapType.png == bitmapType ? Bitmap.CompressFormat.PNG : Bitmap.CompressFormat.JPEG, 100, out);
            byte[] bytes = out.toByteArray();
            writeBytesToCompressDisk(url, key, bytes);

            // 如果是GIF图片，无论如何，返回压缩格式图片
            if (bitmapType == BitmapUtil.BitmapType.gif) {
                Logger.v(TAG, String.format("parse gif image[url=%s,key=%s]", url, key));
                bitmap.recycle();
                bitmap = BitmapDecoder.decodeSampledBitmapFromByte(context, bytes);
            }
        }

        myBitmap.setBitmap(bitmap);
        return myBitmap;


    }
}
