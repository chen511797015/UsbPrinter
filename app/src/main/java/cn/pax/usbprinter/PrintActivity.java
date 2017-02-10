package cn.pax.usbprinter;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;

import com.pax.api.CustomizedPrintCmd;

import java.util.Timer;
import java.util.TimerTask;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class PrintActivity extends AppCompatActivity {

    private static final String TAG = "PrintActivity";

    @BindView(R.id.m_btn_print_self_information)
    Button mBtnPrintSelfInformation;
    @BindView(R.id.m_btn_open_cash_box)
    Button mBtnOpenCashBox;
    @BindView(R.id.activity_print)
    RelativeLayout activityPrint;
    @BindView(R.id.m_btn_print_factory_information)
    Button mBtnPrintFactoryInformation;
    @BindView(R.id.m_btn_print_concentration_level)
    Button mBtnPrintConcentrationLevel;


    byte[] OpenCashBox = {0x1b, 0x70, 0x00, 0x1e, (byte) 0xff, 0x00};

    int count = 0;

    Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (msg.what == 1) {
                count++;
                if (count <= 10000) {
                    mBtnOpenCashBox.setText("弹钱箱:" + count);
                    new PrintThread(OpenCashBox).start();
                }
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_print);
        ButterKnife.bind(this);
        PrintUtil.getInstance(this);

    }

    @OnClick({R.id.m_btn_print_self_information, R.id.m_btn_open_cash_box, R.id.activity_print, R.id.m_btn_print_factory_information, R.id.m_btn_print_concentration_level})
    public void onClick(View view) {
        //PrintUtil.getInstance(PrintActivity.this).openUsb();
        switch (view.getId()) {
            case R.id.m_btn_print_self_information://打印自检信息
                new PrintThread(CustomizedPrintCmd.PRINT_SELF_CHECKING_INFORMATION).start();
                break;
            case R.id.m_btn_open_cash_box://开钱箱
                //new PrintThread(OpenCashBox).start();
                openCashBox();
                mBtnOpenCashBox.setClickable(false);
                break;
            case R.id.m_btn_print_factory_information://打印工厂信息
                new PrintThread(CustomizedPrintCmd.PRINT_FACTORY_INFORMATION).start();
                break;
            case R.id.m_btn_print_concentration_level://打印浓度等级
                new PrintThread(CustomizedPrintCmd.PRINT_CONCENTRATION_LEVEL).start();
                //PrintUtil.sendData(PrintActivity.this, new byte[]{0x1b, 0x23, 0x4b});
                break;
        }
    }

    /**
     * 每5s打印一次,打印10000次
     */
    private void openCashBox() {
        Timer mTimer = new Timer();
        mTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                Message msg = new Message();
                msg.what = 1;
                mHandler.sendMessage(msg);
            }
        }, 1000, 5000);
    }

    class PrintThread extends Thread {
        String msg = null;
        byte[] mCommand = null;

        public PrintThread(String msg) {
            this.msg = msg;
        }

        public PrintThread(byte[] mCommand) {
            this.mCommand = mCommand;
        }

        @Override
        public void run() {
            if (msg != null) {
                PrintUtil.sendData(PrintActivity.this, msg);
            }
            if (mCommand != null) {
                PrintUtil.sendData(PrintActivity.this, mCommand);
            }
        }
    }
}
