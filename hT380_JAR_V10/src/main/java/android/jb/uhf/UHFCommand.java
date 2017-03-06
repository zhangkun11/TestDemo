package android.jb.uhf;

import java.util.ArrayList;
import java.util.List;

import android.jb.uhf.TransmittedPowerMsgBean.Antenna;
import android.jb.utils.Tools;
import android.util.Log;

/**
 * 指令集合
 * 
 * @author Ivan.Wang 2015-5-28
 */
public class UHFCommand {
	private static byte[] serialPortData_buffer = null; // 缓存断开数据

	/**
	 * get device version command
	 * 
	 * @return command byte
	 */
	public static byte[] getCommand_Version() {
		return createCommand((byte) 0x02, null);
	}

	/**
	 * get device id command
	 * 
	 * @return command byte
	 */
	public static byte[] getCommand_ID() {
		return createCommand((byte) 0x04, null);
	}

	/**
	 * get transmittedPower command
	 * 
	 * @return
	 */
	public static byte[] getCommand_TransmittedPower() {
		// TODO Auto-generated method stub
		return createCommand((byte) 0x12, null);
	}

	/**
	 * get find card command
	 * 
	 * @return
	 */
	public static byte[] getCommand_FindCardContinue(int times) {
		byte timesH = (byte) ((times >> 8 & 0x00FF));
		byte timesL = (byte) (times & 0x00FF);
		return createCommand((byte) 0x82, new byte[] { timesH, timesL });
	}

	public static byte[] getComand_Stop_Contine_find_Card() {
		byte[] comand = { (byte) 0xA5, (byte) 0x5A, (byte) 0x00, (byte) 0x08,
				(byte) 0x8C, (byte) 0x84, (byte) 0x0D, (byte) 0x0A };
		return comand;
	}

	public static byte[] getComand_Area_Setting() {
		// A5 5A 00 08 2E 26 0D 0A
		byte[] comand = { (byte) 0xA5, (byte) 0x5A, (byte) 0x00, (byte) 0x08,
				(byte) 0x2E, (byte) 0x26, (byte) 0x0D, (byte) 0x0A };
		return comand;
	}

	public static byte[] getComand_Set_Area(byte isSave, byte area) {
		// A5 5A 00 0A 2C 01 08 2F 0D 0A
		return createCommand((byte) 0x2C, new byte[] { isSave, area });
	}

	/**
	 * search card once,it will stop searching when find a card or time out
	 * 
	 * @param timeout
	 * @return
	 */
	public static byte[] getCommand_FindCardOneOnce(int timeout) {
		byte tH = (byte) ((timeout >> 8) & 0xFF);
		byte tL = (byte) (timeout & 0xFF);
		return createCommand((byte) 0x80, new byte[] { tH, tL });
	}

	/**
	 * 
	 * @param antennaNumber
	 *            antenna number,such as 0,1,2
	 * @param readPower
	 *            use dBm as unit
	 * @param writePower
	 *            use dBm as unit
	 * @param isOpenLoop
	 * @param isSave
	 *            whether keep the params after device power off;
	 * @return
	 */
	public static byte[] getCommand_SetPower(int antennaNumber, int readPower,
			int writePower, boolean isOpenLoop, boolean isSave) {
		byte annN = (byte) (antennaNumber & 0x00FF);
		byte status = (byte) 0x00;
		byte readH = 0x00, readL = 0x00, writeH = 0x00, writeL = 0x00;
		if (!isOpenLoop) {
			status = (byte) (status | 0x01);
		}
		if (isSave) {
			status = (byte) (status | 0x02);
		}
		int tempR = readPower * 100;
		int tempW = writePower * 100;
		readH = (byte) ((tempR >> 8) & 0x00FF);
		readL = (byte) (tempR & 0x00FF);
		writeH = (byte) ((tempW >> 8) & 0x00FF);
		writeL = (byte) (tempW & 0x00FF);
		return createCommand((byte) 0x10, new byte[] { status, annN, readH,
				readL, writeH, writeL });
	}

