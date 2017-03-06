package com.example.admin.myapplication.meter;


import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.jb.meter.MeterController;
import android.jb.simpleic.SimpleIcController;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.example.admin.myapplication.R;
import com.example.admin.myapplication.utils.Tools;
import com.example.admin.myapplication.utils.WakeLockUtil;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class MeterActivity extends Activity implements View.OnClickListener {

	private final int Pro_Idle = 0;
	private final int Pro_One = 1;
	private final int Pro_Two = 2;

	// 视图
	private CheckBox continueCb;
	private EditText bdz;
	private Button send;
	private TextView jiegou;
	private Spinner sp9701, sp9702, sp9703, sp0701, sp0702, sp0703;
	private TextView recevier_data;
	private EditTextDialog etD;
	// 变量
	private String resultData;
	private String bz = "", bdzString;
	private int caobiao;
	private boolean isRead = false;
	// public static boolean T485;
	public static boolean isContinues = false;;
	private byte[] T485bytes;
	private String changedString;
	private int curTime, preTime;
	private int caobiao97Or07;
	private ReadThread readThread;
	private myListAdapter adapter;
	private ListView lv;
	// 控制
	public Message message;
	private File file;
	private FileOutputStream out;
	private MeterController controler;
	private WakeLockUtil mWakeLockUtil = null;
	private byte[] sendBuffer;
	private boolean IntervalOrNot = false;
	private byte[] one = null;
	private int currentPro = Pro_Idle;
	private SimpleIcController pSamCon;

	Handler mHandler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub

			super.handleMessage(msg);
			switch (msg.what) {
			case 1000:
				resultData = (String) msg.obj;
				// Log.i("info", "msg.obj == " + resultData);
				// jiegou.setText(resultData);
				if (!Tools.isEmpty(resultData)) {
					if (adapter != null) {
						adapter.addStr(resultData);
					}
				}
				if (adapter != null) {
					adapter.notifyDataSetChanged();
					lv.setSelection(adapter.getCount() - 1);
				}
				controler.Meter_Close();
				resetEsam();
				getRand();
				break;

			case 1001:
				resultData = (String) msg.obj;
				Toast.makeText(MeterActivity.this, resultData,
						Toast.LENGTH_SHORT).show();
				break;
			default:
				break;
			}

		}

	};

	private void initSimpleIc()
	{
		pSamCon = SimpleIcController.getInstance();

		try {
			pSamCon.Simpleic_Open(this);
			//pSamCon.Simpleic_Reset();

		} catch (SecurityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private void resetEsam()
	{
		if (pSamCon != null) {
			try {
				if (pSamCon.Simpleic_Reset()) {
					Toast.makeText(this, getString(R.string.reset_success), Toast.LENGTH_LONG).show();
					
				} else {
					Toast.makeText(this, getString(R.string.reset_failed), Toast.LENGTH_LONG).show();
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	private void getRand()
	{
		if (pSamCon != null) {
			try {
				byte[] result = pSamCon.Simpleic_Write(new byte[] { (byte) 0x00,
						(byte) 0x84, (byte) 0x00, (byte) 0x00, (byte) 0x04});
				if (result != null) {
					Toast.makeText(this, bytesToHexString(result), Toast.LENGTH_LONG).show();
				} else {
					Toast.makeText(this, getString(R.string.reset_failed), Toast.LENGTH_LONG).show();
				}
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		}
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_meter);
		mWakeLockUtil = new WakeLockUtil(this);
		init();
		initSimpleIc();



		send.setOnClickListener(this);

		/*ArrayAdapter<String> adpter9701 = new ArrayAdapter<String>(this,
				android.R.layout.simple_spinner_item, getResources()
						.getStringArray(R.array.DNL97));
		ArrayAdapter<String> adpter9702 = new ArrayAdapter<String>(this,
				android.R.layout.simple_spinner_item, getResources()
						.getStringArray(R.array.ZDX97));
		ArrayAdapter<String> adpter9703 = new ArrayAdapter<String>(this,
				android.R.layout.simple_spinner_item, getResources()
						.getStringArray(R.array.DC97));
		ArrayAdapter<String> adpter0701 = new ArrayAdapter<String>(this,
				android.R.layout.simple_spinner_item, getResources()
						.getStringArray(R.array.DNL07));
		ArrayAdapter<String> adpter0702 = new ArrayAdapter<String>(this,
				android.R.layout.simple_spinner_item, getResources()
						.getStringArray(R.array.ZDX07));
		ArrayAdapter<String> adpter0703 = new ArrayAdapter<String>(this,
				android.R.layout.simple_spinner_item, getResources()
						.getStringArray(R.array.DC07));
		sp9701.setAdapter(adpter9701);
		sp9702.setAdapter(adpter9702);
		sp9703.setAdapter(adpter9703);
		sp0701.setAdapter(adpter0701);
		sp0702.setAdapter(adpter0702);
		sp0703.setAdapter(adpter0703);

		sp9701.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

			@Override
			public void onItemSelected(AdapterView<?> arg0, View arg1,
									   int arg2, long arg3) {
				if (arg2 == 0) {
					return;
				}
				sp9702.setSelection(0);
				sp9703.setSelection(0);
				sp0701.setSelection(0);
				sp0702.setSelection(0);
				sp0703.setSelection(0);
				sp9701.setSelection(arg2);
				if (controler.Meter_GetType()) {
					controler.Meter_Close();
					changeTo485(97);
				}
				bz = (getResources().getStringArray(R.array.DNL97)[arg2]
						.toString().trim().substring(0, 4));
				Log.i("info", "onItemSelected: "+bz);
				jiegou.setText("");
			}

			@Override
			public void onNothingSelected(AdapterView<?> arg0) {
				// TODO Auto-generated method stub

			}
		});
		sp9702.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

			@Override
			public void onItemSelected(AdapterView<?> arg0, View arg1,
									   int arg2, long arg3) {
				if (arg2 == 0) {
					return;
				}
				sp9701.setSelection(0);
				sp9703.setSelection(0);
				sp0701.setSelection(0);
				sp0702.setSelection(0);
				sp0703.setSelection(0);
				sp9702.setSelection(arg2);
				if (controler.Meter_GetType()) {
					controler.Meter_Close();
					changeTo485(97);
				}
				bz = (getResources().getStringArray(R.array.ZDX97)[arg2]
						.toString().trim().substring(0, 4));
				jiegou.setText("");
			}

			@Override
			public void onNothingSelected(AdapterView<?> arg0) {
				// TODO Auto-generated method stub

			}
		});
		sp9703.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

			@Override
			public void onItemSelected(AdapterView<?> arg0, View arg1,
									   int arg2, long arg3) {
				if (arg2 == 0) {
					return;
				}

				sp9701.setSelection(0);
				sp9702.setSelection(0);
				sp0701.setSelection(0);
				sp0702.setSelection(0);
				sp0703.setSelection(0);

				sp9703.setSelection(arg2);
				if (controler.Meter_GetType()) {
					controler.Meter_Close();
					changeTo485(97);
				}
				bz = (getResources().getStringArray(R.array.DC97)[arg2]
						.toString().trim().substring(0, 4));
				jiegou.setText("");
			}

			@Override
			public void onNothingSelected(AdapterView<?> arg0) {
				// TODO Auto-generated method stub

			}
		});
		sp0701.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

			@Override
			public void onItemSelected(AdapterView<?> arg0, View arg1,
									   int arg2, long arg3) {
				if (arg2 == 0) {
					return;
				}
				sp9701.setSelection(0);
				sp9703.setSelection(0);
				sp9702.setSelection(0);
				sp0702.setSelection(0);
				sp0703.setSelection(0);
				if (controler.Meter_GetType()) {
					controler.Meter_Close();
					changeTo485(07);
				}
				bz = (getResources().getStringArray(R.array.DNL07)[arg2]
						.toString().trim().substring(0, 8));
				Log.i("info", "onItemSelected: "+bz);
				jiegou.setText("");
			}

			@Override
			public void onNothingSelected(AdapterView<?> arg0) {
				// TODO Auto-generated method stub

			}
		});
		sp0702.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

			@Override
			public void onItemSelected(AdapterView<?> arg0, View arg1,
									   int arg2, long arg3) {
				if (arg2 == 0) {
					return;
				}
				sp9701.setSelection(0);
				sp9703.setSelection(0);
				sp9702.setSelection(0);
				sp0701.setSelection(0);
				sp0703.setSelection(0);
				if (controler.Meter_GetType()) {
					controler.Meter_Close();
					changeTo485(07);
				}
				bz = (getResources().getStringArray(R.array.ZDX07)[arg2]
						.toString().trim().substring(0, 8));
				jiegou.setText("");
			}

			@Override
			public void onNothingSelected(AdapterView<?> arg0) {
				// TODO Auto-generated method stub

			}
		});
		sp0703.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

			@Override
			public void onItemSelected(AdapterView<?> arg0, View arg1,
									   int arg2, long arg3) {
				if (arg2 == 0) {
					return;
				}
				sp9701.setSelection(0);
				sp9703.setSelection(0);
				sp9702.setSelection(0);
				sp0702.setSelection(0);
				sp0701.setSelection(0);
				if (controler.Meter_GetType()) {
					controler.Meter_Close();
					changeTo485(07);
				}
				bz = (getResources().getStringArray(R.array.DC07)[arg2]
						.toString().trim().substring(0, 8));
				jiegou.setText("");
			}

			@Override
			public void onNothingSelected(AdapterView<?> arg0) {
				// TODO Auto-generated method stub

			}
		});*/


		//continueCb = (CheckBox) findViewById(R.id.chaobiao_continue_cb);
		isContinues = false;
		/*continueCb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

			@Override
			public void onCheckedChanged(CompoundButton buttonView,
					boolean isChecked) {
				// TODO Auto-generated method stub
				if (isChecked) {

					send.setEnabled(false);
					etD = new EditTextDialog(MeterActivity.this,
							getString(R.string.chaobiao_continuse_set),
							getString(R.string.chaobiao_continue_start),
							getString(R.string.cancel),
							new EditTextDialog.ClickListener() {

								@Override
								public boolean onBtn2Click(View v, String etStr) {
									// TODO Auto-generated method stub
									send.setEnabled(true);
									isContinues = false;
									etD.dismiss();
									continueCb.setChecked(false);
									return true;
								}

								@Override
								public boolean onBtn1Click(View v, String etStr) {
									// TODO Auto-generated method stub
									isContinues = true;
									if (readThread != null) {
										readThread.interrupt();
										readThread.run = false;
									}
									readThread = new ReadThread();
									readThread.run = true;
									long time = 3000;
									try {
										time = Long.parseLong(etStr);
										if (time < 1000) {
											time = 1000;
										}
									} catch (NumberFormatException e) {
										// TODO: handle exception
										e.printStackTrace();
										time = 3000;
									}
									readThread.time = time;
									readThread.start();
									etD.dismiss();
									mWakeLockUtil.lock();// 保持屏幕唤醒
									return true;
								}
							});
					etD.show();
				} else {
					if (readThread != null) {
						readThread.interrupt();
						readThread.run = false;
					}
					mWakeLockUtil.unLock();// 保持屏幕唤醒
					isContinues = false;
					send.setEnabled(true);

				}
			}
		});*/

		adapter = new myListAdapter(this);
		lv = (ListView) findViewById(R.id.scan_lv);
		lv.setAdapter(adapter);
	}

	@Override
	protected void onStart() {
		// TODO Auto-generated method stub
		super.onStart();
		Log.e("CaoBiaoAct", "onStart");
	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		sendBroadcast(new Intent("ReleaseCom"));
		if (null != controler) {
			Log.i("info", "onResume: ------controler closed");
			controler.Meter_Close();
		}
		controler = MeterController.getInstance();
		if (caobiao == 0) {
			controler.Meter_Open(MeterController.METER_Infrared, 1200, 8, 'E',
					1, portData , this);
		} else {
			changeTo485(caobiao97Or07);
		}
		Log.e("CaoBiaoAct", "onResume");
	}

	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
		if (null != controler) {
			Log.i("info", "onPause:-------controler closed ");
			controler.Meter_Close();
		}
		sendBroadcast(new Intent("ReLoadCom"));
		Log.e("CaoBiaoAct", "onPause");
	}

	@Override
	protected void onStop() {
		// TODO Auto-generated method stub
		super.onStop();
		Log.e("CaoBiaoAct", "onStop");
	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();

		if (readThread != null) {
			readThread.interrupt();
			readThread.run = false;
			readThread = null;
		}
	}

	private void findView() {
		bdz = (EditText) findViewById(R.id.bdz);
		send = (Button) findViewById(R.id.caobiao_send);
		jiegou = (TextView) findViewById(R.id.caobiao_jiegou);
		/*sp9701 = (Spinner) findViewById(R.id.spinner_1_97);
		sp9702 = (Spinner) findViewById(R.id.spinner_2_97);
		sp9703 = (Spinner) findViewById(R.id.spinner_3_97);
		sp0701 = (Spinner) findViewById(R.id.spinner_1_07);
		sp0702 = (Spinner) findViewById(R.id.spinner_2_07);
		sp0703 = (Spinner) findViewById(R.id.spinner_3_07);*/
		//recevier_data = (TextView) findViewById(R.id.recevier_data);
	}

	@Override
	public void onClick(View arg0) {
		switch (arg0.getId()) {
		case R.id.caobiao_send:
			/**
			 * 必须打开红外控制器
			 */
			if (caobiao == 0) {
				controler.Meter_Open(MeterController.METER_Infrared, 1200, 8, 'E',
						1, portData , this);
			}

			Log.i("info", "onClick: send======"+controler.Meter_GetType()+"    closed");
			if (controler.Meter_GetType()) {
				Log.i("info", "onClick: send======"+controler.Meter_GetType()+"    closed");
				controler.Meter_Close();
				changeTo485(07);
			}
			bz = "00010000";

			jiegou.setText("");
			bdzString = bdz.getText().toString().trim();
			Log.i("info", "bdzString  = " + bdzString);
			if (bdzString.length() == 0 || bdzString.equals("")) {
				Toast.makeText(MeterActivity.this,
						getResources().getText(R.string.enter_address),
						Toast.LENGTH_SHORT).show();
				return;
			}
			if (bdzString.equals("")
					|| bdzString.substring(0, 1).equals("0123456789ABCDEF")) {
				Toast.makeText(
						MeterActivity.this,
						getResources().getText(
								R.string.Please_select_Infrared_order),
						Toast.LENGTH_SHORT).show();
				return;
			}
			Log.i("info", "bz == " + bz);
			if (bz.length() != 4 && bz.length() != 8) {
				Log.i("info", "bz.length = " + bz.toString().trim().length());
				Toast.makeText(
						MeterActivity.this,
						getResources().getText(
								R.string.Please_select_Infrared_order),
						Toast.LENGTH_SHORT).show();
				return;
			}
			if (bdzString.length() > 12) {
				Toast.makeText(
						MeterActivity.this,
						getResources().getText(
								R.string.address_length_cross_border),
						Toast.LENGTH_SHORT).show();
				return;
			}
			// controler.power_up(controler.getIs485());
			sendMscToDianbiao(bdzString, bz.toString().trim());

			break;
		}
	}

	private void init() {
		findView();
		Intent intent = getIntent();
		caobiao = intent.getBooleanExtra("IS485", false) ? 1 : 0;
		Log.i("info", "caobiao ==  " + caobiao);
	}

	public void changeTo485(int is07) {
		Log.i("info", "切换到485");

		if (is07 == 97) {
			controler.Meter_Open(MeterController.METER_485, 1200, 8, 'E', 1,
					portData , this);
			caobiao97Or07 = 97;
			Log.v("changeTo485", "is07 == 97");
		} else {
			controler.Meter_Open(MeterController.METER_485, 2400, 8, 'E', 1,
					portData , this);
			caobiao97Or07 = 07;
			Log.v("changeTo485", "is07 == 07");
		}
	}

	public void changeTohongwai(int is07) {
		Log.i("info", "切换到红外");
		if (is07 == 97) {
			controler.Meter_Open(MeterController.METER_Infrared, 1200, 8, 'E',
					1, portData , this);
			caobiao97Or07 = 97;
			Log.v("changeTochongwai", "is07 == 97");
		} else {
			controler.Meter_Open(MeterController.METER_Infrared, 1200, 8, 'E',
					1, portData ,this);
			caobiao97Or07 = 07;
			Log.v("changeTohongwai", "is07 == 07");
		}
	}

	MeterController.Callback portData = new MeterController.Callback() {

		@Override
		public void Meter_Read(byte[] buffer, int size) {
			// TODO Auto-generated method stub
			if (null != controler) {
				SerialPortData serialPortData = new SerialPortData(buffer, size);
				if (controler.Meter_GetType()) {
					dealData(serialPortData);
				} else {
					if (IntervalOrNot) {
						dealData(serialPortData);
					} else {
						intervalDoRead(serialPortData);// 数据间有间隔
					}
				}
			}
		}

	};

	private void intervalDoRead(SerialPortData serialPortData) {
		if (serialPortData.getSize() > 0) {
			switch (currentPro) {
			case Pro_Idle:

				break;
			case Pro_One:
				one = serialPortData.getDataByte();
				dealData(serialPortData);
				currentPro = Pro_Two;
				break;
			case Pro_Two:
				if (one != null) {
					byte[] temp = new byte[one.length
							+ serialPortData.getSize()];
					System.arraycopy(one, 0, temp, 0, one.length);
					System.arraycopy(serialPortData.getDataByte(), 0, temp,
							one.length, serialPortData.getSize());
					byte[] availableData = new byte[temp.length
							- sendBuffer.length];
					System.arraycopy(temp, sendBuffer.length, availableData, 0,
							availableData.length);
					System.out.println("avaliable_data:"
							+ Tools.bytesToHexString(availableData));
					SerialPortData data = new SerialPortData(availableData,
							availableData.length);

					dealData(data);
				}
				currentPro = Pro_Idle;

				break;
			default:
				break;
			}

		}
	}

	public static String getMsg2(byte[] bytes, Context context) {
		// //接收数据帧
		byte[] recvBuffer = bytes;
		byte[] dataBuffer = new byte[10];
		int x = 0;
		int i = 0;

		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			// TODO 自动生成的 catch 块
			e.printStackTrace();
		}
		Log.i("info", "recvBuffer.length == " + recvBuffer.length);
		for (i = 0; i < recvBuffer.length; i++) {
			if (recvBuffer[i] == 0x16) // 判断接收帧的起始位置
			{
				for (int q = 0; q < recvBuffer.length; q++) {
					Log.i("info", "recvbuffer" + q + " = "
							+ bytesToHexString1234(recvBuffer[q]));
				}
				x = i + 1;
				break;
			}
		}
		if (i >= recvBuffer.length) {
			Log.i("info", "错误：没有接收到数据帧！");
			recvBuffer = null;
			dataBuffer = null;
			return null;
		}

		if (recvBuffer[x] == '\0') {
			// // sp.DiscardInBuffer();//清除缓冲区
			// recvBuffer = null;
			Log.i("info", "2");
			Toast.makeText(context, "错误：没有接收到数据帧！", Toast.LENGTH_SHORT).show();
			Log.i("info", "---2----错误：没有接收到数据帧！");
			return null;
		}
		// 判断控制码
		// 97协议为 0x81
		// 07协议为 0x91
		if (recvBuffer[x + 8] == 0x81 || recvBuffer[x + 6] == 0x91) {
			Log.i("info", "接收到了 电表数据！");
		} else {
			// sp.DiscardInBuffer();//清除缓冲区
			Log.i("info", "---3----错误：没有接收到数据帧！");
			// return getResources().getText(R.string.not_received_data) + "";
		}
		// 97协议： 2 + 4 = 6
		// 07协议： 4 + 4 = 8
		int dataL = recvBuffer[x + 9];

		// 计算校验位，并进行对比
		int SumMod = 0;
		for (int y = x; y <= x + 9 + dataL; y++) {
			SumMod += recvBuffer[y];
		}
		if (recvBuffer[x + 10 + dataL] != (SumMod % 256)) {
			// 清除缓冲区
			recvBuffer = null;
			dataBuffer = null;
			return null;

		}

		// 判断结束位
		if (recvBuffer[x + 11 + dataL] != 0x16) {
			recvBuffer = null;
			dataBuffer = null;
			return null;
		}

		// 数据标识的长度
		int iData = 0;
		if (recvBuffer[x + 8] == 0x81) {
			iData = 2;
		} else if (recvBuffer[x + 8] == 0x91) {
			iData = 4;
		} else {
			recvBuffer = null;
			dataBuffer = null;
			return null;
		}
		// 保存数据域中对电表数据
		byte[] dClearData = new byte[dataL - iData];

		// 数据解析
		String buf = "";
		int mm = 0;
		for (mm = x + 10 + iData, i = 0; mm < x + 9 + dataL; mm++, i++) {
			dClearData[i] = (byte) (recvBuffer[mm] - 0x33);
		}
		for (; i >= 0; i--) {
			buf += Tools.byteToString(dClearData[i]);
			Log.i("info", "buf == " + buf);
		}
		recvBuffer = null;

		dataBuffer = null;
		return buf;
	}

	public String getMsg(byte[] bytes, Context context) {
		// //接收数据帧
		String buf = "";
		try {
			byte[] recvBuffer = bytes;
			byte[] recvBuffer2 = new byte[256];// 2014/7.17
			// byte[] dataBuffer = new byte[10];
			// int type = 0;
			int x = 0;
			int i = 0;
			String bzString = bz.toString().trim();

			// 2014/7/17判断接收帧中 第一个 68 的位置
			for (i = 0; i < recvBuffer.length; i++) {
				if (recvBuffer[i] == (byte) 0x68) {
					x = i;

					// 判断控制码
					// 97协议为 0x81
					// 07协议为 0x91

					if ((x + 8) < recvBuffer.length) {
						if (recvBuffer[x + 8] == (byte) 0x81
								|| recvBuffer[x + 8] == (byte) 0x91) {
							Log.i("info", "接收到了 电表数据！");
							// 数据域长度：有数据时长度为 2+4；无数据时长度为：2
							// 97协议： 2 + 4 = 6
							// 07协议： 4 + 4 = 8
							if ((x + 9) < recvBuffer.length) {
								int dataL = recvBuffer[x + 9];

								if (x + 11 + dataL < recvBuffer.length) {
									if (recvBuffer[x + 11 + dataL] != (byte) 0x16) {
										// recvBuffer = null;

										Log.i("info", "---5----错误：没有接收到数据帧！");
										// return
										// getResources().getText(R.string.not_received_data)
										// + "";
										// return "";
										continue;
									}

									// 数据标识的长度
									int iData = 0;
									if (recvBuffer[x + 8] == (byte) 0x81) {
										iData = 2;
									} else if (recvBuffer[x + 8] == (byte) 0x91) {
										iData = 4;
									} else {

										Log.i("info", "---6----错误：没有接收到数据帧！");
										// return
										// getResources().getText(R.string.not_received_data)
										// + "";
										// return "";
										continue;
									}
									// 保存数据域中对电表数据
									byte[] dClearData = new byte[dataL - iData];
									Log.i("info", "对比数组长度 == "
											+ dClearData.length);
									// 数据解析
									int mm = 0;

									for (mm = x + 10 + iData, i = 0; mm <= x
											+ 9 + dataL; mm++, i++) {
										// Log.i("info",
										// "re mm = "+recvBuffer[mm]);
										dClearData[i] = (byte) (recvBuffer[mm] - 0x33);
									}

									// for(i--;i>=0;i--){
									// buf += Tools.byteToString(dClearData[i]);
									// }
									for (int j = dClearData.length - 1; j >= 0; j--) {
										buf += Tools
												.byteToString(dClearData[j]);

									}

									if (bz.length() == 4) {
										if (bz.substring(0, 1).equals("9")) {

											buf = buf.substring(0, 6) + "."
													+ buf.substring(6, 8);
											float f = Float.valueOf(buf);
											String bufer = String.valueOf(f);
											return bufer;
										} else if (bz.endsWith("C010")
												|| bz.endsWith("c010")) {
											buf = buf.substring(0, 2) + "-"
													+ buf.substring(2, 4) + "-"
													+ buf.substring(4, 6);
											return buf;
										} else if (bz.substring(0, 1).endsWith(
												"A")
												|| bz.substring(0, 1).endsWith(
														"a")) {
											buf = buf.subSequence(0, 2) + "."
													+ buf.subSequence(2, 6);
											float f = Float.valueOf(buf);
											String bufer = String.valueOf(f);
											return bufer;
										} else if (bz.equals("C011")
												|| bz.equals("c011")) {
											buf = buf.substring(0, 2) + ":"
													+ buf.subSequence(2, 4)
													+ ":" + buf.substring(4, 6);
											return buf;
										} else if (bz.substring(0, 2).equals(
												"c1")
												|| bz.substring(0, 2).equals(
														"C1")) {
											buf = buf.substring(0, 2);
											return buf;
										}
									} else if (bzString.length() == 8) {

										if (bzString.substring(0, 2).equals(
												"01")) {
											Log.i("info", "buf === " + buf);
											String str = buf.substring(10, 12)
													+ "."
													+ buf.substring(12,
															buf.length());

											float f = Float.valueOf(str);
											String bufer = String.valueOf(f)
													+ "\n"
													+ getResources()
															.getString(
																	R.string.Generated_time)
													+ buf.substring(0, 2) + "-"
													+ buf.substring(2, 4) + "-"
													+ buf.substring(4, 6);
											return bufer;
										} else if (bzString.equals("04000101")) {
											buf = buf.substring(0, 2) + "-"
													+ buf.substring(2, 4) + "-"
													+ buf.substring(4, 6);
											return buf;
										} else if (bzString.equals("04000102")) {
											buf = buf.substring(0, 2) + ":"
													+ buf.subSequence(2, 4)
													+ ":" + buf.substring(4, 6);
											return buf;
										} else if (bzString.equals("04000103")
												|| bzString.equals("04000104")) {
											buf = buf.substring(0, 2);
											return buf;
										} else if (bzString.substring(0, 2)
												.equals("00")) {
											Log.i("info", "00kai");
											buf = buf.substring(0, 6) + "."
													+ buf.substring(6, 8);
											float f = Float.valueOf(buf);
											String bufer = String.valueOf(f);
											return bufer;
										}
									}
									Log.i("info", "buf ---- " + buf);
									try {
										Thread.sleep(100);
									} catch (InterruptedException e) {
										// TODO 自动生成的 catch 块
										e.printStackTrace();
									}
								}
							} else {
								continue;
							}

						} else {

							// recvBuffer=null;//2014/7/17
							Log.i("info", "---3----错误：没有接收到数据帧！");
							// return
							// getResources().getText(R.string.not_received_data)
							// +
							// "";
							// return "";
							continue;
						}
					} else {
						continue;
					}
					// break;
				}
			}
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
		return buf.toString();
	}

	public String getMsgT485(byte[] bytes, Context context) {
		// //接收数据帧
		byte[] recvBuffer = bytes;
		// byte[] dataBuffer = new byte[10];
		// int type = 0;
		int x = 0;
		int i = 0;
		String bzString = bz.toString().trim();

		// //判断控制码
		// //97协议为 0x81
		// //07协议为 0x91

		if (recvBuffer[x + 12] == (byte) 0x81
				|| recvBuffer[x + 12] == (byte) 0x91) {
			Log.i("info", "接收到了 电表数据！");

		} else {

			Log.i("info", "---3----错误：没有接收到数据帧！");
			// return getResources().getText(R.string.not_received_data) + "";
			return "";
		}

		// 数据域长度：有数据时长度为 2+4；无数据时长度为：2
		// 97协议： 2 + 4 = 6
		// 07协议： 4 + 4 = 8
		int dataL = recvBuffer[x + 13];
		// Log.i("info", "dataL == "+dataL);
		// if(bzString.length() == 8&& bzString.substring(0, 3).equals("010")){
		// // dataL = recvBuffer[8];
		// }
		// 判断结束位

		if (recvBuffer[recvBuffer.length - 1] != 0x16) {
			// recvBuffer = null;

			Log.i("info", "---5----错误：没有接收到数据帧！");
			// return getResources().getText(R.string.not_received_data) + "";
			return "";
		}

		// 数据标识的长度
		int iData = 0;
		if (recvBuffer[x + 12] == (byte) 0x81) {
			iData = 2;
		} else if (recvBuffer[x + 12] == (byte) 0x91) {
			iData = 4;
		} else {

			Log.i("info", "---6----错误：没有接收到数据帧！");
			// return getResources().getText(R.string.not_received_data) + "";
			return "";
		}
		// 保存数据域中对电表数据
		byte[] dClearData = new byte[dataL - iData];
		Log.i("info", "对比数组长度 == " + dClearData.length);
		// 数据解析
		String buf = "";
		int mm = 0;

		for (mm = x + 38, i = 0; mm <= x + 41; mm++, i++) {
			// Log.i("info", "re mm = "+recvBuffer[mm]);
			dClearData[i] = (byte) (recvBuffer[mm] - 0x33);
		}

		for (int j = dClearData.length - 1; j >= 0; j--) {
			buf += Tools.byteToString(dClearData[j]);

		}

		if (bz.length() == 4) {
			if (bz.substring(0, 1).equals("9")) {

				buf = buf.substring(0, 6) + "." + buf.substring(6, 8);
				float f = Float.valueOf(buf);
				String bufer = String.valueOf(f);
				return bufer;
			} else if (bz.endsWith("C010") || bz.endsWith("c010")) {
				buf = buf.substring(0, 2) + "-" + buf.substring(2, 4) + "-"
						+ buf.substring(4, 6);
				return buf;
			} else if (bz.substring(0, 1).endsWith("A")
					|| bz.substring(0, 1).endsWith("a")) {
				buf = buf.subSequence(0, 2) + "." + buf.subSequence(2, 6);
				float f = Float.valueOf(buf);
				String bufer = String.valueOf(f);
				return bufer;
			} else if (bz.equals("C011") || bz.equals("c011")) {
				buf = buf.substring(0, 2) + ":" + buf.subSequence(2, 4) + ":"
						+ buf.substring(4, 6);
				return buf;
			} else if (bz.substring(0, 2).equals("c1")
					|| bz.substring(0, 2).equals("C1")) {
				buf = buf.substring(0, 2);
				return buf;
			}
		} else if (bzString.length() == 8) {

			if (bzString.substring(0, 2).equals("01")) {
				Log.i("info", "buf === " + buf);
				String str = buf.substring(10, 12) + "."
						+ buf.substring(12, buf.length());

				float f = Float.valueOf(str);
				String bufer = String.valueOf(f) + "\n"
						+ getResources().getString(R.string.Generated_time)
						+ buf.substring(0, 2) + "-" + buf.substring(2, 4) + "-"
						+ buf.substring(4, 6);
				return bufer;
			} else if (bzString.equals("04000101")) {
				buf = buf.substring(0, 2) + "-" + buf.substring(2, 4) + "-"
						+ buf.substring(4, 6);
				return buf;
			} else if (bzString.equals("04000102")) {
				buf = buf.substring(0, 2) + ":" + buf.subSequence(2, 4) + ":"
						+ buf.substring(4, 6);
				return buf;
			} else if (bzString.equals("04000103")
					|| bzString.equals("04000104")) {
				buf = buf.substring(0, 2);
				return buf;
			} else if (bzString.substring(0, 2).equals("00")) {
				Log.i("info", "00kai");
				buf = buf.substring(0, 6) + "." + buf.substring(6, 8);
				float f = Float.valueOf(buf);
				String bufer = String.valueOf(f);
				return bufer;
			}
		}
		Log.i("info", "buf ---- " + buf);
		return buf.toString();
	}

	public static String bytesToHexString(byte[] src) {
		StringBuilder stringBuilder = new StringBuilder("");
		if (src == null || src.length <= 0) {
			return null;
		}
		for (int i = 0; i < src.length; i++) {
			int v = src[i] & 0xFF;
			String hv = Integer.toHexString(v);
			if (hv.length() < 2) {
				stringBuilder.append(0);
			}
			stringBuilder.append(hv);
		}
		return stringBuilder.toString();
	}

	public static String bytesToHexString1234(byte src) {
		StringBuilder stringBuilder = new StringBuilder("");
		int v = src & 0xFF;
		String hv = Integer.toHexString(v);
		if (hv.length() < 2) {
			stringBuilder.append(0);
		}
		stringBuilder.append(hv);
		return stringBuilder.toString();
	}

	private class ReadThread extends Thread {
		public boolean run;
		public long time = 0;

		@Override
		public void run() {
			// TODO Auto-generated method stub
			while (run) {
				if (controler != null) {
					if (!isRead) {
						Message toastMessage = new Message();
						toastMessage.what = 1001;

						bdzString = bdz.getText().toString().trim();
						Log.i("info", "bdzString  = " + bdzString);
						if (bdzString.length() == 0 || bdzString.equals("")) {
							toastMessage.obj = getResources().getText(
									R.string.enter_address);
							mHandler.sendMessage(toastMessage);
							isRead = false;
							return;
						}
						if (bdzString.equals("")
								|| bdzString.substring(0, 1).equals(
										"0123456789ABCDEF")) {
							toastMessage.obj = getResources().getText(
									R.string.Please_select_Infrared_order);
							mHandler.sendMessage(toastMessage);
							isRead = false;
							return;
						}
						Log.i("info", "bz == " + bz);
						if (bz.length() != 4 && bz.length() != 8) {
							Log.i("info", "bz.length = "
									+ bz.toString().trim().length());
							toastMessage.obj = getResources().getText(
									R.string.Please_select_Infrared_order);
							mHandler.sendMessage(toastMessage);
							isRead = false;
							return;
						}
						if (bdzString.length() > 12) {
							toastMessage.obj = getResources().getText(
									R.string.address_length_cross_border);
							mHandler.sendMessage(toastMessage);
							isRead = false;
							return;
						}
						// controler.power_up(controler.getIs485());
						sendMscToDianbiao(bdzString, bz.toString().trim());
						isRead = true;
						mWakeLockUtil.lock();// 保持屏幕唤醒
						try {
							Thread.sleep(time);
						} catch (InterruptedException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
				}
			}
		}
	}

	public void sendMscToDianbiao(String bdz, String bz) {
		int sendL = 0;
		int i = 0;

		String StrCortrol = "";
		String StrLength = "";
		bdz = bdz.trim();
		bz = bz.trim();
		int bzLength = bz.length();
		if (bzLength == 4) // 97协议
		{
			sendL = 18;
			StrCortrol = "01";
			StrLength = "02";
		} else if (bzLength == 8) // 07协议
		{
			sendL = 20;
			StrCortrol = "11";
			StrLength = "04";
		} else {
			return;
		}
		sendBuffer = new byte[sendL];
		if (bdz.length() < 12) {
			bdz = (Tools.AddZero(12 - bdz.length()) + bdz).trim();
		} else if (bdz.length() > 12) {

		}
		sendBuffer[0] = (byte) 0xFE;
		sendBuffer[1] = (byte) 0xFE;
		sendBuffer[2] = (byte) 0xFE;
		sendBuffer[3] = (byte) 0xFE;
		sendBuffer[4] = 0x68;

		// 表地址
		sendBuffer[5] = Tools.hexString2Bytes(bdz.substring(10, 12))[0];
		sendBuffer[6] = Tools.hexString2Bytes(bdz.substring(8, 10))[0];
		sendBuffer[7] = Tools.hexString2Bytes(bdz.substring(6, 8))[0];
		sendBuffer[8] = Tools.hexString2Bytes(bdz.substring(4, 6))[0];
		sendBuffer[9] = Tools.hexString2Bytes(bdz.substring(2, 4))[0];
		sendBuffer[10] = Tools.hexString2Bytes(bdz.substring(0, 2))[0];
		Log.i("info",
				"表地址 == " + Tools.bytesToHexString1234(sendBuffer[5]) + "   "
						+ Tools.bytesToHexString1234(sendBuffer[6]) + "  "
						+ Tools.bytesToHexString1234(sendBuffer[7]) + "  "
						+ Tools.bytesToHexString1234(sendBuffer[8]) + "  "
						+ Tools.bytesToHexString1234(sendBuffer[9]) + "  "
						+ Tools.bytesToHexString1234(sendBuffer[10]));

		sendBuffer[11] = 0x68;

		// 控制码
		sendBuffer[12] = Tools.hexString2Bytes(StrCortrol)[0];

		// //数据域长度
		sendBuffer[13] = Tools.hexString2Bytes(StrLength)[0];

		for (i = 1; i <= Integer.parseInt(StrLength.substring(1, 2)); i++) {
			int j = 2 * (Integer.parseInt(StrLength.substring(1, 2)) - i);
			sendBuffer[13 + i] = (byte) (Tools.hexString2Bytes(bz.substring(j,
					j + 2))[0] + 0x33);
		}

		// 校验位
		int sumMod = 0;
		for (i = 4; i <= sendL - 3; i++) {
			sumMod += (int) sendBuffer[i];
		}
		sendBuffer[sendL - 2] = (byte) (sumMod % 256);

		// 结束符
		sendBuffer[sendL - 1] = 0x16;
		// 发送数据 设置延迟10毫秒
		// try {
		try {
			Thread.sleep(100);
		} catch (InterruptedException e) {
			// TODO 自动生成的 catch 块
			e.printStackTrace();
		}
		Log.i("info", "sendBuffer === " + Tools.bytesToHexString(sendBuffer));
		setCurrentPro(Pro_One);
		if (null != controler) {
			controler.writeCommand(sendBuffer);
		}
	}

	private void dealData(SerialPortData serialPortData) {
		byte[] b = serialPortData.getDataByte();
		Log.i("info", "serialPortData == " + bytesToHexString(b));
		curTime = (int) System.currentTimeMillis();
		if (controler.Meter_GetType()) {
			byte[] bytes = null;
			if (caobiao97Or07 == 97) {
				bytes = new byte[(sendBuffer.length) + b.length];

				bytes[sendBuffer.length - 1] = 0x16;

				for (int i = 0; i < sendBuffer.length; i++) {
					bytes[i] = sendBuffer[i];
				}
				for (int i = 0; i < b.length; i++) {
					bytes[sendBuffer.length + i] = b[i];
				}

				// bdz.append(bytesToHexString(bytes));

				Log.v("onReadSerialPortData", "bytes="
						+ bytesToHexString(bytes));
			}

			else if (caobiao97Or07 == 07) {
				bytes = new byte[sendBuffer.length + b.length - 4];

				bytes[sendBuffer.length - 1] = 0x16;

				for (int i = 0; i < sendBuffer.length; i++) {
					bytes[i] = sendBuffer[i];
				}
				for (int i = 0; i < (b.length - 4); i++) {
					bytes[sendBuffer.length + i] = b[4 + i];
				}
				// bdz.append(bytesToHexString(bytes));

				Log.v("onReadSerialPortData", "bytes="
						+ bytesToHexString(bytes));
			}
			String result = getMsg(b, MeterActivity.this).trim();
			if (result != null) {
				Log.i("info", "result data == " + result);
				message = new Message();
				message.obj = result;
				message.what = 1000;
				mHandler.sendMessage(message);
			}

		} else {
			Log.v("onReadSerialPortData", "bytes=" + bytesToHexString(b));
			String result = getMsg(b, MeterActivity.this).trim();
			if (result != null && result.length() > 0) {
				Log.i("info", "result data == " + result);
				message = new Message();
				message.obj = result;
				message.what = 1000;
				mHandler.sendMessage(message);
			}
		}
		isRead = false;
	}

	private void setCurrentPro(int pro) {
		this.currentPro = pro;
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
}
