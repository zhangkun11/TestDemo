package com.example.admin.myapplication.buttontest;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.admin.myapplication.GpsActivity;
import com.example.admin.myapplication.MyApplication;
import com.example.admin.myapplication.R;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;

/**
 * Created by admin on 2017-03-15.
 */

public class ButtonTestActivity extends AppCompatActivity {
    @InjectView(R.id.button_show)
    TextView buttonShow;
    @InjectView(R.id.next_test)
    Button nextTest;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_button_test);
        ButterKnife.inject(this);

    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {

        if (keyCode == KeyEvent.KEYCODE_BACK) {
            Toast.makeText(this, "后退键", Toast.LENGTH_SHORT).show();
            finish();
            return true;
        } else if (keyCode == KeyEvent.KEYCODE_VOLUME_UP) {
            Toast.makeText(this, "声音+", Toast.LENGTH_SHORT).show();
            return false;
        } else if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN) {
            Toast.makeText(this, "声音-", Toast.LENGTH_SHORT).show();
            return false;
        } else if (keyCode == KeyEvent.KEYCODE_HOME) {
            Toast.makeText(this, "Home", Toast.LENGTH_SHORT).show();
            return true;
        } else if (keyCode == KeyEvent.KEYCODE_0) {
            Toast.makeText(this, "0键", Toast.LENGTH_SHORT).show();
            return false;
        } else if (keyCode == KeyEvent.KEYCODE_1) {
            Toast.makeText(this, "1键", Toast.LENGTH_SHORT).show();
            return false;
        } else if (keyCode == KeyEvent.KEYCODE_2) {
            Toast.makeText(this, "2键", Toast.LENGTH_SHORT).show();
            return false;
        } else if (keyCode == KeyEvent.KEYCODE_3) {
            Toast.makeText(this, "3键", Toast.LENGTH_SHORT).show();
            return false;
        } else if (keyCode == KeyEvent.KEYCODE_4) {
            Toast.makeText(this, "4键", Toast.LENGTH_SHORT).show();
            return false;
        } else if (keyCode == KeyEvent.KEYCODE_5) {
            Toast.makeText(this, "5键", Toast.LENGTH_SHORT).show();
            return false;
        } else if (keyCode == KeyEvent.KEYCODE_6) {
            Toast.makeText(this, "6键", Toast.LENGTH_SHORT).show();
            return false;
        } else if (keyCode == KeyEvent.KEYCODE_7) {
            Toast.makeText(this, "7键", Toast.LENGTH_SHORT).show();
            return false;
        } else if (keyCode == KeyEvent.KEYCODE_8) {
            Toast.makeText(this, "8键", Toast.LENGTH_SHORT).show();
            return false;
        } else if (keyCode == KeyEvent.KEYCODE_9) {
            Toast.makeText(this, "9键", Toast.LENGTH_SHORT).show();
            return false;
        } else if (keyCode == 139) {
            Toast.makeText(this, "条码快捷键", Toast.LENGTH_SHORT).show();
            return false;
        } else if (keyCode == 131) {
            Toast.makeText(this, "自定义F1键", Toast.LENGTH_SHORT).show();
            return false;
        } else if (keyCode == 132) {
            Toast.makeText(this, "自定义F2键", Toast.LENGTH_SHORT).show();
            return false;
        } else if (keyCode == 17) {
            Toast.makeText(this, "*键", Toast.LENGTH_SHORT).show();
            return false;
        } else if (keyCode == 18) {
            Toast.makeText(this, "#键", Toast.LENGTH_SHORT).show();
            return false;
        }
        Log.i("info", "onKeyDown: ----------" + keyCode + "\n");
        return super.onKeyDown(keyCode, event);
    }

    @OnClick(R.id.next_test)
    public void onClick() {
        Intent intent=new Intent(ButtonTestActivity.this, GpsActivity.class);
        startActivity(intent);
        MyApplication.getSession().set("button",true);
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        MyApplication.getSession().set("button",true);
    }
}
