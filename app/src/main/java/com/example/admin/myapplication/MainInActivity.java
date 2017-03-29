package com.example.admin.myapplication;

import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.ToneGenerator;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.example.admin.myapplication.buttontest.ButtonTestActivity;
import com.example.admin.myapplication.meter.MeterActivity;
import com.example.admin.myapplication.meter.ReadAddress;
import com.example.admin.myapplication.nfc.NFCActivity;
import com.example.admin.myapplication.rs232.RS232Activity;
import com.example.admin.myapplication.scan.ScanActivity;
import com.example.admin.myapplication.simpleIc.SimpleIcActivity;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;

/**
 * Created by admin on 2017-02-22.
 */

public class MainInActivity extends AppCompatActivity {
    @InjectView(R.id.screen_test)
    Button screenTest;
    @InjectView(R.id.gps_test)
    Button gpsTest;
    @InjectView(R.id.photo_test)
    Button photoTest;
    @InjectView(R.id.meter_test)
    Button meterTest;
    @InjectView(R.id.flash_test)
    Button flashTest;
    @InjectView(R.id.simpleIc_test)
    Button simpleIcTest;
    @InjectView(R.id.scan_test)
    Button scanTest;
    @InjectView(R.id.button_test)
    Button buttonTest;
    @InjectView(R.id.rs_test)
    Button rsTest;
    @InjectView(R.id.nfc_test)
    Button nfcTest;
    @InjectView(R.id.rs4_test)
    Button rs4Test;
    @InjectView(R.id.readaddress_test)
    Button readaddressTest;

