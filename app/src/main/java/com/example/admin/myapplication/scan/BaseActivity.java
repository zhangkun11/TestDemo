package com.example.admin.myapplication.scan;


import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.ContextThemeWrapper;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;

import com.example.admin.myapplication.R;


public class BaseActivity extends FragmentActivity {

//	private BroadcastReceiver mBroadcastReceiver;
//	public AppTitleBar mTitleBar;
	public AlertDialog loadingBulider;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// requestWindowFeature(Window.FEATURE_NO_TITLE);
//		if (mBroadcastReceiver == null) {
//			mBroadcastReceiver = new BroadcastReceiver() {
//				@Override
//				public void onReceive(Context context, Intent intent) {
//					finish();
//				}
//			};
//			IntentFilter filter = new IntentFilter();
//			filter.addAction(getApplicationContext().getPackageName()
//					+ Constant.ACTION_EXIT_SYSTEM);
//			this.registerReceiver(mBroadcastReceiver, filter);
//		}
		// ActionBar actionBar = getSupportActionBar();
		// if (actionBar != null) {
		// actionBar.hide();
		// actionBar.setDisplayHomeAsUpEnabled(true);
		// actionBar.setDisplayShowHomeEnabled(false);
		// actionBar.setDisplayShowCustomEnabled(false);
		// }
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
//		if (mBroadcastReceiver != null) {
//			this.unregisterReceiver(mBroadcastReceiver);
//		}
	}

	public void showLoadinDialog() {
		if (null == loadingBulider) {
			View view = LayoutInflater.from(this).inflate(
					R.layout.layout_widget_loading, null);
			AlertDialog.Builder builder = new AlertDialog.Builder(
					new ContextThemeWrapper(this, R.style.myLoadingTheme));
			loadingBulider = builder.create();
			loadingBulider.setCancelable(false);
			loadingBulider.setView(view, 0, 0, 0, 0);
			loadingBulider
					.setOnKeyListener(new DialogInterface.OnKeyListener() {

						@Override
						public boolean onKey(DialogInterface dialog,
								int keyCode, KeyEvent event) {
							// TODO Auto-generated method stub
							if (keyCode == KeyEvent.KEYCODE_BACK) {
								closeLoadinDialog();
							}
							return false;
						}
					});
			loadingBulider.setCanceledOnTouchOutside(false);
		}
		if (!loadingBulider.isShowing()) {
			loadingBulider.show();
		}
	}

	public void closeLoadinDialog() {
		if (!isFinishing()) {
			if (null != loadingBulider) {
				if (loadingBulider.isShowing()) {
					loadingBulider.dismiss();
				}
			}
		}
	}

	@Override
	public void onBackPressed() {
		super.onBackPressed();
		this.finish();
	}

	public void showActivity(Class<?> classz, boolean finishSelf) {
		startActivity(new Intent(this, classz));
		if (finishSelf) {
			this.finish();
		}
	}

	public void showActivity(Intent intent, boolean finishSelf) {
		startActivity(intent);
		if (finishSelf) {
			this.finish();
		}
	}
}
