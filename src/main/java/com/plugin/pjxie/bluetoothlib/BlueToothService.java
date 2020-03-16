package com.plugin.pjxie.bluetoothlib;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.ParcelUuid;
import android.util.Log;
import android.widget.Toast;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static android.content.ContentValues.TAG;
import static com.plugin.pjxie.bluetoothlib.SdkMananger.ERRORBOND_CODE;
import static com.plugin.pjxie.bluetoothlib.SdkMananger.ERRORCONNECT_CODE;
import static com.plugin.pjxie.bluetoothlib.SdkMananger.ERRORNOTFOUND_CODE;

/**
 * 创建者：pjxie
 * 创建日期：2019-05-08BlueToothService
 * 邮箱：pjxie@iflytek.com
 * 描述：TODO
 */
class BlueToothService extends BtBase {
    private static final UUID MY_UUID =
            UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    private BluetoothAdapter bluetoothadapter;
    private BluetoothGatt bluetoothGatt;
    private BlueToothReceiver blueToothReceiver;
    private List<BluetoothDevice> list = new ArrayList<>();
    private List<BluetoothGattCharacteristic> cServices = new ArrayList<>();
    private String mac = "";

    protected BlueToothService(Context context) {
        mContext = context;
        BluetoothManager bluetoothmanger = (BluetoothManager) mContext.getSystemService(Context.BLUETOOTH_SERVICE);
        bluetoothadapter = bluetoothmanger.getAdapter();
    }

    public String getMac() {
        return mac;
    }

    public List<BluetoothDevice> getList() {
        return list;
    }

