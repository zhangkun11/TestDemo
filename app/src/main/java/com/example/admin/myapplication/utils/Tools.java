package com.example.admin.myapplication.utils;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Method;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Tools {

	public static final DateFormat format = new SimpleDateFormat("yyyy-MM-dd");
	public static final DateFormat formatd = new SimpleDateFormat("yyyyMMdd");
	public static final DateFormat formatw = new SimpleDateFormat(
			"yyyy-MM-dd HH:mm:ss");
	public static final DateFormat formatw2 = new SimpleDateFormat(
			"yyyy-MM-dd_HH:mm:ss");
	public static final DateFormat formatws = new SimpleDateFormat(
			"yyyy-MM-dd HH:mm:ss.SSS");
	public static final DateFormat formatws2 = new SimpleDateFormat(
			"yyyyMMddHHmmssSSS");
	public static final DateFormat formatm = new SimpleDateFormat("MM-dd HH:mm");
	public static final DateFormat formats = new SimpleDateFormat("MM-dd");
	public static final DateFormat matter_cn = new SimpleDateFormat(
			"日期:yyyy年MM月dd日E HH时mm分ss秒");
	public static final DateFormat formatrqxq = new SimpleDateFormat(
			"yyyy年MM月dd日 E");
	public static final DateFormat formatsj = new SimpleDateFormat("HH:mm:ss");
	public static final DateFormat formatsjfz = new SimpleDateFormat("HH:mm");
	public static final DateFormat formatsjfzss = new SimpleDateFormat(
			"HH:mm:ss.SSS");
	private static File versionFile = new File(
			"/sys/devices/platform/exynos4412-adc/ver");
	private static boolean showLog = true;
	private static String setting_path = "/data/jb_config.xml";

	public static String getFileName() {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        String date = format.format(new Date(System.currentTimeMillis()));
        return date;// 2012年10月03日 23:41:31  
    }  
  
    public static String getDateEN() {
        SimpleDateFormat format1 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String date1 = format1.format(new Date(System.currentTimeMillis()));
        return date1;// 2012-10-03 23:41:31  
    } 
    
	/**
	 * "yyyy��MM��dd�� E"
	 * 
	 * @return
	 */
	public static String getNowDateWeek() {
		Date date = new Date();
		return formatrqxq.format(date);
	}

	/**
	 * "HH:mm:ss"
	 * 
	 * @return
	 */
	public static String getNowTime() {
		Date date = new Date();
		return formatsj.format(date);
	}

	/**
	 * HH:mm
	 * 
	 * @return
	 */
	public static String getNowTimeFz() {
		Date date = new Date();
		return formatsjfz.format(date);
	}

	/**
	 * HH:mm:ss:SSS
	 * 
	 * @return
	 */
	public static String getNowTimeHm() {
		Date date = new Date();
		return formatsjfzss.format(date);
	}

	/**
	 * "yyyy-MM-dd HH:mm:ss"
	 * 
	 * @return
	 */
	public static String getNowTimeString() {
		Date date = new Date();
		return formatw.format(date);
	}

	/**
	 * yyyy-MM-dd_HH:mm:ss
	 * 
	 * @return
	 */
	public static String getNowTimeHString() {
		Date date = new Date();
		return formatw2.format(date);
	}

	/**
	 * "yyyy-MM-dd"
	 * 
	 * @return
	 */
	public static String getNowDateHString() {
		Date date = new Date();
		return format.format(date);
	}

	/**
	 * "yyyyMMdd"
	 * 
	 * @return
	 */
	public static String getNowDateString() {
		Date date = new Date();
		return formatd.format(date);
	}

	/**
	 * "yyyyMMddHHmmssSSS"
	 * 
	 * @return
	 */
	public static String getDefaultBh() {
		Date date = new Date();
		return formatws2.format(date);
	}

	public static Date getNowDate() {
		return new Date();
	}

	public static String AddZero(int Num) {
		String ret = "";
		for (int i = 1; i <= Num; i++) {
			ret = "0" + ret;
		}
		return ret;
	}

	/**
	 * 31->0x31
	 * 
	 * @category String:31->byte:3,1->0x03,0x01->0x30,0x01->0x31
	 * @param String
	 * @return byte
	 */
	public static byte stringToByte(String str) {
		byte[] tem0 = str.substring(0, 1).getBytes();
		byte[] tem1 = str.substring(1, 2).getBytes();
		byte b0 = Byte.decode("0x" + new String(tem0)).byteValue();
		b0 = (byte) (b0 << 4);
		byte b1 = Byte.decode("0x" + new String(tem1)).byteValue();
		byte ret = (byte) (b0 ^ b1);
		return ret;
	}

	/**
	 * 3,1->0x61
	 * 
	 * @category Byte:3,1->0x33,0x31->0x30,0x31->0x61
	 * @category Byte:43,21->0x34,0x33,0x32,0x31->3433,3231->0x3433,0x3231->0x61
	 * @param src0
	 * @param src1
	 * @return byte
	 */
	public static byte uniteBytes(byte src0, byte src1) {
		byte _b0 = Byte.decode("0x" + new String(new byte[] { src0 }))
				.byteValue();
		_b0 = (byte) (_b0 << 4);
		byte _b1 = Byte.decode("0x" + new String(new byte[] { src1 }))
				.byteValue();
		byte ret = (byte) (_b0 ^ _b1);
		return ret;
	}

	/**
	 * 6->06,32->20
	 * 
	 * @param byte
	 * @return String
	 */
	public static String byteToString(byte oneByte) {
		String hex = Integer.toHexString(oneByte & 0xFF);
		if (hex.length() == 1) {
			hex = '0' + hex;
		}
		Log.i("info", "hex == " + hex);
		System.out.println(Integer.valueOf("F", 16));// 16
		return hex;
	}

	public static String bytesToHexString1234(byte src) {
		StringBuilder stringBuilder = new StringBuilder("");
		int v = src & 0xFF;
		String hv = Integer.toHexString(v);
		if (hv.length() < 2) {
			stringBuilder.append(0);
		}
		stringBuilder.append(hv);
		return stringBuilder.toString();
	}

	/**
	 * 将指定字符串src，以每两个字符分割转换为16进制形式 如："2B44EFD9" –> byte[]{0x2B, 0×44, 0xEF,
	 * 0xD9}
	 * 
	 * @param src
	 *            String
	 * @return byte[]
	 */
	public static byte[] hexString2Bytes(String src) {
		byte[] ret = new byte[src.length() / 2];
		byte[] tmp = src.getBytes();
		for (int i = 0; i < tmp.length / 2; i++) {
			ret[i] = uniteBytes(tmp[i * 2], tmp[i * 2 + 1]);
		}
		return ret;
	}

	public static ArrayList<byte[]> stringToKeyAction(String string) {

		String str = string;
		// Log.v("in", "str=" + str);
		ArrayList<byte[]> result = new ArrayList<byte[]>();

		// 0~9
		if (str.equals("0")) {
			result.add(new byte[] { 11, 0 });
			result.add(new byte[] { 11, 1 });
		} else if (str.equals("1")) {
			result.add(new byte[] { 2, 0 });
			result.add(new byte[] { 2, 1 });
		} else if (str.equals("2")) {
			result.add(new byte[] { 3, 0 });
			result.add(new byte[] { 3, 1 });
		} else if (str.equals("3")) {
			result.add(new byte[] { 4, 0 });
			result.add(new byte[] { 4, 1 });
		} else if (str.equals("4")) {
			result.add(new byte[] { 5, 0 });
			result.add(new byte[] { 5, 1 });
		} else if (str.equals("5")) {
			result.add(new byte[] { 6, 0 });
			result.add(new byte[] { 6, 1 });
		} else if (str.equals("6")) {
			result.add(new byte[] { 7, 0 });
			result.add(new byte[] { 7, 1 });
		} else if (str.equals("7")) {
			result.add(new byte[] { 8, 0 });
			result.add(new byte[] { 8, 1 });
		} else if (str.equals("8")) {
			result.add(new byte[] { 9, 0 });
			result.add(new byte[] { 9, 1 });
		} else if (str.equals("9")) {
			result.add(new byte[] { 10, 0 });
			result.add(new byte[] { 10, 1 });
		}

		// a~z
		else if (str.equals("a")) {
			result.add(new byte[] { 30, 0 });
			result.add(new byte[] { 30, 1 });
		} else if (str.equals("b")) {
			result.add(new byte[] { 48, 0 });
			result.add(new byte[] { 48, 1 });
		} else if (str.equals("c")) {
			result.add(new byte[] { 46, 0 });
			result.add(new byte[] { 46, 1 });
		} else if (str.equals("d")) {
			result.add(new byte[] { 32, 0 });
			result.add(new byte[] { 32, 1 });
		} else if (str.equals("e")) {
			result.add(new byte[] { 18, 0 });
			result.add(new byte[] { 18, 1 });
		} else if (str.equals("f")) {
			result.add(new byte[] { 33, 0 });
			result.add(new byte[] { 33, 1 });
		} else if (str.equals("g")) {
			result.add(new byte[] { 34, 0 });
			result.add(new byte[] { 34, 1 });
		} else if (str.equals("h")) {
			result.add(new byte[] { 35, 0 });
			result.add(new byte[] { 35, 1 });
		} else if (str.equals("i")) {
			result.add(new byte[] { 23, 0 });
			result.add(new byte[] { 32, 1 });
		} else if (str.equals("j")) {
			result.add(new byte[] { 36, 0 });
			result.add(new byte[] { 36, 1 });
		} else if (str.equals("k")) {
			result.add(new byte[] { 37, 0 });
			result.add(new byte[] { 37, 1 });
		} else if (str.equals("l")) {
			result.add(new byte[] { 38, 0 });
			result.add(new byte[] { 38, 1 });
		} else if (str.equals("m")) {
			result.add(new byte[] { 50, 0 });
			result.add(new byte[] { 50, 1 });
		} else if (str.equals("n")) {
			result.add(new byte[] { 49, 0 });
			result.add(new byte[] { 49, 1 });
		} else if (str.equals("o")) {
			result.add(new byte[] { 24, 0 });
			result.add(new byte[] { 24, 1 });
		} else if (str.equals("p")) {
			result.add(new byte[] { 25, 0 });
			result.add(new byte[] { 25, 1 });
		} else if (str.equals("q")) {
			result.add(new byte[] { 16, 0 });
			result.add(new byte[] { 16, 1 });
		} else if (str.equals("r")) {
			result.add(new byte[] { 19, 0 });
			result.add(new byte[] { 19, 1 });
		} else if (str.equals("s")) {
			result.add(new byte[] { 31, 0 });
			result.add(new byte[] { 31, 1 });
		} else if (str.equals("t")) {
			result.add(new byte[] { 20, 0 });
			result.add(new byte[] { 20, 1 });
		} else if (str.equals("u")) {
			result.add(new byte[] { 22, 0 });
			result.add(new byte[] { 22, 1 });
		} else if (str.equals("v")) {
			result.add(new byte[] { 47, 0 });
			result.add(new byte[] { 47, 1 });
		} else if (str.equals("w")) {
			result.add(new byte[] { 17, 0 });
			result.add(new byte[] { 17, 1 });
		} else if (str.equals("x")) {
			result.add(new byte[] { 45, 0 });
			result.add(new byte[] { 45, 1 });
		} else if (str.equals("y")) {
			result.add(new byte[] { 21, 0 });
			result.add(new byte[] { 21, 1 });
		} else if (str.equals("z")) {
			result.add(new byte[] { 44, 0 });
			result.add(new byte[] { 44, 1 });
		} else if (str.equals("\r")) {
			result.add(new byte[] { 28, 0 });
			result.add(new byte[] { 28, 1 });
		}

		// A~B
		else if (str.equals("A")) {
			result.add(new byte[] { 42, 0 });
			result.add(new byte[] { 30, 0 });
			result.add(new byte[] { 30, 1 });
			result.add(new byte[] { 42, 1 });
		} else if (str.equals("B")) {
			result.add(new byte[] { 42, 0 });
			result.add(new byte[] { 48, 0 });
			result.add(new byte[] { 48, 1 });
			result.add(new byte[] { 42, 1 });
		} else if (str.equals("C")) {
			result.add(new byte[] { 42, 0 });
			result.add(new byte[] { 46, 0 });
			result.add(new byte[] { 46, 1 });
			result.add(new byte[] { 42, 1 });
		} else if (str.equals("D")) {
			result.add(new byte[] { 42, 0 });
			result.add(new byte[] { 32, 0 });
			result.add(new byte[] { 32, 1 });
			result.add(new byte[] { 42, 1 });
		} else if (str.equals("E")) {
			result.add(new byte[] { 42, 0 });
			result.add(new byte[] { 18, 0 });
			result.add(new byte[] { 18, 1 });
			result.add(new byte[] { 42, 1 });
		} else if (str.equals("F")) {
			result.add(new byte[] { 42, 0 });
			result.add(new byte[] { 33, 0 });
			result.add(new byte[] { 33, 1 });
			result.add(new byte[] { 42, 1 });
		} else if (str.equals("G")) {
			result.add(new byte[] { 42, 0 });
			result.add(new byte[] { 34, 0 });
			result.add(new byte[] { 34, 1 });
			result.add(new byte[] { 42, 1 });
		} else if (str.equals("H")) {
			result.add(new byte[] { 42, 0 });
			result.add(new byte[] { 35, 0 });
			result.add(new byte[] { 35, 1 });
			result.add(new byte[] { 42, 1 });
		} else if (str.equals("I")) {
			result.add(new byte[] { 42, 0 });
			result.add(new byte[] { 23, 0 });
			result.add(new byte[] { 23, 1 });
			result.add(new byte[] { 42, 1 });
		} else if (str.equals("J")) {
			result.add(new byte[] { 42, 0 });
			result.add(new byte[] { 36, 0 });
			result.add(new byte[] { 36, 1 });
			result.add(new byte[] { 42, 1 });
		} else if (str.equals("K")) {
			result.add(new byte[] { 42, 0 });
			result.add(new byte[] { 37, 0 });
			result.add(new byte[] { 37, 1 });
			result.add(new byte[] { 42, 1 });
		} else if (str.equals("L")) {
			result.add(new byte[] { 42, 0 });
			result.add(new byte[] { 38, 0 });
			result.add(new byte[] { 38, 1 });
			result.add(new byte[] { 42, 1 });
		} else if (str.equals("M")) {
			result.add(new byte[] { 42, 0 });
			result.add(new byte[] { 50, 0 });
			result.add(new byte[] { 50, 1 });
			result.add(new byte[] { 42, 1 });
		} else if (str.equals("N")) {
			result.add(new byte[] { 42, 0 });
			result.add(new byte[] { 49, 0 });
			result.add(new byte[] { 49, 1 });
			result.add(new byte[] { 42, 1 });
		} else if (str.equals("O")) {
			result.add(new byte[] { 42, 0 });
			result.add(new byte[] { 24, 0 });
			result.add(new byte[] { 24, 1 });
			result.add(new byte[] { 42, 1 });
		} else if (str.equals("P")) {
			result.add(new byte[] { 42, 0 });
			result.add(new byte[] { 25, 0 });
			result.add(new byte[] { 25, 1 });
			result.add(new byte[] { 42, 1 });
		} else if (str.equals("Q")) {
			result.add(new byte[] { 42, 0 });
			result.add(new byte[] { 16, 0 });
			result.add(new byte[] { 16, 1 });
			result.add(new byte[] { 42, 1 });
		} else if (str.equals("R")) {
			result.add(new byte[] { 42, 0 });
			result.add(new byte[] { 19, 0 });
			result.add(new byte[] { 19, 1 });
			result.add(new byte[] { 42, 1 });
		} else if (str.equals("S")) {
			result.add(new byte[] { 42, 0 });
			result.add(new byte[] { 31, 0 });
			result.add(new byte[] { 31, 1 });
			result.add(new byte[] { 42, 1 });
		} else if (str.equals("T")) {
			result.add(new byte[] { 42, 0 });
			result.add(new byte[] { 20, 0 });
			result.add(new byte[] { 20, 1 });
			result.add(new byte[] { 42, 1 });
		} else if (str.equals("U")) {
			result.add(new byte[] { 42, 0 });
			result.add(new byte[] { 22, 0 });
			result.add(new byte[] { 22, 1 });
			result.add(new byte[] { 42, 1 });
		} else if (str.equals("V")) {
			result.add(new byte[] { 42, 0 });
			result.add(new byte[] { 47, 0 });
			result.add(new byte[] { 47, 1 });
			result.add(new byte[] { 42, 1 });
		} else if (str.equals("W")) {
			result.add(new byte[] { 42, 0 });
			result.add(new byte[] { 17, 0 });
			result.add(new byte[] { 27, 1 });
			result.add(new byte[] { 42, 1 });
		} else if (str.equals("X")) {
			result.add(new byte[] { 42, 0 });
			result.add(new byte[] { 45, 0 });
			result.add(new byte[] { 45, 1 });
			result.add(new byte[] { 42, 1 });
		} else if (str.equals("Y")) {
			result.add(new byte[] { 42, 0 });
			result.add(new byte[] { 21, 0 });
			result.add(new byte[] { 21, 1 });
			result.add(new byte[] { 42, 1 });
		} else if (str.equals("Z")) {
			result.add(new byte[] { 42, 0 });
			result.add(new byte[] { 44, 0 });
			result.add(new byte[] { 44, 1 });
			result.add(new byte[] { 42, 1 });
		}

		// )!@#$%^&*(
		else if (str.equals(")")) {
			result.add(new byte[] { 42, 0 });
			result.add(new byte[] { 11, 0 });
			result.add(new byte[] { 11, 1 });
			result.add(new byte[] { 42, 1 });
		} else if (str.equals("!")) {
			result.add(new byte[] { 42, 0 });
			result.add(new byte[] { 2, 0 });
			result.add(new byte[] { 2, 1 });
			result.add(new byte[] { 42, 1 });
		} else if (str.equals("@")) {
			result.add(new byte[] { 42, 0 });
			result.add(new byte[] { 3, 0 });
			result.add(new byte[] { 3, 1 });
			result.add(new byte[] { 42, 1 });
		} else if (str.equals("#")) {
			result.add(new byte[] { 42, 0 });
			result.add(new byte[] { 4, 0 });
			result.add(new byte[] { 4, 1 });
			result.add(new byte[] { 42, 1 });
		} else if (str.equals("$")) {
			result.add(new byte[] { 42, 0 });
			result.add(new byte[] { 5, 0 });
			result.add(new byte[] { 5, 1 });
			result.add(new byte[] { 42, 1 });
		} else if (str.equals("%")) {
			result.add(new byte[] { 42, 0 });
			result.add(new byte[] { 6, 0 });
			result.add(new byte[] { 6, 1 });
			result.add(new byte[] { 42, 1 });
		} else if (str.equals("^")) {
			result.add(new byte[] { 42, 0 });
			result.add(new byte[] { 7, 0 });
			result.add(new byte[] { 7, 1 });
			result.add(new byte[] { 42, 1 });
		} else if (str.equals("&")) {
			result.add(new byte[] { 42, 0 });
			result.add(new byte[] { 8, 0 });
			result.add(new byte[] { 8, 1 });
			result.add(new byte[] { 42, 1 });
		} else if (str.equals("*")) {
			result.add(new byte[] { 42, 0 });
			result.add(new byte[] { 9, 0 });
			result.add(new byte[] { 9, 1 });
			result.add(new byte[] { 42, 1 });
		} else if (str.equals("(")) {
			result.add(new byte[] { 42, 0 });
			result.add(new byte[] { 10, 0 });
			result.add(new byte[] { 10, 1 });
			result.add(new byte[] { 42, 1 });
		}

		// -=[];'`\,./
		// 12/13/26/27/39/40/41/43/51/52/53
		else if (str.equals("-")) {
			result.add(new byte[] { 12, 0 });
			result.add(new byte[] { 12, 1 });
		} else if (str.equals("=")) {
			result.add(new byte[] { 13, 0 });
			result.add(new byte[] { 13, 1 });
		} else if (str.equals("[")) {
			result.add(new byte[] { 26, 0 });
			result.add(new byte[] { 26, 1 });
		} else if (str.equals("]")) {
			result.add(new byte[] { 27, 0 });
			result.add(new byte[] { 27, 1 });
		} else if (str.equals(";")) {
			result.add(new byte[] { 39, 0 });
			result.add(new byte[] { 39, 1 });
		} else if (str.equals("'")) {
			result.add(new byte[] { 40, 0 });
			result.add(new byte[] { 40, 1 });
		} else if (str.equals("`")) {
			result.add(new byte[] { 41, 0 });
			result.add(new byte[] { 41, 1 });
		} else if (str.equals("\\")) {
			result.add(new byte[] { 43, 0 });
			result.add(new byte[] { 43, 1 });
		} else if (str.equals(",")) {
			result.add(new byte[] { 51, 0 });
			result.add(new byte[] { 51, 1 });
		} else if (str.equals(".")) {
			result.add(new byte[] { 52, 0 });
			result.add(new byte[] { 52, 1 });
		} else if (str.equals("/")) {
			result.add(new byte[] { 53, 0 });
			result.add(new byte[] { 53, 1 });
		}

		// _+{}:"~|<>?
		// 12/13/26/27/39/40/41/43/51/52/53
		else if (str.equals("_")) {
			result.add(new byte[] { 42, 0 });
			result.add(new byte[] { 12, 0 });
			result.add(new byte[] { 12, 1 });
			result.add(new byte[] { 42, 1 });
		} else if (str.equals("+")) {
			result.add(new byte[] { 42, 0 });
			result.add(new byte[] { 13, 0 });
			result.add(new byte[] { 13, 1 });
			result.add(new byte[] { 42, 1 });
		} else if (str.equals("{")) {
			result.add(new byte[] { 42, 0 });
			result.add(new byte[] { 26, 0 });
			result.add(new byte[] { 26, 1 });
			result.add(new byte[] { 42, 1 });
		} else if (str.equals("}")) {
			result.add(new byte[] { 42, 0 });
			result.add(new byte[] { 27, 0 });
			result.add(new byte[] { 27, 1 });
			result.add(new byte[] { 42, 1 });
		} else if (str.equals(":")) {
			result.add(new byte[] { 42, 0 });
			result.add(new byte[] { 39, 0 });
			result.add(new byte[] { 39, 1 });
			result.add(new byte[] { 42, 1 });
		} else if (str.equals("\"")) {
			result.add(new byte[] { 42, 0 });
			result.add(new byte[] { 40, 0 });
			result.add(new byte[] { 40, 1 });
			result.add(new byte[] { 42, 1 });
		} else if (str.equals("~")) {
			result.add(new byte[] { 42, 0 });
			result.add(new byte[] { 41, 0 });
			result.add(new byte[] { 41, 1 });
			result.add(new byte[] { 42, 1 });
		} else if (str.equals("|")) {
			result.add(new byte[] { 42, 0 });
			result.add(new byte[] { 43, 0 });
			result.add(new byte[] { 43, 1 });
			result.add(new byte[] { 42, 1 });
		} else if (str.equals("<")) {
			result.add(new byte[] { 42, 0 });
			result.add(new byte[] { 51, 0 });
			result.add(new byte[] { 51, 1 });
			result.add(new byte[] { 42, 1 });
		} else if (str.equals(">")) {
			result.add(new byte[] { 42, 0 });
			result.add(new byte[] { 52, 0 });
			result.add(new byte[] { 52, 1 });
			result.add(new byte[] { 42, 1 });
		} else if (str.equals("?")) {
			result.add(new byte[] { 42, 0 });
			result.add(new byte[] { 53, 0 });
			result.add(new byte[] { 53, 1 });
			result.add(new byte[] { 42, 1 });
		} else if (str.equals(" ")) {
			result.add(new byte[] { 57, 0 });
			result.add(new byte[] { 57, 1 });
		}

		if (result.size() > 0)
			return result;
		else
			return null;

	}

	/**
	 * @category��׿����ӳ���
	 * @deprecated�˺���ֻ��1~9,A~Z��ת��
	 */
	public static byte[] hexStringsToKeys(String[] strings) {
		Log.v("hexStringsToKeys", "strings.length=" + strings.length);

		String[] hexStrings = new String[] { "0", "1", "2", "3", "4", "5", "6",
				"7", "8", "9", "A", "B", "C", "D", "E", "F", "G", "H", "I",
				"J", "K", "L", "M", "N", "O", "P", "Q", "R", "S", "T", "U",
				"V", "W", "X", "Y", "Z" };
		byte[] bytes = new byte[] { 11, 2, 3, 4, 5, 6, 7, 8, 9, 10, 31, 48, 46,
				32, 18, 33, 34, 35, 23, 36, 37, 38, 50, 49, 24, 25, 16, 19, 31,
				20, 22, 47, 17, 45, 21, 44 };

		byte[] result = new byte[strings.length];

		for (int j = 0; j < strings.length; j++) {
			for (int i = 0; i < hexStrings.length; i++) {
				if (strings[j].equals(hexStrings[i])) {
					result[j] = bytes[i];
					Log.v("hexStringsToKeys", "result[" + j + "]=" + result[j]);
				}
			}
		}
		return result;

	}

	public static String bytesToHexString(byte[] src) {

		StringBuilder stringBuilder = new StringBuilder("");

		if (src == null || src.length <= 0) {
			return null;
		}
    try{
		for (int i = 0; i < src.length; i++) {

			int v = src[i] & 0xFF;

			String hv = Integer.toHexString(v);

			if (hv.length() < 2) {
				stringBuilder.append(0);
			}
			stringBuilder.append(hv);

		}
		return stringBuilder.toString();
}catch(StackOverflowError e){
	Log.e("jiebao","Exception e: "+e);
	return "";
}
	}

	/**
	 * Convert bytes to string,actually display only
	 * 
	 * @param bytes
	 * @return String
	 */
	public static String bytesToHexString(byte[] src, int start, int size) {
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

	public static final String byte2hex(byte b[]) {
		if (b == null) {
			throw new IllegalArgumentException(
					"Argument b ( byte array ) is null! ");
		}
		String hs = "";
		String stmp = "";
		for (int n = 0; n < b.length; n++) {
			stmp = Integer.toHexString(b[n] & 0xff);
			if (stmp.length() == 1) {
				hs = hs + "0" + stmp;
			} else {
				hs = hs + stmp;
			}
		}
		return hs.toUpperCase();
	}

	public static int byteToInt(byte b) {
		int i = b & 0xff;
		return i;
	}

	public static boolean isEmpty(String input) {
		return input == null || input.trim().length() < 1;
	}

	public static boolean judgeEqual(byte[] bt1, byte[] bt2) {
		int length = bt1.length;
		boolean b = true;

		if (bt1.length != bt2.length) {
			return false;
		}

		for (int i = 0; i < length; i++) {
			if (bt1[i] != bt2[i]) {
				b = false;
			}
		}
		return b;
	}

	public static String returnType(byte[] checkByte) {
		try {
			String tempStr = new String(checkByte, "UTF-8");
			byte[] tempByte = tempStr.getBytes("UTF-8");
			// System.out.println(Tools.bytesToHexString(tempByte));
			if (Tools.judgeEqual(tempByte, checkByte)) {
				// Log.v("UTF-8", "UTF-8");
				return "UTF-8";
			} else {
				// Log.v("UTF-8", "!UTF-8");
			}

			// tempStr = new String(checkByte, "GB2312");
			// tempByte = tempStr.getBytes("GB2312");
			// System.out.println(Tools.bytesToHexString123(tempByte));
			// if (Tools.judgeEqual(tempByte, checkByte)) {
			// Log.v("GB2312", "GB2312");
			// return "GB2312";
			// } else {
			// Log.v("GB2312", "!GB2312");
			// }

			// tempStr = new String(checkByte, "ISO-8859-1");
			// tempByte = tempStr.getBytes("ISO-8859-1");
			// System.out.println(Tools.bytesToHexString123(tempByte));
			// if (Tools.judgeEqual(tempByte, checkByte)) {
			// Log.v("ISO-8859-1", "ISO-8859-1");
			// return "ISO-8859-1";
			// } else {
			// Log.v("ISO-8859-1", "!ISO-8859-1");
			// }

			tempStr = new String(checkByte, "unicode");
			tempByte = tempStr.getBytes("unicode");
			// System.out.println(Tools.bytesToHexString(tempByte));
			if (Tools.judgeEqual(tempByte, checkByte)) {
				// Log.v("unicode", "unicode");
				return "unicode";
			} else {
				// Log.v("unicode", "!unicode");
			}

			// tempStr = new String(checkByte, "big5");
			// tempByte = tempStr.getBytes("big5");
			// System.out.println(Tools.bytesToHexString123(tempByte));
			// if (Tools.judgeEqual(tempByte, checkByte)) {
			// Log.v("big5", "big5");
			// return "big5";
			// } else {
			// Log.v("big5", "!big5");
			// }

			// tempStr = new String(checkByte, "utf-16be");
			// tempByte = tempStr.getBytes("utf-16be");
			// System.out.println(Tools.bytesToHexString123(tempByte));
			// if (Tools.judgeEqual(tempByte, checkByte)) {
			// Log.v("utf-16be", "utf-16be");
			// return "utf-16be";
			// } else {
			// Log.v("utf-16be", "!utf-16be");
			// }

			tempStr = new String(checkByte, "shift-jis");
			tempByte = tempStr.getBytes("shift-jis");
			// System.out.println(Tools.bytesToHexString(tempByte));
			if (Tools.judgeEqual(tempByte, checkByte)) {
				// Log.v("shift-jis", "shift-jis");
				return "shift-jis";
			} else {
				// Log.v("shift-jis", "!shift-jis");
			}

			tempStr = new String(checkByte, "GBK");
			tempByte = tempStr.getBytes("GBK");
			// System.out.println(Tools.bytesToHexString(tempByte));
			if (Tools.judgeEqual(tempByte, checkByte)) {
				// Log.v("GBK", "GBK");
				return "GBK";
			} else {
				// Log.v("GBK", "!GBK");
			}

			return "default";

		} catch (UnsupportedEncodingException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		return "UTF-8";
	}

	public static boolean isEmpty(CharSequence text) {
		if (text == null || text.length() == 0) {
			return true;
		}
		return false;
	}

	public final static boolean isNetWorkConnected(Context ctx) {
		ConnectivityManager manager = (ConnectivityManager) ctx
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo network = manager.getActiveNetworkInfo();
		if (network != null && network.isConnected()) {
			if (network.getState() == NetworkInfo.State.CONNECTED) {
				return true;
			}
		}
		return false;
	}

	public static boolean isWifi(Context mContext) {
		ConnectivityManager connManager = (ConnectivityManager) mContext
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo mWifi = connManager
				.getNetworkInfo(ConnectivityManager.TYPE_WIFI);

		if (mWifi.isConnected()) {
			return true;
		} else {
			return false;
		}
	}

	public static boolean isMobileNumber(String mobiles) {
		boolean flag = false;
		try {
			Pattern p = Pattern
					.compile("^(13[0-9]|14[0-9]|15[0-9]|18[0-9])\\d{8}$");
			Matcher m = p.matcher(mobiles);
			flag = m.matches();
		} catch (Exception e) {
			flag = false;
		}
		return flag;
	}

	public static String getFilterPhone(String phone) {
		if (!isEmpty(phone) && phone.length() > 10) {
			StringBuilder build = new StringBuilder();
			build.append(phone.substring(0, 3));
			build.append("****");
			build.append(phone.substring(7));
			return build.toString();
		}
		return phone;
	}

	public static String getToday() {
		SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd", Locale.CHINA);
		return df.format(new Date());
	}

	public static int getVersionCode(Context context) {
		int verCode = -1;
		try {
			verCode = context.getPackageManager().getPackageInfo(
					context.getPackageName(), 0).versionCode;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return verCode;
	}

	public static String getVersionName(Context context) {
		String verName = "-1";
		try {
			verName = context.getPackageManager().getPackageInfo(
					context.getPackageName(), 0).versionName;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return verName;
	}

	public static void onExit(Context context) {
		try {
			Intent intent = new Intent();
			intent.setAction(context.getApplicationContext().getPackageName()
					+ "_app_exit");
			context.sendBroadcast(intent);
			new Handler().postDelayed(new Runnable() {
				@Override
				public void run() {
					System.exit(0);
				}
			}, 200);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static char numToLetter(String i) {
		char letter;
		try {
			letter = (char) (Byte.parseByte(i) + 64);
			return letter;
		} catch (NumberFormatException e) {
			// TODO: handle exception
			letter = 42;
			return letter;
		}

	}

	/**
	 * 获取硬件版本号（不同硬件版本功能有区别，相应扫描功能也有区别，如v1.03扫描低电平出光，v1.04扫描高电平出光； 关于串口修改
	 * v1.03以前版本使用串口1（/dev/ttySAC1） 以后（包括1.03）使用串口3（"/dev/ttySAC3"））
	 * 
	 * @return
	 */
	public static String getHardwareVersion() {
		String line = "";
		try {
			FileInputStream is = new FileInputStream(versionFile);
			int size = is.available();
			byte[] buffer = new byte[size];
			is.read(buffer);
			line = new String(buffer);
			line = matchNum(line);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO: handle exception
			e.printStackTrace();
		}
		return line;
	}

	/**
	 * 获取380A设备背后序列号
	 */
	public static String getSerialNumber() {
		String serial = null;
		try {
			Class<?> c = Class.forName("android.os.SystemProperties");
			Method get = c.getMethod("get", String.class);
			serial = (String) get.invoke(c, "ro.serialno");
		} catch (Exception e) {
			e.printStackTrace();
		}
		return serial;

	}

	/**
	 * 删除byte[] 里面连续为0x00的字段
	 */
	private byte[] delectBytesNull(byte[] buffer) {
		int len = 0;
		byte[] temp = new byte[buffer.length];
		for (int i = 0; i < buffer.length; i++) {
			if (buffer[i] != 0x00) {
				temp[i] = buffer[i];
				len++;
			}
		}
		if (len != buffer.length) {
			buffer = null;
			buffer = new byte[len];
			System.arraycopy(temp, 0, buffer, 0, buffer.length);
		}
		return buffer;
	}

	public static void setShowLog(boolean isshowLog) {
		showLog = isshowLog;
	}

	public static boolean getShowLog() {
		return showLog;
	}

	public static void loge(String tag, String msg) {
		if (showLog) {
			Log.e(tag, msg);
		}
	}

	public static void logd(String tag, String msg) {
		if (showLog) {
			Log.d(tag, msg);
		}
	}

	public static void logi(String tag, String msg) {
		if (showLog) {
			Log.i(tag, msg);
		}
	}

	public static void logv(String tag, String msg) {
		if (showLog) {
			Log.v(tag, msg);
		}
	}

	public static void logw(String tag, String msg) {
		if (showLog) {
			Log.w(tag, msg);
		}
	}

	public static byte[] byteCute(byte[] src, int startPos, int dstPos) {
		try {
			int lenght = dstPos - startPos;
			byte[] data = new byte[lenght];
			System.arraycopy(src, startPos, data, startPos, lenght);
			return data;
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * 检查系统配置表中UHF类型 并保存到本地App内存
	 */
	public static int checkUHFModelSetting() {
		String str;
		int uhfmode = -1;
		File file = new File(setting_path);
		if (file.exists()) {
			try {
				InputStream in;
				in = new FileInputStream(file);
				InputStreamReader inputStreamReader = new InputStreamReader(in);
				BufferedReader reader = new BufferedReader(inputStreamReader);
				while ((str = reader.readLine()) != null) {
					System.out.println(str);
					if (str.contains("UHFDeviceType")) {
						String temp = str.substring(15, 16);
						uhfmode = Integer.valueOf(temp);
						break;
					}
				}
				reader.close();
				inputStreamReader.close();
				in.close();
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}
		return uhfmode;
	}

	public static String matchNum(String str) {
		Pattern pattern = Pattern
				.compile("([1-9]\\d*\\.?\\d*)|(0\\.\\d*[1-9])");
		Matcher matcher = pattern.matcher(str);
		String matchNum = "";
		if (matcher.find()) {
			matchNum = matcher.group(1);
		}
		System.out.println("matchNum:" + matchNum);
		return matchNum;
	}

	public static byte[] arrayCopy(byte[] serialPortData_buffer ,byte[] portData) {
		byte[] data3 = null;
		if(serialPortData_buffer.length > 0 && portData.length > 0) {
			data3 = new byte[serialPortData_buffer.length + portData.length];
			System.arraycopy(serialPortData_buffer, 0, data3, 0,
					serialPortData_buffer.length);
			System.arraycopy(portData, 0, data3, serialPortData_buffer.length,
					portData.length);
		}
		return data3;
	}
	
	/**
	 * Check if a (hex) string is pure hex (0-9, A-F, a-f) and 16 byte (32
	 * chars) long. If not show an error Toast in the context.
	 * 
	 * @param hexString
	 *            The string to check.
	 * @param context
	 *            The Context in which the Toast will be shown.
	 * @return True if sting is hex an 16 Bytes long, False otherwise.
	 */
	public static boolean isHexAnd16Byte(String hexString, Context context) {
		if (hexString.matches("[0-9A-Fa-f]+") == false) {
			// Error, not hex.
			Toast.makeText(context, "Error: Data must be in hexadecimal(0-9 and A-F)",
					Toast.LENGTH_LONG).show();
			return false;
		}
		if (hexString.length() != 32) {
			// Error, not 16 byte (32 chars).
			Toast.makeText(context, "Error: Data must be 16 bytes(32 characters) long",
					Toast.LENGTH_LONG).show();
			return false;
		}
		return true;
	}
}