	/**
	 * 
	 * @param bank
	 *            0x01 means EPC,0x02 means TID,0x03 means USR;
	 * @param pwd
	 *            password default is 0x00000000;
	 * @param startAddress
	 *            start from 2,the unit is word
	 * @param length
	 *            the unit is word,two bits;
	 * @return
	 */
	public static byte[] getCommand_ReadData(byte bank, byte[] pwd,
			byte[] startAddress, int length) {
		byte[] data = new byte[14];
		if (pwd != null) {
			if (pwd.length > 0)
				data[0] = pwd[0];
			if (pwd.length > 1)
				data[1] = pwd[1];
			if (pwd.length > 2)
				data[2] = pwd[2];
			if (pwd.length > 3)
				data[3] = pwd[3];
		}
		data[9] = bank;
		if (startAddress != null) {
			if (startAddress.length > 0) {
				data[10] = startAddress[0];
			}
			if (startAddress.length > 1) {
				data[11] = startAddress[1];
			}
		}
		// if (data[10] == (byte) 0x00 && data[11] == (byte) 0x00) {
		// data[10] = 0x00;
		// data[11] = 0x02;
		// }
		byte lengthH = (byte) ((length >> 8) & 0x00FF);
		byte lengthL = (byte) (length & 0x00FF);
		data[12] = lengthH;
		data[13] = lengthL;
		return createCommand((byte) 0x84, data);
	}

	public static byte[] getCommand_Reflection() {
		return createCommand((byte) 0x32, null);
	}

	public static byte[] getCommand_WriteData(byte bank, byte[] pwd,
			byte[] startAddress, byte[] writeData) {
		if (writeData != null) {
			byte data[] = new byte[14 + writeData.length];
			if (pwd != null) {
				if (pwd.length > 0)
					data[0] = pwd[0];
				if (pwd.length > 1)
					data[1] = pwd[1];
				if (pwd.length > 2)
					data[2] = pwd[2];
				if (pwd.length > 3)
					data[3] = pwd[3];
			}
			data[9] = bank;
			if (startAddress != null) {
				if (startAddress.length > 0) {
					data[10] = startAddress[0];
				}
				if (startAddress.length > 1) {
					data[11] = startAddress[1];
				}
			}
			// if (data[10] == (byte) 0x00 && data[11] == (byte) 0x00) {
			// data[10] = 0x00;
			// data[11] = 0x02;
			// }
			byte lengthH = (byte) (((writeData.length / 2) >> 8) & 0x00FF);
			byte lengthL = (byte) ((writeData.length / 2) & 0x00FF);
			data[12] = lengthH;
			data[13] = lengthL;
			System.arraycopy(writeData, 0, data, 14, writeData.length);
			return createCommand((byte) 0x86, data);
		}
		return null;

	}

	public static byte[] getComand_Read_TID() {
		return createCommand((byte) 0x8e, new byte[] { 0x00, 0x02, 0x00, 0x40 });
	}

	/**
	 * 
	 * @param bank
	 *            area to write: 0x01--epc,0x02--tid,0x03--usr
	 * @param pwd
	 *            password
	 * @param startAddress
	 *            start address
	 * @param writeData
	 *            data to write
	 * @param filterArr
	 *            filter data
	 * @param filterTag
	 *            0x01--epc,0x02--tid,0x03--usr
	 * @return
	 */
	public static byte[] getCommand_WriteData_With_Filter(byte bank,
			byte[] pwd, byte[] startAddress, byte[] writeData,
			int filterAddress, byte[] filterArr, byte filterTag) {
		if (filterArr == null || filterArr.length == 0) {
			return getCommand_WriteData(bank, pwd, startAddress, writeData);
		} else {
			if (writeData != null) {
				byte data[] = new byte[14 + filterArr.length + writeData.length];
				if (pwd != null) {
					if (pwd.length > 0)
						data[0] = pwd[0];
					if (pwd.length > 1)
						data[1] = pwd[1];
					if (pwd.length > 2)
						data[2] = pwd[2];
					if (pwd.length > 3)
						data[3] = pwd[3];
				}
				data[4] = filterTag;
				byte filterAddressH = (byte) (((filterAddress * 8) >> 8) & 0x00FF);
				byte filterAddressL = (byte) ((filterAddress * 8) & 0x00FF);
				data[5] = filterAddressH;
				data[6] = filterAddressL;
//				if (filterAddress != null) {
//					if (filterAddress.length > 0) {
//						data[5] = filterAddress[0];
//					}
//					if (filterAddress.length > 1) {
//						data[6] = filterAddress[1];
//					}
//				}
				// data[5] = 0x00;
				// data[6] = 0x00;
				data[7] = (byte) (((filterArr.length * 8) >> 8) & 0x00FF);
				data[8] = (byte) ((filterArr.length * 8) & 0x00FF);
				System.arraycopy(filterArr, 0, data, 9, filterArr.length);

				data[9 + filterArr.length] = bank;
				if (startAddress != null) {
					if (startAddress.length > 0) {
						data[10 + filterArr.length] = startAddress[0];
					}
					if (startAddress.length > 1) {
						data[11 + filterArr.length] = startAddress[1];
					}
				}
				// if (data[10 + filterArr.length] == (byte) 0x00
				// && data[11 + filterArr.length] == (byte) 0x00) {
				// data[10 + filterArr.length] = 0x00;
				// data[11 + filterArr.length] = 0x02;
				// }
				byte lengthH = (byte) (((writeData.length / 2) >> 8) & 0x00FF);
				byte lengthL = (byte) ((writeData.length / 2) & 0x00FF);
				data[12 + filterArr.length] = lengthH;
				data[13 + filterArr.length] = lengthL;
				System.arraycopy(writeData, 0, data, 14 + filterArr.length,
						writeData.length);
				return createCommand((byte) 0x86, data);
			}
		}

		return null;
	}

