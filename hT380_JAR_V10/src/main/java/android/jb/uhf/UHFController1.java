package android.jb.uhf;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import android.content.Context;
import android.jb.utils.Tools;
import android.widget.SlidingDrawer;
import android_serialport_api.SerialPort;

public class UHFController1{
	public static final int Action_Idle = -1;
	public static final int Action_FindCardContiue = 3;
	public static final int Action_FindCardContiue_NotStop = 10;
	private static UHFController1 mUHFCon;
	private int currentAction = Action_Idle;
	private Callback listener;
	private static Object lock = new Object();
	
	private InputStream in;
	private OutputStream out;
	private final String RootPath = "/sys/devices/platform/uhf/";
	private String serialPort_Path = "/dev/ttySAC3";	//串口地址
	private final String dc_power = "dc_power";
	private final String en = "en";
	private final String com = "com";
	private ReadThread readThread;
	private SerialPort sP;
	public boolean isReceive = false;
	private long setting_waitTime = 100;

	private UHFController1() {
	}

	public static UHFController1 getInstance() {
		if (mUHFCon == null) {
			synchronized (lock) {
				if (mUHFCon == null) {
					mUHFCon = new UHFController1();
				}
			}
		}
		return mUHFCon;

	}

	/**
	 * 
	 * @author Ivan.Wang 2015-4-20
	 */
	public interface Callback {
		/**
		 * 回执错误回调接口
		 * 
		 * @param errorBean
		 */
		void onOprateFailed(int errorCode);

		/**
		 * 循环巡卡回调接口
		 * 
		 * @param epcList
		 *            数据返回队列
		 */
		void onFindCardContinue(List<CardBean> epcList);

	}

