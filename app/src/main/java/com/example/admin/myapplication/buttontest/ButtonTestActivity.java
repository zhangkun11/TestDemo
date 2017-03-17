package com.example.admin.myapplication.buttontest;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.admin.myapplication.MyApplication;
import com.example.admin.myapplication.PhotoActivity;
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
    StringBuilder stringBuilder;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_button_test);
        ButterKnife.inject(this);
        Toast.makeText(ButtonTestActivity.this,"按键测试",Toast.LENGTH_SHORT).show();
        stringBuilder=new StringBuilder();
        stringBuilder.append("点击实体按键，核对按键与显示是否一致").append("\n");

    }
    private void setText(){
        buttonShow.setText(stringBuilder);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {

       if (keyCode == KeyEvent.KEYCODE_VOLUME_UP) {
            //Toast.makeText(this, "声音+", Toast.LENGTH_SHORT).show();
            stringBuilder.append("声音+").append("   ");
            setText();
            return false;
        } else if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN) {
            //Toast.makeText(this, "声音-", Toast.LENGTH_SHORT).show();
           stringBuilder.append("声音-").append("   ");
           setText();
            return false;
        } else if (keyCode == KeyEvent.KEYCODE_0) {
            //Toast.makeText(this, "0键", Toast.LENGTH_SHORT).show();
            stringBuilder.append("0键").append("   ");
            setText();
            return false;
        } else if (keyCode == KeyEvent.KEYCODE_1) {
            //Toast.makeText(this, "1键", Toast.LENGTH_SHORT).show();
            stringBuilder.append("1键").append("   ");
            setText();
            return false;
        } else if (keyCode == KeyEvent.KEYCODE_2) {
            //Toast.makeText(this, "2键", Toast.LENGTH_SHORT).show();
            stringBuilder.append("2键").append("   ");
            setText();
            return false;
        } else if (keyCode == KeyEvent.KEYCODE_3) {
            //Toast.makeText(this, "3键", Toast.LENGTH_SHORT).show();
            stringBuilder.append("3键").append("   ");
            setText();
            return false;
        } else if (keyCode == KeyEvent.KEYCODE_4) {
            //Toast.makeText(this, "4键", Toast.LENGTH_SHORT).show();
            stringBuilder.append("4键").append("   ");
            setText();
            return false;
        } else if (keyCode == KeyEvent.KEYCODE_5) {
            //Toast.makeText(this, "5键", Toast.LENGTH_SHORT).show();
            stringBuilder.append("5键").append("   ");
            setText();
            return false;
        } else if (keyCode == KeyEvent.KEYCODE_6) {
            //Toast.makeText(this, "6键", Toast.LENGTH_SHORT).show();
            stringBuilder.append("6键").append("   ");
            setText();
            return false;
        } else if (keyCode == KeyEvent.KEYCODE_7) {
            //Toast.makeText(this, "7键", Toast.LENGTH_SHORT).show();
            stringBuilder.append("7键").append("   ");
            setText();
            return false;
        } else if (keyCode == KeyEvent.KEYCODE_8) {
            //Toast.makeText(this, "8键", Toast.LENGTH_SHORT).show();
            stringBuilder.append("8键").append("   ");
            setText();
            return false;
        } else if (keyCode == KeyEvent.KEYCODE_9) {
            //Toast.makeText(this, "9键", Toast.LENGTH_SHORT).show();
            stringBuilder.append("9键").append("   ");
            setText();
            return false;
        } else if (keyCode == 139) {
            //Toast.makeText(this, "条码快捷键", Toast.LENGTH_SHORT).show();
            stringBuilder.append("条码快捷键").append("   ");
            setText();
            return false;
        } else if (keyCode == 131) {
            //Toast.makeText(this, "自定义F1键", Toast.LENGTH_SHORT).show();
            stringBuilder.append("自定义F1键").append("   ");
            setText();
            return false;
        } else if (keyCode == 132) {
            //Toast.makeText(this, "自定义F2键", Toast.LENGTH_SHORT).show();
            stringBuilder.append("自定义F2键").append("   ");
            setText();
            return false;
        } else if (keyCode == 17) {
            //Toast.makeText(this, "*键", Toast.LENGTH_SHORT).show();
            stringBuilder.append("*键").append("   ");
            setText();
            return false;
        } else if (keyCode == 18) {
            //Toast.makeText(this, "#键", Toast.LENGTH_SHORT).show();
            stringBuilder.append("#键").append("   ");
            setText();
            return false;
        }
        Log.i("info", "onKeyDown: ----------" + keyCode + "\n");
        return super.onKeyDown(keyCode, event);
    }

    @OnClick(R.id.next_test)
    public void onClick() {
        showDialog();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //MyApplication.getSession().set("button",true);
        if(MyApplication.getSession().getBoolean("button")!=true){
            MyApplication.getSession().set("button",false);
        }
    }
    private void showDialog(){
        AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        dialog.setMessage("是否确认成功完成该项检测并跳转下一项测试");
        dialog.setPositiveButton("成功",
                new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface arg0, int arg1) {

                        MyApplication.getSession().set("button",true);
                        Intent intent=new Intent(ButtonTestActivity.this, PhotoActivity.class);
                        startActivity(intent);
                        finish();

                    }
                });
        dialog.setNeutralButton("失败", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface arg0, int arg1) {
                MyApplication.getSession().set("button",false);
                arg0.dismiss();

            }
        });
        dialog.show();
    }
}