	public static byte[] getCommand_ReadData_With_Filter(byte bank, byte[] pwd,
			byte[] startAddress, int length, int filterAddress,
			byte[] filterArr, byte filterTag) {
		if (filterArr == null || filterArr.length == 0) {
			return getCommand_ReadData(bank, pwd, startAddress, length);
		} else {
			byte[] data = new byte[14 + filterArr.length];
			if (pwd != null) {
				if (pwd.length > 0)
					data[0] = pwd[0];
				if (pwd.length > 1)
					data[1] = pwd[1];
				if (pwd.length > 2)
					data[2] = pwd[2];
				if (pwd.length > 3)
					data[3] = pwd[3];
			}
			data[4] = filterTag;
			byte filterAddressH = (byte) (((filterAddress * 8) >> 8) & 0x00FF);
			byte filterAddressL = (byte) ((filterAddress * 8) & 0x00FF);
			data[5] = filterAddressH;
			data[6] = filterAddressL;
//			if (filterAddress != null) {
//				if (filterAddress.length > 0) {
//					data[5] = filterAddress[0];
//				}
//				if (filterAddress.length > 1) {
//					data[6] = filterAddress[1];
//				}
//			}
			// data[5] = 0x00;
			// data[6] = 0x00;
			data[7] = (byte) (((filterArr.length * 8) >> 8) & 0x00FF);
			data[8] = (byte) ((filterArr.length * 8) & 0x00FF);
			System.arraycopy(filterArr, 0, data, 9, filterArr.length);
			data[9 + filterArr.length] = bank;
			if (startAddress != null) {
				if (startAddress.length > 0) {
					data[10 + filterArr.length] = startAddress[0];
				}
				if (startAddress.length > 1) {
					data[11 + filterArr.length] = startAddress[1];
				}
			}
			// if (data[10 + filterArr.length] == (byte) 0x00
			// && data[11 + filterArr.length] == (byte) 0x00) {
			// data[10 + filterArr.length] = 0x00;
			// data[11 + filterArr.length] = 0x02;
			// }
			byte lengthH = (byte) ((length >> 8) & 0x00FF);
			byte lengthL = (byte) (length & 0x00FF);
			data[12 + filterArr.length] = lengthH;
			data[13 + filterArr.length] = lengthL;
			return createCommand((byte) 0x84, data);
		}
	}

	public static ReflectionBean paser_parse_GetReflection(byte[] buffer) {
		if(buffer == null){
			return null;
		}
		System.out.println("paser_parse_GetReflection:"
				+ Tools.bytesToHexString(buffer, 0, buffer.length));
		byte[] reCommand = cutRightData(buffer);
		ReflectionBean bean = null;
		if (reCommand != null && reCommand.length >= 8) {
			if (checkHeadAndSuffix(reCommand) && reCommand[4] == (byte) 0x33) {
				if (reCommand[5] == (byte) 0x01) {
					bean = new ReflectionBean(reCommand[6], reCommand[7],
							reCommand[5]);
				} else {
					bean = new ReflectionBean((byte) 0, (byte) 0, reCommand[5]);
				}
			}
		}
		return bean;
	}

