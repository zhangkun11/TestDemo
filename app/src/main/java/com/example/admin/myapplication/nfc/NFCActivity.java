package com.example.admin.myapplication.nfc;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.jb.Preference;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.IsoDep;
import android.nfc.tech.MifareClassic;
import android.nfc.tech.MifareUltralight;
import android.nfc.tech.Ndef;
import android.nfc.tech.NdefFormatable;
import android.nfc.tech.NfcA;
import android.nfc.tech.NfcB;
import android.nfc.tech.NfcF;
import android.nfc.tech.NfcV;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;


import com.example.admin.myapplication.MyApplication;
import com.example.admin.myapplication.R;


import java.io.IOException;
import java.nio.ByteBuffer;

public class NFCActivity extends Activity {
	private NfcAdapter nfcAdapter;
	private Intent intent;
	private PendingIntent mPendingIntent;
	private boolean canWrite = true;
	private int cardType = CardType.unknow;
	private CheckBox cb_nfc_background_support;
	private TextView cardTypeTv, cardNumTv, cardInfoTv, dataReadTv;
	private Spinner blockS;
	private EditText keyEt, dataWriteEt;
	private RadioGroup rg_outmode;
	private RadioButton rd_scan_out_mode_1, rd_scan_out_mode_2;
	private static final String[] blocks = new String[] { "0", "1", "2" };
	private static final String LOG_TAG = NFCActivity.class.getSimpleName();
	private byte[] key = { (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff,
			(byte) 0xff, (byte) 0xff };
	private byte keyA[] = { (byte) 0xd3, (byte) 0xf7, (byte) 0xd3, (byte) 0xf7,
			(byte) 0xd3, (byte) 0xf7 };
	byte write[] = { (byte) 0x11, (byte) 0x11, (byte) 0x11, (byte) 0x11,
			(byte) 0x11, (byte) 0x11, (byte) 0x11, (byte) 0x11, (byte) 0x11,
			(byte) 0x11, (byte) 0x11, (byte) 0x11, (byte) 0x11, (byte) 0x11,
			(byte) 0x11, (byte) 0x11 };
	private boolean dialogEnable;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.nfc);
		dialogEnable=true;
        //Toast.makeText(NFCActivity.this,"NFC测试",Toast.LENGTH_SHORT).show();

		initView();

		nfcAdapter = NfcAdapter.getDefaultAdapter(this);
		if (nfcAdapter == null) {
			Toast.makeText(this, getString(R.string.nfc_device_not_support),
					Toast.LENGTH_SHORT).show();
			finish();
		}
		if (!nfcAdapter.isEnabled()) {
			AlertDialog dialog = new AlertDialog.Builder(this)
					.setMessage(getString(R.string.nfc_not_open_go_open))
					.setPositiveButton(getString(R.string.yes),
							new OnClickListener() {

								@Override
								public void onClick(DialogInterface dialog,
										int which) {
									// TODO Auto-generated method stub
									Intent intent = new Intent(
											"android.settings.NFC_SETTINGS");
									startActivityForResult(intent, 100);
								}
							}).create();
			dialog.show();
		}

