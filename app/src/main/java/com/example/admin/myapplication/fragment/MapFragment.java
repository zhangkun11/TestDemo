package com.example.admin.myapplication.fragment;


import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
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
import com.baidu.mapapi.map.SupportMapFragment;
import com.baidu.mapapi.model.LatLng;
import com.example.admin.myapplication.R;

/**
 * Created by admin on 2017-02-22.
 */

public class MapFragment extends SupportMapFragment {
    private MapView mapFragment=null;
    private StringBuilder stringBuilder;
    private BaiduMap baiduMap=null;
    public LocationClient locationClient;
    private Context context;
    private TextView mapInfo;
    private Activity mActivity;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        this.mActivity= (Activity) context;

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        //SDKInitializer.initialize(getActivity().getApplicationContext());

        View view = inflater.inflate(R.layout.fragement_map, container, false);
        mapFragment= (MapView) view.findViewById(R.id.map);
        mapInfo= (TextView) view.findViewById(R.id.map_info);

        baiduMap=mapFragment.getMap();
        baiduMap.setMyLocationEnabled(true);

        locationClient = new LocationClient(mActivity.getApplicationContext());
        locationClient.registerLocationListener(new MyLocationListener());

        initLocation();
        locationClient.start();

        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }

    @Override
    public void onPause() {
        super.onPause();

        mapFragment.onPause();
    }

    @Override
    public void onResume() {
        super.onResume();

        mapFragment.onResume();
    }

    @Override
    public void onStop() {
        super.onStop();

    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        locationClient.stop();
        mapFragment.onDestroy();
        baiduMap.setMyLocationEnabled(false);
    }

    public class MyLocationListener implements BDLocationListener {

        @Override
        public void onReceiveLocation(BDLocation bdLocation) {

            StringBuilder currentPosition = new StringBuilder();
            currentPosition.append("纬度：").append(bdLocation.getLatitude()).append("\n");
            currentPosition.append("经度：").append(bdLocation.getLongitude()).append("\n");
            currentPosition.append("省：").append(bdLocation.getProvince()).append("\n");
            currentPosition.append("市：").append(bdLocation.getCity()).append("\n");
            currentPosition.append("区：").append(bdLocation.getDistrict()).append("\n");
            currentPosition.append("街道：").append(bdLocation.getStreet()).append("\n");
            currentPosition.append("定位方式：");
            if (bdLocation.getLocType() == BDLocation.TypeGpsLocation) {
                currentPosition.append("GPS");
                //navigateTo(bdLocation);
            } else if (bdLocation.getLocType() == BDLocation.TypeNetWorkLocation) {
                currentPosition.append("网络");
                //navigateTo(bdLocation);
            }
            if(bdLocation.getLocType() == BDLocation.TypeGpsLocation||bdLocation.getLocType() == BDLocation.TypeNetWorkLocation){
                navigateTo(bdLocation);
            }

            //此线程不在UI线程内，UI操作使用handler
            //gpsInfo.setText(currentPosition);
            stringBuilder = currentPosition;
            handler.sendEmptyMessage(0);
        }

        @Override
        public void onConnectHotSpotMessage(String s, int i) {

        }
    }

    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            mapInfo.setText(stringBuilder);

        }
    };

    private void initLocation() {
        //1秒更新一次位置
        LocationClientOption locationClientOption = new LocationClientOption();
        locationClientOption.setScanSpan(1000);

        locationClientOption.setIsNeedAddress(true);
        locationClient.setLocOption(locationClientOption);
    }

    //地图定位显示
    private void navigateTo(BDLocation bdLocation){
        //if(isFirstLocate){
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

        //MapStatusUpdate update= MapStatusUpdateFactory.newLatLng(latLng);
        //update=MapStatusUpdateFactory.zoomTo(16f);
        //baiduMap.setMapStatus(update);
        //baiduMap.animateMapStatus(update);

        //baiduMap.animateMapStatus(update);
        //isFirstLocate=false;
        // }
        //位置显示
        MyLocationData.Builder builder=new MyLocationData.Builder();
        builder.latitude(bdLocation.getLatitude());
        builder.longitude(bdLocation.getLongitude());
        MyLocationData myLocationData=builder.build();
        baiduMap.setMyLocationData(myLocationData);
    }
}
