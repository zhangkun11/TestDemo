package com.example.admin.myapplication.fragment;

import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.MapStatus;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.model.LatLng;
import com.example.admin.myapplication.R;
import com.example.admin.myapplication.interfaces.CallBack;

/**
 * Created by admin on 2017-03-03.
 */

public class BDMapFragment extends Fragment {

    private MapView mapView=null;
    private BaiduMap baiduMap=null;
    private TextView textView;
    public LocationClient locationClient;
    private StringBuilder mapInfo=new StringBuilder();
    private String test="sdgadhusadhua";
    private String getMsg=null;
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

    }

    @Override
    public void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    @Override
    public void onPause() {
        super.onPause();
        mapView.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        locationClient.stop();
        baiduMap.setMyLocationEnabled(false);
        mapView.onDestroy();
    }


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view=inflater.inflate(R.layout.fragement_map,container,false);
        mapView= (MapView) view.findViewById(R.id.map);
        textView= (TextView) view.findViewById(R.id.map_info);

        baiduMap=mapView.getMap();
        baiduMap.setMyLocationEnabled(true);

        locationClient=new LocationClient(getActivity().getApplicationContext());
        locationClient.registerLocationListener(new MyLocationListener());

        initLocationClient();
        locationClient.start();


        return view;

    }

    private void initLocationClient() {
        //1秒更新一次位置
        LocationClientOption locationClientOption = new LocationClientOption();
        locationClientOption.setScanSpan(1000);

        locationClientOption.setIsNeedAddress(true);
        locationClient.setLocOption(locationClientOption);
    }

    private class MyLocationListener implements BDLocationListener{

        @Override
        public void onReceiveLocation(BDLocation bdLocation) {
            mapInfo=new StringBuilder();
            mapInfo.append("纬度：").append(bdLocation.getLatitude()).append("\n");
            mapInfo.append("经度：").append(bdLocation.getLongitude()).append("\n");
            mapInfo.append("省：").append(bdLocation.getProvince()).append("\n");
            mapInfo.append("市：").append(bdLocation.getCity()).append("\n");
            mapInfo.append("区：").append(bdLocation.getDistrict()).append("\n");
            mapInfo.append("街道：").append(bdLocation.getStreet()).append("\n");
            mapInfo.append("定位方式：");
            if (bdLocation.getLocType() == BDLocation.TypeGpsLocation) {
                mapInfo.append("GPS");
                //navigateTo(bdLocation);
            } else if (bdLocation.getLocType() == BDLocation.TypeNetWorkLocation) {
                mapInfo.append("网络");
                //navigateTo(bdLocation);
            }
            if(bdLocation.getLocType() == BDLocation.TypeGpsLocation||bdLocation.getLocType() == BDLocation.TypeNetWorkLocation){
                navigateTo(bdLocation);
            }
            handler.sendEmptyMessage(0);

        }

        @Override
        public void onConnectHotSpotMessage(String s, int i) {

        }
    }

    private void navigateTo(BDLocation bdLocation) {
        LatLng latLng=new LatLng(bdLocation.getLatitude(),bdLocation.getLongitude());
        MapStatus mapStatus = new MapStatus.Builder()
                //定位到定位点
                .target(latLng)
                //决定缩放的尺寸
                .zoom(16)
                .build();
        //利用MapStatus构建一个MapStatusUpdate对象
        MapStatusUpdate mapStatusUpdate = MapStatusUpdateFactory.newMapStatus(mapStatus);
        //更新BaiduMap，此时BaiduMap的界面就会从初始位置（北京），移动到定位点
        baiduMap.setMapStatus(mapStatusUpdate);

        //位置显示
        MyLocationData.Builder builder=new MyLocationData.Builder();
        builder.latitude(bdLocation.getLatitude());
        builder.longitude(bdLocation.getLongitude());
        MyLocationData myLocationData=builder.build();
        baiduMap.setMyLocationData(myLocationData);
    }
    private Handler handler=new Handler(){
        @Override
        public void handleMessage(Message msg) {
            textView.setText(mapInfo);
        }
    };


    //回调测试
    public void sendMsg(CallBack callBack){

        callBack.getMsg(test);

    }

}