    private boolean mDTMFToneEnabled;// 按键操作音
    private Object mToneGeneratorLock = new Object();// 监视器对象锁
    private ToneGenerator mToneGenerator;
    private static final int TONE_RELATIVE_VOLUME = 80;
    private static final int TONE_LENGTH_MS = 150;// 延迟时间

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_in);
        ButterKnife.inject(this);
        Log.i("info", "onCreate: ");
        //setButton();
        mDTMFToneEnabled = Settings.System.getInt(this.getContentResolver(),
                Settings.System.DTMF_TONE_WHEN_DIALING, 1) == 1;
        synchronized (mToneGeneratorLock) {
            if (mToneGenerator == null) {
                try {
                    mToneGenerator = new ToneGenerator(
                            AudioManager.STREAM_DTMF, TONE_RELATIVE_VOLUME);
                } catch (RuntimeException e) {
                    Log.d("jiebao",
                            "Exception caught while creating local tone generator: "
                                    + e);
                    mToneGenerator = null;
                }

            }
        }

    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.i("info", "onStart: ");
        //setButton();
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.i("info", "onResume: ");
        //setButton();
        new Thread(runnable).start();

    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.i("info", "onStop: ");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.i("info", "onDestroy: ");
    }

    private void goToActivity(Class activity) {
        Intent intent = new Intent(this, activity);
        startActivity(intent);
    }

    @OnClick({R.id.screen_test, R.id.gps_test, R.id.photo_test, R.id.flash_test, R.id.simpleIc_test, R.id.scan_test, R.id.button_test,
            R.id.rs_test, R.id.nfc_test, R.id.rs4_test,R.id.readaddress_test})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.screen_test:
                goToActivity(MainActivity.class);
                break;
            case R.id.gps_test:
                goToActivity(GpsActivity.class);
                break;
            case R.id.photo_test:
                goToActivity(PhotoActivity.class);
                break;
            case R.id.flash_test:
                goToActivity(ElectrictorchActivity.class);
                break;
            case R.id.simpleIc_test:

                goToActivity(SimpleIcActivity.class);
                break;
            case R.id.scan_test:
                goToActivity(ScanActivity.class);
                break;
            case R.id.button_test:
                goToActivity(ButtonTestActivity.class);
                break;
            case R.id.rs_test:
                goToActivity(RS232Activity.class);
                break;
            case R.id.rs4_test:
                MyApplication.getSession().set("rs_4", true);
                Intent intentd = new Intent(this, MeterActivity.class);
                intentd.putExtra("From", "caobiao");
                intentd.putExtra("IS485", true);
                startActivity(intentd);
                break;
            case R.id.nfc_test:
                playTone(ToneGenerator.TONE_DTMF_1);
                goToActivity(NFCActivity.class);
                break;
            case R.id.readaddress_test:
                goToActivity(ReadAddress.class);
                break;
        }
    }

    Runnable runnable = new Runnable() {
        @Override
        public void run() {
            while (true) {
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                handler.sendEmptyMessage(0);
            }

        }
    };
    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            setButton();
        }
    };

    private void playTone(int tone) {
        if (!mDTMFToneEnabled) {
            return;
        }
        AudioManager audioManager = (AudioManager) MainInActivity.this
                .getSystemService(Context.AUDIO_SERVICE);
        int ringerMode = audioManager.getRingerMode();
        if ((ringerMode == AudioManager.RINGER_MODE_SILENT)
                || (ringerMode == AudioManager.RINGER_MODE_VIBRATE)) {
            return;
        }

        synchronized (mToneGeneratorLock) {
            if (mToneGenerator == null) {
                Log.d("jiebao", "playTone: mToneGenerator == null, tone: " + tone);
                return;
            }
            mToneGenerator.startTone(tone, TONE_LENGTH_MS);
        }
    }

    @OnClick(R.id.meter_test)
    public void onClick() {
        MyApplication.getSession().set("rs_4", false);
        Intent intentd = new Intent(MainInActivity.this, MeterActivity.class);
        intentd.putExtra("From", "caobiao");
        intentd.putExtra("IS485", false);
        startActivity(intentd);
    }

    private void setButton() {
        boolean checkMain = MyApplication.getSession().getBoolean("main");
        boolean checkBut = MyApplication.getSession().getBoolean("button");
        boolean checkGps = MyApplication.getSession().getBoolean("gps");
        boolean checkPhoto = MyApplication.getSession().getBoolean("photo");
        boolean checkElec = MyApplication.getSession().getBoolean("elec");
        boolean checkMeter = MyApplication.getSession().getBoolean("meter");
        boolean checkEsam = MyApplication.getSession().getBoolean("esam");
        boolean checkScan = MyApplication.getSession().getBoolean("scan");
        boolean checkNfc = MyApplication.getSession().getBoolean("nfc");
        boolean checkPs = MyApplication.getSession().getBoolean("ps");
        boolean checkRs = MyApplication.getSession().getBoolean("rs");

        //boolean check=MyApplication.getSession().getBoolean("main");
        //Log.i("session", "setButton: =======    "+MyApplication.getSession().getObj("main"));
        if (checkMain == true && MyApplication.getSession().getObj("main") != null) {
            screenTest.setTextColor(getResources().getColor(R.color.colorGreen));
            screenTest.setText("1 屏幕测试（成功）");
        }
        if (checkMain == false && MyApplication.getSession().getObj("main") != null) {
            screenTest.setTextColor(getResources().getColor(R.color.colorRed));
            screenTest.setText("1 屏幕测试（未成功）");
        }
        if (checkGps == true && MyApplication.getSession().getObj("gps") != null) {
            gpsTest.setTextColor(getResources().getColor(R.color.colorGreen));
            gpsTest.setText("5 GPS测试（成功）");
        }
        if (checkGps == false && MyApplication.getSession().getObj("gps") != null) {
            gpsTest.setTextColor(getResources().getColor(R.color.colorRed));
            gpsTest.setText("5 GPS测试（未成功）");
        }
        if (checkPhoto == true && MyApplication.getSession().getObj("photo") != null) {
            photoTest.setTextColor(getResources().getColor(R.color.colorGreen));
            photoTest.setText("3 拍照测试（成功）");
        }
        if (checkPhoto == false && MyApplication.getSession().getObj("photo") != null) {
            photoTest.setTextColor(getResources().getColor(R.color.colorRed));
            photoTest.setText("3 拍照测试（未成功）");
        }
        if (checkElec == true && MyApplication.getSession().getObj("elec") != null) {
            flashTest.setTextColor(getResources().getColor(R.color.colorGreen));
            flashTest.setText("4 手电筒测试（成功）");
        }
        if (checkElec == false && MyApplication.getSession().getObj("elec") != null) {
            flashTest.setTextColor(getResources().getColor(R.color.colorRed));
            flashTest.setText("4 手电筒测试（未成功）");
        }
        if (checkEsam == true && MyApplication.getSession().getObj("esam") != null) {
            simpleIcTest.setTextColor(getResources().getColor(R.color.colorGreen));
            simpleIcTest.setText("7 ESAM测试（成功）");
        }
        if (checkEsam == false && MyApplication.getSession().getObj("esam") != null) {
            simpleIcTest.setTextColor(getResources().getColor(R.color.colorRed));
            simpleIcTest.setText("7 ESAM测试（未成功）");
        }
        if (checkMeter == true && MyApplication.getSession().getObj("meter") != null) {
            meterTest.setTextColor(getResources().getColor(R.color.colorGreen));
            meterTest.setText("6 红外测试（成功）");
        }
        if (checkMeter == false && MyApplication.getSession().getObj("meter") != null) {
            meterTest.setTextColor(getResources().getColor(R.color.colorRed));
            meterTest.setText("6 红外测试（未成功）");
        }
        if (checkScan == true && MyApplication.getSession().getObj("scan") != null) {
            scanTest.setTextColor(getResources().getColor(R.color.colorGreen));
            scanTest.setText("8 条码扫描测试（成功）");
        }
        if (checkScan == false && MyApplication.getSession().getObj("scan") != null) {
            scanTest.setTextColor(getResources().getColor(R.color.colorRed));
            scanTest.setText("8 条码扫描测试（未成功）");
        }
        if (checkBut == true && MyApplication.getSession().getObj("button") != null) {
            buttonTest.setTextColor(getResources().getColor(R.color.colorGreen));
            buttonTest.setText("2 按键测试（成功）");
        }
        if (checkBut == false && MyApplication.getSession().getObj("button") != null) {
            buttonTest.setTextColor(getResources().getColor(R.color.colorRed));
            buttonTest.setText("2 按键测试（未成功）");
        }
        if (checkNfc == true && MyApplication.getSession().getObj("nfc") != null) {
            nfcTest.setTextColor(getResources().getColor(R.color.colorGreen));
            nfcTest.setText("11 NFC测试（成功）");
        }
        if (checkNfc == false && MyApplication.getSession().getObj("nfc") != null) {
            nfcTest.setTextColor(getResources().getColor(R.color.colorRed));
            nfcTest.setText("11 NFC测试（未成功）");
        }
        if (checkPs == true && MyApplication.getSession().getObj("ps") != null) {
            rsTest.setTextColor(getResources().getColor(R.color.colorGreen));
            rsTest.setText("9 串口/PS2测试（成功）");
        }
        if (checkPs == false && MyApplication.getSession().getObj("ps") != null) {
            rsTest.setTextColor(getResources().getColor(R.color.colorRed));
            rsTest.setText("9 串口/PS2测试（未成功）");
        }
        if (checkRs == true && MyApplication.getSession().getObj("rs") != null) {
            rs4Test.setTextColor(getResources().getColor(R.color.colorGreen));
            rs4Test.setText("10 RS485测试（成功）");
        }
        if (checkRs == false && MyApplication.getSession().getObj("rs") != null) {
            rs4Test.setTextColor(getResources().getColor(R.color.colorRed));
            rs4Test.setText("10 RS485测试（未成功）");
        }

    }


}
