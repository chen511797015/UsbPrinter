package cn.pax.usbprinter;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbConstants;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbEndpoint;
import android.hardware.usb.UsbInterface;
import android.hardware.usb.UsbManager;
import android.util.Log;

import java.util.HashMap;
import java.util.Iterator;


/**
 * Created by chendd on 2017/1/16.
 * usb管理
 */

public class UsbAdmin {

    private static final String TAG = "UsbAdmin";


    private Context mContext;
    private final UsbManager mUsbManager;
    private UsbDevice mDevice;
    private UsbDeviceConnection mConnection;
    private UsbEndpoint mUsbEndpoint;
    private PendingIntent mPendingIntent = null;

    private static final String ACTION_USB_PERMISSION =
            "com.android.example.USB_PERMISSION";

    /**
     * 构造实例化有参函数
     *
     * @param mContext
     */
    public UsbAdmin(Context mContext) {
        this.mContext = mContext;
        mUsbManager = (UsbManager) mContext.getSystemService(Context.USB_SERVICE);
        mPendingIntent = PendingIntent.getBroadcast(mContext, 0, new Intent(ACTION_USB_PERMISSION), 0);
        IntentFilter mFilter = new IntentFilter(ACTION_USB_PERMISSION);
        mFilter.addAction(UsbManager.ACTION_USB_DEVICE_ATTACHED);
        mFilter.addAction(UsbManager.ACTION_USB_DEVICE_DETACHED);
        mContext.registerReceiver(mReceiver, mFilter);


    }


    /**
     * Open UsbDevice
     */
    public void openUsb() {
        if (mDevice != null) {
            findPrintDevice(mDevice);
            if (mConnection == null) {
                HashMap<String, UsbDevice> deviceList = mUsbManager.getDeviceList();
                Iterator<UsbDevice> deviceIterator = deviceList.values().iterator();

                while (deviceIterator.hasNext()) {
                    UsbDevice device = deviceIterator.next();
                    mUsbManager.requestPermission(device, mPendingIntent);
                }
            }
        } else {
            HashMap<String, UsbDevice> deviceList = mUsbManager.getDeviceList();
            Iterator<UsbDevice> deviceIterator = deviceList.values().iterator();

            while (deviceIterator.hasNext()) {
                UsbDevice device = deviceIterator.next();
                mUsbManager.requestPermission(device, mPendingIntent);
            }
        }
        Log.d(TAG, "--time--end--" + System.currentTimeMillis());
    }


    /**
     * Close usb connection
     */
    public void closeUsb() {
        if (mConnection != null) {
            mConnection.close();
            mConnection = null;
        }
    }

    public String GetUsbStatus(boolean Language) {
        if (mDevice == null) {
            if (Language)
                return "没有Usb设备！";
            else
                return "No Usb Device!";
        }
        if (mConnection == null) {
            if (Language)
                return "Usb设备不是打印机！";
            else
                return "Usb device is not a printer!";
        }
        if (Language)
            return "Usb打印机打开成功！";
        return "Usb Printer Open success！";
    }

    public boolean GetUsbStatus() {
        if (mConnection == null)
            return false;
        return true;
    }

    /**
     * 找到usb打印机
     *
     * @param device
     */
    private void findPrintDevice(UsbDevice device) {
        if (device != null) {
            UsbInterface intf = null;
            UsbEndpoint ep = null;
            int InterfaceCount = device.getInterfaceCount();
            int j;
            mDevice = device;
            for (j = 0; j < InterfaceCount; j++) {
                int i;

                intf = device.getInterface(j);
                Log.i(TAG, "接口是:" + j + "类是:" + intf.getInterfaceClass());
                if (intf.getInterfaceClass() == 7) {
                    int UsbEndpointCount = intf.getEndpointCount();
                    for (i = 0; i < UsbEndpointCount; i++) {
                        ep = intf.getEndpoint(i);
                        Log.i(TAG, "端点是:" + i + "方向是:" + ep.getDirection() + "类型是:" + ep.getType());
                        if (ep.getDirection() == 0 && ep.getType() == UsbConstants.USB_ENDPOINT_XFER_BULK) {
                            Log.i(TAG, "接口是:" + j + "端点是:" + i);
                            break;
                        }
                    }
                    if (i != UsbEndpointCount) {
                        break;
                    }
                }
            }
            if (j == InterfaceCount) {
                Log.i(TAG, "No printer interface!");
                return;
            }
            mUsbEndpoint = ep;
            if (device != null) {
                UsbDeviceConnection connection = mUsbManager.openDevice(device);
                if (connection != null && connection.claimInterface(intf, true)) {
                    Log.i(TAG, "Open success！");
                    mConnection = connection;

                } else {
                    Log.i(TAG, "Open failed！");
                    mConnection = null;
                }
            }
        }
    }

    public String getResult() {
        StringBuffer sb = new StringBuffer();
        return sb.toString();
    }


    public boolean sendCommand(byte[] mContents) {
        boolean mResult;
        synchronized (this) {
            int length = -1;
            if (mConnection != null) {
                length = mConnection.bulkTransfer(mUsbEndpoint, mContents, mContents.length, 10000);
            }

            if (length < 0) {
                mResult = false;
                Log.i(TAG, "fail in send！" + length);
                openUsb();
            } else {
                mResult = true;
                Log.i(TAG, "send" + length + "byte data");
            }
        }
        return mResult;
    }


    BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            Log.e(TAG, "onReceive: " + action);
            if (ACTION_USB_PERMISSION.equals(action)) {
                synchronized (this) {
                    UsbDevice device = intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
                    //获取用户点击返回权限信息
                    boolean fromUser = intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false);
                    Log.e(TAG, "用户授权是否授权: " + fromUser);
                    if (fromUser) {
                        if (device != null) {//设备不为空,找到usb打印机
                            findPrintDevice(device);
                        } else {
                            //关闭usb连接
                        }
                    }

                }
            }
            //TODO usb设备的拔插
        }
    };


}
