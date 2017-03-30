package com.example.admin.myapplication.meter;

import android.jb.meter.MeterController;
import android.jb.utils.Tools;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
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
    @InjectView(R.id.read_btn)
    Button readBtn;
    @InjectView(R.id.data_sign)
    EditText dataSign;

    private byte[] sendBuffer;
    private byte[] sendBuffer2;
    private MeterController controler;
    private StringBuilder sb;
    private byte[] address = new byte[6];

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_readadress);
        ButterKnife.inject(this);
        readBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                /*controler.Meter_Close();
                controler.Meter_Open(MeterController.METER_Infrared, 1200, 8, 'E',
                        1, portData, ReadAddress.this);*/
                sendReadTime();

            }
        });
        dataSign.setText("04000101");
        dataSign.setSelection(8);
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
                1, portData, this);
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (controler != null) {
            controler.Meter_Close();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @OnClick(R.id.read_address)
    public void onClick() {
        /*controler.Meter_Close();
        controler.Meter_Open(MeterController.METER_Infrared, 1200, 8, 'E',
                1, portData, this);*/
        sendReadAddress();
    }


    private void sendReadAddress() {

        int length = 16;
        sendBuffer = new byte[length];
        //头部
        sendBuffer[0] = (byte) 0xFE;
        sendBuffer[1] = (byte) 0xFE;
        sendBuffer[2] = (byte) 0xFE;
        sendBuffer[3] = (byte) 0xFE;
        //起始符 68H
        sendBuffer[4] = 0x68;
        //地址域
        for (int i = 5; i < 11; i++) {
            sendBuffer[i] = (byte) 0xAA;
        }
        //起始符 68H
        sendBuffer[11] = 0x68;
        //控制码 13H
        sendBuffer[12] = 0x13;
        //数据域长度 00H
        sendBuffer[13] = 0x00;
        //校验码
        int sumMod = 0;
        for (int i = 4; i <= length - 3; i++) {
            sumMod += (int) sendBuffer[i];
        }
        sendBuffer[length - 2] = (byte) (sumMod % 256);
        //结束位 16H
        sendBuffer[length - 1] = 0x16;
        Log.i("info", "sendReadAddress: " + Tools.bytesToHexString(sendBuffer));

        controler.writeCommand(sendBuffer);
    }

    MeterController.Callback portData = new MeterController.Callback() {
        @Override
        public void Meter_Read(byte[] buffer, int size) {
            Log.i("info", "Meter_Read: " + Tools.bytesToHexString(buffer));
            if (size == 22) {
                SerialPortData data = new SerialPortData(buffer, size);
                dealData(data);
            }
            if (size == 24) {
                SerialPortData data = new SerialPortData(buffer, size);
                dealData2(data);
            }

        }
    };


    private void dealData(SerialPortData data) {
        byte[] info = data.getDataByte();
        Log.i("info", "dealData:  info==" + Tools.bytesToHexString(info));
        if (info != null) {
            /*byte[] b=new byte[info.length-16];
            for(int i=16;i<info.length;i++){
                b[i-16]=info[i];
            }
            Log.i("info", "dealData:  b[]=="+Tools.bytesToHexString(b));*/
            final byte[] getB = new byte[6];
            byte[] getA = new byte[6];
            boolean first = true;
            for (int i = 0; i < info.length; i++) {

                if (info[i] == 0x68 && first == true) {
                    first = false;
                    int x = i + 1;
                    for (int j = 0; j < 6; j++) {
                        getB[j] = info[x + j];
                        address[j] = getB[j];
                    }
                    Log.i("info", "dealData: " + Tools.bytesToHexString(getB));

                    if (info[x + 6] == 0x68) {

                        if (Tools.byteToString(info[x + 7]).equals("93") && Tools.byteToString(info[x + 8]).equals("06")) {
                            Log.i("info", "dealData:   获取成功");
                            for (int j = 0; j < 6; j++) {
                                getA[j] = (byte) (info[x + 9 + j] - 0x33);
                            }
                            sb = new StringBuilder();
                            for (int k = getA.length - 1; k >= 0; k--) {
                                sb.append(Tools.byteToString(getA[k]));
                            }
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    meterAdress.setText(sb);
                                    meterAdress.setSelection(sb.length());
                                    meterInfo.setText("表地址:" + Tools.bytesToHexString(getB));
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

    private void sendReadTime() {
        int length = 20;
        String bz = dataSign.getText().toString();
        sendBuffer2 = new byte[length];
        //头部
        sendBuffer2[0] = (byte) 0xFE;
        sendBuffer2[1] = (byte) 0xFE;
        sendBuffer2[2] = (byte) 0xFE;
        sendBuffer2[3] = (byte) 0xFE;
        //起始符 68H
        sendBuffer2[4] = 0x68;
        //地址域
        if (address != null) {
            for (int i = 5; i < 11; i++) {
                sendBuffer2[i] = address[i - 5];
            }
        } else {
            sendBuffer2[5] = Tools.hexString2Bytes("15")[0];
            sendBuffer2[6] = Tools.hexString2Bytes("09")[0];
            sendBuffer2[7] = Tools.hexString2Bytes("07")[0];
            sendBuffer2[8] = Tools.hexString2Bytes("15")[0];
            sendBuffer2[9] = Tools.hexString2Bytes("20")[0];
            sendBuffer2[10] = Tools.hexString2Bytes("00")[0];
        }
        //起始符 68H
        sendBuffer2[11] = 0x68;
        //控制码 11H
        sendBuffer2[12] = 0x11;
        //数据域长度 04H
        sendBuffer2[13] = 0x04;
        //数据域
        for (int i = 1; i <= 4; i++) {
            int j = 2 * (4 - i);
            sendBuffer2[13 + i] = (byte) (Tools.hexString2Bytes(bz.substring(j,
                    j + 2))[0] + 0x33);
        }
        /*sendBuffer2[14] = (byte) (Tools.hexString2Bytes("01")[0] + 0x33);
        sendBuffer2[15] = (byte) (Tools.hexString2Bytes("01")[0] + 0x33);
        sendBuffer2[16] = (byte) (Tools.hexString2Bytes("00")[0] + 0x33);
        sendBuffer2[17] = (byte) (Tools.hexString2Bytes("04")[0] + 0x33);*/


        //校验码
        //int sumMod = 0;
        byte sumMod = 0x00;
        for (int i = 4; i <= length - 3; i++) {
            //sumMod += (int) sendBuffer2[i];
            sumMod += sendBuffer2[i];
        }
        //sendBuffer2[length - 2] = (byte) (sumMod % 256);
        sendBuffer2[length - 2] = sumMod;
        //结束位 16H
        sendBuffer2[length - 1] = 0x16;
        Log.i("info", "sendReadAddress: " + Tools.bytesToHexString(sendBuffer2));

        controler.writeCommand(sendBuffer2);
    }

    private void dealData2(SerialPortData data) {
        byte[] info = data.getDataByte();
        Log.i("info", "dealData:  info==" + Tools.bytesToHexString(info));
        if (info != null) {


            final byte[] getA = new byte[8];
            boolean first = true;
            for (int i = 0; i < info.length; i++) {

                if (info[i] == 0x68 && first == true) {
                    first = false;
                    int x = i + 1;


                    if (info[x + 6] == 0x68) {

                        if (info[x + 7] == Tools.stringToByte("91") && info[x + 8] == Tools.stringToByte("08")) {
                            Log.i("info", "dealData:   获取成功");
                            for (int j = 0; j < 8; j++) {
                                getA[j] = (byte) (info[x + 9 + j] - 0x33);
                            }
                            sb = new StringBuilder();
                            sb.append("数据标识：");
                            for (int k = 0; k < getA.length / 2; k++) {
                                sb.append(Tools.byteToString(getA[k]));
                            }
                            sb.append("\n").append("时间：(年.月.日.星期)");
                            for (int k = getA.length - 1; k >= getA.length / 2; k--) {
                                sb.append(Tools.byteToString(getA[k])).append(".");
                            }

                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {

                                    meterInfo.setText(sb);
                                }
                            });
                        }
                    }
                }
            }
        }
    }
}
