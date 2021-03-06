package com.example.admin.myapplication.rs232;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.jb.rs232.RS232Controller;
import android.jb.rs232.RS232Controller.Callback;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.admin.myapplication.MyApplication;
import com.example.admin.myapplication.R;
import com.example.admin.myapplication.meter.MeterActivity;

import butterknife.ButterKnife;
import butterknife.InjectView;


public class RS232Activity extends Activity implements Callback {


    @InjectView(R.id.ps_title)
    TextView psTitle;
    private TextView tv;
    private Button btn;
    private RS232Controller rs232Con;
    private Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            byte[] data = (byte[]) msg.obj;
            switch (msg.what) {
                case 0:
                /*if (cb.isChecked())
					tv.setText(Tools.bytesToHexString(data, 0,
							data.length));
				else*/
                    tv.setText(new String(data));
                    showDialog();
                    break;

                default:
                    break;
            }
        }
    };
    private boolean dialogEnable;

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if(MyApplication.getSession().getBoolean("ps")!=true){
            MyApplication.getSession().set("ps",false);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();

    }

    @Override
    protected void onResume() {
        // TODO Auto-generated method stub
        super.onResume();
        sendBroadcast(new Intent("ReleaseCom"));
        try {
            Thread.sleep(200);
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        System.out.println("RS232Activity_onResume");
        rs232Con = RS232Controller.getInstance();
        //baurateSp.setSelection(0);
        initCon();
    }

    @Override
    protected void onPause() {
        // TODO Auto-generated method stub
        super.onPause();
        dialogEnable=false;
        if (rs232Con != null) {
            rs232Con.Rs232_Open(9600, 8, 'N', 1,
                    RS232Activity.this, this);
            rs232Con.Rs232_Close();
        }
        sendBroadcast(new Intent("ReLoadCom"));
        System.out.println("RS232Activity_onPause");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rs232);
        ButterKnife.inject(this);
        dialogEnable=true;


        tv = (TextView) findViewById(R.id.rs232_show_received_tv);
        btn = (Button) findViewById(R.id.rs232_sendbtn);
        btn.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                try {
                    String etStr = "FE FE FE FE 68 32 00 32 00 68 5B FF FF FF FF 01 0A 60 00 00 01 0B CE 16";
                    if (etStr != null && !etStr.trim().equals("")) {
                        try {
//							rs232Con.converToRS232();
							/*if (cb.isChecked())
								rs232Con.Rs232_Write(Tools
										.hexString2Bytes(etStr));
							else*/
                            rs232Con.Rs232_Write(etStr.getBytes());

                        } catch (Exception e) {
                            // TODO: handle exception
                            e.printStackTrace();
                            tv.setText("error:" + e.getMessage());
                        }

                    } else {

                        Toast.makeText(
                                RS232Activity.this,
                                getString(R.string.rs232_cannot_send_empty_string),
                                Toast.LENGTH_SHORT).show();
                    }

                } catch (NumberFormatException e) {
                    // TODO: handle exception
                    e.printStackTrace();
                }

            }
        });

    }

    protected void initCon() {
        // TODO Auto-generated method stub
        rs232Con = RS232Controller.getInstance();
        int baud = 9600;
        int dataBit = 8;
        int stopBit = 1;
        char even = 'E';


        rs232Con.Rs232_Open(baud, dataBit, even, stopBit,
                RS232Activity.this, this);
    }


    @Override
    public void RS232_Read(byte[] data) {
        // TODO Auto-generated method stub
        if (data != null)
            mHandler.sendMessage(mHandler.obtainMessage(0, data));
    }
    private void showDialog(){
        final AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        dialog.setMessage("检测成功，是否确认完成该项检测并跳转下一项测试");
        dialog.setPositiveButton("是",
                new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface arg0, int arg1) {

                        MyApplication.getSession().set("ps",true);
                        Intent intent=new Intent(RS232Activity.this, MeterActivity.class);
                        startActivity(intent);

                        finish();

                    }
                });
        dialog.setNeutralButton("否", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface arg0, int arg1) {

                MyApplication.getSession().set("ps",true);
                arg0.dismiss();

            }
        });
        if(dialogEnable==true){
            dialog.show();}
    }

}