	public static byte paser_parse_GetAreaSetting(byte[] buffer, ErrorBean bean) {
		if(buffer == null){
			return 0;
		}
		System.out.println("paser_parse_GetAreaSetting:"
				+ Tools.bytesToHexString(buffer, 0, buffer.length));
		byte[] reCommand = cutRightData(buffer);
		byte area = 0;
		if (reCommand != null && reCommand.length >= 8) {
			if (checkHeadAndSuffix(reCommand) && reCommand[4] == (byte) 0x2F) {
				if (reCommand[5] == (byte) 0x01) {
					area = reCommand[6];
				} else {
					if (null != bean) {
						bean.errorCode = reCommand[6];
					}
				}
			}
		}
		return area;
	}

	public static boolean paser_parse_SetArea(byte[] buffer) {
		if(buffer == null){
			return false;
		}
		System.out.println("paser_parse_SetArea:"
				+ Tools.bytesToHexString(buffer, 0, buffer.length));
		byte[] reCommand = cutRightData(buffer);
		boolean isSuccess = false;
		if (reCommand != null && reCommand.length >= 8) {
			if (checkHeadAndSuffix(reCommand) && reCommand[4] == (byte) 0x2D) {
				if (reCommand[5] == (byte) 0x01) {
					isSuccess = true;
				}
			}
		}
		return isSuccess;
	}

	public static byte[] paser_parse_Read_TID(byte[] buffer) {
		byte[] reCommand = cutRightData(buffer);
		if (reCommand != null && reCommand.length >= 8) {
			if (checkHeadAndSuffix(reCommand) && reCommand[4] == (byte) 0x8F) {
				if (reCommand[6] == 0x00) {
					int length = ((reCommand[7] << 8) & 0xFF00);
					length = length + reCommand[8];
					byte[] data = new byte[length];
					System.arraycopy(reCommand, 9, data, 0, length);
					return data;
				}
			}
		}
		return null;
	}

	public static List<CardBean> paser_parse_FindCardContinue(byte[] reCommand) {
		List<CardBean> epcList = new ArrayList<CardBean>();
		List<byte[]> temp = new ArrayList<byte[]>();

		if (reCommand != null && reCommand.length >= 8) {
			if (serialPortData_buffer != null) {
				arraycopy(reCommand);
				temp = checkSerialPortData(serialPortData_buffer);
			} else {
				temp = checkSerialPortData(reCommand);
			}
			
			try {
				if (temp != null && temp.size() > 0) {
					for (int i = 0; i < temp.size(); i++) {
						CardBean cardBean = paser_parse_FindCardContine(temp.get(i));
						if (null != cardBean) {
							a: if (epcList.size() > 0) {
								for (int j = 0; j < epcList.size(); j++) {
									if (epcList.get(j).getEpc()
											.equals(cardBean.getEpc())) {
										int time = epcList.get(j).getFindTime();
										time++;
										epcList.get(j).setFindTime(time);
										break a;
									}
								}
								epcList.add(cardBean);
							} else {
								epcList.add(cardBean);
							}
							cardBean = null;
						} else {
							if (i == temp.size() - 1) {
								if (checkContineSuffix(temp.get(i))) {
									cardBean = new CardBean();
									cardBean.setEnd(true);
									epcList.add(cardBean);
								}
							}
						}
					}
				}
			} catch (Exception e) {
				// TODO: handle exception
			}
		}
		return epcList;
	}

	public static List<CardBean> paser_parse_FindCardContinue_NotStop(
			byte[] buffer) {
		byte[] reCommand = cutRightData(buffer);
		List<CardBean> epcList = new ArrayList<CardBean>();
		List<byte[]> temp = new ArrayList<byte[]>();

		try {
			if (reCommand != null && reCommand.length >= 8) {
				if (serialPortData_buffer != null) {
					arraycopy(reCommand);
					temp = checkSerialPortData(serialPortData_buffer);
				} else {
					temp = checkSerialPortData(reCommand);
				}
				if (temp != null && temp.size() > 0) {
					for (int i = 0; i < temp.size(); i++) {
						CardBean cardBean = paser_parse_FindCardContine(temp.get(i));
						if (null != cardBean) {
							a: if (epcList.size() > 0) {
								for (int j = 0; j < epcList.size(); j++) {
									if (epcList.get(j).getEpc()
											.equals(cardBean.getEpc())) {
										int time = epcList.get(j).getFindTime();
										time++;
										epcList.get(j).setFindTime(time);
										break a;
									}
								}
								epcList.add(cardBean);
							} else {
								epcList.add(cardBean);
							}
							cardBean = null;
						}
					}
				}
			}
			return epcList;
		} finally {
			epcList = null;
			temp = null;
		}
		
	}