    /**
     * @desc 是否具有蓝牙功能
     * @author pjxie
     * @time 2019-05-08 13:53
     * @changed
     */
    protected boolean isHaveBlueTooth() {
        return mContext.getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE);
    }

    /**
     * @desc 蓝牙是否打开
     * @author pjxie
     * @time 2019-05-08 13:52
     * @changed
     */
    protected boolean isBlueToothOpen() {
        return bluetoothadapter == null ? false : bluetoothadapter.isEnabled();
    }

    /**
     * @desc 开启蓝牙
     * @author pjxie
     * @time 2019-05-08 13:55
     * @changed
     */
    protected void openBlueTooth() {
        checkBlue();
        bluetoothadapter.enable();
    }

    /**
     * @desc 是否连接
     * @author pjxie
     * @time 2019-05-08 16:21
     * @changed
     */
    protected boolean isConnect() {
        checkBlue();
        if (bluetoothGatt != null) {
            return bluetoothGatt.connect();
        }
        return mSocket != null && mSocket.isConnected();
    }

    /**
     * @desc 开始搜索设备
     * @author pjxie
     * @time 2019-05-08 13:56
     * @changed
     */
    protected void searchDevice(boolean isBLe) {
        checkBlue();
        list.clear();
        if (bluetoothadapter.isDiscovering()) {
            bluetoothadapter.cancelDiscovery();
        }
        if (isBLe) {
            bluetoothadapter.getBluetoothLeScanner().startScan(buildScanFilters(), buildScanSettings(), scanCallback);
        } else {
            bluetoothadapter.startDiscovery();
            registerReceiver();
        }
    }

    /**
     * @desc BLE过滤
     * @author pjxie
     * @time 2019-05-14 10:07
     * @changed
     */
    private List<ScanFilter> buildScanFilters() {
        UUID uuid = UUID.fromString("00002dee-0000-1000-8000-00805f9b34fb");
        List<ScanFilter> scanFilters = new ArrayList<>();

        ScanFilter.Builder builder = new ScanFilter.Builder();
        // 注释掉下面的扫描过滤规则，才能扫描到（uuid不匹配没法扫描到的）
        builder.setServiceUuid(new ParcelUuid(uuid));
        scanFilters.add(builder.build());

        return scanFilters;
    }

    /**
     * @desc BLE设置
     * @author pjxie
     * @time 2019-05-14 10:07
     * @changed
     */
    private ScanSettings buildScanSettings() {
        ScanSettings.Builder builder = new ScanSettings.Builder();
        builder.setScanMode(ScanSettings.SCAN_MODE_LOW_POWER);
        return builder.build();
    }

    /**
     * @desc 对相关设备配对
     * @author pjxie
     * @time 2019-05-08 15:53
     * @changed
     */
    protected void bondDevice(int position) {
        checkBlue();
        Method method = null;
        try {
            method = BluetoothDevice.class.getMethod("createBond");
            method.invoke(list.get(position));
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            notifyUI(BluetoothListenser.ERROR, ERRORBOND_CODE);
        }
    }

    /**
     * @desc 进行连接
     * @author pjxie
     * @time 2019-05-08 15:53
     * @changed
     */
    protected void connect(int position) {
        checkBlue();
        disConnect();
        bluetoothadapter.cancelDiscovery();
        BluetoothDevice bluetoothDevice = list.get(position);
        if (isConnect() && mSocket.getRemoteDevice().getAddress().equals(bluetoothDevice.getAddress())) {
            Toast.makeText(mContext, "蓝牙已连接", Toast.LENGTH_SHORT).show();
            return;
        }
        Set<BluetoothDevice> bluetoothDevices = bluetoothadapter.getBondedDevices();
        for (BluetoothDevice bluetoothDevice1 : bluetoothDevices) {
            if (bluetoothDevice.getName().equals(bluetoothDevice1.getName())) {
                disConnect();
                try {
//             final BluetoothSocket socket = dev.createRfcommSocketToServiceRecord(SPP_UUID); //加密传输，Android系统强制配对，弹窗显示配对码
                    mSocket = bluetoothDevice1.createInsecureRfcommSocketToServiceRecord(SPP_UUID); //明文传输(不安全)，无需配对
                    // 开启子线程
                    ClsUtils.EXECUTOR.execute(() -> {
                                loopRead(); //循环读取
                            }
                    );
                } catch (Exception e) {
                    notifyUI(BluetoothListenser.ERROR, ERRORCONNECT_CODE);
                    close();
                }
                break;
            }
        }
        if (!isConnect()) {
            bondDevice(position);
        }
    }

    /**
     * 监听客户端发起的连接
     */
    protected void accept() {
        try {
//            mSSocket = adapter.listenUsingRfcommWithServiceRecord(TAG, SPP_UUID); //加密传输，Android强制执行配对，弹窗显示配对码
            BluetoothServerSocket mSSocket = bluetoothadapter.listenUsingInsecureRfcommWithServiceRecord(TAG, SPP_UUID); //明文传输(不安全)，无需配对
            // 开启子线程
            ClsUtils.EXECUTOR.execute(new Runnable() {
                @Override
                public void run() {
                    try {
                        mSocket = mSSocket.accept(); // 监听连接
                        mSSocket.close(); // 关闭监听，只连接一个设备
                        loopRead(); // 循环读取
                    } catch (Throwable e) {
                        close();
                    }
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
            close();
        }
    }

    /**
     * @desc 进行心跳设备连接
     * @author pjxie
     * @time 2019-05-08 15:53
     * @changed
     */
    protected void connectAceg(int position) {
        checkBlue();
        disConnect();
        bluetoothadapter.getBluetoothLeScanner().stopScan(scanCallback);
        BluetoothDevice currentBluetoothDevice = list.get(position);
        if (currentBluetoothDevice != null && currentBluetoothDevice.getName() != null) {
            try {
                bluetoothGatt = currentBluetoothDevice.connectGatt(mContext, false, mGattCallback);

                if (bluetoothGatt.connect()) {
                    notifyUI(BluetoothListenser.CONNECTED, null);
                }
            } catch (Exception e) {
                notifyUI(BluetoothListenser.ERROR, ERRORCONNECT_CODE);
            }
        } else {
            notifyUI(BluetoothListenser.ERROR, ERRORNOTFOUND_CODE);
        }


    }


    /**
     * @desc 断开蓝牙连接
     * @author pjxie
     * @time 2019-05-15 10:14
     * @changed
     */
    protected void disConnect() {
        checkBlue();
        if (bluetoothGatt != null && bluetoothGatt.connect()) {
            for (BluetoothGattCharacteristic bluetoothGattCharacteristic : cServices) {
                enableNotifications(bluetoothGattCharacteristic, false);
            }
            bluetoothGatt.disconnect();
            bluetoothGatt.close();
            cServices.clear();
            bluetoothGatt = null;
        }
        if (mSocket != null && mSocket.isConnected()) {
            close();
            mSocket = null;
        }
    }

    /**
     * @desc 销毁对象
     * @author pjxie
     * @time 2019-05-08 14:02
     * @changed
     */
    protected void destroy() {
        checkBlue();
        if (blueToothReceiver != null) {
            try {
                mContext.unregisterReceiver(blueToothReceiver);
            } catch (Exception e) {

            }
        }
        disConnect();
        list.clear();
        blueToothReceiver = null;
        bluetoothadapter = null;
        mContext = null;
        setmBluetoothListenser(null);
    }

    /**
     * @desc 注册监听蓝牙的相关广播
     * @author pjxie
     * @time 2019-05-08 15:54
     * @changed
     */
    private void registerReceiver() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);//蓝牙状态改变的广播
        filter.addAction(BluetoothDevice.ACTION_FOUND);//找到设备的广播
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);//搜索完成的广播
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);//开始扫描的广播
        filter.addAction(BluetoothDevice.ACTION_BOND_STATE_CHANGED);//状态改变
        if (blueToothReceiver == null) {
            blueToothReceiver = new BlueToothReceiver();
        }
        mContext.registerReceiver(blueToothReceiver, filter);
    }

    /**
     * @desc BLE蓝牙搜索回调
     * @author pjxie
     * @time 2019-05-15 10:14
     * @changed
     */
    private ScanCallback scanCallback = new ScanCallback() {
        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            super.onScanResult(callbackType, result);
            initdevice(result.getDevice());
            bluetoothadapter.getBluetoothLeScanner().stopScan(scanCallback);
        }

        @Override
        public void onBatchScanResults(List<ScanResult> results) {
            super.onBatchScanResults(results);
        }

        @Override
        public void onScanFailed(int errorCode) {
            super.onScanFailed(errorCode);
        }
    };


    /**
     * @author pjxie
     * @desc 经典蓝牙广播
     * @time 2019-05-15 10:13
     * @changed
     */
    private class BlueToothReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                BluetoothDevice bluetoothDevice = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                initdevice(bluetoothDevice);
            } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED
                    .equals(action)) {
                notifyUI(BluetoothListenser.OVERSEARCH, null);
            }
        }
    }

    /**
     * @desc 填充设备列表
     * @author pjxie
     * @time 2019-05-15 10:13
     * @changed
     */
    private void initdevice(BluetoothDevice bluetoothDevice) {
        if (!list.contains(bluetoothDevice)) {
            list.add(bluetoothDevice);
            notifyUI(BluetoothListenser.UPDATEDEVICE, null);
        }
    }

    /**
     * @desc BLE蓝牙连接监听接口
     * @author pjxie
     * @time 2019-05-15 10:12
     * @changed
     */
    private final BluetoothGattCallback mGattCallback = new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                gatt.discoverServices();
            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {

            }
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {//服务被发现
            if (status == BluetoothGatt.GATT_SUCCESS) {
                List<BluetoothGattService> services = gatt.getServices();
                for (BluetoothGattService bluetoothGattService : services) {
                    for (BluetoothGattCharacteristic bluetoothGattCharacteristic : bluetoothGattService.getCharacteristics()) {
                        String uuid = bluetoothGattCharacteristic.getUuid().toString().split("-")[0].toUpperCase();
                        if ("00002A23".equals(uuid)) {
                            bluetoothGatt.readCharacteristic(bluetoothGattCharacteristic);
                        }
                    }
                }
            } else {
                Log.w(TAG, "onServicesDiscovered received: " + status);
            }
        }

        @Override
        public void onCharacteristicRead(BluetoothGatt gatt,
                                         BluetoothGattCharacteristic characteristic,
                                         int status) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                mac = ClsUtils.bytesToHexString(characteristic.getValue());
                List<BluetoothGattService> services = gatt.getServices();
                for (BluetoothGattService bluetoothGattService : services) {
                    for (BluetoothGattCharacteristic bluetoothGattCharacteristic : bluetoothGattService.getCharacteristics()) {
                        String uuid = bluetoothGattCharacteristic.getUuid().toString().split("-")[0].toUpperCase();
                        if ("00002D37".equals(uuid)) {
                            enableNotifications(bluetoothGattCharacteristic, true);
                            cServices.add(bluetoothGattCharacteristic);
                        }
                    }
                }
            }
        }

        @Override
        public void onCharacteristicWrite(BluetoothGatt gatt,
                                          BluetoothGattCharacteristic characteristic,
                                          int status) {
        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt,
                                            BluetoothGattCharacteristic characteristic) {
            Log.e("CharacteristicChanged中", "数据接收了哦" + ClsUtils.bytesToHexString(characteristic.getValue()));
            notifyUI(BluetoothListenser.RECEIVEDATA, ClsUtils.bytesToHexString(characteristic.getValue()));
        }
    };

    /**
     * Enables notifications on given characteristic
     *
     * @return true is the request has been sent, false if one of the arguments was <code>null</code> or the characteristic does not have the CCCD.
     */
    private  boolean enableNotifications(final BluetoothGattCharacteristic characteristic, boolean enabled) {
        if (bluetoothGatt == null || characteristic == null){
            return false;
        }


        // Check characteristic property
        final int properties = characteristic.getProperties();
        if ((properties & BluetoothGattCharacteristic.PROPERTY_NOTIFY) == 0) {
            return false;
        }
        bluetoothGatt.setCharacteristicNotification(characteristic, enabled);
        final BluetoothGattDescriptor descriptor = characteristic.getDescriptor(CLIENT_CHARACTERISTIC_CONFIG_DESCRIPTOR_UUID);
        if (descriptor != null) {
            if (enabled) {
                descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
            } else {
                descriptor.setValue(BluetoothGattDescriptor.DISABLE_NOTIFICATION_VALUE);
            }
            return bluetoothGatt.writeDescriptor(descriptor);
        }
        return false;
    }

    private void checkBlue() {
        if (bluetoothadapter == null) {
            throw new IllegalArgumentException("请先初始化蓝牙");
        }
    }
}
