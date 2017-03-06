package com.example.admin.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

import com.example.admin.myapplication.meter.MeterActivity;

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

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_in);
        ButterKnife.inject(this);
    }

    private void goToActivity(Class activity) {
        Intent intent = new Intent(this, activity);
        startActivity(intent);
    }

    @OnClick({R.id.screen_test, R.id.gps_test, R.id.photo_test})
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
        }
    }

    @OnClick(R.id.meter_test)
    public void onClick() {
        Intent intentd = new Intent(MainInActivity.this, MeterActivity.class);
        intentd.putExtra("From", "caobiao");
        intentd.putExtra("IS485", false);
        startActivity(intentd);
    }
}
