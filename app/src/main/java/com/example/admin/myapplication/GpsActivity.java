package com.example.admin.myapplication;

import android.Manifest;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.location.LocationClient;
import com.baidu.mapapi.map.BaiduMap;
import com.example.admin.myapplication.interfaces.CallBack;
import com.example.admin.myapplication.meter.MeterActivity;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;

/**
 * Created by admin on 2017-02-22.
 */

public class GpsActivity extends AppCompatActivity implements CallBack {
    @InjectView(R.id.gps_info)
    TextView gpsInfo;
    public LocationClient locationClient;
    @InjectView(R.id.gps_try)
    Button gpsTry;
    @InjectView(R.id.next_test)
    Button nextTest;

    private StringBuilder stringBuilder;
    private BaiduMap baiduMap;
    private boolean isFirstLocate = true;
    //android 原生GPS API ,LocationManager调用
    LocationManager locationManager;
    private boolean isGpsEnable = false, isShowProgress = false;
    private boolean dialogEnable;
    private boolean isContune;
    private int count;
    private boolean isCount,ischeckLoca;

    private ProgressDialog progressDialog;
    Runnable runnable = new Runnable() {
        @Override
        public void run() {
            while (true) {
                try {
                    Thread.sleep(1000);
                    if(isCount){
                    count++;}
                    else {
                        count=0;
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                handler.sendEmptyMessage(0);
            }

        }
    };


    @Override
    protected void onStart() {
        super.onStart();
        dialogEnable=true;

        ischeckLoca=true;

        checkLocation();
        Location location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        isGpsEnable=true;
        //updataView(location);
        //每1秒获取一次gps定位信息
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 0, new LocationListener() {

            @Override
            public void onLocationChanged(Location location) {
                updataView(location);
            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {

            }

            @Override
            public void onProviderEnabled(String provider) {
                //可用
                if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    // TODO: Consider calling
                    //    ActivityCompat#requestPermissions
                    // here to request the missing permissions, and then overriding
                    //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                    //                                          int[] grantResults)
                    // to handle the case where the user grants the permission. See the documentation
                    // for ActivityCompat#requestPermissions for more details.
                    Toast.makeText(GpsActivity.this, "定位权限未获取，请在权限管理中打开定位授权", Toast.LENGTH_LONG).show();
                }
                checkLocation();
                isGpsEnable = true;

                gpsInfo.setText("正在查询GPS信息,请等待...");
                //showProgressCheck();
                Toast.makeText(GpsActivity.this, "正在查询GPS信息", Toast.LENGTH_LONG).show();
                updataView(locationManager.getLastKnownLocation(provider));
            }

            @Override
            public void onProviderDisabled(String provider) {
                Log.i("info", "onProviderDisabled:    try......");
                gpsInfo.setText("正在自动查询定位信息...");
                if(ischeckLoca){
                checkLocation();
                ischeckLoca=false;}
                updataView(null);
            }
        });

    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //map
        //SDKInitializer.initialize(getApplicationContext());


        setContentView(R.layout.activity_gps);

        ButterKnife.inject(this);
        isContune=true;
        //Toast.makeText(GpsActivity.this,"GPS测试",Toast.LENGTH_SHORT).show();


        nextTest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                MyApplication.getSession().set("rs_4",false);
                Intent intent=new Intent(GpsActivity.this,MeterActivity.class);
                intent.putExtra("From", "caobiao");
                intent.putExtra("IS485", false);
                startActivity(intent);
                finish();
            }
        });
        count=0;
        isCount=true;

        gpsInfo.setText("定位信息：自动查询中...");
        new Thread(runnable).start();
        /*Fragment mapFragment= new BDMapFragment();
        getFragmentManager().beginTransaction().replace(R.id.fragment,mapFragment).commit();*/


        /**
         * android gps api定位
         */
        //android gps api
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        //从gps获取最近的位置信息
        if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            Toast.makeText(GpsActivity.this, "定位权限未获取，请在权限管理中打开定位授权", Toast.LENGTH_LONG).show();
        }




    }

    private void updataView(Location location) {
        if (location != null) {

            StringBuilder currentPosition = new StringBuilder();
            Log.i("info", "updataView: before " + currentPosition);
            currentPosition.append("纬度：").append(location.getLatitude()).append("\n");
            currentPosition.append("经度：").append(location.getLongitude()).append("\n");
            currentPosition.append("高度：").append(location.getAltitude()).append("\n");
            currentPosition.append("速度：").append(location.getSpeed()).append("\n");
            currentPosition.append("方向：").append(location.getBearing()).append("\n");
            currentPosition.append("时间：").append(location.getTime()).append("\n");
            Log.i("info", "updataView: " + currentPosition);
            gpsInfo.setText(currentPosition);

            isShowProgress = false;
            isCount=false;
            handler.sendEmptyMessage(0);
            if(isContune){
            showDialog();
                isContune=false;
            }




        } else {
            if (isGpsEnable == false) {

                gpsInfo.setText("暂时无法获取GPS信息，自动重试中...");
                Toast.makeText(GpsActivity.this, "程序正在自动重试查找，请等待...", Toast.LENGTH_LONG).show();

            } else {
                gpsInfo.setText("正在查询GPS信息,请等待...");
                Toast.makeText(GpsActivity.this, "查询GPS信息中...", Toast.LENGTH_LONG).show();

                isShowProgress = true;
                Message message=new Message();
                message.what=1;
                handler.sendMessage(message);
            }

        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        //map.onResume();
        dialogEnable=true;
    }

    @Override
    protected void onPause() {
        super.onPause();
        //map.onPause();
        dialogEnable=false;
    }

    @OnClick(R.id.gps_try)
    public void onClick() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            Toast.makeText(GpsActivity.this, "定位权限未获取，请在权限管理中打开定位授权", Toast.LENGTH_LONG).show();
        }
        checkLocation();
        Location location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        isContune=true;
        updataView(location);

        //回调函数测试
        //new BDMapFragment().sendMsg(GpsActivity.this);
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        //MyApplication.getSession().set("gps",true);
        if(MyApplication.getSession().getBoolean("gps")!=true){
            MyApplication.getSession().set("gps",false);
        }

    }

    @Override
    protected void onStop() {
        super.onStop();
        dialogEnable=false;
    }


    @Override
    public void getMsg(String stringBuilder) {

        Log.i("getinfo", "getMsg: =======" + stringBuilder);
        gpsInfo.setText(stringBuilder);
    }

    private void checkLocation() {
        //Log.i("info", "checkLocation: ------>");
        boolean isOpen = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        if (isOpen == false) {
            isGpsEnable = false;
            Toast.makeText(GpsActivity.this, "未打开GPS，请开启定位服务", Toast.LENGTH_SHORT).show();
            //Log.i("info", "checkLocation: ------>  open -------->");
            AlertDialog.Builder dialog = new AlertDialog.Builder(this);
            dialog.setMessage("GPS未开启，请打开GPS");
            dialog.setPositiveButton("确定",
                    new DialogInterface.OnClickListener() {

                        @Override
                        public void onClick(DialogInterface arg0, int arg1) {

                            // 转到手机设置界面，用户设置GPS
                            Intent intent = new Intent(
                                    Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                            startActivityForResult(intent, 0); // 设置完成后返回到原来的界面
                            count=0;

                        }
                    });
            dialog.setNeutralButton("取消", new DialogInterface.OnClickListener() {

                @Override
                public void onClick(DialogInterface arg0, int arg1) {
                    arg0.dismiss();
                    MyApplication.getSession().set("gps",false);
                    finish();
                }
            });
            if(dialogEnable==true){
            dialog.show();}

        }
    }

    private void showProgressCheck() {
        if (isGpsEnable) {
            if (isShowProgress) {
                progressDialog = new ProgressDialog(GpsActivity.this);
                progressDialog.setTitle("GPS查询");
                progressDialog.setMessage("查询中，请等待...");
                progressDialog.setCancelable(true);
                progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                progressDialog.setIndeterminate(false);
                progressDialog.show();
            } else {
                if (progressDialog != null) {
                    progressDialog.setMessage("已完成");
                    progressDialog.dismiss();
                    isGpsEnable = false;
                }
            }
        }

    }

    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if(isCount){
            gpsInfo.setText("定位信息：自动查询中...   ("+count+"s)");
            if(count==120){
                //if(MyApplication.getSession().getBoolean("gps")==false){
                    MyApplication.getSession().set("gps",false);
                    gpsInfo.setText("定位信息获取失败！");
                    finish();
                //}
            }
            }else {

            }
            if(msg.what==1){
            showProgressCheck();
            }
        }
    };

    private void showDialog(){
        final AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        dialog.setMessage("检测成功，是否确认完成该项检测并跳转下一项测试");
        dialog.setPositiveButton("是",
                new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface arg0, int arg1) {

                        MyApplication.getSession().set("gps",true);
                        MyApplication.getSession().set("rs_4",false);
                        Intent intent=new Intent(GpsActivity.this, MeterActivity.class);
                        intent.putExtra("From", "caobiao");
                        intent.putExtra("IS485", false);
                        startActivity(intent);

                        finish();

                    }
                });
        dialog.setNeutralButton("否", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface arg0, int arg1) {
                MyApplication.getSession().set("gps",true);
                arg0.dismiss();

            }
        });
        if(dialogEnable==true){
        dialog.show();}
    }


}
