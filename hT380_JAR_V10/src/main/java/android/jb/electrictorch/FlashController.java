package android.jb.electrictorch;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class FlashController {
	
	private File power = new File("/sys/devices/soc.0/m9_dev.69/torch_light");
	private static FlashController flashCon;
	private static Object lock = new Object();
	
	public static FlashController getInstance() {
		if (flashCon == null) {
			synchronized (lock) {
				if (flashCon == null) {
					flashCon = new FlashController();
				}
			}
		}
		return flashCon;
	}
	
	private FlashController(){}
	/**
	 * 给扫描头上下电
	 * 
	 * @param power
	 *            1 开，0关
	 */
	public void power(String p) {
//		try {
//			//Thread.sleep(100);
//		} catch (InterruptedException e) {
//			e.printStackTrace();
//		}

		writeFile(power, p);
	}

	private void writeFile(File file, String value) {
		FileWriter writer = null;
		try {
			writer = new FileWriter(file);
			writer.write(value);
			writer.flush();
			writer.close();
		} catch (IOException e1) {
			e1.printStackTrace();
		}
	}
}
