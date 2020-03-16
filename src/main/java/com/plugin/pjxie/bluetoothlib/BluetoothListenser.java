package com.plugin.pjxie.bluetoothlib;

/**
 * 创建者：pjxie
 * 创建日期：2019-05-08BluetoothListenser
 * 邮箱：pjxie@iflytek.com
 * 描述：TODO
 */
public interface BluetoothListenser {
    int DISCONNECTED = 0;//断开回调
    int CONNECTED = 1;//连接回调
    int MSG = 2;
    int UPDATEDEVICE = 3;//更新设备的回调
    int OVERSEARCH = 4;//搜索设备结束的回调
    int ERROR = 5;//错误的回调
    int RECEIVEDATA = 6;//回调服务返回的数据

    void notify(int state, Object obj);
//    /**
//     * @desc 更新设备的回调
//     * @author pjxie
//     * @time 2019-05-10 14:18
//     * @changed
//     */
//    void updateBlueDevice();
//
//    /**
//     * @desc 已连接的回调
//     * @author pjxie
//     * @time 2019-05-10 14:18
//     * @changed
//     */
//    void connected();

//    /**
//     * @desc 错误的回调
//     * @author pjxie
//     * @time 2019-05-10 14:18
//     * @changed
//     */
//    void error(int error);
//
//    /**
//     * @desc 回调服务返回的数据
//     * @author pjxie
//     * @time 2019-05-10 14:18
//     * @changed
//     */
//    void receiverData(String data);
//
//    /**
//     * @desc 搜索设备结束的回调
//     * @author pjxie
//     * @time 2019-05-10 14:32
//     * @changed
//     */
//    void overSearch();
}