		// BaseApplication.setNfcAdapter(nfcAdapter);
		// if (BaseApplication.isExternalStorageWritableErrorToast(this)) {
		// // Create keys directory.
		// File path = new File(Environment.getExternalStoragePublicDirectory(
		// BaseApplication.HOME_DIR) + "/" + BaseApplication.KEYS_DIR);
		// if (path.exists() == false && !path.mkdirs()) {
		// // Could not create directory.
		// Log.e(LOG_TAG, "Error while crating '" + BaseApplication.HOME_DIR
		// + "/" + BaseApplication.KEYS_DIR + "' directory.");
		// return;
		// }
		//
		// // Create dumps directory.
		// path = new File(Environment.getExternalStoragePublicDirectory(
		// BaseApplication.HOME_DIR) + "/" + BaseApplication.DUMPS_DIR);
		// if (path.exists() == false && !path.mkdirs()) {
		// // Could not create directory.
		// Log.e(LOG_TAG, "Error while crating '" + BaseApplication.HOME_DIR
		// + "/" + BaseApplication.DUMPS_DIR + "' directory.");
		// return;
		// }
		//
		// // Create tmp directory.
		// path = new File(Environment.getExternalStoragePublicDirectory(
		// BaseApplication.HOME_DIR) + "/" + BaseApplication.TMP_DIR);
		// if (path.exists() == false && !path.mkdirs()) {
		// // Could not create directory.
		// Log.e(LOG_TAG, "Error while crating '" + BaseApplication.HOME_DIR
		// + BaseApplication.TMP_DIR + "' directory.");
		// return;
		// }
		// // Clean up tmp directory.
		// for (File file : path.listFiles()) {
		// file.delete();
		// }
		//
		// // Create std. key file if there is none.
		// copyStdKeysFilesIfNecessary();
		// }
		mPendingIntent = PendingIntent.getActivity(this, 0, new Intent(this,
				getClass()), 0);

	}

	@Override
	protected void onStart() {
		super.onStart();
		dialogEnable=true;
	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		if(MyApplication.getSession().getBoolean("nfc")!=true){
			MyApplication.getSession().set("nfc",false);
		}
	}

	private void initView() {
		// TODO Auto-generated method stub
		cardTypeTv = (TextView) findViewById(R.id.nfc_cardtype_tv);
		cardNumTv = (TextView) findViewById(R.id.nfc_cardnumber_tv);
		cardInfoTv = (TextView) findViewById(R.id.nfc_cardinfo_tv);
		dataReadTv = (TextView) findViewById(R.id.nfc_data_reaed_tv);
		blockS = (Spinner) findViewById(R.id.nfc_fan_sp);
		keyEt = (EditText) findViewById(R.id.nfc_section_key_et);
		dataWriteEt = (EditText) findViewById(R.id.nfc_data_write_ev);

		rd_scan_out_mode_1 = (RadioButton) findViewById(R.id.rd_scan_out_mode_1);
		rd_scan_out_mode_2 = (RadioButton) findViewById(R.id.rd_scan_out_mode_2);
		if (Preference.getNfcSimulateKeySupport(NFCActivity.this, false)) {
			rd_scan_out_mode_2.setChecked(true);
			rd_scan_out_mode_1.setChecked(false);
		} else {
			rd_scan_out_mode_2.setChecked(false);
			rd_scan_out_mode_1.setChecked(true);
		}
		rg_outmode = (RadioGroup) findViewById(R.id.rg_outmode);
		rg_outmode
				.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {

					@Override
					public void onCheckedChanged(RadioGroup group, int checkedId) {
						// TODO Auto-generated method stub
						// int radioButtonId = group.getCheckedRadioButtonId();
						if (checkedId == R.id.rd_scan_out_mode_2) {
							Preference.setNfcSimulateKeySupport(
									NFCActivity.this, true);
						} else {
							Preference.setNfcSimulateKeySupport(
									NFCActivity.this, false);
						}
					}
				});

		cb_nfc_background_support = (CheckBox) findViewById(R.id.cb_nfc_background_support);
		cb_nfc_background_support.setChecked(Preference
				.getNfcBackgroundSupport(this, true));
		cb_nfc_background_support
				.setOnCheckedChangeListener(new OnCheckedChangeListener() {

					@Override
					public void onCheckedChanged(CompoundButton buttonView,
							boolean isChecked) {
						// TODO Auto-generated method stub
						Preference.setNfcBackgroundSupport(NFCActivity.this,
								isChecked);
						if (isChecked) {
							rg_outmode.setVisibility(View.VISIBLE);
						} else {
							rg_outmode.setVisibility(View.GONE);
						}
					}
				});

		if (Preference.getNfcBackgroundSupport(this, true)) {
			rg_outmode.setVisibility(View.VISIBLE);
		} else {
			rg_outmode.setVisibility(View.GONE);
		}
	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		dialogEnable=true;
		if (nfcAdapter != null) {
			nfcAdapter.enableForegroundDispatch(this, mPendingIntent, null,
					null);
		}
	}

	@Override
	protected void onStop() {
		super.onStop();
		dialogEnable=false;
	}

	@Override
	protected void onNewIntent(Intent intent) {
		// TODO Auto-generated method stub
		Tag tag = intent.getParcelableExtra(nfcAdapter.EXTRA_TAG);
		if (tag == null)
			return;
		final byte[] id = tag.getId();
        dialogEnable=true;
		String str = bytesToHexString(tag.getId());
		// if (str.contains("4555425e") || str.contains("1467c2ad")) {
		// Intent intent2 = new Intent(this, HaiMinTestActivity.class);
		// intent2.putExtra("data", str);
		// startActivity(intent2);
		// return;
		// }
		if (cardNumTv != null)
			cardNumTv.setText(str);

		if (dataReadTv != null) {
			dataReadTv.setText("");
		}
		String[] techList = tag.getTechList();


		if (techList != null && techList.length > 0) {

			if (cardTypeTv != null) {
				StringBuilder sb = new StringBuilder();
				for (int i = 0; i < techList.length; i++) {

					sb.append((i + 1)
							+ " "
							+ techList[i].subSequence(
									techList[i].lastIndexOf(".") + 1,
									techList[i].length()));
					sb.append("\n");
				}
				cardTypeTv.setText(sb.subSequence(0, sb.length() - 1));


				Log.i("info", "onNewIntent: "+dialogEnable);
				showDialog();


			}

			if (techList[0].contains("MifareClassic")) {
				Card.mMifareClassic = MifareClassic.get(tag);
				cardType = CardType.MifareClassic;
				dataReadTv.setText(getString(R.string.nfc_block_count)
						+ Card.mMifareClassic.getBlockCount() + "\n"
						+ getString(R.string.nfc_max_command_len)
						+ Card.mMifareClassic.getMaxTransceiveLength());
			} else if (techList[0].contains("MifareUltralight")) {
				Card.mMifareUltralight = MifareUltralight.get(tag);
				cardType = CardType.MifareUltralight;
				dataReadTv.setText("\n"
						+ getString(R.string.nfc_max_command_len)
						+ Card.mMifareUltralight.getMaxTransceiveLength());
			} else if (techList[0].contains("NdefFormatable")) {
				Card.mNdefFormatable = NdefFormatable.get(tag);
				cardType = CardType.NdefFormatable;
			} else if (techList[0].contains("Ndef")) {
				Card.mNdef = Ndef.get(tag);
				cardType = CardType.Ndef;
				// Card.mNfcF.transceive(data)
			} else if (techList[0].contains("NfcV")) {
				Card.mNfcV = NfcV.get(tag);
				cardType = CardType.NfcV;
				dataReadTv
						.setText("DSF ID:"
								+ bytesToHexString(new byte[] { Card.mNfcV
										.getDsfId() }));
			} else if (techList[0].contains("NfcF")) {
				Card.mNfcF = NfcF.get(tag);
				cardType = CardType.NfcF;
				try {
					dataReadTv
							.setText(getString(R.string.nfc_manu)
									+ new String(Card.mNfcF.getManufacturer(),
											"GB2312") + "\n"
									+ getString(R.string.nfc_max_command_len)
									+ Card.mNfcF.getMaxTransceiveLength());
				} catch (Exception e) {
					// TODO: handle exception
					e.printStackTrace();
				}

			} else if (techList[0].contains("NfcB")) {
				Card.mNfcB = NfcB.get(tag);
				cardType = CardType.NfcB;
				try {
					dataReadTv
							.setText(getString(R.string.nfc_prof)
									+ new String(Card.mNfcB.getProtocolInfo(),
											"GB2312") + "\n"
									+ getString(R.string.nfc_max_command_len)
									+ Card.mNfcB.getMaxTransceiveLength());
				} catch (Exception e) {
					// TODO: handle exception
					e.printStackTrace();
				}
			} else if (techList[0].contains("NfcA")) {
				Card.mNfcA = NfcA.get(tag);
				cardType = CardType.NfcA;
				dataReadTv.setText("ATQA:"
						+ bytesToHexString(Card.mNfcA.getAtqa()) + "\nSak:"
						+ Card.mNfcA.getSak() + "\n"
						+ getString(R.string.nfc_max_command_len)
						+ Card.mNfcA.getMaxTransceiveLength());

				try {
					Card.mNfcA.connect();
					Card.mNfcA.transceive(new byte[] { 0x00 });
					if (Card.mNfcA.isConnected()) {
						Card.mNfcA.close();
					}
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			} else if (techList[0].contains("IsoDep")) {
				Card.mIsoDep = IsoDep.get(tag);
				cardType = CardType.IsoDep;
				StringBuilder sb = new StringBuilder();
				sb.append(getString(R.string.nfc_max_command_len)
						+ Card.mIsoDep.getMaxTransceiveLength());
				try {
					Card.mIsoDep.connect();
					if (Card.mIsoDep.isConnected()) {

						// select the card manager applet
						byte[] mf = { (byte) '1', (byte) 'P', (byte) 'A',
								(byte) 'Y', (byte) '.', (byte) 'S', (byte) 'Y',
								(byte) 'S', (byte) '.', (byte) 'D', (byte) 'D',
								(byte) 'F', (byte) '0', (byte) '1', };
						byte[] mfRsp = Card.mIsoDep
								.transceive(getSelectCommand(mf));
						Log.d("test", "mfRsp:" + bytesToHexString(mfRsp));
						// select Main Application
						byte[] szt = { (byte) 'P', (byte) 'A', (byte) 'Y',
								(byte) '.', (byte) 'S', (byte) 'Z', (byte) 'T' };
						byte[] sztRsp = Card.mIsoDep
								.transceive(getSelectCommand(szt));
						Log.d("test", "sztRsp:" + bytesToHexString(sztRsp));

						byte[] balance = { (byte) 0x80, (byte) 0x5C, 0x00,
								0x02, 0x04 };
						byte[] balanceRsp = Card.mIsoDep.transceive(balance);
						Log.d("test", "balanceRsp:"
								+ bytesToHexString(balanceRsp));
						if (balanceRsp != null && balanceRsp.length > 4) {
							int cash = byteToInt(balanceRsp, 4);
							float ba = cash / 100.0f;
							if (dataReadTv != null) {
								sb.append("\n" + "余额:" + ba);
							}
						}
					}
					if (Card.mIsoDep.isConnected()) {
						Card.mIsoDep.close();
					}
				} catch (IOException e) {
					// TODO: handle exception
					e.printStackTrace();
				}
				dataReadTv.setText(sb.toString());
			} else if (techList[0].contains("NfcBarcode")) {
				// Card.mNfcBarcode = NfcBarcode.get(tag);
				// cardType = CardType.NfcBarcode;
				cardType = CardType.unknow;
			} else {
				cardType = CardType.unknow;
			}
		}

		// int typeCheck = BaseApplication.treatAsNewTag(intent, this);
		// if (typeCheck == -1 || typeCheck == -2) {
		// // Device or tag does not support Mifare Classic.
		// // Run the only thing that is possible: The tag info tool.
		// Intent i = new Intent(this, TagInfoTool.class);
		// startActivity(i);
		// }
		// if (!haveMifareUltralight) {
		// Toast.makeText(this, "不支持MifareClassic", Toast.LENGTH_LONG).show();
		// return;
		// }
		// if (canWrite) {
		// writeTag(tag);
		// } else {
		// String data = readTag(tag);
		// if (data != null) {
		// Log.i(data, "ouput");
		// Toast.makeText(this, data, Toast.LENGTH_LONG).show();
		// }
		// }
	}

	private byte[] getSelectCommand(byte[] aid) {
		final ByteBuffer cmd_pse = ByteBuffer.allocate(aid.length + 6);
		cmd_pse.put((byte) 0x00) // CLA Class
				.put((byte) 0xA4) // INS Instruction
				.put((byte) 0x04) // P1 Parameter 1
				.put((byte) 0x00) // P2 Parameter 2
				.put((byte) aid.length) // Lc
				.put(aid).put((byte) 0x00); // Le
		return cmd_pse.array();
	}

	private int byteToInt(byte[] b, int n) {
		int ret = 0;
		for (int i = 0; i < n; i++) {
			ret = ret << 8;
			ret |= b[i] & 0x00FF;
		}
		if (ret > 100000 || ret < -100000)
			ret -= 0x80000000;
		return ret;
	}

	public String readTag(Tag tag) {
		MifareClassic mfc = MifareClassic.get(tag);
		for (String tech : tag.getTechList()) {
			System.out.println(tech);
		}
		boolean auth = false;
		// 读取TAG

		try {
			String metaInfo = "";
			// Enable I/O operations to the tag from this TagTechnology object.
			mfc.connect();
			int type = mfc.getType();// 获取TAG的类型
			int sectorCount = mfc.getSectorCount();// 获取TAG中包含的扇区数
			String typeS = "";
			switch (type) {
			case MifareClassic.TYPE_CLASSIC:
				typeS = "TYPE_CLASSIC";
				break;
			case MifareClassic.TYPE_PLUS:
				typeS = "TYPE_PLUS";
				break;
			case MifareClassic.TYPE_PRO:
				typeS = "TYPE_PRO";
				break;
			case MifareClassic.TYPE_UNKNOWN:
				typeS = "TYPE_UNKNOWN";
				break;
			}
			metaInfo += "卡片类型：" + typeS + "\n共" + sectorCount + "个扇区\n共"
					+ mfc.getBlockCount() + "个块\n存储空间: " + mfc.getSize()
					+ "B\n";
			for (int j = 0; j < sectorCount; j++) {
				// Authenticate a sector with key A.
				auth = mfc.authenticateSectorWithKeyA(j,
						MifareClassic.KEY_NFC_FORUM);
				int bCount;
				int bIndex;
				if (auth) {
					metaInfo += "Sector " + j + ":验证成功\n";
					// 读取扇区中的块
					bCount = mfc.getBlockCountInSector(j);
					bIndex = mfc.sectorToBlock(j);
					for (int i = 0; i < bCount; i++) {
						byte[] data = mfc.readBlock(bIndex);
						metaInfo += "Block " + bIndex + " : "
								+ bytesToHexString(data) + "\n";
						bIndex++;
					}
				} else {
					metaInfo += "Sector " + j + ":验证失败\n";
				}
			}
			return metaInfo;
		} catch (Exception e) {
			Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show();
			e.printStackTrace();
		} finally {
			if (mfc != null) {
				try {
					mfc.close();
				} catch (IOException e) {
					Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG)
							.show();
				}
			}
		}
		return null;

	}

	// 字符序列转换为16进制字符串
	private String bytesToHexString(byte[] src) {
		StringBuilder stringBuilder = new StringBuilder("");
		if (src == null || src.length <= 0) {
			return null;
		}
		char[] buffer = new char[2];
		for (int i = 0; i < src.length; i++) {
			buffer[0] = Character.forDigit((src[i] >>> 4) & 0x0F, 16);
			buffer[1] = Character.forDigit(src[i] & 0x0F, 16);
			System.out.println(buffer);
			stringBuilder.append(buffer);
		}
		return stringBuilder.toString();
	}

	public void writeTag(Tag tag) {

		MifareClassic mfc = MifareClassic.get(tag);

		try {
			mfc.connect();
			boolean auth = false;
			short sectorAddress = 1;
			auth = mfc.authenticateSectorWithKeyA(sectorAddress,
					MifareClassic.KEY_NFC_FORUM);
			if (auth) {
				// the last block of the sector is used for KeyA and KeyB cannot
				// be overwritted
				mfc.writeBlock(4, "1313838438000000".getBytes());
				mfc.writeBlock(5, "1322676888000000".getBytes());
				mfc.close();
				Toast.makeText(this, "写入成功", Toast.LENGTH_SHORT).show();
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			try {
				mfc.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
		dialogEnable=false;
		if (nfcAdapter != null) {
			nfcAdapter.disableForegroundDispatch(this);
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// TODO Auto-generated method stub
		super.onActivityResult(requestCode, resultCode, data);
		if (requestCode == 100) {
			nfcAdapter = NfcAdapter.getDefaultAdapter(this);
			if (!nfcAdapter.isEnabled()) {
				Toast.makeText(this, getString(R.string.nfc_not_open),
						Toast.LENGTH_SHORT).show();
			}
		}
	}

	private void showDialog(){
		final AlertDialog.Builder dialog = new AlertDialog.Builder(NFCActivity.this);
		dialog.setMessage("检测成功，是否确认完成该项检测");
		dialog.setPositiveButton("是",
				new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface arg0, int arg1) {

						MyApplication.getSession().set("nfc",true);

						finish();

					}
				});
		dialog.setNeutralButton("否", new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface arg0, int arg1) {
				MyApplication.getSession().set("nfc",true);
				arg0.dismiss();

			}
		});

		if(dialogEnable==true){
			dialog.show();}
	}


	// /**
	// * Show the {@link ReadTag}.
	// * @param view The View object that triggered the method
	// * (in this case the read tag button).
	// * @see ReadTag
	// */
	// public void onShowReadTag(View view) {
	// Intent intent = new Intent(this, ReadTag.class);
	// startActivity(intent);
	// }
	//
	// /**
	// * Show the {@link WriteTag}.
	// * @param view The View object that triggered the method
	// * (in this case the write tag button).
	// * @see WriteTag
	// */
	// public void onShowWriteTag(View view) {
	// Intent intent = new Intent(this, WriteTag.class);
	// startActivity(intent);
	// }
	//
	// /**
	// * Copy the standard key files ({@link BaseApplication#STD_KEYS} and
	// * {@link BaseApplication#STD_KEYS_EXTENDED}) form assets to {@link
	// BaseApplication#KEYS_DIR}.
	// * Key files are simple text files. Any plain text editor will do the
	// trick.
	// * All key and dump data from this App is stored in
	// * getExternalStoragePublicDirectory(BaseApplication.HOME_DIR) to remain
	// * there after App uninstallation.
	// * @see BaseApplication#KEYS_DIR
	// * @see BaseApplication#HOME_DIR
	// * @see BaseApplication#copyFile(InputStream, OutputStream)
	// */
	// private void copyStdKeysFilesIfNecessary() {
	// File std = new File(Environment.getExternalStoragePublicDirectory(
	// BaseApplication.HOME_DIR) + "/" + BaseApplication.KEYS_DIR,
	// BaseApplication.STD_KEYS);
	// File extended = new File(Environment.getExternalStoragePublicDirectory(
	// BaseApplication.HOME_DIR) + "/" + BaseApplication.KEYS_DIR,
	// BaseApplication.STD_KEYS_EXTENDED);
	// AssetManager assetManager = getAssets();
	//
	// if (!std.exists()) {
	// // Copy std.keys.
	// try {
	// InputStream in = assetManager.open(
	// BaseApplication.KEYS_DIR + "/" + BaseApplication.STD_KEYS);
	// OutputStream out = new FileOutputStream(std);
	// BaseApplication.copyFile(in, out);
	// in.close();
	// out.flush();
	// out.close();
	// } catch(IOException e) {
	// Log.e(LOG_TAG, "Error while copying 'std.keys' from assets "
	// + "to external storage.");
	// }
	// }
	// if (!extended.exists()) {
	// // Copy extended-std.keys.
	// try {
	// InputStream in = assetManager.open(
	// BaseApplication.KEYS_DIR + "/" + BaseApplication.STD_KEYS_EXTENDED);
	// OutputStream out = new FileOutputStream(extended);
	// BaseApplication.copyFile(in, out);
	// in.close();
	// out.flush();
	// out.close();
	// } catch(IOException e) {
	// Log.e(LOG_TAG, "Error while copying 'extended-std.keys' "
	// + "from assets to external storage.");
	// }
	// }
	// }
}