	/**
	 * 初始化模块
	 * 
	 * @param context
	 * @param baud
	 *            波特率
	 * @param listener
	 *            结果返回监听器
	 */
	public void UHF_Open(Context context, int baud, Callback listener) {
		this.listener = listener;
		power_up();
		openserialPort(baud);
		try {
			Thread.sleep(300);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}


	/**
	 * 关闭模块
	 * 
	 * @return　true,关闭成功;false 关闭失败
	 */
	public boolean UHF_Close() {
		return closeSerialPort() && power_down();
	}

	/**
	 * 开始巡卡　
	 * 
	 * @param times
	 *            　巡卡次数
	 */
	public void UHF_FindCardContinue(int times) {
		byte[] command = UHFCommand.getCommand_FindCardContinue(times);
		System.out
				.println("startFindCard:"
						+ Tools.bytesToHexString(command, 0,
								command.length));
		isReceive = true;
		notifyReader();
		UHF_Write(command);
		if (times == 0) {
			setCurrentAction(Action_FindCardContiue_NotStop);
		} else {
			setCurrentAction(Action_FindCardContiue);
		}
	}

	/**
	 * 停止连续巡卡
	 */
	public boolean UHF_Stop_FindCardContinue() {
		// if(currentAction == Action_FindCardContiue_NotStop){
		byte[] command = UHFCommand.getComand_Stop_Contine_find_Card();
		System.out
				.println("stopContineFindCard:"
						+ Tools.bytesToHexString(command, 0,
								command.length));
		UHF_Write(command);
//		setCurrentAction(Action_StopContiueFindCard);
		
		try {
			Thread.sleep(50);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		Boolean b = UHFCommand
		.paser_parse_StopContiueFindCard(buffer_Read());
		if (b) {
			System.out.println("Action_StopContiueFindCard_RE:succsse");
		} else {
			System.out.println("Action_StopContiueFindCard_RE:failue");
			// stopContineFindCard();
			// return;
		}
		setCurrentAction(Action_Idle);
		return b;
		// }
	}

	/**
	 * 获取反射功率,结果在监听器中回调
	 */
	public String UHF_GetReflection() {
		setCurrentAction(Action_Idle);
		String ref = null;
		byte[] command = UHFCommand.getCommand_Reflection();
		System.out
				.println("getReflection:"
						+ Tools.bytesToHexString(command, 0,
								command.length));
		UHF_Write(command);
//		setCurrentAction(Action_GetReflection);
		try {
			Thread.sleep(100);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		ReflectionBean refBean = UHFCommand.paser_parse_GetReflection(buffer_Read());
		if(null != refBean){
			ref = refBean.power;
			System.out.println("refBean.power:"+refBean.power);
		}
		return ref;
	}

	/**
	 * 获取区域设置,结果在监听器中回调
	 */
	public byte UHF_GetAreaSetting() {
		setCurrentAction(Action_Idle);
		byte area = 0;
		byte[] command = UHFCommand.getComand_Area_Setting();
		System.out
				.println("GetAreaSetting:"
						+ Tools.bytesToHexString(command, 0,
								command.length));
		UHF_Write(command);
//		setCurrentAction(Action_GetReflection);
		try {
			Thread.sleep(50);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		ErrorBean errorBean = new ErrorBean();
		area = UHFCommand.paser_parse_GetAreaSetting(buffer_Read() , errorBean);
		
		return area;
	}
	
	/**
	 * 获取区域设置,结果在监听器中回调
	 */
	public boolean UHF_SetArea(byte isSave , byte area) {
		setCurrentAction(Action_Idle);
		byte[] command = UHFCommand.getComand_Set_Area(isSave , area);
		System.out
				.println("SetArea:"
						+ Tools.bytesToHexString(command, 0,
								command.length));
		UHF_Write(command);
//		setCurrentAction(Action_GetReflection);
		try {
			Thread.sleep(50);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		boolean isSuccess =  UHFCommand.paser_parse_SetArea(buffer_Read());
		return isSuccess;
	}
	
	/**
	 * 单步识别标识
	 * 
	 * @param timeout
	 *            　持续时间
	 */
	public CardBean UHF_FindCardOneOnce(int timeout) {
		setCurrentAction(Action_Idle);
		byte[] command = UHFCommand.getCommand_FindCardOneOnce(timeout);
		System.out
				.println("findCardOnce:"
						+ Tools.bytesToHexString(command, 0,
								command.length));
		UHF_Write(command);
//		setCurrentAction(Action_FindCardOnce);
		
		try {
			Thread.sleep(timeout);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		CardBean data = UHFCommand.paser_parse_FindCardOnce(buffer_Read());
		return data;
	}

	/**
	 * 写数据到卡的区块中
	 * 
	 * @param bank
	 *            　要写到哪个区块，0x01--EPC区,0x03--USR区
	 * @param pwd
	 *            区块密钥
	 * @param startAddress
	 *            　起始地址
	 * @param writeData
	 *            　要写入的数据
	 */
//	public void writeData(byte bank, byte[] pwd, byte[] startAddress,
//			byte[] writeData) {
//		byte[] command = UHFCommand.getCommand_WriteData(bank, pwd,
//				startAddress, writeData);
//		System.out
//				.println("writeData:"
//						+ Tools.bytesToHexString(command, 0,
//								command.length));
//		UHF_Write(command);
//		setCurrentAction(Action_WriteData);
//	}

	/**
	 * 带过滤的写数据到卡中
	 * 
	 * @param bank
	 *            ,要写入的区块,0x01--EPC区,0x03--USR区;
	 * @param pwd
	 *            ,区块的密钥
	 * @param startAddress
	 *            起始地址
	 * @param writeData
	 *            　要写入的数据
	 * @param filterArr
	 *            　要过滤的数据
	 * @param filterTag
	 *            　要过的区块,0x01--EPC区,0x03--USR区;
	 */
	public int UHF_WriteData(byte bank, byte[] pwd, byte[] startAddress,
			byte[] writeData, int filterAddress , byte[] filterArr, byte filterTag) {
		setCurrentAction(Action_Idle);
		byte[] command = UHFCommand.getCommand_WriteData_With_Filter(bank, pwd,
				startAddress, writeData, filterAddress, filterArr, filterTag);
		System.out
				.println("writeData:"
						+ Tools.bytesToHexString(command, 0,
								command.length));
		UHF_Write(command);
//		setCurrentAction(Action_WriteData);
		
		try {
			Thread.sleep(100);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		byte[] buffer = buffer_Read();
		if(null == buffer || buffer.length <= 0){
			System.err.println("writedata read is null!");
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			buffer = buffer_Read();
			
			if(null == buffer || buffer.length <= 0){
				System.err.println("writedata read is null!");
				try {
					Thread.sleep(150);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				buffer = buffer_Read();
			}
		}
		ErrorBean errorBean2 = new ErrorBean();
		UHFCommand.paser_parse_WriteData(buffer ,
				errorBean2);
		return errorBean2.errorCode;
	}

	/**
	 * 设置设备功率
	 * 
	 * @param antennaNumber
	 *            　天线号
	 * @param readPower
	 *            　读功率
	 * @param writePower
	 *            　写功率
	 * @param isOpenLoop
	 *            　是否开环
	 * @param isSave
	 *            　是否断电保存
	 */
	public boolean UHF_SetPower(int antennaNumber, int readPower, int writePower,
			boolean isOpenLoop, boolean isSave) {
		setCurrentAction(Action_Idle);
		byte[] command = UHFCommand.getCommand_SetPower(antennaNumber,
				readPower, writePower, isOpenLoop, isSave);
		System.out
				.println("setPower:"
						+ Tools.bytesToHexString(command, 0,
								command.length));
		UHF_Write(command);
//		setCurrentAction(Action_SetPower);
		
		try {
			Thread.sleep(50);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return UHFCommand.paser_parse_SetPower(buffer_Read());
	}

	/**
	 * 获取TID,结果在监听器中回调
	 */
	public ResultBean UHF_GetTID(){
		setCurrentAction(Action_Idle);
		ResultBean resultBean = new ResultBean();
		byte[] command = UHFCommand.getCommand_ReadData((byte) 0x02,
				new byte[] { 0x00, 0x00, 0x00, 0x00 },
				new byte[] { 0x00, 0x00 }, 4);

		System.out
				.println("getTID:"
						+ Tools.bytesToHexString(command, 0,
								command.length));
		UHF_Write(command);
//		setCurrentAction(Action_GetTID);
		
		try {
			Thread.sleep(100);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		byte[] buffer = buffer_Read();
		if(null == buffer || buffer.length <= 0){
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			buffer = buffer_Read();
			
			if(null == buffer || buffer.length <= 0){
				try {
					Thread.sleep(150);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				buffer = buffer_Read();
			}
		}
		ErrorBean errorBean = UHFCommand.paser_parse_Oprate_Failed(buffer);
		resultBean.setErrorCode(errorBean.errorCode);
		if(errorBean.errorCode == -1){
			byte[] readData = UHFCommand.paser_parse_ReadData(buffer , errorBean);
			resultBean.setResultData(readData);
		}
		return resultBean;
	}

	/**
	 * 获取设备版本,结果在监听器中回调
	 */
	public String UHF_GetVersion() {
		setCurrentAction(Action_Idle);
		String version = null;
		int[] re = null;

		byte[] command = UHFCommand.getCommand_Version();
		System.out
				.println("getDeviceVersion:"
						+ Tools.bytesToHexString(command, 0,
								command.length));
		UHF_Write(command);
//		setCurrentAction(Action_GetVersion);
		try {
			Thread.sleep(50);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		re = UHFCommand.parser_parse_version(buffer_Read());
		if (re != null) {
			version = re[0] + "." + re[1] + "."
					+ re[2];
		}
		return version;
	}

	/**
	 * 获取设备ID,结果在监听器中回调
	 */
	public long UHF_GetID() {
		setCurrentAction(Action_Idle);
		byte[] command = UHFCommand.getCommand_ID();
		System.out
				.println("getDeviceId:"
						+ Tools.bytesToHexString(command, 0,
								command.length));
		UHF_Write(command);
//		setCurrentAction(Action_GetID);
		try {
			Thread.sleep(50);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		long id = UHFCommand.parser_parse_ID(buffer_Read());
		return id;
	}

	/**
	 * 读取数据
	 * 
	 * @param bank
	 *            0x01 means EPC,0x02 means TID,0x03 means USR;
	 * @param pwd
	 *            password default is 0x00000000;
	 * @param startAddress
	 *            start from 2,the unit is word
	 * @param length
	 *            the unit is word,two bits;
	 */
//	public void readData(byte bank, byte[] pwd, byte[] startAddress, int length) {
//		byte[] command = UHFCommand.getCommand_ReadData(bank, pwd,
//				startAddress, length);
//		System.out
//				.println("readData:"
//						+ Tools.bytesToHexString(command, 0,
//								command.length));
//		UHF_Write(command);
//		setCurrentAction(Action_ReadData);
//
//	}

	/**
	 * 带过滤的读数据
	 * 
	 * @param bank
	 *            要读的区块,0x01--EPC区;0x02--TID区;0x03--USR区
	 * @param pwd
	 *            区块密钥
	 * @param startAddress
	 *            超始地址
	 * @param length
	 *            　要读取的长度
	 * @param filterArr
	 *            　要过滤的数据
	 * @param filterTag
	 *            　要过滤的区块,0x01--EPC区;0x02--TID区;0x03--USR区
	 */
	public ResultBean UHF_ReadData(byte bank, byte[] pwd, byte[] startAddress,
			int length, int filterAddress, byte[] filterArr, byte filterTag) {
		// byte[] command = new byte[] { (byte) 0xa5, 0x5a, 0x00, 0x18,
		// (byte) 0x84, 0x00, 0x00, 0x00, 0x00, 0x02, 0x00, 0x02, 0x00,
		// 0x0d, (byte) 0xe2, 0x00, 0x01, 0x00, 0x02, 0x00, 0x06, 0x76,
		// 0x0d, 0x0a };
		setCurrentAction(Action_Idle);
		ResultBean resultBean = new ResultBean();
		byte[] command = UHFCommand.getCommand_ReadData_With_Filter(bank, pwd,
				startAddress, length, filterAddress, filterArr, filterTag);
		System.out
				.println("readData:"
						+ Tools.bytesToHexString(command, 0,
								command.length));
		UHF_Write(command);
//		setCurrentAction(Action_ReadData);
		
		try {
			Thread.sleep(100);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		byte[] buffer = buffer_Read();
		
		if(null == buffer || buffer.length <= 0){
			System.out.println("readdata read is null!");
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			buffer = buffer_Read();
			
			if(null == buffer || buffer.length <= 0){
				try {
					Thread.sleep(150);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				buffer = buffer_Read();
			}
		}

		ErrorBean errorBean = UHFCommand.paser_parse_Oprate_Failed(buffer);
		resultBean.setErrorCode(errorBean.errorCode);
		if(errorBean.errorCode == -1){
			byte[] readData = UHFCommand.paser_parse_ReadData(buffer , errorBean);
			resultBean.setResultData(readData);
		}
		return resultBean;
	}

	/**
	 * 获取发射功率
	 */
	public TransmittedPowerMsgBean UHF_GetTransmittedPower() {
		setCurrentAction(Action_Idle);
		byte[] command = UHFCommand.getCommand_TransmittedPower();
		// System.out
		// .println("getTransmittedPower:"
		// + Tools.bytesToHexString(command, 0,
		// command.length));
		UHF_Write(command);
//		setCurrentAction(Action_GetTransmittedPower);
		try {
			Thread.sleep(50);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		TransmittedPowerMsgBean transmittedPowerMsgBean = UHFCommand
				.paser_parse_TransmittedPower(buffer_Read());
		return transmittedPowerMsgBean;
	}
	
	public ResultBean UHF_Setting(byte type ,byte[] data){
		setCurrentAction(Action_Idle);
		ResultBean resultBean = new ResultBean();
		byte[] command = UHFCommand.createCommand(type , data);
		System.out.println("UHF_Setting:"+Tools.bytesToHexString(command));
		UHF_Write(command);
		
		try {
			Thread.sleep(setting_waitTime);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		byte[] buffer = buffer_Read();
		
		if(null == buffer || buffer.length <= 0){
			System.err.println("UHF_Setting read is null!");
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			buffer = buffer_Read();
			
			if(null == buffer || buffer.length <= 0){
				System.err.println("UHF_Setting read is null!");
				try {
					Thread.sleep(150);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				buffer = buffer_Read();
			}
		}
		ErrorBean errorBean = UHFCommand.paser_parse_Oprate_Failed(buffer);
		resultBean.setErrorCode(errorBean.errorCode);
		resultBean.setResultData(buffer);
		return resultBean;
	}

	private void setCurrentAction(int action) {
		System.out.println("isRecrive:"+isReceive);
		if(action == Action_Idle){
			isReceive = false;
		}else {
			isReceive = true;
		}
		this.currentAction = action;
	}

	public boolean isOprateSucces(byte[] temp) {
		if(null != temp){
			System.out.println("isOprateSucces:"
					+ Tools.bytesToHexString(temp, 0, temp.length));
			ErrorBean bean = UHFCommand.paser_parse_Oprate_Failed(temp);
			if (bean != null && bean.errorCode != -1) {
				setCurrentAction(Action_Idle);
				System.out.println("UHF Error:"+bean.errorCode);
				if (listener != null) {
					int errorcode = bean.errorCode;
					listener.onOprateFailed(errorcode);
				}
				return false;
			}
			return true;
		}
		return false;
	}

	public void handDetail(byte[] temp) {
		// TODO Auto-generated method stub
		switch (currentAction) {
		case Action_FindCardContiue:
			System.out.println("Action_FindCardContiue_RE:"
					+ Tools.bytesToHexString(temp, 0, temp.length));
			List<CardBean> epcList = UHFCommand
					.paser_parse_FindCardContinue(temp);
			if(epcList != null && epcList.size() > 0){
				if(epcList.get(epcList.size()-1).isEnd()){
					System.out.println("Action_FindCardContiue_RE:End");
					epcList.remove(epcList.get(epcList.size()-1));
					setCurrentAction(Action_Idle);
				}
			}
			if (listener != null) {
				listener.onFindCardContinue(epcList);
			}
			break;
		case Action_FindCardContiue_NotStop:
			System.out.println("Action_FindCardContiue_RE:"
					+ Tools.bytesToHexString(temp, 0, temp.length));
			List<CardBean> epcList2 = UHFCommand
					.paser_parse_FindCardContinue_NotStop(temp);
			if (listener != null) {
				listener.onFindCardContinue(epcList2);
			}
			epcList2 = null;
			break;
		default:
			break;
		}
	}

	/**
	 * 打开串口,连接到超高频模块
	 * 
	 * @param baud
	 *            波特率
	 * @return true,关闭成功;false 关闭失败
	 */
	private boolean openserialPort(int baud) {
		try {
//			closeSerialPort();
			sP = new SerialPort(new File(serialPort_Path), baud, 8, 'N', 1, 0);
//			sP = new SerialPort(new File(serialPort_Path), baud, 0, false);
			in = sP.getInputStream();
			out = sP.getOutputStream();
			readThread = new ReadThread();
			readThread.run = true;
			readThread.start();
			return true;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}
	}
	
	/**
	 * 关闭串口,断开与超高频模块的连接
	 * 
	 * @return
	 */
	private boolean closeSerialPort() {
		// TODO Auto-generated method stub
		try {
//			stopContineFindCard();
			notifyReader();
			if (in != null)
				in.close();
			if (out != null)
				out.close();
			if (readThread != null)
				readThread.run = false;
			readThread = null;
			in = null;
			out = null;
			if (sP != null)
				sP.close();
			sP = null;
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
			return false;
		}
		return true;

	}

	/**
	 * 选择串口地址 默认新版串口3
	 * @param i 1:串口1 主要基于1.01以前的旧版 3:串口3 主要基于1.01以后的新版（可以进入手机设置——关于手机——版本号查看）
	 */
	private void setSerialPort_Path(int i){
		if(i == 1){
			serialPort_Path = "/dev/ttySAC1";
		}else {
			serialPort_Path = "/dev/ttySAC3";
		}
	}
	
	public void setSettingTime(long time){
		this.setting_waitTime = time;
	}
	
	public long getSettingTime(){
		return setting_waitTime;
	}
	
	private void notifyReader() {
		if (readThread != null && readThread.isAlive()) {
			readThread.interrupt();
		}
	}

	/**
	 * 写指令到模块
	 * 
	 * @param data
	 * @return
	 */
	public synchronized boolean UHF_Write(byte[] data) {
		try {
			if (out != null) {
				power_up();
				Thread.sleep(10);
				out.write(data);
				out.flush();
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
	 * 超高频上电
	 * 
	 * @return
	 */
	private boolean power_up() {
		return writeFile(dc_power, 1) && writeFile(com, 1) && writeFile(en, 1);
	}

	/**
	 * 超高频下电
	 * 
	 * @return
	 */
	private boolean power_down() {
		return writeFile(en, 0) && writeFile(com, 0) && writeFile(dc_power, 0);
	}
	
	private class ReadThread extends Thread {
		public boolean run;

		@Override
		public void run() {
			// TODO Auto-generated method stub
			while (run) {
				// if (currentAction == Action_Idle && isInitFinish) {
				// setCurrentAction(Action_GetTransmittedPower);
				// getTransmittedPower();
				//
				// }
				if (isReceive) {
					if (in != null) {
						try {
							int len = in.available();
							if (len > 0) {
								sleep(10);
								while (len < in.available()) {
									sleep(20);
									len = in.available();
								}
								byte[] temp = new byte[len];
								in.read(temp);
								if (listener != null) {
//									UHF_Read(temp);
									if (isOprateSucces(temp)) {
										handDetail(temp);
									}
								}
								temp = null;
							}
						} catch (Exception e) {
							// TODO: handle exception
							e.printStackTrace();
						}
					}
					try {
						Thread.sleep(10);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
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

	private synchronized boolean writeFile(String name, int status) {
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
	
	private synchronized byte[] buffer_Read(){
		byte[] buffer = null;
		try {
			if(null != in){
				int len = in.available();
				if(len > 0){
					Thread.sleep(10);
					while (len < in.available()) {
						len = in.available();
						Thread.sleep(10);
					}
					len = in.available();
					buffer = new byte[len];
					in.read(buffer);
				}
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InterruptedException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		return buffer;
	}
}
