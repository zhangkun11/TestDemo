package com.example.admin.myapplication.scan;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.jb.Preference;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.example.admin.myapplication.MyApplication;

public class BootBroadcastReceiver extends BroadcastReceiver {

	private Handler mHandler = new Handler(){
		public void handleMessage(android.os.Message msg) {
			switch (msg.what) {
			case 1000:
				Context arg0 = (Context) msg.obj;
				if (Preference.getScanSelfopenSupport(
						MyApplication.getMyApplicationContext(), true)) {
					Intent service = new Intent(arg0, ScanService.class);
					arg0.startService(service);
					System.out.println("BootBroadcastReceiver serviceup");
				}
				break;

			default:
				break;
			}
		};
	};

	@Override
	public void onReceive(Context arg0, Intent arg1) {
		// TODO Auto-generated method stub
		Log.v("jiebao", "BootBroadcastReceiver onReceive getAction(): "+arg1.getAction());
		if (// Intent.ACTION_SCREEN_ON.equals(arg1.getAction())
		// Intent.ACTION_USER_PRESENT.equals(arg1.getAction())
		// Intent.ACTION_BOOT_COMPLETED.equals(arg1.getAction())
		"ScanServiceDestroy".equals(arg1.getAction())) {
			if (Preference.getScanSelfopenSupport(
					MyApplication.getMyApplicationContext(), true)) {
				Intent service = new Intent(arg0, ScanService.class);
				arg0.startService(service);
				System.out.println("BootBroadcastReceiver ScanServiceDestroy");
			}
		}else if (Intent.ACTION_BOOT_COMPLETED.equals(arg1.getAction())) {
			if (Preference.getScanSelfopenSupport(
					MyApplication.getMyApplicationContext(), true)) {
				Intent service = new Intent(arg0, ScanService.class);
				arg0.startService(service);
				System.out.println("BootBroadcastReceiver ACTION_BOOT_COMPLETED");
			}
			Message message = new Message();
			message.what = 1000;
			message.obj = arg0;
			mHandler.sendMessageDelayed(message, 5000);
			System.out.println("BootBroadcastReceiver restart");
		}
	}

}