	// public static List<CardBean> paser_parse_FindCardContinue(byte[]
	// reCommand) {
	// List<CardBean> epcList = new ArrayList<CardBean>();
	//
	// if (reCommand != null && reCommand.length >= 8) {
	// int size = reCommand.length;
	// int srcPos = 0;
	// while (size > 8) {
	// int lenght = Integer.valueOf(reCommand[srcPos + 3]);
	// if (lenght > 0) {
	// byte[] temp = new byte[lenght];
	// if (srcPos + lenght < reCommand.length) {
	// System.arraycopy(reCommand, srcPos, temp, 0, lenght);
	// CardBean cardBean = paser_parse_FindCardContine(temp);
	// if (null != cardBean) {
	// // if (!epcList.contains(epc)) {
	// a: if (epcList.size() > 0) {
	// for (int i = 0; i < epcList.size(); i++) {
	// if (epcList.get(i).getEpc()
	// .equals(cardBean.getEpc())) {
	// int time = epcList.get(i).getFindTime();
	// time++;
	// epcList.get(i).setFindTime(time);
	// break a;
	// }
	// }
	// epcList.add(cardBean);
	// } else {
	// epcList.add(cardBean);
	// }
	// cardBean = null;
	// // }
	//
	// }
	// }
	// size = size - lenght;
	// srcPos += lenght;
	// }
	// }
	//
	// }
	// return epcList;
	// }

	public static boolean paser_parse_WriteData(byte[] buffer, ErrorBean bean) {
		if(buffer == null){
			bean.errorCode = 0;
			return false;
		}
		System.out.println("paser_parse_WriteData:"
				+ Tools.bytesToHexString(buffer));
		byte[] reCommand = cutRightData(buffer);
		if (reCommand != null && reCommand.length >= (byte) 0x0A) {
			if (checkHeadAndSuffix(reCommand) && reCommand[4] == (byte) 0x87) {
				if (reCommand[6] == 0x00) {
					return true;
				} else {
					if (bean != null) {
						bean.errorCode = reCommand[6];
					}
				}
			}
		}
		bean.errorCode = 0;
		return false;
	}

	public static byte[] paser_parse_ReadData(byte[] buffer, ErrorBean bean) {
		if(buffer == null){
			return null;
		}
		byte[] reCommand = cutRightData(buffer);
		System.out.println("paser_parse_ReadData:"
				+ Tools.bytesToHexString(reCommand));
		if (reCommand != null && reCommand.length >= 10) {
			if (checkHeadAndSuffix(reCommand) && reCommand[4] == (byte) 0x85) {
				if (reCommand[5] == (byte) 0x01) {
					int length = ((reCommand[7] << 8) & 0xFF00);
					length = length + (reCommand[8]);
					length = length * 2;
					if (reCommand.length >= 9 + length + 3) {
						byte[] data = new byte[length];
						System.arraycopy(reCommand, 9, data, 0, data.length);
						return data;
					}
				} else {
					if (bean != null)
						bean.errorCode = reCommand[6];
				}
			}
		}
		return null;
	}

	public static boolean paser_parse_SetPower(byte[] buffer) {
		byte[] reCommand = cutRightData(buffer);
		if (reCommand != null && reCommand.length >= (byte) 0x09) {
			if (checkHeadAndSuffix(reCommand) && reCommand[4] == (byte) 0x11) {
				if (reCommand[5] == (byte) 0x01) {
					return true;
				}
			}
		}
		return false;
	}

	public static boolean paser_parse_StopContiueFindCard(byte[] buffer) {
		byte[] reCommand = cutRightData(buffer);
		// A5 5A 00 09 8D 01 85 0D 0A
		if (reCommand != null && reCommand.length >= 9) {
			if (checkHeadAndSuffix(reCommand) && reCommand[4] == (byte) 0x8D) {
				if (reCommand[5] == (byte) 0x01) {
					return true;
				}
			}
		}
		return false;
	}

