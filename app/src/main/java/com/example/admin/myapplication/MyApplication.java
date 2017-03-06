package com.example.admin.myapplication;

import android.app.Application;
import android.content.Context;

/**
 * Created by admin on 2017-02-28.
 */

public class MyApplication extends Application {
    private static MyApplication getInstance;
    private static Context context;


    public static Context getMyApplicationContext(){
        return context;
    }



    public static MyApplication getMyApplication(){
        return getInstance;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        getInstance=this;
        context=getApplicationContext();
        //map
        //SDKInitializer.initialize(context);
    }
}
