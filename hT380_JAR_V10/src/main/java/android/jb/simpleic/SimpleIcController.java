package android.jb.simpleic;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import android.content.Context;
import android.jb.Preference;
import android.jb.utils.Tools;
import android.util.Log;
import android_serialport_api.SerialPort;

/**
 * 简易ic卡控制模块，单例 iic协议
 * 
 * @author Administrator
 * 
 */
public class SimpleIcController {
	private static SimpleIcController pSamCon;
	private int device_type = 1;
	private Context mContext;

	public String serialPort_Path = "/dev/ttyHSL0";
	protected SerialPort mSerialPort;
	protected InputStream mInputStream;
	protected OutputStream mOutputStream;
	private File power = new File("/sys/devices/platform/c110sysfs/gpio");
	public File pSamfile;
	
//	private String IO_OE = "/sys/devices/soc.0/m9_dev.68/switch_oe"; // 默认值：1，其他值无效
//	private String IO_CS1 = "/sys/devices/soc.0/m9_dev.68/cs1";// 默认值：1，其他值无效
//	private String IO_CS0 = "/sys/devices/soc.0/m9_dev.68/cs0";// 默认值：1，其他值无效

	public static SimpleIcController getInstance() {
		if (pSamCon == null) {
			synchronized (SimpleIcController.class) {
				if (pSamCon == null) {
					pSamCon = new SimpleIcController();
				}
			}
		}
		return pSamCon;
	}

	/**
	 * 开启设备
	 */
	public void Simpleic_Open(Context context) throws IOException,
			SecurityException {
		// TODO Auto-generated method stub
		mContext = context;
		device_type = Preference.getDeviceType(mContext);

		if (device_type == 3) {
			power = new File("/sys/devices/soc.0/m9_dev.69/psam_en");
			serialPort_Path = "/dev/ttyHSL0"; // 串口地址
		} else {
			power = new File("/sys/devices/platform/c110sysfs/gpio");
			serialPort_Path = "/dev/psam";
		}
		Simpleic_Close();
		power_up();

		if (device_type == 3) {
			try {
				if (mSerialPort == null) {
					int baudrate = 19200;
					mSerialPort = new SerialPort(new File(serialPort_Path),
							baudrate, 8, 'N', 1, 0);
//					 mSerialPort = new SerialPort(new File(serialPort_Path),
//					 baudrate, 0, false);
					mOutputStream = mSerialPort.getOutputStream();
					mInputStream = mSerialPort.getInputStream();
					System.out.println("init()");
				}
			} catch (IOException e) {
				throw new IOException("Device not found");
			}
		} else {
			pSamfile = new File(serialPort_Path);
			if (pSamfile.canRead() && pSamfile.canWrite()) {
				if (pSamfile.exists()) {
					mInputStream = new FileInputStream(pSamfile);
					mOutputStream = new FileOutputStream(pSamfile);
				} else {
					throw new IOException("Device not found");
				}
			} else {
				throw new IOException("Device not found");
			}
		}

	}

	/**
	 * Psam模块上电
	 */
	public void power_up() {
		writeFile(power, "1");
	}

	/**
	 * Psam模块下电
	 */
	public void power_down() {
		writeFile(power, "0");
	}

	/**
	 * 复位sam卡
	 */
	public boolean Simpleic_Reset() throws IOException {
		if (mOutputStream != null) {
//			byte[] sourceByteArr = new byte[4];
//			sourceByteArr[0] = (byte) 0x03;// 数据长度
//			sourceByteArr[1] = 0x37;// 指令码
//			sourceByteArr[2] = (byte) (((0 & 0xFF) << 4) & 0XF0);// 要操作的psam卡代号
//			sourceByteArr[3] = (byte) ((sourceByteArr[0] & 0xFF)
//					^ (sourceByteArr[1] & 0xFF) ^ (sourceByteArr[2] & 0xFF));// 校验位
			byte [] res = {(byte) 0xaa,0x66,0x00,0x04,0x37,0x00,0x3b};
			//System.out.println("write:" + Tools.bytesToHexString(sourceByteArr));
			mOutputStream.write(res);
			mOutputStream.flush();
			try {
				Thread.sleep(600);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			byte[] buffer = new byte[1024];
			int ret = mInputStream.read(buffer);
			if (ret > 0) {
					if (buffer[4] == (byte) 0x37) {
						return true;
					}
			}
		}
		return false;
	}

	/**
	 * 执行cos命令
	 * 
	 * @param 0 要操作哪张sam卡
	 * @param cosMommand
	 *            cos命令
	 */
	public byte[] Simpleic_Write(byte[] cosMommand) throws IOException {
		byte[] result = null;
		if (mOutputStream != null && cosMommand != null) {
			byte[] sourceByteArr = new byte[7 + cosMommand.length];
			sourceByteArr[0] = (byte) 0xAA;// 数据长度
			sourceByteArr[1] = 0x66;
			sourceByteArr[2] = (byte) 0x00;// 数据长度 高位
			sourceByteArr[3] = (byte) (sourceByteArr.length - 3);// 长度 地位
			sourceByteArr[4] = 0x38;// 指令码
			sourceByteArr[5] = (byte) (0 & 0xFF);// 要操作的卡代号
			// sourceByteArr[0] = (byte) (sourceByteArr.length - 1);// 长度
			// sourceByteArr[1] = 0x38;// 指令码
			// sourceByteArr[2] = (byte) (samNum & 0xFF);// 要操作的卡代号

			// 填充cos指令
			for (int i = 0; i < cosMommand.length; i++) {
				sourceByteArr[i + 6] = cosMommand[i];
			}
			// sourceByteArr[sourceByteArr.length - 2] = 0x00;// 这一位默认为0

			// 算校验
			for (int i = 2; i < sourceByteArr.length - 1; i++) {
				sourceByteArr[sourceByteArr.length - 1] = (byte) (((sourceByteArr[sourceByteArr.length - 1] & 0xFF) + (sourceByteArr[i] & 0xFF)) & 0xFF);
			}
			writeCommand(sourceByteArr);

			byte[] buffer = onDataReceviced();
			if (null != buffer && buffer.length > 0) {
				return buffer;
			}
		}
		return result;
	}

	private void writeCommand(byte[] command) {
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
	
	private byte[] onDataReceviced() throws IOException {
		byte[] buffer = null;
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
		return buffer;
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
	
//	private void IO_OE(String status) {
//		// TODO Auto-generated method stub
//		System.out.println("power:" + IO_OE + " "
//				+ status);
//		writeFile(new File(IO_OE), status);
//	}
//
//	private void IO_CS1(String status) {
//		// TODO Auto-generated method stub
//		System.out.println("power:" + IO_CS1 + " "
//				+ status);
//		writeFile(new File(IO_CS1), status);
//	}
//	
//	private void IO_CS0(String status) {
//		// TODO Auto-generated method stub
//		System.out.println("power:" + IO_CS0 + " "
//				+ status);
//		writeFile(new File(IO_CS0), status);
//	}

	/**
	 * 关闭设备
	 */
	public void Simpleic_Close() throws IOException {
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

}