	public static ErrorBean paser_parse_Oprate_Failed(byte[] reCommand) {
		ErrorBean bean = new ErrorBean();
		if (reCommand != null && reCommand.length >= (byte) 0x0A) {
			if (checkHeadAndSuffix(reCommand) && reCommand[4] == (byte) 0xFF) {
				if (reCommand[5] == 0x04 && reCommand[5] == 0x02) {
					bean.errorCode = 1;
				}else {
					bean.errorCode = reCommand[6];
				}
			}
		}
		return bean;
	}

	public static CardBean paser_parse_FindCardOnce(byte[] buffer) {
		byte[] reCommand = cutRightData(buffer);
		if (reCommand != null && reCommand.length >= 8) {
			if (checkHeadAndSuffix(reCommand) && reCommand[4] == (byte) 0x81) {
				byte[] data = new byte[reCommand.length - 8];
				System.arraycopy(reCommand, 5, data, 0, data.length);
				byte[] epcArr = new byte[data.length - 5];
				System.arraycopy(data, 2, epcArr, 0, epcArr.length);
				byte[] rssiArr = new byte[2];
				rssiArr[0] = data[data.length - 3];
				rssiArr[1] = data[data.length - 2];
				byte[] pcArr = new byte[2];
				pcArr[0] = data[0];
				pcArr[1] = data[1];
				CardBean cardBean = new CardBean();
				cardBean.setEpc(Tools
						.bytesToHexString(epcArr, 0, epcArr.length));
				cardBean.setPcId(Tools.bytesToHexString(pcArr, 0, pcArr.length));
				cardBean.setRssi(Tools.bytesToHexString(rssiArr, 0,
						rssiArr.length));
				cardBean.setFindTime(1);
				return cardBean;
				// StringBuffer sb = new StringBuffer();
				// sb.append(Tools.bytesToHexString(pcArr, 0,
				// pcArr.length));
				// sb.append("+");
				// sb.append(Tools.bytesToHexString(epcArr, 0,
				// epcArr.length));
				// String epc;
				// epc = new String(epcArr , "GBK");
				// epc = Tools.bytesToHexString(epcArr, 0,
				// epcArr.length);
			}
		}
		return null;
	}

	public static CardBean paser_parse_FindCardContine(byte[] reCommand) {
		if (reCommand != null && reCommand.length > 8) {
			if (checkHeadAndSuffix(reCommand) && reCommand[4] == (byte) 0x83) {
				byte[] data = new byte[reCommand.length - 8];
				System.arraycopy(reCommand, 5, data, 0, data.length);
				byte[] epcArr = new byte[data.length - 5];
				System.arraycopy(data, 2, epcArr, 0, epcArr.length);
				byte[] rssiArr = new byte[2];
				rssiArr[0] = data[data.length - 3];
				rssiArr[1] = data[data.length - 2];
				byte[] pcArr = new byte[2];
				pcArr[0] = data[0];
				pcArr[1] = data[1];
				CardBean cardBean = new CardBean();
				cardBean.setEpc(Tools
						.bytesToHexString(epcArr, 0, epcArr.length));
				cardBean.setPcId(Tools.bytesToHexString(pcArr, 0, pcArr.length));
				cardBean.setRssi(Tools.bytesToHexString(rssiArr, 0,
						rssiArr.length));
				cardBean.setFindTime(1);
				return cardBean;
				// StringBuffer sb = new StringBuffer();
				// sb.append(Tools.bytesToHexString(pcArr, 0,
				// pcArr.length));
				// sb.append("+");
				// sb.append(Tools.bytesToHexString(epcArr, 0,
				// epcArr.length));
				// String epc;
				// epc = new String(epcArr , "GBK");
				// epc = Tools.bytesToHexString(epcArr, 0,
				// epcArr.length);
				// return sb.toString();
			}
		}
		return null;
	}

