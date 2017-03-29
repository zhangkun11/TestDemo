package com.example.admin.myapplication.meter;

import android.jb.meter.MeterController;
import android.jb.utils.Tools;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.example.admin.myapplication.R;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;


/**
 * Created by admin on 2017-03-29.
 */

public class ReadAddress extends AppCompatActivity {
    @InjectView(R.id.meter_adress)
    EditText meterAdress;
    @InjectView(R.id.read_address)
    Button readAddress;
    @InjectView(R.id.meter_info)
    TextView meterInfo;

    private byte[] sendBuffer;
    private MeterController controler;
    private StringBuilder sb;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_readadress);
        ButterKnife.inject(this);
    }

    @Override
    protected void onStart() {
        super.onStart();

    }

    @Override
    protected void onResume() {
        super.onResume();
        controler = MeterController.getInstance();
        controler.Meter_Open(MeterController.METER_Infrared, 1200, 8, 'E',
                1, portData , this);
    }

    @Override
    protected void onStop() {
        super.onStop();
        if(controler!=null){
        controler.Meter_Close();}
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @OnClick(R.id.read_address)
    public void onClick() {
        controler.Meter_Close();
        controler.Meter_Open(MeterController.METER_Infrared, 1200, 8, 'E',
                1, portData , this);
        sendReadAddress();
    }

    private void sendReadAddress(){

        int length=16;
        sendBuffer=new byte[length];
        //头部
        sendBuffer[0] = (byte) 0xFE;
        sendBuffer[1] = (byte) 0xFE;
        sendBuffer[2] = (byte) 0xFE;
        sendBuffer[3] = (byte) 0xFE;
        //起始符 68H
        sendBuffer[4] = 0x68;
        //地址域
        for(int i=5;i<11;i++){
            sendBuffer[i]= (byte) 0xAA;
        }
        //起始符 68H
        sendBuffer[11] = 0x68;
        //控制码 13H
        sendBuffer[12]=0x13;
        //数据域长度 00H
        sendBuffer[13]=0x00;
        //校验码
        int sumMod = 0;
        for (int i = 4; i <= length - 3; i++) {
            sumMod += (int) sendBuffer[i];
        }
        sendBuffer[length - 2] = (byte) (sumMod % 256);
        //结束位 16H
        sendBuffer[length-1]=0x16;
        Log.i("info", "sendReadAddress: "+Tools.bytesToHexString(sendBuffer));

        controler.writeCommand(sendBuffer);
    }
    MeterController.Callback portData=new MeterController.Callback() {
        @Override
        public void Meter_Read(byte[] buffer, int size) {
            Log.i("info", "Meter_Read: "+ Tools.bytesToHexString(buffer));
            if(size==22){
            SerialPortData data=new SerialPortData(buffer,size);
            dealData(data);}
        }
    };


    private void dealData(SerialPortData data){
        byte[] info=data.getDataByte();
        Log.i("info", "dealData:  info=="+Tools.bytesToHexString(info));
        if(info!=null){
            /*byte[] b=new byte[info.length-16];
            for(int i=16;i<info.length;i++){
                b[i-16]=info[i];
            }
            Log.i("info", "dealData:  b[]=="+Tools.bytesToHexString(b));*/
            final byte[] getB=new byte[6];
            byte[] getA=new byte[6];
            boolean first=true;
            for(int i=0;i<info.length;i++){

                if(info[i]==0x68&&first==true){
                    first=false;
                    int x=i+1;
                    for(int j=0;j<6;j++){
                        getB[j]=info[x+j];
                    }
                    Log.i("info", "dealData: "+Tools.bytesToHexString(getB));

                    if(info[x+6]==0x68){

                        if(Tools.byteToString(info[x+7]).equals("93")&&Tools.byteToString(info[x+8]).equals("06")){
                            Log.i("info", "dealData:   获取成功");
                            for(int j=0;j<6;j++){
                            getA[j]= (byte) (info[x+9+j]-0x33);
                            }
                            sb=new StringBuilder();
                            for(int k=getA.length-1;k>=0;k--){
                                sb.append(Tools.byteToString(getA[k]));
                            }
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    meterAdress.setText(sb);
                                    meterAdress.setSelection(sb.length());
                                    meterInfo.setText("表地址:"+Tools.bytesToHexString(getB));
                                }
                            });
                        }
                    }
                }
            }
        }

    }

    public class SerialPortData {
        private byte[] dataByte;
        private int size;

        public SerialPortData(byte[] _dataByte, int _size) {
            this.setDataByte(_dataByte);
            this.setSize(_size);
        }

        public int getSize() {
            return size;
        }

        public void setSize(int size) {
            this.size = size;
        }

        public byte[] getDataByte() {
            return dataByte;
        }

        public void setDataByte(byte[] dataByte) {
            this.dataByte = dataByte;
        }
    }
}
