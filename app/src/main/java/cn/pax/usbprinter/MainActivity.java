package cn.pax.usbprinter;


import android.app.Activity;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbEndpoint;
import android.hardware.usb.UsbInterface;
import android.hardware.usb.UsbManager;
import android.hardware.usb.UsbRequest;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Iterator;


import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * 打印机接收测试
 */
public class MainActivity extends Activity {
    private static final String TAG = "MainActivity";
    @BindView(R.id.m_btn_write)
    Button mBtnWrite;
    @BindView(R.id.m_btn_read)
    Button mBtnRead;

    //设备列表
    private HashMap<String, UsbDevice> deviceList;
    //从设备读数据
    private Button read_btn;
    //给设备写数据（发指令）
    private Button write_btn;
    //USB管理器:负责管理USB设备的类
    private UsbManager manager;
    //找到的USB设备
    private UsbDevice mUsbDevice;
    //代表USB设备的一个接口
    private UsbInterface mInterface;
    private UsbDeviceConnection mDeviceConnection;
    //代表一个接口的某个节点的类:写数据节点
    private UsbEndpoint usbEpOut;
    //代表一个接口的某个节点的类:读数据节点
    private UsbEndpoint usbEpIn;
    //要发送信息字节
    private byte[] sendbytes;
    //接收到的信息字节
    private byte[] receiveytes;

    private static final String ACTION_USB_PERMISSION =
            "com.android.example.USB_PERMISSION";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        initReceiver();

        initUsbData();

    }

    private void initReceiver() {
        this.registerReceiver(mReceiver, new IntentFilter(ACTION_USB_PERMISSION));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        this.unregisterReceiver(mReceiver);
    }

    private void initUsbData() {

        // 获取USB设备
        manager = (UsbManager) getSystemService(Context.USB_SERVICE);

        //获取到设备列表
        deviceList = manager.getDeviceList();
        Iterator<UsbDevice> deviceIterator = deviceList.values().iterator();
        while (deviceIterator.hasNext()) {
            UsbDevice device = deviceIterator.next();
            if (device.getVendorId() == 10473 && device.getProductId() == 649) {
                manager.requestPermission(device, PendingIntent.getBroadcast(MainActivity.this, 0, new Intent(ACTION_USB_PERMISSION), 0));
            }
        }
    }

    @OnClick({R.id.m_btn_write, R.id.m_btn_read})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.m_btn_write:

                new Thread() {
                    @Override
                    public void run() {
                        sendCommand("按照规则给设备发指令！");
                    }
                }.start();
                break;
            case R.id.m_btn_read:
                readFromUsb();
                break;
        }
    }

    private void sendCommand(String content) {
        try {
            sendbytes = content.getBytes("GBK");
            byte[] PRINT_CONCENTRATION_LEVEL = {0x1b, '#', 0x4b};
            int ret = -1;
            // 发送准备命令
            ret = mDeviceConnection.bulkTransfer(usbEpOut, PRINT_CONCENTRATION_LEVEL, PRINT_CONCENTRATION_LEVEL.length, 10000);
            Log.e(TAG, "指令已经发送！ " + ret);
            //接收发送成功信息(相当于读取设备数据)
            receiveytes = new byte[128];   //根据设备实际情况写数据大小
            ret = mDeviceConnection.bulkTransfer(usbEpIn, receiveytes, receiveytes.length, 10000);
            Log.e(TAG, String.valueOf(ret) + ": " + ret);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            Log.e(TAG, "发送失败: " + e.getMessage());
        }

    }

    private void readFromUsb() {
        //读取数据2
        int outMax = usbEpOut.getMaxPacketSize();
        int inMax = usbEpIn.getMaxPacketSize();
        ByteBuffer byteBuffer = ByteBuffer.allocate(inMax);
        UsbRequest usbRequest = new UsbRequest();
        usbRequest.initialize(mDeviceConnection, usbEpIn);
        usbRequest.queue(byteBuffer, inMax);
        if (mDeviceConnection.requestWait() == usbRequest) {
            byte[] retData = byteBuffer.array();
            try {
                StringBuilder sb = new StringBuilder();
                for (int i = 0; i < retData.length; i++) {
                    sb.append("0x" + retData[i] + ",");
                }
                Log.e(TAG, "收到数据长度: " + retData.length);
                Log.e(TAG, "收到数据: " + sb.toString());


            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    //文字提示方法
    private void showTmsg(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }


    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
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

        }
    };

    private void findPrintDevice(UsbDevice device) {
        try {
            if (device != null) {
                mUsbDevice = device;
                //获取设备接口
                for (int i = 0; i < mUsbDevice.getInterfaceCount(); ) {
                    // 一般来说一个设备都是一个接口，你可以通过getInterfaceCount()查看接口的个数
                    // 这个接口上有两个端点，分别对应OUT 和 IN
                    UsbInterface usbInterface = mUsbDevice.getInterface(i);
                    mInterface = usbInterface;
                    break;
                }
                //用UsbDeviceConnection 与 UsbInterface 进行端点设置和通讯
                if (mInterface.getEndpoint(1) != null) {
                    usbEpOut = mInterface.getEndpoint(1);
                }
                if (mInterface.getEndpoint(0) != null) {
                    usbEpIn = mInterface.getEndpoint(0);
                }
                if (mInterface != null) {
                    // 判断是否有权限
                    if (manager.hasPermission(mUsbDevice)) {
                        // 打开设备，获取 UsbDeviceConnection 对象，连接设备，用于后面的通讯
                        mDeviceConnection = manager.openDevice(device);
                        if (mDeviceConnection == null) {
                            return;
                        }
                        if (mDeviceConnection.claimInterface(mInterface, true)) {
                            Log.e(TAG, "找到设备接口: ");
                        } else {
                            mDeviceConnection.close();
                        }
                    } else {
                        Log.e(TAG, "没有权限: ");
                    }
                } else {
                    Log.e(TAG, "没有找到设备接口: ");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
