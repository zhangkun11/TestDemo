package com.example.admin.myapplication.scan;


import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.jb.Preference;
import android.jb.barcode.BarcodeManager;
import android.jb.utils.Tools;
import android.jb.utils.WakeLockUtil;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.androidquery.AQuery;
import com.example.admin.myapplication.MyApplication;
import com.example.admin.myapplication.R;

import java.io.UnsupportedEncodingException;



public class ScanActivity extends BaseActivity implements ScanListener {
	private int Scan_Mode = -1;
	private int scan_time_limit = 100;
	AQuery aQuery = null;
	boolean bind = false;
	private Intent service;
	private ScanService scanService;
	private BarcodeManager scanManager;
	boolean isClick = false;
	WakeLockUtil mWakeLockUtil = null;
	private ScanListAdapter adapter;
	private ListView lv;
	private TextView code_type;
	private String codeType;
	private String context;
	private long nowTime = 0;
	private long lastTime = 0;
	private CheckBox continueCb;
	private ScanThread scanThread;
	public static boolean isContinues = false;;
	public long intervalTime = 0;
	private EditTextDialog etD;

	private Handler mHandler = new Handler() {
		public void handleMessage(android.os.Message msg) {
			switch (msg.what) {
			case 1000:
				mWakeLockUtil.lock();// 保持屏幕唤醒
				break;
			case 1001:
				mWakeLockUtil.unLock();// 屏幕不保持唤醒
				break;
			case 8://
				if (isContinues) {
					// mHandler.removeCallbacks(cutDownReadSleep);
					// mHandler.postDelayed(cutDownReadSleep, intervalTime);
				} else {
					lastTime = System.currentTimeMillis();
				}
				code_type.setText("");
				if (context != null) {
					if (adapter != null) {
						adapter.addStr(context);
					}
				}
				if (codeType != null) {
					code_type.setText(codeType);

					showDialog();
				}
				if(codeType==null){
					MyApplication.getSession().set("scan",false);
				}
				if (adapter != null) {
					adapter.notifyDataSetChanged();
					lv.setSelection(adapter.getCount() - 1);
				}

				break;
			case 5:// 无协议数据
				code_type.setText("");
				String data3 = (String) msg.obj;
				if (data3 != null) {
					if (adapter != null) {
						adapter.addStr(data3);
					}
				}
				if (adapter != null) {
					adapter.notifyDataSetChanged();
					lv.setSelection(adapter.getCount() - 1);
				}

				break;

			default:
				break;
			}
		};
	};

	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
		if (scanService != null)
			scanService.setActivityUp(false);
		ScanService.isScanActivityUp = true;
	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		if (scanService != null)
			scanService.setActivityUp(true);
		ScanService.isScanActivityUp = false;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		/*getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);*/
		setContentView(R.layout.act_scan);
		Toast.makeText(ScanActivity.this,"一维条码扫描测试",Toast.LENGTH_SHORT).show();
		showLoadinDialog();
		mHandler.postDelayed(closeLodingIcon, 3000);

		code_type = (TextView) findViewById(R.id.scan_code_type_tv);
		ArrayAdapter<String> deviceAdapter = new ArrayAdapter<String>(this,
				android.R.layout.simple_spinner_item, getResources()
						.getStringArray(R.array.ScanDeviceType));
		//continueCb = (CheckBox) findViewById(R.id.scan_continue_cb);
		isContinues = false;
		/*continueCb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

			@Override
			public void onCheckedChanged(CompoundButton buttonView,
					boolean isChecked) {
				// TODO Auto-generated method stub
				if (isChecked) {
					String s = getString(R.string.scan_continuse_scan_set);
					s = String.format(s, ""+scan_time_limit);
					aQuery.id(R.id.scanButton).enabled(false);
					etD = new EditTextDialog(ScanActivity.this,
							s,
							getString(R.string.scan_continue_start_scan),
							getString(R.string.cancel),
							new EditTextDialog.ClickListener() {

								@Override
								public boolean onBtn2Click(View v, String etStr) {
									// TODO Auto-generated method stub
									aQuery.id(R.id.scanButton).enabled(true);
									isContinues = false;
									etD.dismiss();
									continueCb.setChecked(false);
									return true;
								}

								@Override
								public boolean onBtn1Click(View v, String etStr) {
									// TODO Auto-generated method stub
									isContinues = true;
									if (scanThread != null) {
										scanThread.interrupt();
										scanThread.run = false;
									}
									scanThread = new ScanThread();
									scanThread.run = true;
									long time = 3000;
									try {
										time = Long.parseLong(etStr);
										if (time < scan_time_limit) {
											time = scan_time_limit;
										}
									} catch (NumberFormatException e) {
										// TODO: handle exception
										e.printStackTrace();
										time = scan_time_limit;
									}
									intervalTime = time;
									if (scanService != null) {
										// 出光状态
										if (null != scanManager) {
											// && scanManager.isScan()) {
											System.out
													.println("ScanActivity Barcode_Stop8");
											scanManager.Barcode_Stop();
											mHandler.sendEmptyMessage(1001);
										}
									}
									scanThread.start();
									etD.dismiss();
									return true;
								}
							});
					etD.show();
				} else {
					if (scanThread != null) {
						scanThread.interrupt();
						scanThread.run = false;
					}
					isContinues = false;
					aQuery.id(R.id.scanButton).enabled(true);
				}
			}
		});*/
		deviceAdapter
				.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

