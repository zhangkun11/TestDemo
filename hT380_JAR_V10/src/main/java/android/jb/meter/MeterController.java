package android.jb.meter;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.InvalidParameterException;

import android.content.Context;
import android.content.Intent;
import android.jb.Preference;
import android.jb.utils.Tools;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android_serialport_api.SerialPort;

public class MeterController {

	private static MeterController chaobiaoCon;
	private boolean T485 = false;
	public static final int METER_485 = 1;
	public static final int METER_Infrared = 2;
	private Context mContext;
	private int device_type = 1;
	private File openPower = new File("/sys/devices/platform/ir/dc_power");
	private File openCom = new File("/sys/devices/platform/ir/com");
	private File openPower485 = new File("/sys/devices/platform/uhf/rs232");
	private File openCom485 = new File("/sys/devices/platform/em3095/com");
	private File openFun = new File(
			"/sys/devices/soc.0/m9_dev.69/rs232_rs485_switch");// 0:RS232，1:RS485

	private String POWER = "/sys/devices/soc.0/m9_dev.69/ir_pwr_en";// 1开，0关
	private String IO_OE = "/sys/devices/soc.0/m9_dev.69/switch_oe"; // 默认值：1，其他值无效
	private String IO_CS1 = "/sys/devices/soc.0/m9_dev.69/cs1";// 默认值：1，其他值无效
	private String IO_CS0 = "/sys/devices/soc.0/m9_dev.69/cs0";// 默认值：1，其他值无效

	// 变量
	protected SerialPort mSerialPort;
	private boolean begin;
	private boolean isReceive = false; // 控制串口读取线程状态 ture:接受数据 false:停止接受数据
	private boolean is_SerialPortOpen = false; // 是否打开串口
	private String data = "";

	// 控制
	protected OutputStream mOutputStream;
	private InputStream mInputStream;
	private ReadThread mReadThread;
	private Callback onReadSerialPortDataListener;
	private String serialPort_Path = "/dev/ttySAC3"; // 串口地址

	public final static String TAG = "RS232Controller";
	private int hardwareVersion = 104;
	private static File versionFile = new File(
			"/sys/devices/platform/exynos4412-adc/ver");
	private Handler mHandler = new Handler() {
		public void handleMessage(Message msg) {
		};
	};

	public static MeterController getInstance() {
		if (null == chaobiaoCon) {
			synchronized (MeterController.class) {
				if (null == chaobiaoCon) {
					chaobiaoCon = new MeterController();
				}
			}
		}
		return chaobiaoCon;
	}