	public static TransmittedPowerMsgBean paser_parse_TransmittedPower(
			byte[] buffer) {
		if(buffer == null){
			return null;
		}
		System.out.println("paser_parse_TransmittedPower:"
				+ Tools.bytesToHexString(buffer, 0, buffer.length));
		byte[] reCommand = cutRightData(buffer);
		TransmittedPowerMsgBean bean = null;
		if (reCommand != null && reCommand.length >= 9) {
			if (checkHeadAndSuffix(reCommand) && reCommand[4] == (byte) 0x13) {
				bean = new TransmittedPowerMsgBean();
				bean.status = reCommand[5];
				int l = reCommand.length - 9;
				if (l > 0) {
					int j = 1;
					Antenna an = bean.new Antenna();
					for (int i = 6; i < reCommand.length - 3; i++) {

						switch (j) {
						case 1:
							an.number = reCommand[i];
							break;
						case 2:
							an.rH = reCommand[i];
							break;
						case 3:
							an.rL = reCommand[i];
							break;
						case 4:
							an.wH = reCommand[i];
							break;
						case 5:
							an.wL = reCommand[i];
							break;
						default:
							break;
						}
						j++;
						if (j > 5) {
							j = 1;
							an.calculatePower();
							bean.antennaList.add(an);
							an = bean.new Antenna();
						}

					}

				}
			}
		}
		return bean;
	}

	/**
	 * parser ID return
	 * 
	 * @param reCommand
	 *            command return ;
	 * @return id; -1 parse filed
	 */
	public static long parser_parse_ID(byte[] buffer) {
		byte[] reCommand = cutRightData(buffer);
		if (reCommand != null && reCommand.length == 12) {
			if (checkHeadAndSuffix(reCommand) && reCommand[4] == (byte) 0x05) {
				long re = 0;
				byte eL = (byte) (reCommand[8] & 0x0F);
				byte eH = (byte) ((reCommand[8] >> 4) & 0x0F);
				byte seL = (byte) (reCommand[7] & 0x0F);
				byte seH = (byte) ((reCommand[7] >> 4) & 0x0F);
				byte siL = (byte) (reCommand[6] & 0x0F);
				byte siH = (byte) ((reCommand[6] >> 4) & 0x0F);
				byte fL = (byte) (reCommand[5] & 0x0F);
				byte fH = (byte) ((reCommand[5] >> 4) & 0x0F);
				re = eL + eH * 16l + seL * 256l + seH * 4096l + siL * 65536l
						+ siH * 1048576l + fL * 16777216l + fH * 268435456l;
				return re;

			}
		}
		return -1;
	}

	/**
	 * parse Version return,
	 * 
	 * @param reCommand
	 *            command return;
	 * @return re[0]--main version no;re[1]--subversion no; re[2]--supplemental
	 *         version;
	 */
	public static int[] parser_parse_version(byte[] reCommand) {
		if(reCommand == null){
			return null;
		}
		int[] re = null;
		byte[] buffer = cutRightData(reCommand);
		if (buffer != null && buffer.length >= 11) {
			System.out.println("parser_parse_version:"
					+ Tools.bytesToHexString(reCommand, 0, reCommand.length));
			if (checkHeadAndSuffix(buffer) && buffer[4] == (byte) 0x03) {
				re = new int[3];
				re[0] = buffer[5];
				re[1] = buffer[6];
				re[2] = buffer[7];
			}
		}
		return re;
	}

	private static byte[] cutRightData(byte[] reCommand) {
		byte[] cutBuffer = null;
		if (null != reCommand) {
			for (int i = 0; i < reCommand.length; i++) {
				if (i < (reCommand.length - 1) && reCommand[i] == (byte) 0xA5
						&& reCommand[i + 1] == (byte) 0x5A) {
					cutBuffer = new byte[reCommand.length - i];
					System.arraycopy(reCommand, i, cutBuffer, 0,
							cutBuffer.length);
					break;
				}
			}
		}
		return cutBuffer;
	}

	private static boolean checkHeadAndSuffix(byte[] reCommand) {
		// TODO Auto-generated method stub
		if (reCommand != null && reCommand.length >= 4
				&& reCommand[0] == (byte) 0xA5 && reCommand[1] == (byte) 0x5A
				&& reCommand[reCommand.length - 1] == (byte) 0x0A
				&& reCommand[reCommand.length - 2] == (byte) 0x0D) {
			return true;
		}
		return false;
	}

