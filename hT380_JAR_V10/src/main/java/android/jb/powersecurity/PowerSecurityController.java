package android.jb.powersecurity;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.InvalidParameterException;

import android.content.Context;
import android.jb.Preference;
import android.jb.utils.Tools;
import android_serialport_api.SerialPort;

public class PowerSecurityController {

	private static PowerSecurityController pSamCon;
	private int device_type = 1;
	private Context mContext;

	protected SerialPort mSerialPort;
	public OutputStream mOutputStream;
	private InputStream mInputStream;
	private ReadThread readThread;
	private ReceiveListener l;
	protected boolean bWriteCardData = false;
	private File power = new File("/sys/devices/platform/c110sysfs/gpio");
	private File power2 = new File("/sys/devices/platform/tesam/dc_power");
	private static String serialPort_Path = "/dev/ttySAC1"; // 串口地址
	private final String dc_power = "dc_power";
	private final String com = "com";
	private final String en = "en";
	private final String RootPath = "/sys/devices/platform/uhf/";
	/**
	 * 串口数据回调接口
	 * 
	 * @author Ivan.Wang 2015-4-18
	 */
	public interface ReceiveListener {
		void onReceive(byte[] data);
	}
	
	public static PowerSecurityController getInstance() {
		if (pSamCon == null) {
			synchronized (PowerSecurityController.class) {
				if (pSamCon == null) {
					pSamCon = new PowerSecurityController();
				}
			}
		}
		return pSamCon;
	}

	public void writeCommand(byte[] command) {
		try {
			if (mOutputStream != null) {
				System.out.println();
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
	public void PowerSecurity_Open(Context context , ReceiveListener l) throws IOException, SecurityException {
		// TODO Auto-generated method stub
		mContext = context;
		device_type = Preference.getDeviceType(mContext);
		this.l = l;
		
		if(device_type == 1){
			power = new File("/sys/devices/platform/c110sysfs/gpio");
			power2 = new File("/sys/devices/platform/tesam/dc_power");
			serialPort_Path = "/dev/ttySAC1"; // 串口地址
		} else {
			power = new File("/sys/devices/soc.0/m9_dev.69/psam_en");
			serialPort_Path = "/dev/ttyHSL0"; // 串口地址
		}
		
		PowerSecurity_Close();
		
		if(device_type == 1){
			uhf_power_up();
		}
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
				mSerialPort = new SerialPort(new File(serialPort_Path),
						baudrate, 8, 'E', 1, 0);
				// mSerialPort = new SerialPort(new File("/dev/ttySAC1"),
				// 9600, 8,'N',1,0);
				mOutputStream = mSerialPort.getOutputStream();
				mInputStream = mSerialPort.getInputStream();
				
				readThread = new ReadThread();
				readThread.run = true;
				readThread.start();
				System.out.println("init()");
			}
		} catch (InvalidParameterException e) {

		}
	}

	/**
	 * 超高频上电
	 * 
	 * @return
	 */
	private boolean uhf_power_up() {
		return writeFile(dc_power, 1) && writeFile(com, 1) && writeFile(en, 1);
	}
	
	/**
	 * 超高频下电
	 * 
	 * @return
	 */
	private boolean uhf_power_down() {
		return writeFile(en, 0) && writeFile(com, 0) && writeFile(dc_power, 0);
	}
	
	private boolean writeFile(String name, int status) {
		try {
			File file = new File(RootPath + name);
			if (file.exists()) {
				OutputStream out = new FileOutputStream(file);
				out.write((status + "").getBytes());
				out.flush();
				out.close();
				return true;
			}
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
			return false;
		}
		return false;
	}
	/**
	 * Psam模块上电
	 */
	private void power_up() {
		writeFile(power, "1");
		if(device_type == 1){
			writeFile(power2, "1");
		}
	}

	/**
	 * Psam模块下电
	 */
	private void power_down() {
		writeFile(power, "0");
		if(device_type == 1){
			writeFile(power2, "0");
		}
	}


	/**
	 * 关闭设备
	 */
	public void PowerSecurity_Close() throws IOException {
		// TODO Auto-generated method stub
		if (readThread != null) {
			readThread.run = false;
			readThread = null;
		}
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
		
		if(device_type == 1){
			uhf_power_down();
		}
	}
	
	private class ReadThread extends Thread {
		public boolean run;

		@Override
		public void run() {
			// TODO Auto-generated method stub
			while (run) {
				if (mInputStream != null) {
					try {
//						System.out.println("is recevice");
						int len = mInputStream.available();
						if (len > 0) {
//							sleep(10);
							int temp = 0;
							while (run) {
								len = mInputStream.available();
								try {
									Thread.sleep(1);
								} catch (InterruptedException e) {
									e.printStackTrace();
									break;
								}
								if (temp == len) {
									break;
								}
								temp = len;
							}
							System.out.println("is recevice");
							len = mInputStream.available();
							byte[] buffer = new byte[len];
							mInputStream.read(buffer);
							if (l != null)
								l.onReceive(buffer);
						}
						sleep(5);
					} catch (Exception e) {
						// TODO: handle exception
						e.printStackTrace();
					}

				}
			}
		}
	}
}