	/**
	 * 打开抄表模块
	 * 
	 * @param brz
	 *            波特率
	 * @param bits
	 *            数据位
	 * @param event
	 *            校验位
	 * @param stop
	 *            停止位
	 */
	public void Meter_Open(int type, int brz, int bits, char event, int stop,
			Callback r, Context context) {
		// //发送广播 关闭扫描服务
		// context.sendBroadcast(new Intent("ReleaseCom"));

		if (type == METER_485) {
			T485 = true;
		} else {
			T485 = false;
		}
		this.onReadSerialPortDataListener = r;

		mContext = context;
		device_type = Preference.getDeviceType(mContext);

		if (device_type == 3) {
			serialPort_Path = "/dev/ttyHSL1"; // 串口地址
		} else {
			String line = Tools.getHardwareVersion();
			if (Tools.isEmpty(line)) {
				Log.e(TAG, "获取硬件版本号失败！");
			} else {
				line = line.replace(".", "");
				hardwareVersion = Integer.valueOf(line);
				Log.e(TAG, "HardwareVersion:" + hardwareVersion);
			}
			if (hardwareVersion < 103) {
				setSerialPort_Path(1);
			} else {
				setSerialPort_Path(3);
			}
		}

		power_up(T485);
		try {
			Thread.sleep(10);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if (!is_SerialPortOpen) {
			AbsSerialPort(serialPort_Path, brz, bits, event, stop);
			is_SerialPortOpen = true;
		}
		mHandler.postDelayed(powerRunable, 1000);
	}

	/**
	 * 关闭抄表模块
	 */
	public void Meter_Close() {
		System.out.println("Meter_Close()");
		mHandler.removeCallbacks(stopReceice);
		mHandler.removeCallbacks(powerRunable);
		power_down(T485);
		begin = false;
		isReceive = false;
		// System.out.println("Meter_Close() + begin:" + begin);
		if (mReadThread != null) {
			notifyReader();
			mReadThread = null;
		}
		try {
			if (mInputStream != null)
				mInputStream.close();
			if (mOutputStream != null)
				mOutputStream.close();
			if (mSerialPort != null) {
				mSerialPort.close();
			}
			mOutputStream = null;
			mInputStream = null;
			mSerialPort = null;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			System.out.println("Meter_Close IOException");
			e.printStackTrace();
		}
		is_SerialPortOpen = false;

		// 发送广播 开启扫描服务
		// if(null != mContext)
		// mContext.sendBroadcast(new Intent("ReleaseCom"));
	}

	/**
	 * 获取当前是红外还是485抄表
	 * 
	 * @param is485
	 *            <code>true</code> 485 抄表 <code>false</code> 红外抄表
	 */
	public boolean Meter_GetType() {
		return T485;
	}

	/**
	 * 抄表模块下电（485、红外）
	 * 
	 * @param is485
	 *            ture:485模块下电 false:红外抄表模块下电
	 */
	private void power_down(boolean is485) {
		if (is485) {
			com485("0");
			power485("0");
			System.out.println("caobiao close com485");
		} else {
			com("0");
			power("0");
			IO_CS1("0");
			IO_CS0("0");
			System.out.println("caobiao close com");
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
			System.out.println(e.getMessage() + "File:"
					+ file.getAbsolutePath());
		} catch (IOException e) {
			e.printStackTrace();
			System.out.println(e.getMessage() + "File:"
					+ file.getAbsolutePath());
		}
	}

	private void power(String status) {
		// TODO Auto-generated method stub
		System.out.println("power:" + openPower.getAbsolutePath() + " "
				+ status);
		writeFile(openPower, status);
	}
	
	private void IO_OE(String status) {
		// TODO Auto-generated method stub
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

	private void com(String status) {
		// TODO Auto-generated method stub
		System.out.println("com:" + openCom.getAbsolutePath() + " " + status);
		writeFile(openCom, status);
	}

	private void power485(String status) {
		System.out.println("power485:" + openPower485.getAbsolutePath() + " "
				+ status);
		writeFile(openPower485, status);
	}

	private void com485(String status) {
		System.out.println("com485:" + openCom485.getAbsolutePath() + " "
				+ status);
		writeFile(openCom485, status);
		if (device_type == 3) {
			writeFile(openFun, "0");
		}
	}

	/**
	 * 抄表模块上电（485、红外）
	 * 
	 * @param is485
	 *            ture:485模块上电 false:红外抄表模块上电
	 */
	private void power_up(boolean is485) {
		if (is485) {
			if (device_type == 3) {
				openPower485 = new File(
						"/sys/devices/soc.0/m9_dev.69/rs232_pwr_en");
				openCom485 = new File(
						"/sys/devices/soc.0/m9_dev.69/rs232_com_switch");
			} else {
				openPower485 = new File("/sys/devices/platform/uhf/rs232");
				openCom485 = new File("/sys/devices/platform/em3095/com");
			}
			com485("1");
			power485("1");
			IO_OE("1");
			IO_CS1("0");
			IO_CS0("0");
			System.out.println("caobiao opencom485");
		} else {
			if (device_type == 3) {
				openPower = new File("/sys/devices/soc.0/m9_dev.69/ir_pwr_en");//
				openCom = new File("/sys/devices/soc.0/m9_dev.69/ir_com_switch");//
			} else {
				openPower = new File("/sys/devices/platform/ir/dc_power");
				openCom = new File("/sys/devices/platform/ir/com");
			}
			
			power("1");
			com("1");
			IO_OE("1");// 修改红外chaobiao
			IO_CS1("1");
			IO_CS0("1");
			System.out.println("caobiao opencom");
		}
		try {
			Thread.sleep(10);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
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

	private void AbsSerialPort(String port, int baudrate, int bits, char event,
			int stop) {
		try {
			mSerialPort = this.getSerialPort(port, baudrate, bits, event, stop);
			System.out.println("AbsSerialPort open");
			mOutputStream = mSerialPort.getOutputStream();
			mInputStream = mSerialPort.getInputStream();

			begin = true;
			mReadThread = new ReadThread();
			mReadThread.start();

		} catch (SecurityException e) {
			System.err
					.println("You do not have read/write permission to the serial port.");
			e.printStackTrace();
		} catch (IOException e) {
			System.err
					.println("The serial port can not be opened for an unknown reason.");
			e.printStackTrace();
		} catch (InvalidParameterException e) {
			System.err.println("Please configure your serial port first.");
			e.printStackTrace();
		}
	}

	private SerialPort getSerialPort(String port, int baudrate, int bits,
			char event, int stop) throws SecurityException, IOException,
			InvalidParameterException {
		if (mSerialPort == null) {
			if ((port.length() == 0) || (baudrate == -1)) {
				throw new InvalidParameterException();
			}
			mSerialPort = new SerialPort(new File(port), baudrate, bits, event,
					stop, 0);
		}
		return mSerialPort;
	}

	/**
	 * 数据回调接口
	 * 
	 * @author Ivan.Wang 2015-4-20
	 */
	public interface Callback {
		public void Meter_Read(byte[] buffer, int size);
	}

	private class ReadThread extends Thread {
		byte[] buffer1;

		public void run() {
			while (begin) {
				// System.out.println("run() + begin:" + begin);
				// read();
				if (true) {
					try {
						int size;
						if (mInputStream == null) {
							return;
						}
						// System.out.println("read() + begin:" + begin);
						int cout = mInputStream.available();
						if (cout > 0) {
							Thread.sleep(50);
							int temp = 0;
							while (begin) {
								// System.out.println("read() + begin:" +
								// begin);
								if (mInputStream == null)
									return;
								Thread.sleep(100);
								cout = mInputStream.available();
								if (temp == cout) {
									break;
								}
								temp = cout;
							}
						} else {
							continue;
						}
						if (mInputStream == null)
							return;
						cout = mInputStream.available();
						buffer1 = new byte[cout];
						size = mInputStream.read(buffer1);
						System.out.println("size ==== " + size);
						if (size > 0) {
							Log.v("caoBiao485Read", "cout > 0");
							if (onReadSerialPortDataListener != null) {
								System.out.println("Meter_Read:"
										+ Tools.bytesToHexString(buffer1));
								onReadSerialPortDataListener.Meter_Read(
										buffer1, size);
							}
						}

					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (Exception e) {
						e.printStackTrace();
						return;
					}
				} else {
					try {
						Thread.sleep(10000);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
		}
	}

	// private void read() {
	//
	// }

	public void writeCommand(byte[] b) {
		try {
			if (allowToWrite()) {
				if (b == null)
					return;
				System.out.println("对串口写入：" + b);
				isReceive = true;
				notifyReader();
				mHandler.removeCallbacks(stopReceice);
				mHandler.postDelayed(stopReceice, 3 * 1000);
				mOutputStream.write(b);
				mOutputStream.flush(); // 1
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private boolean allowToWrite() {
		if (mOutputStream == null) {
			return false;
		}
		return true;
	}

	private void notifyReader() {
		if (mReadThread != null && mReadThread.isAlive()) {
			mReadThread.interrupt();
		}
	}

	private Runnable powerRunable = new Runnable() {

		@Override
		public void run() {
			// TODO Auto-generated method stub
			power_up(T485);
		}
	};

	/**
	 * 防止后台扫描 接受数据线程被阻断
	 */
	private Runnable stopReceice = new Runnable() {

		@Override
		public void run() {
			System.out.println("isReceive:" + false);
			isReceive = false;
		}
	};
}
