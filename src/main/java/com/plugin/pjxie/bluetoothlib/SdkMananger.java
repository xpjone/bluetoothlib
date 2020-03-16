package com.plugin.pjxie.bluetoothlib;

import android.Manifest;
import android.app.Activity;
import android.app.Application;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.pm.PackageManager;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.lzy.okgo.OkGo;
import com.lzy.okgo.callback.StringCallback;
import com.lzy.okgo.model.HttpHeaders;
import com.lzy.okgo.model.Response;

import java.util.List;
import java.util.Map;

/**
 * 创建者：pjxie
 * 创建日期：2019-05-07SdkMananger
 * 邮箱：pjxie@iflytek.com
 * 描述：TODO
 */
public final class SdkMananger implements BlueToothInterface {
    public final static int ERRORCONNECT_CODE = 30001;
    public final static int ERRORBOND_CODE = 30002;
    public final static int ERRORNOTFOUND_CODE = 30003;
    private static SdkMananger instance;
    private static Object object = new Object();
    private BlueToothService blueToothService;

    /**
     * @desc 单例操作
     * @author pjxie
     * @time 2019-05-08 09:10
     * @changed
     */
    public static SdkMananger getInstance() {
        if (instance == null) {
            synchronized (object) {
                if (instance == null) {
                    instance = new SdkMananger();
                }
            }
        }
        return instance;
    }

    @Override
    public void init(Context context, InitListener initListener) {
        OkGo.getInstance().init((Application) context.getApplicationContext());
        String[] permissions = new String[]{Manifest.permission.ACCESS_FINE_LOCATION};
        if (!hasPermission(context, permissions)) {
            requestPermission(context, permissions);
        }
        if (blueToothService != null) {
            blueToothService.destroy();
            blueToothService = null;
        }
        blueToothService = new BlueToothService(context);
        if (!blueToothService.isHaveBlueTooth()) {
            initListener.initError();
        }
    }

    @Override
    public void openBluetooth() {
        blueToothService.openBlueTooth();
    }

    @Override
    public void searchBlueTooth(boolean isBle) {
        blueToothService.searchDevice(isBle);
    }

    @Override
    public boolean isOpen() {
        return blueToothService.isBlueToothOpen();
    }

    @Override
    public boolean isConnect() {
        return false;
    }

    @Deprecated
    @Override
    public String connectDevice(int position) {
        blueToothService.connect(position);
        return null;
    }

    @Override
    public String connectAiecgDevice(int position) {
        blueToothService.connectAceg(position);
        return null;
    }


    @Override
    public void disConnectDevice() {
        blueToothService.disConnect();
    }

    @Override
    public void destory() {
        blueToothService.destroy();
    }

    @Override
    public void setBluetoothListener(BluetoothListenser blueListener) {
        blueToothService.setmBluetoothListenser(blueListener);
    }

    @Override
    public List<BluetoothDevice> getDevices() {
        return blueToothService.getList();
    }


    @Override
    public void sendData(String url, String clientId, String clientSecret, Map<String, String> headers, String json, final HttpListener httpListener) {
        String mUrl = String.format(url, clientId, clientSecret);
        HttpHeaders httpHeaders = new HttpHeaders();
        for (String key : headers.keySet()) {
            httpHeaders.put(key, headers.get(key));
        }


        OkGo.<String>post(mUrl).tag(this).isMultipart(true).headers(httpHeaders).upJson(json).execute(new StringCallback() {
            @Override
            public void onSuccess(Response<String> response) {
                if (httpListener != null) {
                    httpListener.success(response.body());
                }
            }

            @Override
            public void onError(Response<String> response) {
                super.onError(response);
                if (httpListener != null) {
                    httpListener.error(response.code());
                }
            }
        });
    }

    @Override
    public void accept() {
        blueToothService.accept();
    }

    @Override
    public void sendBlData(String data) {
        blueToothService.sendMsg(data);
    }

    @Override
    public boolean isHaveBlueTooth() {
        return blueToothService.isHaveBlueTooth();
    }


    /**
     * @desc 请求权限获取
     * @author pjxie
     * @time 2019-05-13 16:33
     * @changed
     */
    private void requestPermission(Context context, String[] permissions) {
        ActivityCompat.requestPermissions((Activity) context, permissions, 10001);
    }

    /**
     * 判断是否拥有权限
     *
     * @param permissions
     * @return
     */
    private boolean hasPermission(Context context, String[] permissions) {
        for (String permission : permissions) {
            if (ContextCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }


}
