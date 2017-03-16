package com.example.admin.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

import com.example.admin.myapplication.buttontest.ButtonTestActivity;
import com.example.admin.myapplication.meter.MeterActivity;
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

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_in);
        ButterKnife.inject(this);

    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onResume() {
        super.onResume();

        new Thread(runnable).start();

    }

    private void goToActivity(Class activity) {
        Intent intent = new Intent(this, activity);
        startActivity(intent);
    }

    @OnClick({R.id.screen_test, R.id.gps_test, R.id.photo_test, R.id.flash_test, R.id.simpleIc_test, R.id.scan_test,R.id.button_test})
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
        }
    }

    Runnable runnable = new Runnable() {
        @Override
        public void run() {
            while (true) {
                try {
                    Thread.sleep(1000);
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

    @OnClick(R.id.meter_test)
    public void onClick() {
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
        //boolean check=MyApplication.getSession().getBoolean("main");
        if (checkMain == true) {
            screenTest.setTextColor(getResources().getColor(R.color.colorGreen));
            screenTest.setText("1 屏幕测试（已测试）");
        }
        if (checkGps == true) {
            gpsTest.setTextColor(getResources().getColor(R.color.colorGreen));
            gpsTest.setText("3 GPS测试（已测试）");
        }
        if (checkPhoto == true) {
            photoTest.setTextColor(getResources().getColor(R.color.colorGreen));
            photoTest.setText("4 拍照测试（已测试）");
        }
        if (checkElec == true) {
            flashTest.setTextColor(getResources().getColor(R.color.colorGreen));
            flashTest.setText("5 手电筒测试（已测试）");
        }
        if (checkEsam == true) {
            simpleIcTest.setTextColor(getResources().getColor(R.color.colorGreen));
            simpleIcTest.setText("7 ESAM测试（已测试）");
        }
        if (checkMeter == true) {
            meterTest.setTextColor(getResources().getColor(R.color.colorGreen));
            meterTest.setText("6 红外测试（已测试）");
        }
        if (checkScan == true) {
            scanTest.setTextColor(getResources().getColor(R.color.colorGreen));
            scanTest.setText("8 条码扫描测试（已测试）");
        }
        if (checkBut == true) {
            buttonTest.setTextColor(getResources().getColor(R.color.colorGreen));
            buttonTest.setText("2 按键测试（已测试）");
        }

    }


}
