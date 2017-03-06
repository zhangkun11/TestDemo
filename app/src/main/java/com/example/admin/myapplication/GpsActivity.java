package com.example.admin.myapplication;

import android.Manifest;
import android.app.Fragment;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;

import com.baidu.location.LocationClient;
import com.baidu.mapapi.SDKInitializer;
import com.baidu.mapapi.map.BaiduMap;
import com.example.admin.myapplication.fragment.BDMapFragment;
import com.example.admin.myapplication.interfaces.CallBack;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;

/**
 * Created by admin on 2017-02-22.
 */

public class GpsActivity extends AppCompatActivity implements CallBack{
    @InjectView(R.id.gps_info)
    TextView gpsInfo;
    public LocationClient locationClient;
    @InjectView(R.id.gps_try)
    Button gpsTry;

    private StringBuilder stringBuilder;
    private BaiduMap baiduMap;
    private boolean isFirstLocate = true;
    //android 原生GPS API ,LocationManager调用
    LocationManager locationManager;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //map
        SDKInitializer.initialize(getApplicationContext());


        setContentView(R.layout.activity_gps);

        ButterKnife.inject(this);


        Fragment mapFragment= new BDMapFragment();
        getFragmentManager().beginTransaction().replace(R.id.fragment,mapFragment).commit();


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
            return;
        }
        Location location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        updataView(location);
        //每三秒获取一次gps定位信息
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 8, new LocationListener() {
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
                    return;
                }
                updataView(locationManager.getLastKnownLocation(provider));
            }

            @Override
            public void onProviderDisabled(String provider) {
                updataView(null);
            }
        });

    }

    private void updataView(Location location) {
        if (location != null) {
            StringBuilder currentPosition = new StringBuilder();
            currentPosition.append("纬度：").append(location.getLatitude()).append("\n");
            currentPosition.append("经度：").append(location.getLongitude()).append("\n");
            currentPosition.append("高度：").append(location.getAltitude()).append("\n");
            currentPosition.append("速度：").append(location.getSpeed()).append("\n");
            currentPosition.append("方向：").append(location.getBearing()).append("\n");
            currentPosition.append("时间：").append(location.getTime()).append("\n");
            Log.i("info", "updataView: "+currentPosition);
            gpsInfo.setText(currentPosition);
        } else {
            gpsInfo.setText("没有GPS");
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        //map.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        //map.onPause();
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
            return;
        }
        Location location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        updataView(location);

        //回调函数测试
        new BDMapFragment().sendMsg(GpsActivity.this);
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();

    }

    @Override
    public void getMsg(String stringBuilder) {

        Log.i("getinfo", "getMsg: ======="+stringBuilder);
        gpsInfo.setText(stringBuilder);
    }
}
