package com.example.admin.myapplication;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.view.MotionEvent;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

import com.example.admin.myapplication.buttontest.ButtonTestActivity;


public class MainActivity extends AppCompatActivity {

    TextView text;
    private int i=0;
    private int[] colors={
            R.color.colorRed,
            R.color.colorBlue,
            R.color.colorGreen,
            R.color.colorWhite,
            R.color.colorBlack,
            R.color.colorYellow};
    private boolean dialogEnable;
    Handler handler=new Handler(){
        @Override
        public void handleMessage(Message msg) {
            if (msg.what==1){

                i++;
                if(i>5){
                    i=0;
                }
                text.setBackground(getResources().getDrawable(colors[i]));
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_main);
        dialogEnable=true;
        text= (TextView) findViewById(R.id.textTest);
        Toast.makeText(MainActivity.this,"屏幕测试",Toast.LENGTH_SHORT).show();
        new Thread(runnable).start();

    }

    @Override
    protected void onStart() {
        super.onStart();
        dialogEnable=true;
    }

    @Override
    protected void onPause() {
        super.onPause();
        dialogEnable=false;
    }

    @Override
    protected void onResume() {
        super.onResume();
        dialogEnable=true;
    }

    @Override
    protected void onStop() {
        super.onStop();
        dialogEnable=false;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if(event.getAction()==MotionEvent.ACTION_DOWN){
            showDialog();
        }
        return super.onTouchEvent(event);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //MyApplication.getSession().set("main",true);
        if(MyApplication.getSession().getBoolean("main")!=true){
            MyApplication.getSession().set("main",false);
        }
    }

    Runnable runnable = new Runnable() {
        @Override
        public void run() {
            while (true) {
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                Message message = new Message();
                message.what = 1;
                handler.sendMessage(message);
            }

        }
    };
    private void showDialog(){
        AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        dialog.setMessage("是否确认成功完成该项检测并跳转下一项测试");
        dialog.setPositiveButton("成功",
                new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface arg0, int arg1) {

                       MyApplication.getSession().set("main",true);
                        Intent intent=new Intent(MainActivity.this, ButtonTestActivity.class);
                        startActivity(intent);
                        finish();

                    }
                });
        dialog.setNeutralButton("失败", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface arg0, int arg1) {
                MyApplication.getSession().set("main",false);
                arg0.dismiss();
                finish();
            }
        });
        if(dialogEnable==true){
        dialog.show();}
    }

}

