package android.jb.electrictorch;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

public class ElectrictorchController {

	private File power = new File("/sys/devices/soc.0/m9_dev.69/torch_light");//1开，0关
	private static ElectrictorchController controller;
	private static Object object = new Object();
	
	public static ElectrictorchController getInstance() {
		if(null == controller){
			synchronized (object) {
				controller = new ElectrictorchController();
			}
		}
		return controller;
	}
	
	public void Electrictorch_Open(){
		writeFile(power, "1");
	}
	
	public void Electrictorch_Close(){
		writeFile(power, "0");
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
}
