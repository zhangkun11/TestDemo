package android.jb.rs232;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import android.content.Context;
import android.content.Intent;
import android.jb.Preference;
import android.jb.utils.Tools;
import android.util.Log;
import android_serialport_api.SerialPort;

public class RS232Controller {
	private File power = new File("/sys/devices/platform/uhf/rs232");
	private File infCom = new File("/sys/devices/platform/em3095/com");
	private String serialPort_Path = "/dev/ttySAC3"; // 串口地址
	private File openFun = new File("/sys/devices/soc.0/m9_dev.69/rs232_rs485_switch");// 0:RS232，1:RS485
	
	
	private String IO_OE = "/sys/devices/soc.0/m9_dev.69/switch_oe"; // 默认值：1，其他值无效
	private String IO_CS1 = "/sys/devices/soc.0/m9_dev.69/cs1";// 默认值：0，其他值无效
	private String IO_CS0 = "/sys/devices/soc.0/m9_dev.69/cs0";// 默认值：0，其他值无效
	
	public File versionFile = new File(
			"/sys/devices/platform/exynos4412-adc/ver");
	public final static String TAG = "RS232Controller";
	private int hardwareVersion = 104;
	
	private Context mContext;
	private int device_type = 1;
	private static RS232Controller rs232Con;
	private SerialPort sP;
	private OutputStream out;
	private InputStream in;
	private ReadThread readThread;
	private volatile boolean run;
	private Callback l;
	private static Object lock = new Object();

	/**
	 * 串口数据回调接口
	 * 
	 * @author Ivan.Wang 2015-4-18
	 */
	public interface Callback {
		void RS232_Read(byte[] data);
	}

	private RS232Controller() {
	}

