package com.pax.printertest;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

import com.pax.api.EcrPosPrintManage;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class MainActivity extends AppCompatActivity {

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


    }

    public static final byte[] FullCut = {0x1b, 0x23, 0x23, 0x43, 0x54, 0x47, 0x48, 0x30};//全切

    byte[] SendCut = {0x0a, 0x0a, 0x1d, 0x56, 0x30};//全切有效

    boolean isBlack = true;

    @OnClick({R.id.m_btn_print_str, R.id.m_btn_print_gray, R.id.m_btn_print_font, R.id.m_btn_print_cut, R.id.m_btn_print_black_white
            , R.id.m_btn_print_black_bit})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.m_btn_print_str:
                new Runnable() {
                    @Override
                    public void run() {
                        mEcrPosPrintManage.prnStr("123456789");
                        //mEcrPosPrintManage.prnBytes(new byte[]{0x1b, 0x23, 0x23, 0x46, 0x4c, 0x4c, 0x46, 0x01});
                        mEcrPosPrintManage.prnBytes(new byte[]{10});
                        mEcrPosPrintManage.prnStr("987654321");
                        //mEcrPosPrintManage.prnCutAll();
                        mEcrPosPrintManage.prnBytes(SendCut);
                        //mEcrPosPrintManage.prnBytes(FullCut);
                    }
                }.run();
                break;
            case R.id.m_btn_print_gray://设置灰度
                break;
            case R.id.m_btn_print_font://设置字体
                break;
            case R.id.m_btn_print_cut://全切
                mEcrPosPrintManage.prnCutAll();
                break;
            case R.id.m_btn_print_black_white://黑白反显打印
                if (isBlack) {
                    mEcrPosPrintManage.prnBytes(new byte[]{0x1d, 0x42, 0x01});//选择反显打印
                    isBlack = false;
                } else {
                    mEcrPosPrintManage.prnBytes(new byte[]{0x1d, 0x42, 0x00});//取消反显打印
                    isBlack = true;
                }
                break;

            case R.id.m_btn_print_black_bit:

                new Runnable() {
                    @Override
                    public void run() {
                        ByteArrayOutputStream mBaos = new ByteArrayOutputStream();
                        for (int i = 0; i < 20; i++) {
                            mEcrPosPrintManage.prnBytes(ImgUtil.decodeBitmap(600, 200));
                        }
                        //mEcrPosPrintManage.prnBytes(SendCut);
                    }
                }.run();

                break;
        }
    }

}
