package cn.pax.usbprinter;

import android.content.Context;

import java.io.UnsupportedEncodingException;

/**
 * Created by chendd on 2017/1/16.
 * print util
 */

public class PrintUtil {
    private static final String TAG = "PrintUtil";

    static UsbAdmin mUsbAdmin = null;

    public static UsbAdmin getInstance(Context mContext) {
        if (mUsbAdmin == null) {
            mUsbAdmin = new UsbAdmin(mContext);
            mUsbAdmin.openUsb();
        }
        return mUsbAdmin;
    }


    /**
     * send String data
     *
     * @param mContext: Context object
     * @param mText:    Data to be sent
     */
    public static void sendData(Context mContext, String mText) {
        if (mUsbAdmin == null)
            getInstance(mContext);
        byte SendCut[] = {0x0a, 0x0a, 0x1d, 0x56, 0x01};
        try {
            mUsbAdmin.sendCommand(mText.getBytes("GBK"));
            //mUsbAdmin.sendCommand(SendCut);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

    /**
     * send byte array data
     *
     * @param mContext: Context object
     * @param mBytes:   Data to be sent
     */
    public static void sendData(Context mContext, byte[] mBytes) {
        if (mUsbAdmin == null)
            getInstance(mContext);
        mUsbAdmin.sendCommand(mBytes);
        byte SendCut[] = {0x0a, 0x0a, 0x1d, 0x56, 0x01};
        //mUsbAdmin.sendCommand(SendCut);
    }
}