		service = new Intent(this, ScanService.class);
		startService(service);
		bindService(service, serviceConnection, BIND_AUTO_CREATE);
		mWakeLockUtil = new WakeLockUtil(this);

		aQuery = new AQuery(this);

		aQuery.id(R.id.scanButton).clicked(new View.OnClickListener() {

			@Override
			public void onClick(View v) {

				if (scanService != null) {

					v.setEnabled(false);
					nowTime = System.currentTimeMillis();

					// if (Preference.getScannerModel(ScanActivity.this) ==
					// BarcodeManager.MODEL_EM3095) {
					// scan_time_limit = 300;
					// } else {
					scan_time_limit = 100;
					// }
					if (nowTime - lastTime > scan_time_limit) {
						// if (scanManager.isScan()) {
						System.out.println("ScanActivity Barcode_Stop9");
						scanManager.Barcode_Stop();
						// }
						System.out.println("scan(0)");
						scanManager.Barcode_Start();

						mWakeLockUtil.lock();// 保持屏幕唤醒
						lastTime = nowTime;
					}

					// if (!scanService.isClick()) {// 如果已处于关闭状态，则开启
					// System.out.println("scan(0)");
					// scanService.scan("0");
					//
					// mWakeLockUtil.lock();// 保持屏幕唤醒
					//
					// } else {// 如果已处于开启状态，则关闭
					// System.out.println("scan(1)");
					// scanService.scan("1");
					//
					// mWakeLockUtil.unLock();// 屏幕不保持唤醒
					//
					// }
					v.setEnabled(true);

				} else {

					Toast.makeText(ScanActivity.this, "Failure!!!",
							Toast.LENGTH_SHORT).show();

				}

			}
		});
		/*aQuery.id(R.id.scan_set_btn).clicked(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				Intent intent = new Intent(ScanActivity.this,
						ScanSetupActivity.class);
				startActivity(intent);
			}
		});*/
		adapter = new ScanListAdapter(this);
		lv = (ListView) findViewById(R.id.scan_lv);
		lv.setAdapter(adapter);
		Bundle bundle = getIntent().getExtras();
		if (bundle != null) {
			String model = bundle.getString("Model");
			if (model != null) {
				if (model.equals("TwoModel")) {
					Preference.setScanDeviceType(this, ScanDeviceType.TwoD);
				} else if (model.equals("oneModel")) {
					Preference.setScanDeviceType(this, ScanDeviceType.OneD);
				}
			}
		}
	}

	ServiceConnection serviceConnection = new ServiceConnection() {

		@Override
		public void onServiceDisconnected(ComponentName name) {
			bind = false;
		}

		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			bind = true;
			ScanService.MyBinder myBinder = (ScanService.MyBinder) service;
			scanService = myBinder.getService();
			//
			scanManager = scanService.getScanManager();

			scanService.setOnScanListener(ScanActivity.this);
			scanService.setActivityUp(true);
		}
	};

	public void onClick(View view) {
		switch (view.getId()) {
		case R.id.scan_clean_btn:
			System.out.println("onClick");
			if (adapter != null)
				adapter.cleanData();
			adapter.notifyDataSetChanged();
			break;

		default:
			break;
		}
		// aQuery.id(R.id.dataView).text("");

	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		//MyApplication.getSession().set("scan",true);
		if(MyApplication.getSession().getBoolean("scan")!=true){
			MyApplication.getSession().set("scan",false);
		}
		if (scanService != null) {
			if (null != scanManager) {
				System.out.println("ScanActivity Barcode_Stop5");
				scanManager.Barcode_Stop();
			}
		}

		if (bind) {
			unbindService(serviceConnection);
			if (!Preference.getScanSelfopenSupport(this, true)) {
				this.stopService(service);
			}
		}

		mWakeLockUtil.unLock();//
		if (scanThread != null) {
			scanThread.run = false;
			isContinues = false;
			notifyReader();
		}

	}

	@Override
	public void result(String content) {
		// TODO Auto-generated method stub
		// mHandler.sendMessage(mHandler.obtainMessage(0, data));
		mHandler.sendMessage(mHandler.obtainMessage(5, content));
	}

	public String createString(byte[] byteData) {
		String data = null;
		try {
			String encoding = Tools.returnType(byteData);
			encoding = encoding.toUpperCase().equals("WINDOWS-1252") ? "GB2312"
					: encoding;

			data = new String(byteData, encoding);
			return data;

		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
			return "";
		}
	}

	@Override
	public void henResult(String codeType, String context) {
		// TODO Auto-generated method stub
		this.codeType = codeType;
		this.context = context;
		mHandler.sendEmptyMessage(8);
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		// TODO Auto-generated method stub
		if (keyCode == 82) {
			//startActivity(new Intent(this, ScanSetupActivity.class));
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}

	// private class ScanThread extends Thread {
	// public boolean run;
	// public long time = 0;
	//
	// @Override
	// public void run() {
	// // TODO Auto-generated method stub
	// while (run) {
	// if (scanService != null && null != scanManager) {
	//
	// // if (!scanManager.isScan()) {// 如果已处于关闭状态，则开启
	// scanManager.Barcode_Start();
	// mHandler.sendEmptyMessage(1000);
	// try {
	// sleep(time);
	// } catch (InterruptedException e) {
	// // TODO Auto-generated catch block
	// e.printStackTrace();
	// }
	// if (scanService != null && null != scanManager){
	// // && scanManager.isScan()) {
	// scanManager.Barcode_Stop();
	// // mHandler.sendEmptyMessage(1001);
	// }
	// // }
	// }
	// }
	// if (scanService != null && null != scanManager){
	// // && scanManager.isScan()) {
	// System.out.println("scan(1)");
	// scanManager.Barcode_Stop();
	// isContinues = false;
	// mHandler.sendEmptyMessage(1001);
	// }
	// }
	// }

	private class ScanThread extends Thread {
		public boolean run;

		@Override
		public void run() {
			// TODO Auto-generated method stub
			while (run) {
				try {
					if (scanService != null && null != scanManager) {
						scanManager.Barcode_Start();
						mHandler.sendEmptyMessage(1000);
						sleep(intervalTime);
						if (scanService != null && null != scanManager) {
							System.out.println("ScanActivity Barcode_Stop6");
							scanManager.Barcode_Stop();
						}
					}
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					System.out.println("readThread InterruptedException");
					e.printStackTrace();
				}
			}
			if (scanService != null && null != scanManager) {
				// && scanManager.isScan()) {
				System.out.println("ScanActivity Barcode_Stop7");
				scanManager.Barcode_Stop();
				isContinues = false;
				mHandler.sendEmptyMessage(1001);
			}
		}
	}

	private void notifyReader() {
		if (scanThread != null && scanThread.isAlive()) {
			scanThread.interrupt();
		}
	}

	// private Runnable cutDownReadSleep = new Runnable() {
	// @Override
	// public void run() {
	// // TODO Auto-generated method stub
	// notifyReader();
	// }
	// };

	private Runnable closeLodingIcon = new Runnable() {

		@Override
		public void run() {
			// TODO Auto-generated method stub
			Scan_Mode = Preference.getScannerModel(ScanActivity.this);
			if (Scan_Mode == BarcodeManager.MODEL_N3680) {
				scan_time_limit = 500;
			} else {
				scan_time_limit = 100;
			}
			closeLoadinDialog();
		}
	};
	private void showDialog(){
		AlertDialog.Builder dialog = new AlertDialog.Builder(this);
		dialog.setMessage("检测成功，是否确认完成该项检测");
		dialog.setPositiveButton("是",
				new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface arg0, int arg1) {

						MyApplication.getSession().set("scan",true);

						finish();

					}
				});
		dialog.setNeutralButton("否", new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface arg0, int arg1) {
                MyApplication.getSession().set("scan",true);
				arg0.dismiss();

			}
		});
		dialog.show();
	}
}