	public static RS232Controller getInstance() {
		if (rs232Con == null) {
			synchronized (lock) {
				if (rs232Con == null) {
					rs232Con = new RS232Controller();
				}
			}
		}
		return rs232Con;
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
	 * 写数据
	 * 
	 * @param data
	 */
	public void Rs232_Write(byte[] data) {
		if (out != null) {
			try {
				out.write(data);
				out.flush();
				System.out.println("write:"
						+ bytesToHexString(data, 0, data.length));
			} catch (Exception e) {
				// TODO: handle exception
				e.printStackTrace();
				Log.e("jiebao","write filed:" + e.getMessage());
			}

		}
	}

	/**
	 * Convert bytes to string,actually display only
	 * 
	 * @param bytes
	 * @return String
	 */
	private String bytesToHexString(byte[] src, int start, int size) {
		StringBuilder stringBuilder = new StringBuilder();
		if (src == null || size <= 0) {
			return null;
		}
		for (int i = start; i < size; i++) {
			int v = src[i] & 0xFF;

			String hv = Integer.toHexString(v);
			if (hv.length() < 2) {
				stringBuilder.append(0);
			}
			stringBuilder.append(hv);
		}
		return stringBuilder.toString();
	}

	private void com(String status) {
		// TODO Auto-generated method stub
		writeFile(infCom, status);
		if(device_type == 3){
			writeFile(openFun, "1");
		}
	}

	/**
	 * 232模块上电
	 */
	private void power_up() {
		writeFile(power, "1");
	}

	/**
	 * 232模块下电
	 */
	private void power_down() {
		writeFile(power, "0");
	}

	/**
	 * 初始化串口
	 * 
	 * @param baud
	 *            波特率
	 * @param bits
	 *            数据位
	 * @param event
	 *            校验位
	 * @param stopBits
	 *            停止位
	 * @param l
	 *            监听者
	 */
	public void Rs232_Open(int baud, int bits, char event, int stopBits,
			Callback l , Context context) {
//		//发送广播 关闭扫描服务
//		context.sendBroadcast(new Intent("ReleaseCom"));
				
		mContext = context;
		device_type = Preference.getDeviceType(mContext);

		if(device_type == 3){
			power = new File("/sys/devices/soc.0/m9_dev.69/rs232_pwr_en");//无线psam_en,有线rs232_pwr_en
			infCom = new File("/sys/devices/soc.0/m9_dev.69/rs232_com_switch");
			serialPort_Path = "/dev/ttyHSL1"; // 串口地址
		} else {
			String line = Tools.getHardwareVersion();
			if (Tools.isEmpty(line)) {
				Log.e(TAG, "获取硬件版本号失败！");
			} else {
				line = line.replace(".", "");
				hardwareVersion = Integer.valueOf(line);
//				Log.e(TAG, "HardwareVersion:" + hardwareVersion);
			}
			if (hardwareVersion < 103) {
				setSerialPort_Path(1);
			} else {
				setSerialPort_Path(3);
			}
		}

		this.l = l;

		try {
			Rs232_Close();
			com("1");
			IO_OE("1");
			IO_CS0("0");
			IO_CS1("0");
			power_up();
			sP = new SerialPort(new File(serialPort_Path), baud, bits, event,
					stopBits, 0);
			in = sP.getInputStream();
			out = sP.getOutputStream();
			readThread = new ReadThread();
			run = true;
			readThread.start();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			throw new RuntimeException("open port failed:" + e.getMessage());
		}

	}

	/**
	 * 关闭串口
	 */
	public void Rs232_Close() {
		// TODO Auto-generated method stub
		com("0");
		power_down();
		run = false;

		if (readThread != null) {
			readThread.interrupt();
			readThread = null;
		}
		try {
			if (in != null)
				in.close();
			if (out != null)
				out.close();
			if (sP != null)
				sP.close();
			run = false;
			in = null;
			out = null;
			sP = null;
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
		
		//发送广播 开启扫描服务
//		if(null != mContext)
//			mContext.sendBroadcast(new Intent("ReleaseCom"));
	}

	/**
	 * 选择串口地址 默认新版串口3
	 * 
	 * @param i
	 *            1:串口1 主要基于1.01以前的旧版 3:串口3 主要基于1.01以后的新版（可以进入手机设置——关于手机——版本号查看）
	 */
	private void setSerialPort_Path(int i) {
		if (i == 1) {
			serialPort_Path = "/dev/ttySAC1";
		} else {
			serialPort_Path = "/dev/ttySAC3";
		}
	}

	private class ReadThread extends Thread {
		@Override
		public void run() {
			// TODO Auto-generated method stub
			while (run) {
				try {
					Thread.sleep(50);
					if (in == null) {
						return;
					}
					int size;
					int cout = in.available();
					byte[] buffer1;
					// byte[] buffer1 = new byte[1024];
					if (cout > 0) {
						cout = in.available();
						Thread.sleep(50);
						while (cout < in.available()) {
							Log.v("while", "in.available()=" + in.available());
							cout = in.available();
							Thread.sleep(50);
						}

						cout = in.available();
						buffer1 = new byte[cout];
						size = in.read(buffer1);

						if (size > 0) {
							Log.i("info", "接收到的pakege == " + Tools.bytesToHexString(buffer1));
							if (l != null) {
								l.RS232_Read(buffer1);
							}
						}
					} else {
						continue;
					}
				}catch (InterruptedException e){
					e.printStackTrace();
				}catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
	}

	private void notifyReader() {
		if (readThread != null && readThread.isAlive()) {
			readThread.interrupt();
		}
	}
	
	private void IO_OE (String status){
		System.out.println("power:" + IO_OE + " "
				+ status);
		writeFile(new File(IO_OE), status);
	}
	
	private void IO_CS1(String status) {
		// TODO Auto-generated method stub
		System.out.println("power:" + IO_CS1 + " "
				+ status);
		writeFile(new File(IO_CS1), status);
	}
	
	private void IO_CS0(String status) {
		// TODO Auto-generated method stub
		System.out.println("power:" + IO_CS0 + " "
				+ status);
		writeFile(new File(IO_CS0), status);
	}
}
