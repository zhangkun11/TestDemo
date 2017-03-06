package android.jb.psam;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.InvalidParameterException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import android.content.Context;
import android.jb.Preference;
import android.jb.utils.Tools;
import android.util.Log;
import android_serialport_api.SerialPort;

public class PsamController {

	private static PsamController pSamCon;
	private int device_type = 1;
	private Context mContext;

	protected SerialPort mSerialPort;
	public OutputStream mOutputStream;
	public InputStream mInputStream;
	private File power = new File("/sys/devices/soc.0/m9_dev.69/psam_en");
	private static String serialPort_Path = "/dev/ttyHSL0"; // 串口地址
//	private static String serialPort_Path2 = "/dev/psam";
	public int hardwareVersion = 104;
	public final static String TAG = "PsamController";

	public static PsamController getInstance() {
		if (pSamCon == null) {
			synchronized (PsamController.class) {
				if (pSamCon == null) {
					pSamCon = new PsamController();
				}
			}
		}
		return pSamCon;
	}

	/**
	 * 写数据
	 * @param command
	 */
	public void Psam_Write(byte[] command) {
		try {
			if (mOutputStream != null) {
				mOutputStream.write(command);
				mOutputStream.flush();
				System.out.println(Tools.bytesToHexString(command));
			}

		} catch (Exception e) {
			// TODO: handle exception
		}
	}

	private synchronized void writeFile(File file, String value) {

		try {
			FileOutputStream outputStream = new FileOutputStream(file);
			outputStream.write(value.getBytes());
			outputStream.flush();
			outputStream.close();

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * 开启设备
	 */
	public void Psam_Open(Context context) throws IOException, SecurityException {
		// TODO Auto-generated method stub
		mContext = context;
		device_type = Preference.getDeviceType(mContext);
		
		if(device_type == 3){
			power = new File("/sys/devices/soc.0/m9_dev.69/psam_en");
			serialPort_Path = "/dev/ttyHSL0"; // 串口地址
		}else if(device_type == 2){
			power = new File("/sys/devices/soc.0/m9_dev.68/psam_en");
			serialPort_Path = "/dev/ttyHSL0"; // 串口地址
		}else {
			String line = Tools.getHardwareVersion();
			if (Tools.isEmpty(line)) {
				Log.e(TAG, "获取硬件版本号失败！");
			} else {
				line = line.replace(".", "");
				hardwareVersion = Integer.valueOf(line);
//				Log.e(TAG, "HardwareVersion:" + hardwareVersion);
			}
			if (hardwareVersion <= 103) {
				serialPort_Path = "/dev/psam";
			} else {
				serialPort_Path = "/dev/ttySAC1";
			}
		}
		
		Psam_Close();
		
		power_up();
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		// begin = true;
		try {
			if (mSerialPort == null) {
				int baudrate = 9600;
				if(hardwareVersion <= 103){
					baudrate = 9600;
				}else {
					baudrate = 19200;
				}
				mSerialPort = new SerialPort(new File(serialPort_Path),
						baudrate, 8 , 'N' , 1 , 0);
				mOutputStream = mSerialPort.getOutputStream();
				mInputStream = mSerialPort.getInputStream();
				System.out.println("init()");
			}
		} catch (InvalidParameterException e) {

		}
	}
	
	/**
	 * Psam模块上电
	 */
	private void power_up() {
		writeFile(power, "1");
	}

	/**
	 * Psam模块下电
	 */
	private void power_down() {
		writeFile(power, "0");
	}

	/**
	 * 关闭设备
	 */
	public void Psam_Close() throws IOException {
		// TODO Auto-generated method stub
		if (mInputStream != null)
			mInputStream.close();
		if (mOutputStream != null)
			mOutputStream.close();
		if (mSerialPort != null) {
			mSerialPort.close();
			mSerialPort = null;
		}
		// writeFile(powerF, "0");
		power_down();
	}

	/**
	 * 数据回调方法
	 * 
	 * @return
	 * @throws IOException
	 */
	public byte[] Psam_Read() throws IOException {
		byte[] buffer = null;
		if (mOutputStream != null) {
			if(hardwareVersion >= 104){
				try {
					Thread.sleep(500);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
				int count = mInputStream.available();
				if (count > 0) {
					buffer = new byte[count];
					mInputStream.read(buffer);
					Log.i("onDataReceviced", Tools.bytesToHexString(buffer));
				}
			}else {
				buffer = new byte[512];
				mInputStream.read(buffer);
				
			}
		}
		return buffer;
	}
}
