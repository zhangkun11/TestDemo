package android.jb.tesam;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import android.content.Context;
import android.jb.utils.Tools;
import android.util.Log;
import android_serialport_api.SerialPort;

public class TesamController {

	private static TesamController pSamCon;
	private Context mContext;

	protected SerialPort mSerialPort;
	public OutputStream mOutputStream;
	public InputStream mInputStream;
	private File power = new File("/sys/devices/soc.0/m9_dev.69/tesam_pwr_en");
	private static String serialPort_Path = "/dev/spidev32766.0";
	//private static String serialPort_Path = "/dev/tesam";
	public File pSamfile;	
	public final static String TAG = "TesamController";

	public static TesamController getInstance() {
		if (pSamCon == null) {
			synchronized (TesamController.class) {
				if (pSamCon == null) {
					pSamCon = new TesamController();
				}
			}
		}
		return pSamCon;
	}

	public int Tesam_Open(Context context) throws IOException, SecurityException {
		// TODO Auto-generated method stub
		mContext = context;
		Tesam_Close();
		int i = power_up();
		if(i != 0){
			return -1;
		}
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		// begin = true;
		pSamfile = new File(serialPort_Path);
		if (pSamfile.canRead() && pSamfile.canWrite()) {
			if (pSamfile.exists()) {
				mInputStream = new FileInputStream(pSamfile);
				mOutputStream = new FileOutputStream(pSamfile);
				return 0;
			} else {
				throw new IOException("Device not found");
			}
		} else {
			throw new IOException("Device not found");
		}
	}

	private int power_up() {
		return writeFile(power, "1");
	}

	private int power_down() {
		return writeFile(power, "0");
	}

	public void Tesam_Close() throws IOException {
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

	public byte[] Tesam_Write(byte[] command) {
		try {
			if (mOutputStream != null) {
				mOutputStream.write(command);
				mOutputStream.flush();
				System.out.println(Tools.bytesToHexString(command));
				return Tesam_Read();
			}
		} catch (Exception e) {
			return null;
		}catch(StackOverflowError e){
			Log.e("jiebao","Exception103 e: "+e);
			return null;
			}
		return null;
	}

	private synchronized int writeFile(File file, String value) {

		try {
			FileOutputStream outputStream = new FileOutputStream(file);
			outputStream.write(value.getBytes());
			outputStream.flush();
			outputStream.close();
			return 0;
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return -1;
	}

	public byte[] Tesam_Read() throws IOException {
		byte[] result = null;
		if (mInputStream != null) {
			byte[] buffer = new byte[126];
			int ret = mInputStream.read(buffer);
			
			
//			ret = mInputStream.read(buffer);
//			System.out.println("Tesam_Read:"+Tools.bytesToHexString(buffer));
			return buffer;
		}
		return result;
	}
	
}
