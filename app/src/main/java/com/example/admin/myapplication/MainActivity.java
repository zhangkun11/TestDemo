package com.example.admin.myapplication;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.view.WindowManager;
import android.widget.TextView;


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
        text= (TextView) findViewById(R.id.textTest);
        new Thread(runnable).start();

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        MyApplication.getSession().set("main",true);
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

}

