package com.plugin.pjxie.bluetoothlib;

import android.app.Activity;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.os.Environment;
import android.widget.Toast;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.UUID;


/**
 * 客户端和服务端的基类，用于管理socket长连接
 */
public class BtBase {
    protected Context mContext;
    protected static final UUID CLIENT_CHARACTERISTIC_CONFIG_DESCRIPTOR_UUID =
            UUID.fromString("00002902-0000-1000-8000-00805f9b34fb");
    static final UUID SPP_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    private static final String FILE_PATH = Environment.getExternalStorageDirectory().getAbsolutePath() + "/bluetooth/";
    private static final int FLAG_MSG = 0;  //消息标记
    private static final int FLAG_FILE = 1; //文件标记
    protected BluetoothSocket mSocket;
    private DataOutputStream mOut;

    private boolean isRead;
    private boolean isSending;

    private BluetoothListenser mBluetoothListenser;

    /**
     * 循环读取对方数据(若没有数据，则阻塞等待)
     */
    void loopRead() {
        try {
            if (!mSocket.isConnected()) {
                mSocket.connect();
            }
            if (mSocket.isConnected()) {
                notifyUI(BluetoothListenser.CONNECTED, null);
            }
            mOut = new DataOutputStream(mSocket.getOutputStream());
            DataInputStream in = new DataInputStream(mSocket.getInputStream());
            isRead = true;
            while (isRead) { //死循环读取
                switch (in.readInt()) {
                    case FLAG_MSG: //读取短消息
                        String msg = in.readUTF();
                        notifyUI(BluetoothListenser.MSG, "接收到消息" + msg);
                        break;
                    case FLAG_FILE: //读取文件
                        ClsUtils.mkdirs(FILE_PATH);
                        String fileName = in.readUTF(); //文件名
                        long fileLen = in.readLong(); //文件长度
                        // 读取文件内容
                        long len = 0;
                        int r;
                        byte[] b = new byte[4 * 1024];
                        FileOutputStream out = new FileOutputStream(FILE_PATH + fileName);
                        while ((r = in.read(b)) != -1) {
                            out.write(b, 0, r);
                            len += r;
                            if (len >= fileLen) {
                                break;
                            }
                        }
                        break;
                    default:
                        break;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            notifyUI(BluetoothListenser.ERROR, SdkMananger.ERRORCONNECT_CODE);
            close();
        }
    }

    /**
     * 发送短消息
     */
    public void sendMsg(String msg) {
        if (checkSend()) {
            return;
        }
        isSending = true;
        try {
            mOut.writeInt(FLAG_MSG); //消息标记
            mOut.writeUTF(msg);
            mOut.flush();
            notifyUI(BluetoothListenser.MSG, "发送短消息：" + msg);
        } catch (Throwable e) {
            close();
        }
        isSending = false;
    }

    /**
     * 发送文件
     */
    public void sendFile(final String filePath) {
        if (checkSend()) {
            return;
        }
        isSending = true;
        ClsUtils.EXECUTOR.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    FileInputStream in = new FileInputStream(filePath);
                    File file = new File(filePath);
                    mOut.writeInt(FLAG_FILE); //文件标记
                    mOut.writeUTF(file.getName()); //文件名
                    mOut.writeLong(file.length()); //文件长度
                    int r;
                    byte[] b = new byte[4 * 1024];
                    notifyUI(BluetoothListenser.MSG, "正在发送文件(" + filePath + "),请稍后...");
                    while ((r = in.read(b)) != -1) {
                        mOut.write(b, 0, r);
                    }
                    mOut.flush();
                    notifyUI(BluetoothListenser.MSG, "文件发送完成.");
                } catch (Throwable e) {
                    close();
                }
                isSending = false;
            }
        });
    }


    /**
     * 关闭Socket连接
     */
    public void close() {
        try {
            isRead = false;
            mSocket.close();
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    // ============================================通知UI===========================================================
    private boolean checkSend() {
        if (isSending) {
            Toast.makeText(mContext, "正在发送其它数据,请稍后再发...", Toast.LENGTH_SHORT).show();
            return true;
        }
        return false;
    }

    protected void notifyUI(final int state, final Object obj) {
        if (mContext != null) {
            ((Activity) mContext).runOnUiThread(() -> {
                try {
                    if (mBluetoothListenser != null) {
                        mBluetoothListenser.notify(state, obj);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
        }
    }

    protected void setmBluetoothListenser(BluetoothListenser mBluetoothListenser) {
        this.mBluetoothListenser = mBluetoothListenser;
    }
}