	/**
	 * 
	 * @param type
	 *            　command type
	 * @param data
	 *            command data
	 * @return
	 */
	public static byte[] createCommand(byte type, byte[] data) {
		byte[] command = null;
		int length = 0;
		if (data != null) {
			length = 8 + data.length;
			command = new byte[length];
			fillHeadAndSuffix(command);
			fillLengthByte(command);
			command[4] = type;
			for (int i = 0; i < data.length; i++) {
				command[i + 5] = data[i];
			}
			fillCheckSum(command);
		} else {
			length = 8;
			command = new byte[length];
			fillHeadAndSuffix(command);
			fillLengthByte(command);
			command[4] = type;
			fillCheckSum(command);

		}

		return command;
	}

	private static void fillCheckSum(byte[] command) {
		// TODO Auto-generated method stub
		if (command != null && command.length >= 8) {
			byte check = command[2];
			for (int i = 3; i < command.length - 2; i++) {
				check = (byte) ((check ^ command[i]) & 0xFF);
			}
			command[command.length - 3] = check;
		}
	}

	private static void fillLengthByte(byte[] command) {
		// TODO Auto-generated method stub
		if (command != null) {
			int length = command.length;
			if (length > 0xFF) {
				command[2] = (byte) ((length >> 8) & 0xFF);
			} else {
				command[2] = 0;
			}
			command[3] = (byte) (length & 0xFF);
		}
	}

	private static void fillHeadAndSuffix(byte[] command) {
		// TODO Auto-generated method stub
		if (command != null && command.length >= 4) {
			command[0] = (byte) 0xA5;
			command[1] = (byte) 0x5A;
			command[command.length - 1] = 0x0A;
			command[command.length - 2] = 0x0D;
		}
	}

	private static boolean checkContineSuffix(byte[] command) {
		// TODO Auto-generated method stub
		// a55a0008838b0d0a
		if (command != null && command.length >= 8 && command[0] == (byte) 0xA5
				&& command[1] == (byte) 0x5A && command[2] == (byte) 0x00
				&& command[3] == (byte) 0x08 && command[4] == (byte) 0x83
				&& command[5] == (byte) 0x8b && command[6] == (byte) 0x0d
				&& command[7] == (byte) 0x0a) {
			return true;
		}
		return false;
	}

	private static ArrayList<byte[]> checkSerialPortData(byte[] serialPortData) {
		ArrayList<byte[]> list = new ArrayList<byte[]>();
		// int iStart = 0;
		// int iEnd = 0;
		serialPortData_buffer = null;
		boolean isHasHead = false;
		for (int i = 0; i < serialPortData.length; i++) {

			if (i < serialPortData.length && serialPortData[i] == (byte) 0xA5
					&& serialPortData[i + 1] == (byte) 0x5A) {
				isHasHead = true;
				// iStart = i;
			}

			if (isHasHead) {
				if (i + 3 <= (serialPortData.length - 1)) {
					int lenght = Integer.valueOf(serialPortData[i + 3]);
					if (lenght <= (serialPortData.length - i)) {
						byte[] temp = new byte[lenght];
						int index = 0;
						for (int j = i; j < i + lenght; j++) {
							temp[index] = serialPortData[j];
							index++;
						}
						list.add(temp);
						isHasHead = false;
						i = i + lenght - 1;
						// iStart = 0;
						// iEnd = 0;
					} else {
						serialPortData_buffer = new byte[serialPortData.length
								- i];
						int index = 0;
						for (int k = i; k < serialPortData.length; k++) {
							serialPortData[index] = serialPortData[k];
							index++;
						}
						break;
					}
				} else {
					serialPortData_buffer = new byte[serialPortData.length - i];
					int index = 0;
					for (int k = i; k < serialPortData.length; k++) {
						serialPortData[index] = serialPortData[k];
						index++;
					}
					break;
				}
			}
		}
		Log.i("time", "checkSerialPortData end:" + Tools.getNowTimeHm());
		return list;
	}

	private static void arraycopy(byte[] portData) {
		if (null != serialPortData_buffer) {
			byte[] data3 = new byte[serialPortData_buffer.length
					+ portData.length];
			System.arraycopy(serialPortData_buffer, 0, data3, 0,
					serialPortData_buffer.length);
			System.arraycopy(portData, 0, data3, serialPortData_buffer.length,
					portData.length);
			serialPortData_buffer = new byte[data3.length];
			serialPortData_buffer = data3;
		}
	}
}
