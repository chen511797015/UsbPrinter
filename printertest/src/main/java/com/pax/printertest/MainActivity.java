package com.pax.printertest;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.pax.api.EcrPosPrintManage;

import java.io.ByteArrayOutputStream;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    @BindView(R.id.m_btn_print_str)
    Button mBtnPrintStr;
    @BindView(R.id.m_btn_print_gray)
    Button mBtnPrintGray;
    @BindView(R.id.m_btn_print_font)
    Button mBtnPrintFont;
    @BindView(R.id.m_btn_print_cut)
    Button mBtnPrintCut;
    @BindView(R.id.m_btn_print_black_white)
    Button mBtnPrintBlackWhite;
    @BindView(R.id.m_btn_print_black_bit)
    Button mBtnPrintBlackBit;
    private EcrPosPrintManage mEcrPosPrintManage;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //注册
        mEcrPosPrintManage = EcrPosPrintManage.getInstance(this);
        ButterKnife.bind(this);


        //获取屏幕信息
        DisplayMetrics dm = new DisplayMetrics();
        getWindow().getWindowManager().getDefaultDisplay().getMetrics(dm);
        Log.e(TAG, "densityDpi: " + dm.densityDpi);

    }

    public static final byte[] FullCut = {0x1b, 0x23, 0x23, 0x43, 0x54, 0x47, 0x48, 0x30};//开启全切()
    byte[] SendCut = {0x0a, 0x0a, 0x1d, 0x56, 0x30};//全切有效


    @OnClick({R.id.m_btn_print_str, R.id.m_btn_print_gray, R.id.m_btn_print_font, R.id.m_btn_print_cut, R.id.m_btn_print_black_white
            , R.id.m_btn_print_black_bit})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.m_btn_print_str://打印字符串
                printStringMsg();
                break;
            case R.id.m_btn_print_gray://设置灰度
                selectPrintGray();
                break;
            case R.id.m_btn_print_font://设置字体
                //弹出字体选项菜单
                selectFontDialog();
                break;
            case R.id.m_btn_print_cut://全切
                mEcrPosPrintManage.prnCutAll();
                break;
            case R.id.m_btn_print_black_white://黑白反显打印
                selectBlackOrWhite();
                break;

            case R.id.m_btn_print_black_bit://打印黑色块
                printBlackBit();
                break;
        }
    }

    /**
     * 打印字符串
     */
    private void printStringMsg() {
        new Runnable() {
            @Override
            public void run() {
                mEcrPosPrintManage.prnStr("123456789");
                mEcrPosPrintManage.prnBytes(new byte[]{10});
                mEcrPosPrintManage.prnStr("打印测试页...");
                mEcrPosPrintManage.prnBytes(SendCut);
            }
        }.run();
    }

    /**
     * 打印黑色块
     */
    private void printBlackBit() {
        new Runnable() {
            @Override
            public void run() {
                ByteArrayOutputStream mBaos = new ByteArrayOutputStream();
                for (int i = 0; i < 1; i++) {
                    mEcrPosPrintManage.prnBytes(ImgUtil.decodeBitmap(600, 200));
                }
            }
        }.run();
    }

    /**
     * 黑白反显
     */
    private void selectBlackOrWhite() {
        new AlertDialog.Builder(this)
                .setTitle("请选择")
                .setSingleChoiceItems(new String[]{"黑底白字", "白底黑字"}, 0, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (which == 0) {
                            mEcrPosPrintManage.prnBytes(new byte[]{0x1d, 0x42, 0x01});//选择反显打印
                        } else {
                            mEcrPosPrintManage.prnBytes(new byte[]{0x1d, 0x42, 0x00});//取消反显打印
                        }
                        dialog.dismiss();
                    }
                })
                .show();
    }

    /**
     * 设置灰度
     */
    private void selectPrintGray() {
        String[] mGray = new String[40];
        for (int i = 0; i < 40; i++) {
            mGray[i] = i + "";
        }
        new AlertDialog.Builder(this)
                .setTitle("请选择")
                .setSingleChoiceItems(mGray, 0, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mEcrPosPrintManage.prnSetGray(which);//选择打印浓度
                        dialog.dismiss();
                    }
                })
                //.setPositiveButton("确定", null)
                //.setNegativeButton("取消", null)
                .show();
    }

    /**
     * 设置字体大小
     */
    private void selectFontDialog() {
        new AlertDialog.Builder(this)
                .setTitle("请选择")
                .setSingleChoiceItems(new String[]{"大字体", "小字体"}, 0, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (which == 0) {
                            mEcrPosPrintManage.prnFontSet(EcrPosPrintManage.FONT_SIZE_BIG);//大字体
                        } else {
                            mEcrPosPrintManage.prnFontSet(EcrPosPrintManage.FONT_SIZE_SMALL);//小字体
                        }
                        dialog.dismiss();
                    }
                })
                .show();
    }

}
