package com.pax.printertest;

import android.util.Log;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by chendd on 2017/2/10.
 */

public class ImgUtil {


    public static byte[] decodeBitmap(int mWidth, int mHeight) {

        List<String> mList = new ArrayList<>();//二进制集合
        StringBuffer mSb;

        //每行字节数(除以8，不足补0)
        int mBitLen = mWidth / 8;
        int mZeroCount = mWidth % 8;

        //每行需要补充的0
        String mZeroStr = "";
        if (mZeroCount > 0) {
            mBitLen = mWidth / 8 + 1;
            for (int i = 0; i < (8 - mZeroCount); i++) {
                mZeroStr = mZeroStr + "0";
            }
        }
        // 逐个读取像素颜色，将非白色改为黑色
        for (int i = 0; i < mHeight; i++) {
            mSb = new StringBuffer();
            for (int j = 0; j < mWidth; j++) {
                mSb.append("1");
            }
            //每一行结束的时候,补充剩余的0
            if (mZeroCount > 0) {
                mSb.append(mZeroStr);
            }
            mList.add(mSb.toString());
        }

        // binaryStr每8位调用一次转换方法，再拼合
        List<String> mHexList = ConvertUtil.binaryListToHexStringList(mList);
        String mCommandHexString = "1D763000";
        // 宽度指令
        String mWidthHexString = Integer
                .toHexString(mWidth % 8 == 0 ? mWidth / 8
                        : (mWidth / 8 + 1));
//        if (mWidthHexString.length() > 2) {
//            Log.e("decodeBitmap error", "宽度超出 width is too large");
//            return null;
//        } else if (mWidthHexString.length() == 1) {
//            mWidthHexString = "0" + mWidthHexString;
//        }
        mWidthHexString = mWidthHexString + "00";

        // 高度指令
        String mHeightHexString = Integer.toHexString(mHeight);
//        if (mHeightHexString.length() > 5) {
//            Log.e("decodeBitmap error", "高度超出 height is too large" + mHeightHexString.length());
//            return null;
//        } else if (mHeightHexString.length() == 1) {
//            mHeightHexString = "0" + mHeightHexString;
//        }
        mHeightHexString = mHeightHexString + "00";
        List<String> mCommandList = new ArrayList<>();
        mCommandList.add(mCommandHexString + mWidthHexString + mHeightHexString);
        mCommandList.addAll(mHexList);
        return ConvertUtil.hexList2Byte(mCommandList);
    }

}
