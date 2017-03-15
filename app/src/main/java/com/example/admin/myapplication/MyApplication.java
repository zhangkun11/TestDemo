package com.example.admin.myapplication;

import android.app.Application;
import android.content.Context;

import com.example.admin.myapplication.model.Session;

/**
 * Created by admin on 2017-02-28.
 */

public class MyApplication extends Application {
    private static MyApplication getInstance;
    private static Context context;

    public static Session getSession() {
        return session;
    }

    private static Session session;


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
        session=new Session();
        //map
        //SDKInitializer.initialize(context);
    }
}
