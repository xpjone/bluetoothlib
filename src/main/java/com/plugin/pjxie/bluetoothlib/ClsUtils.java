package com.plugin.pjxie.bluetoothlib;

/**
 * 创建者：pjxie
 * 创建日期：2019-05-09ClUtils
 * 邮箱：pjxie@iflytek.com
 * 描述：TODO
 */

import android.util.Log;

import java.io.File;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class ClsUtils {
    private static final String TAG = ClsUtils.class.getSimpleName();
    public static final Executor EXECUTOR = Executors.newCachedThreadPool();

    public static void mkdirs(String filePath) {
        boolean mk = new File(filePath).mkdirs();
        Log.d(TAG, "mkdirs: " + mk);
    }

    static public String bytesToHexString(byte[] src) {
        StringBuilder stringBuilder = new StringBuilder("");
        if (src == null || src.length <= 0) {
            return null;
        }
        for (int i = 0; i < src.length; i++) {
            int v = src[i] & 0xFF;
            String hv = Integer.toHexString(v);
            if (hv.length() < 2) {
                stringBuilder.append(0);
            }
            stringBuilder.append(hv);
        }
        return stringBuilder.toString();
    }

    static public String formatData(List<String> datas) {
        StringBuilder stringBuilder = new StringBuilder();
        for (String data : datas) {
            String value = data.substring(4, 40);
            for (int i = 0; i < 9; i++) {
                double num = Integer.parseInt(value.substring((0 + i * 4), (4 + i * 4)), 16);//将切割得到的16进制字符转成10进制
                num = (num - 1100) * 0.00167;//该步骤为固定公式，将设备采集数据转化为零点电压为0，增益为1的心电数据
                stringBuilder.append(String.format("%.5f", num));
                stringBuilder.append(",");
            }
        }
        return stringBuilder.toString();
    }
}

