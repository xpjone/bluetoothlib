package com.plugin.pjxie.bluetoothlib;

import android.bluetooth.BluetoothDevice;
import android.content.Context;

import java.util.List;
import java.util.Map;

/**
 * 创建者：pjxie
 * 创建日期：2019-05-08BlueToothInterface
 * 邮箱：pjxie@iflytek.com
 * 描述：蓝牙暴露接口
 */
public interface BlueToothInterface {
    /**
     * @desc 初始化数据，与disconnect配对使用¬
     * @author pjxie
     * @time 2019-05-10 14:07
     * @changed
     */
    void init(Context context, InitListener initListener);

    /**
     * @desc 开启蓝牙
     * @author pjxie
     * @time 2019-05-10 14:07
     * @changed
     */
    void openBluetooth();

    /**
     * @desc 搜索蓝牙
     * @author pjxie
     * @time 2019-05-10 14:07
     * @changed
     */
    void searchBlueTooth(boolean isBLE);

    /**
     * @desc 获取是否打开蓝牙
     * @author pjxie
     * @time 2019-05-10 14:08
     * @changed
     */
    boolean isOpen();

    /**
     * @desc 获取是否连接
     * @author pjxie
     * @time 2019-05-10 14:11
     * @changed
     */
    boolean isConnect();

    /**
     * @desc 连接蓝牙（经典蓝牙）
     * @author pjxie
     * @time 2019-05-10 14:11
     * @changed
     */
    String connectDevice(int position);

    /**
     * @desc 连接心跳设备蓝牙
     * @author pjxie
     * @time 2019-05-10 14:11
     * @changed
     */
    String connectAiecgDevice(int position);

    /**
     * @desc 关闭蓝牙连接（与init配对使用）
     * @author pjxie
     * @time 2019-05-10 14:12
     * @changed
     */
    void disConnectDevice();

    /**
     * @desc 销毁对象
     * @author pjxie
     * @time 2019-05-10 14:12
     * @changed
     */
    void destory();

    void setBluetoothListener(BluetoothListenser blueListener);

    /**
     * @desc 获取设备
     * @author pjxie
     * @time 2019-05-10 17:29
     * @changed
     */
    List<BluetoothDevice> getDevices();

    /**
     * @desc 发送数据到服务端
     * @author pjxie
     * @time 2019-05-10 17:29
     * @changed
     */
    void sendData(String url, String clientId, String clientSecret, Map<String,String> headers, String json, HttpListener httpListener);

    /**
     * @desc 经典蓝牙服务监听接口
     * @author pjxie
     * @time 2019-05-22 11:10
     * @changed
     */
    void accept();

    /**
     * @desc 经典蓝牙发送数据
     * @author pjxie
     * @time 2019-05-22 11:10
     * @changed
     */
    void sendBlData(String data);


    boolean isHaveBlueTooth();
}
