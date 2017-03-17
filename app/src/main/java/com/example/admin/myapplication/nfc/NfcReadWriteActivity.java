package com.example.admin.myapplication.nfc;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.jb.utils.Tools;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.MifareClassic;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.ht380d_jar_demo.R;
import com.main.BaseApplication;

import java.io.IOException;

public class NfcReadWriteActivity extends Activity {
	private NfcAdapter nfcAdapter;
	private PendingIntent mPendingIntent;
	private boolean canWrite = false;
	private TextView nfc_data_reaed_tv;
	private EditText et_write;
	private static final String LOG_TAG = NfcReadWriteActivity.class.getSimpleName();
	private byte[] key = { (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff,
			(byte) 0xff, (byte) 0xff };
	private byte keyA[] = { (byte) 0xd3, (byte) 0xf7, (byte) 0xd3, (byte) 0xf7,
			(byte) 0xd3, (byte) 0xf7 };
//	byte write[] = { (byte) 0x11, (byte) 0x11, (byte) 0x11, (byte) 0x11,
//			(byte) 0x11, (byte) 0x11, (byte) 0x11, (byte) 0x11, (byte) 0x11,
//			(byte) 0x11, (byte) 0x11, (byte) 0x11, (byte) 0x11, (byte) 0x11,
//			(byte) 0x11, (byte) 0x11 };

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_nfc_readwrite);

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

		mPendingIntent = PendingIntent.getActivity(this, 0, new Intent(this,
				getClass()), 0);

	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
	}

	private void initView() {
		// TODO Auto-generated method stub
		nfc_data_reaed_tv = (TextView)findViewById(R.id.nfc_data_reaed_tv);
		et_write = (EditText)findViewById(R.id.et_write);
	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		if (nfcAdapter != null) {
			nfcAdapter.enableForegroundDispatch(this, mPendingIntent, null,
					null);
		}
	}

	@Override
	protected void onNewIntent(Intent intent) {
		// TODO Auto-generated method stub
		Tag tag = intent.getParcelableExtra(nfcAdapter.EXTRA_TAG);
		if (tag == null)
			return;
		final byte[] id = tag.getId();
		String[] techList = tag.getTechList();
		String metaInfo = "";

		if (techList != null && techList.length > 0) {
			a : for(int i = 0 ; i < techList.length ; i++){
				if(techList[i].contains("MifareClassic")){
					Card.mMifareClassic = MifareClassic.get(tag);
					boolean autha = false;
					int j = 2;
					int blockIndex = 8;
					try {
						Card.mMifareClassic.connect();
//						autha = Card.mMifareClassic
//								.authenticateSectorWithKeyA(j, MifareClassic.KEY_DEFAULT);
//						autha = Card.mMifareClassic.authenticateSectorWithKeyA(j,
//								keyA);
//						autha = Card.mMifareClassic
//								.authenticateSectorWithKeyA(j, keyA);
						autha = Card.mMifareClassic
								.authenticateSectorWithKeyA(j, MifareClassic.KEY_DEFAULT);
						if(canWrite){
							if(autha){
								int writeB = 8;
								String str = et_write.getText().toString();
								byte[] writebyte = Tools.hexString2Bytes(str);
								Card.mMifareClassic.writeBlock(writeB, writebyte);
								metaInfo += "Sector " + j + ":" + getResources().getString(R.string.nfc_block_write) + " " + writeB + " " + getResources().getString(R.string.info_success) + "\n";
								
								byte[] data2;
								data2 = Card.mMifareClassic.readBlock(blockIndex);
								metaInfo += "Block " + blockIndex + " : "
										+ bytesToHexString(data2) + "\n";
							}else {
								metaInfo += "Sector " + j + ":" + getResources().getString(R.string.info_validation_failed) + "\n";
							}
							canWrite = false;
						}else {
							if (autha) {
								metaInfo += "Sector " + j + ":" + getResources().getString(R.string.info_validation_success) + "\n";
								// 读取扇区中的块
								byte[] data;
								data = Card.mMifareClassic.readBlock(blockIndex);
								metaInfo += "Block " + blockIndex + " : "
										+ bytesToHexString(data) + "\n";
								metaInfo += "Sector " + j + ":" + getResources().getString(R.string.nfc_block_read) + " " + blockIndex + " " + getResources().getString(R.string.info_success) + "\n";
							} else {
								metaInfo += "Sector " + j + ":" + getResources().getString(R.string.info_validation_failed) + "\n";
							}
						}
						Card.mMifareClassic.close();
					} catch (IOException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
					break a;
				}
			}
			if(Tools.isEmpty(metaInfo)){
				metaInfo = getResources().getString(R.string.info_no_tag_found);
			}
			nfc_data_reaed_tv.setText(metaInfo);
		}
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

	public void OnWrite(View view){
		String data = et_write.getText().toString();
		if (BaseApplication.isHexAnd16Byte(data, this) == false) {
            return;
        }
		Toast.makeText(NfcReadWriteActivity.this, getResources().getString(R.string.nfc_put_card_again), Toast.LENGTH_SHORT).show();
		canWrite = true;
	}
	
	public void OnRead(View view){
		Toast.makeText(NfcReadWriteActivity.this, getResources().getString(R.string.nfc_put_card_again), Toast.LENGTH_SHORT).show();
		canWrite = false;
	}
}

