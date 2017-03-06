package android.jb.barcode;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.security.InvalidParameterException;
import java.util.ArrayList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import android.content.Context;
import android.content.Intent;
import android.jb.Preference;
import android.jb.utils.Tools;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.util.Log;
import android_serialport_api.SerialPort;

public class BarcodeManager {

	private static int device_type = 1;
	private static BarcodeManager instance;
	private static Object lock = new Object();
	protected SerialPort mSerialPort;
	private boolean begin;
	private String trig_scan = "0"; // 记录当前版本出光指令
	private String trig_last = "0"; // 保存上次trig状态
	private int all_scan_count;// 记录出光次数
	private long scan_time_limit = 200; // 扫描间隔控制
	private long scan_time = 3000;// 扫描间隔时间
	private long scan_preTime;
	private long scan_curTime;
	public final static int MODEL_UNKONW = -1;
	public final static int MODEL_N4313 = 0;
	public final static int MODEL_SE955 = 1;
	public final static int MODEL_EM3095 = 2;
	public final static int MODEL_EM3070 = 3;
	public final static int MODEL_N3680 = 4;
	// 扫描头协议内容
	public final static int Protocol_UNKONW = -1; // 无处理
	public final static int Protocol_N4313_Default = 0; // N4313还原出厂设置
	public final static int Protocol_N4313_Protocol = 1; // 自定义N4313前缀1113
															// 后缀1214协议 防止断码误码
	public final static int Protocol_N4313_Codeid = 2; // 设置N4313返回 AimId
	public final static int Protocol_SE955_Default = 3; // SE955还原出厂设置
	public final static int Protocol_SE955_Protocol = 4; // 设置SE955受保护的协议
															// 返回CODEID
															// 防止断码误码
	public final static int Protocol_EM3095_Default = 5; // EM3095还原出厂设置
	public final static int Protocol_EM3095_Protocol = 6; // 设置EM3095受保护的协议
															// 返回CODEID 防止断码误码
	public final static int Protocol_EM3070_Default = 7; // EM3070还原出厂设置
	public final static int Protocol_EM3070_Protocol = 8; // 设置EM3095受保护的协议
															// 防止断码误码
	public final static int Protocol_N3680_Default = 9; // N3680还原出厂设置
	public final static int Protocol_N3680_Codeid = 10; // 自定义N3680前缀Codeid
	// 后缀1214协议 防止断码误码

	public final static String TAG = "BarcodeManager";

	public static String MODELS = "";

	// 控制
	protected OutputStream mOutputStream;
	private InputStream mInputStream;
	private ReadThread mReadThread;
	private Callback onDataListener;

	private ScanThread scanThread;
	public static boolean isContinues = false;
	private boolean isScan = false; // 扫描头状态 ture:出光 false:收光
	private static boolean isReceive = false; // 控制扫描串口读取线程状态 ture:接受数据
												// false:停止接受数据
	private boolean is_SerialPortOpen = false; // 是否打开串口
	private volatile static boolean isCutData = false; // 是否为系统分割数据
	private long lastTime = 0;
	private Context serviceContext;
	private File trigFile = new File("/sys/devices/platform/em3095/trig");// �?0"出光，写�?”关�?
	private File power = new File("/sys/devices/platform/em3095/dc_power");
	private static File openCom = new File("/sys/devices/platform/em3095/com");
	private static String serialPort_Path = "/dev/ttySAC3"; // 串口地址
	private int hardwareVersion = 104;
	private byte[] serialPortData_buffer = null; // 缓存扫码数据
	private static byte[] cutData_buffer = null;
	private static int cutData_buffer_size;
	private int init_index = 0; // 协议没写入 重写次数
	private int middle_index = 0;
	private int current_check = 0;
	private final int MESSAGE_CHECK_INIT_N4313 = 6777;
	private final int MESSAGE_CHECK_INIT_EM3095 = 6778;
	private final int MESSAGE_CHECK_INIT_EM3070 = 6779;
	private final int MESSAGE_CHECK_INIT_SE955 = 6780;
	private final int MESSAGE_EM3095_CodeDate = 1110;
	private final int MESSAGE_EM3070_CodeDate = 1111;
	private final int MESSAGE_N4313_CodeDate = 1112;
	private boolean init_SE955_protocol = false; // 3095协议是否写入
	private boolean init_EM3070_protocol = false; // 3095协议是否写入
	private boolean init_EM3095_protocol = false; // 3095协议是否写入
	private boolean init_N4313_Code_id = false; // n4313 CODEID协议写入
	private boolean init_N4313_start = false; // n4313 前缀协议写入
	private boolean init_N4313_end = false; // n4313 后缀协议写入
	private boolean init_N3680_Code_id = false; // n3680 CODEID协议写入
	// private boolean init_N3680_end = false; // n3680 后缀协议写入

	private final byte[] newLandPrefix_Default_3095 = new byte[] { 0x7E, 0x00,
			0x08, 0x01, 0x00, (byte) 0xD9, 0x00, (byte) 0xDB, (byte) 0x26 };// 新大陆头恢复出厂设置
																			// 新大陆3095返回0x02成功
																			// 0x00失败
	private final byte[] newLandPrefix_Code_id_3095 = new byte[] { 0x7E, 0x00,
			0x08, 0x01, 0x00, 0x02, (byte) 0x80, 0x00, (byte) 0xD2, (byte) 0xEB };// 新大陆头加前缀
																					// //
																					// 新大陆3095返回0x02成功
																					// 0x00失败
	private final byte[] newLandPrefix_Tab_3095 = new byte[] { 0x7E, 0x00,
			0x08, 0x01, 0x00, 0x60, (byte) 0x41, 0x27, (byte) 0x56 };// 新大陆头加后缀TAB
																		// 09
																		// //
																		// 新大陆3095返回0x02成功
																		// 0x00失败

	private final byte[] newLandPrefix_Save_3095 = new byte[] { 0x7E, 0x00,
			0x09, 0x01, 0x00, 0x00, 0x00, (byte) 0xDE, (byte) 0xC8 };// 新大陆头保存设置
																		// 7E 00
																		// 09 01
																		// 00 00
																		// 00 DE
																		// C8
	private final byte[] wakeUp = { 0x00 };
	private final byte[] pack_code_noProtoc = { 0x07, (byte) 0xC6, 0x04, 0x00,
			(byte) 0xFF, (byte) 0xEE, 0x00, (byte) 0xFD, 0x42 }; // 迅宝不带协议包指令
																	// 原始数据
	private final byte[] pack_code_protocol = { 0x07, (byte) 0xC6, 0x04, 0x00,
			(byte) 0xFF, (byte) 0xEE, 0x01, (byte) 0xFD, 0x41 }; // 迅宝协议包指令 带校验
	private final byte[] host_cmd_ack = { 0x04, (byte) 0xD0, 0x04, 0x00,
			(byte) 0xFF, 0x28 }; // 迅宝应答指令
	private final String newlandPrefix_Default_3070 = "NLS0006010;NLS0001000";// EM3070恢复出厂设置
	// // em3070设置前后缀指令 Coid
	private final String newlandPrefix_Protocol_3070 = "NLS0006010;NLS0305010;NLS0300000=0x1113;NLS0308030;NLS0310000=0x1214;NLS0502100;NLS0000160;NLS0006000"; // em3070设置前后缀指令
	private final byte[] N4313Prefix_Code_id = new byte[] { 0x16, 0x4D, 0x0D,
			(byte) 0x70, (byte) 0x72, (byte) 0x65, (byte) 0x62, (byte) 0x6b,
			(byte) 0x32, (byte) 0x39, (byte) 0x39, (byte) 0x35, (byte) 0x63,
			(byte) 0x38, (byte) 0x30, (byte) 0x2e }; // 添加前缀CODEID 返回50 52 45 42
	// 4b 32 39 39 35 63 38
	// 30 06 2e
	private final byte[] N4313Prefix_start = new byte[] { 0x16, 0x4D, 0x0D,
			(byte) 0x70, (byte) 0x72, (byte) 0x65, (byte) 0x62, (byte) 0x6b,
			(byte) 0x32, (byte) 0x39, (byte) 0x39, (byte) 0x31, (byte) 0x31,
			(byte) 0x31, (byte) 0x33, (byte) 0x2e }; // 添加前缀1113
														// 返回505245424b32393931313133062e
	private final byte[] N4313Prefix_end = new byte[] { 0x16, 0x4D, 0x0D,
			(byte) 0x73, (byte) 0x75, (byte) 0x66, (byte) 0x62, (byte) 0x6b,
			(byte) 0x32, (byte) 0x39, (byte) 0x39, (byte) 0x31, (byte) 0x32,
			(byte) 0x31, (byte) 0x34, (byte) 0x2e }; // 添加后缀1214 返回 53 55 46 42
														// 4b 32 39 39 31 32 31
														// 34 06 2e
	private final byte[] N4313Prefix_default = new byte[] { 0x16, 0x4D, 0x0D,
			(byte) 0x64, (byte) 0x65, (byte) 0x66, (byte) 0x61, (byte) 0x6c,
			(byte) 0x74, (byte) 0x2e }; // 还原出厂设置 444546414c54062e 64 65 66 61
										// 6c 74 2e
	private byte[] heads_byte = { (byte) 0x61, (byte) 0x68, (byte) 0x6A,
			(byte) 0x3C, (byte) 0x62, (byte) 0x54, (byte) 0x69, (byte) 0x64,
			(byte) 0x44, (byte) 0x79, (byte) 0x7B, (byte) 0x7D, (byte) 0x49,
			(byte) 0x51, (byte) 0x65, (byte) 0x6D, (byte) 0x59, (byte) 0x66,
			(byte) 0x67, (byte) 0x74, (byte) 0x63, (byte) 0x45, (byte) 0x7A,
			(byte) 0x48, (byte) 0x56, (byte) 0x71, (byte) 0x6C, (byte) 0x77,
			(byte) 0x79, (byte) 0x78, (byte) 0x72, (byte) 0x52, (byte) 0x73,
			(byte) 0x41, (byte) 0x42, (byte) 0x43, (byte) 0x51, (byte) 0x2c,
			(byte) 0x4D, (byte) 0x4A, (byte) 0x4B, (byte) 0x3F, (byte) 0x4C,
			(byte) 0x4E, (byte) 0x50 }; // N4313
										// CodeId

	public static byte[] code_all_off = { 0x16, 0x4D, 0x0D, 0x41, 0x4C, 0x4C,
			0x45, 0x4E, 0x41, 0x30, 0x2e };
	public static byte[] code_all_on = { 0x16, 0x4D, 0x0D, 0x61, 0x6C, 0x6C,
			0x65, 0x6E, 0x61, 0x31, 0x2e };


	private byte[] code_39_off = { 0x16, 0x4D, 0x0D, 0x63, 0x33, 0x39, 0x65,
			0x6e, 0x61, 0x30, 0x2e };
	private byte[] code_39_on = { 0x16, 0x4D, 0x0D, 0x63, 0x33, 0x39, 0x65,
			0x6e, 0x61, 0x31, 0x2e };

	private byte[] i25_off = { 0x16, 0x4D, 0x0D, 0x69, 0x32, 0x35, 0x65, 0x6e,
			0x61, 0x30, 0x2e };
	private byte[] i25_on = { 0x16, 0x4D, 0x0D, 0x69, 0x32, 0x35, 0x65, 0x6e,
			0x61, 0x31, 0x2e };

	private byte[] n25_off = { 0x16, 0x4D, 0x0D, 0x6e, 0x32, 0x35, 0x65, 0x6e,
			0x61, 0x30, 0x2e };
	private byte[] n25_on = { 0x16, 0x4D, 0x0D, 0x6e, 0x32, 0x35, 0x65, 0x6e,
			0x61, 0x31, 0x2e };

	private byte[] c39_off = { 0x16, 0x4D, 0x0D, 0x63, 0x33, 0x39, 0x65, 0x6e,
			0x61, 0x30, 0x2e };
	private byte[] c39_on = { 0x16, 0x4D, 0x0D, 0x63, 0x33, 0x39, 0x65, 0x6e,
			0x61, 0x31, 0x2e };

	private byte[] r25_off = { 0x16, 0x4D, 0x0D, 0x72, 0x32, 0x35, 0x65, 0x6e,
			0x61, 0x30, 0x2e };
	private byte[] r25_on = { 0x16, 0x4D, 0x0D, 0x72, 0x32, 0x35, 0x65, 0x6e,
			0x61, 0x31, 0x2e };

	private byte[] a25_off = { 0x16, 0x4D, 0x0D, 0x61, 0x32, 0x35, 0x65, 0x6e,
			0x61, 0x30, 0x2e };
	private byte[] a25_on = { 0x16, 0x4D, 0x0D, 0x61, 0x32, 0x35, 0x65, 0x6e,
			0x61, 0x31, 0x2e };

	private byte[] x25_off = { 0x16, 0x4D, 0x0D, 0x78, 0x32, 0x35, 0x65, 0x6e,
			0x61, 0x30, 0x2e };
	private byte[] x25_on = { 0x16, 0x4D, 0x0D, 0x78, 0x32, 0x35, 0x65, 0x6e,
			0x61, 0x31, 0x2e };

	private byte[] c11_off = { 0x16, 0x4D, 0x0D, 0x63, 0x31, 0x31, 0x65, 0x6e,
			0x61, 0x30, 0x2e };
	private byte[] c11_on = { 0x16, 0x4D, 0x0D, 0x63, 0x31, 0x31, 0x65, 0x6e,
			0x61, 0x31, 0x2e };

	private byte[] c128_off = { 0x16, 0x4D, 0x0D, 0x31, 0x32, 0x38, 0x65, 0x6e,
			0x61, 0x30, 0x2e };
	private byte[] c128_on = { 0x16, 0x4D, 0x0D, 0x31, 0x32, 0x38, 0x65, 0x6e,
			0x61, 0x31, 0x2e };

	private byte[] gs1_off = { 0x16, 0x4D, 0x0D, 0x67, 0x73, 0x31, 0x65, 0x6e,
			0x61, 0x30, 0x2e };
	private byte[] gs1_on = { 0x16, 0x4D, 0x0D, 0x67, 0x73, 0x31, 0x65, 0x6e,
			0x61, 0x31, 0x2e };

	private byte[] upb_off = { 0x16, 0x4D, 0x0D, 0x75, 0x70, 0x62, 0x65, 0x6e,
			0x61, 0x30, 0x2e };
	private byte[] upb_on = { 0x16, 0x4D, 0x0D, 0x75, 0x70, 0x62, 0x65, 0x6e,
			0x61, 0x31, 0x2e };

	private byte[] cpn_off = { 0x16, 0x4D, 0x0D, 0x63, 0x70, 0x6e, 0x65, 0x6e,
			0x61, 0x30, 0x2e };
	private byte[] cpn_on = { 0x16, 0x4D, 0x0D, 0x63, 0x70, 0x6e, 0x65, 0x6e,
			0x61, 0x31, 0x2e };

	private byte[] upc0_off = { 0x16, 0x4D, 0x0D, 0x75, 0x70, 0x63, 0x65, 0x6e,
			0x30, 0x30, 0x2e };
	private byte[] upc0_on = { 0x16, 0x4D, 0x0D, 0x75, 0x70, 0x63, 0x65, 0x6e,
			0x30, 0x31, 0x2e };

	private byte[] upc1_off = { 0x16, 0x4D, 0x0D, 0x75, 0x70, 0x65, 0x65, 0x6e,
			0x31, 0x30, 0x2e };
	private byte[] upc1_on = { 0x16, 0x4D, 0x0D, 0x75, 0x70, 0x65, 0x65, 0x6e,
			0x31, 0x31, 0x2e };

	private byte[] e13_off = { 0x16, 0x4D, 0x0D, 0x65, 0x31, 0x33, 0x65, 0x6e,
			0x61, 0x30, 0x2e };
	private byte[] e13_on = { 0x16, 0x4D, 0x0D, 0x65, 0x31, 0x33, 0x65, 0x6e,
			0x61, 0x31, 0x2e };

	private byte[] ea8_off = { 0x16, 0x4D, 0x0D, 0x65, 0x61, 0x38, 0x65, 0x6e,
			0x61, 0x30, 0x2e };
	private byte[] ea8_on = { 0x16, 0x4D, 0x0D, 0x65, 0x61, 0x38, 0x65, 0x6e,
			0x61, 0x31, 0x2e };

	private byte[] msi_off = { 0x16, 0x4D, 0x0D, 0x6d, 0x73, 0x69, 0x65, 0x6e,
			0x61, 0x30, 0x2e };
	private byte[] msi_on = { 0x16, 0x4D, 0x0D, 0x6d, 0x73, 0x69, 0x65, 0x6e,
			0x61, 0x31, 0x2e };

	private byte[] rss_off = { 0x16, 0x4D, 0x0D, 0x72, 0x73, 0x73, 0x65, 0x6e,
			0x61, 0x30, 0x2e };
	private byte[] rss_on = { 0x16, 0x4D, 0x0D, 0x72, 0x73, 0x73, 0x65, 0x6e,
			0x61, 0x31, 0x2e };

	private byte[] rsl_off = { 0x16, 0x4D, 0x0D, 0x72, 0x73, 0x6c, 0x65, 0x6e,
			0x61, 0x30, 0x2e };
	private byte[] rsl_on = { 0x16, 0x4D, 0x0D, 0x72, 0x73, 0x6c, 0x65, 0x6e,
			0x61, 0x31, 0x2e };

	private byte[] rse_off = { 0x16, 0x4D, 0x0D, 0x72, 0x73, 0x65, 0x65, 0x6e,
			0x61, 0x30, 0x2e };
	private byte[] rse_on = { 0x16, 0x4D, 0x0D, 0x72, 0x73, 0x65, 0x65, 0x6e,
			0x61, 0x31, 0x2e };

	private byte[] cba_off = { 0x16, 0x4D, 0x0D, 0x63, 0x62, 0x61, 0x65, 0x6e,
			0x61, 0x30, 0x2e };
	private byte[] cba_on = { 0x16, 0x4D, 0x0D, 0x63, 0x62, 0x61, 0x65, 0x6e,
			0x61, 0x31, 0x2e };

	private byte[] cbf_off = { 0x16, 0x4D, 0x0D, 0x63, 0x62, 0x66, 0x65, 0x6e,
			0x61, 0x30, 0x2e };
	private byte[] cbf_on = { 0x16, 0x4D, 0x0D, 0x63, 0x62, 0x66, 0x65, 0x6e,
			0x61, 0x31, 0x2e };

	private byte[] pdf_off = { 0x16, 0x4D, 0x0D, 0x70, 0x64, 0x66, 0x65, 0x6e,
			0x61, 0x30, 0x2e };
	private byte[] pdf_on = { 0x16, 0x4D, 0x0D, 0x70, 0x64, 0x66, 0x65, 0x6e,
			0x61, 0x31, 0x2e };

	private byte[] mac_off = { 0x16, 0x4D, 0x0D, 0x70, 0x64, 0x66, 0x6d, 0x61,
			0x63, 0x30, 0x2e };
	private byte[] mac_on = { 0x16, 0x4D, 0x0D, 0x70, 0x64, 0x66, 0x6d, 0x61,
			0x63, 0x31, 0x2e };

	private byte[] pdd_off = { 0x16, 0x4D, 0x0D, 0x70, 0x64, 0x64, 0x65, 0x6e,
			0x61, 0x30, 0x2e };
	private byte[] pdd_on = { 0x16, 0x4D, 0x0D, 0x70, 0x64, 0x64, 0x65, 0x6e,
			0x61, 0x31, 0x2e };

	private byte[] com_off = { 0x16, 0x4D, 0x0D, 0x63, 0x6f, 0x6d, 0x65, 0x6e,
			0x61, 0x30, 0x2e };
	private byte[] com_on = { 0x16, 0x4D, 0x0D, 0x63, 0x6f, 0x6d, 0x65, 0x6e,
			0x61, 0x31, 0x2e };
	
	private byte[] t39_off = { 0x16, 0x4D, 0x0D, (byte) 0x84, 0x33, 0x39, 0x65, 0x6e,
			0x61, 0x30, 0x2e };
	private byte[] t39_on = { 0x16, 0x4D, 0x0D, (byte) 0x84, 0x33, 0x39, 0x65, 0x6e,
			0x61, 0x31, 0x2e };

	private byte[] qrc_off = { 0x16, 0x4D, 0x0D, 0x71, 0x72, 0x63, 0x65, 0x6e,
			0x61, 0x30, 0x2e };
	private byte[] qrc_on = { 0x16, 0x4D, 0x0D, 0x71, 0x72, 0x63, 0x65, 0x6e,
			0x61, 0x31, 0x2e };

	private byte[] idm_off = { 0x16, 0x4D, 0x0D, 0x69, 0x64, 0x6d, 0x65, 0x6e,
			0x61, 0x30, 0x2e };
	private byte[] idm_on = { 0x16, 0x4D, 0x0D, 0x69, 0x64, 0x6d, 0x65, 0x6e,
			0x61, 0x31, 0x2e };

	private byte[] max_off = { 0x16, 0x4D, 0x0D, 0x6d, 0x61, 0x78, 0x65, 0x6e,
			0x61, 0x30, 0x2e };
	private byte[] max_on = { 0x16, 0x4D, 0x0D, 0x6d, 0x61, 0x78, 0x65, 0x6e,
			0x61, 0x31, 0x2e };

	private byte[] azt_off = { 0x16, 0x4D, 0x0D, 0x61, 0x7a, 0x74, 0x65, 0x6e,
			0x61, 0x30, 0x2e };
	private byte[] azt_on = { 0x16, 0x4D, 0x0D, 0x61, 0x7a, 0x74, 0x65, 0x6e,
			0x61, 0x31, 0x2e };

	private byte[] hx_off = { 0x16, 0x4D, 0x0D, 0x68, 0x78, 0x5f, 0x65, 0x6e,
			0x61, 0x30, 0x2e };
	private byte[] hx_on = { 0x16, 0x4D, 0x0D, 0x68, 0x78, 0x5f, 0x65, 0x6e,
			0x61, 0x31, 0x2e };

	private byte[] cpc_off = { 0x16, 0x4D, 0x0D, 0x63, 0x70, 0x63, 0x65, 0x6e,
			0x61, 0x30, 0x2e };
	private byte[] cpc_on = { 0x16, 0x4D, 0x0D, 0x63, 0x70, 0x63, 0x65, 0x6e,
			0x61, 0x31, 0x2e };

	private String IO_OE = "/sys/devices/soc.0/m9_dev.69/switch_oe"; // 默认值：1，其他值无效
	private String IO_CS1 = "/sys/devices/soc.0/m9_dev.69/cs1";// 默认值：1，其他值无效
	private String IO_CS0 = "/sys/devices/soc.0/m9_dev.69/cs0";// 默认值：1，其他值无效

	public boolean setCodeOnOff(String code) {
		switch (Integer.parseInt(code)) {
		case 01:
			writeCommand(code_39_on);
			break;
		case 00:
			writeCommand(code_39_off);
			break;
		case 11:
			writeCommand(i25_on);
			break;
		case 10:
			writeCommand(i25_off);
			break;
		case 21:
			writeCommand(n25_on);
			break;
		case 20:
			writeCommand(n25_off);
			break;
		case 31:
			writeCommand(c39_on);
			break;
		case 30:
			writeCommand(c39_off);
			break;
		case 41:
			writeCommand(r25_on);
			break;
		case 40:
			writeCommand(r25_off);
			break;
		case 51:
			writeCommand(a25_on);
			break;
		case 50:
			writeCommand(a25_off);
			break;
		case 61:
			writeCommand(x25_on);
			break;
		case 60:
			writeCommand(x25_off);
			break;
		case 71:
			writeCommand(c11_on);
			break;
		case 70:
			writeCommand(c11_off);
			break;
		case 81:
			writeCommand(c128_on);
			break;
		case 80:
			writeCommand(c128_off);
			break;
		case 91:
			writeCommand(gs1_on);
			break;
		case 90:
			writeCommand(gs1_off);
			break;
		case 101:
			writeCommand(upb_on);
			break;
		case 100:
			writeCommand(upb_off);
			break;
		case 111:
			writeCommand(cpn_on);
			break;
		case 110:
			writeCommand(cpn_off);
			break;
		case 121:
			writeCommand(upc0_on);
			break;
		case 120:
			writeCommand(upc0_off);
			break;
		case 131:
			writeCommand(upc1_on);
			break;
		case 130:
			writeCommand(upc1_off);
			break;
		case 141:
			writeCommand(e13_on);
			break;
		case 140:
			writeCommand(e13_off);
			break;
		case 151:
			writeCommand(ea8_on);
			break;
		case 150:
			writeCommand(ea8_off);
			break;
		case 161:
			writeCommand(msi_on);
			break;
		case 160:
			writeCommand(msi_off);
			break;
		case 171:
			writeCommand(rss_on);
			break;
		case 170:
			writeCommand(rss_off);
			break;
		case 181:
			writeCommand(rsl_on);
			break;
		case 180:
			writeCommand(rsl_off);
			break;
		case 191:
			writeCommand(rse_on);
			break;
		case 190:
			writeCommand(rse_off);
			break;
		case 201:
			writeCommand(cba_on);
			break;
		case 200:
			writeCommand(cba_off);
			break;
		case 211:
			writeCommand(cbf_on);
			break;
		case 210:
			writeCommand(cbf_off);
			break;
		case 221:
			writeCommand(pdf_on);
			break;
		case 220:
			writeCommand(pdf_off);
			break;
		case 231:
			writeCommand(mac_on);
			break;
		case 230:
			writeCommand(mac_off);
			break;
		case 241:
			writeCommand(pdd_on);
			break;
		case 240:
			writeCommand(pdd_off);
			break;
		case 251:
			writeCommand(com_on);
			break;
		case 250:
			writeCommand(com_off);
			break;
		case 261:
			writeCommand(t39_on);
			break;
		case 260:
			writeCommand(com_off);
			break;
		case 271:
			writeCommand(qrc_on);
			break;
		case 270:
			writeCommand(qrc_off);
			break;
		case 281:
			writeCommand(idm_on);
			break;
		case 280:
			writeCommand(idm_off);
			break;
		case 291:
			writeCommand(max_on);
			break;
		case 290:
			writeCommand(max_off);
			break;
		case 301:
			writeCommand(azt_on);
			break;
		case 300:
			writeCommand(azt_off);
			break;
		case 311:
			writeCommand(hx_on);
			break;
		case 310:
			writeCommand(hx_off);
			break;
		case 321:
			writeCommand(cpc_on);
			break;
		case 320:
			writeCommand(cpc_off);
			break;
		}
		return true;
	}

	private void IO_OE(String status) {
		// TODO Auto-generated method stub
		System.out.println("power:" + IO_CS1 + " " + status);
		writeFile(new File(IO_OE), status);
	}

	private void IO_CS1(String status) {
		// TODO Auto-generated method stub
		System.out.println("power:" + IO_CS1 + " " + status);
		writeFile(new File(IO_CS1), status);
	}

	private void IO_CS0(String status) {
		// TODO Auto-generated method stub
		System.out.println("power:" + IO_CS0 + " " + status);
		writeFile(new File(IO_CS0), status);
	}

	private Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			byte[] portData;

			switch (msg.what) {
			case MESSAGE_EM3070_CodeDate:
				portData = null;
				portData = (byte[]) msg.obj;
				SerialPortData_total(portData);
				break;

			case MESSAGE_EM3095_CodeDate:
				portData = null;
				portData = (byte[]) msg.obj;
				SerialPortData_total_3095(portData);
				// //Log.d(TAG,
				// "EM3095_CodeDate_total:"
				// + Tools.bytesToHexString(portData));
				break;

			case MESSAGE_N4313_CodeDate:
				System.out.println("MESSAGE_N4313_CodeDate");
				portData = null;
				portData = (byte[]) msg.obj;
				// SerialPortData_total_4313_AimId(portData);
				SerialPortData_total_4313_CodeId(portData);
				break;

			default:
				break;
			}
		};
	};

	private BarcodeManager() {
	}

	public static BarcodeManager getInstance() {
		if (null == instance) {
			synchronized (lock) {
				if (null == instance) {
					instance = new BarcodeManager();
				}
			}
		}
		return instance;
	}

	private void openserialPort(String port, int baudrate, int bits,
			char event, int stop) {
		try {
			mSerialPort = this.getSerialPort(port, baudrate, bits, event, stop);
			mOutputStream = mSerialPort.getOutputStream();
			mInputStream = mSerialPort.getInputStream();

			begin = true;
			mReadThread = new ReadThread();
			mReadThread.start();

		} catch (SecurityException e) {
			Log.e(TAG,
					"You do not have read/write permission to the serial port.");
			e.printStackTrace();
		} catch (IOException e) {
			Log.e(TAG,
					"The serial port can not be opened for an unknown reason.");
			e.printStackTrace();
		} catch (InvalidParameterException e) {
			Log.e(TAG, "Please configure your serial port first.");
			e.printStackTrace();
		}
	}

	/**
	 * 自动选择协议内容 注意：使用本方法前 确保已经调用checkScannerModelSetting()方法 获取到了系统配置表中信息
	 */
	private int autoChoiseScanPrefix() {
		int model = getScannerModel();
		int prefix = -1;
		switch (model) {
		case MODEL_N4313:
			MODELS = "N4313";
			if (getScanIsReturnFactory()) {
				prefix = Protocol_N4313_Default;
			} else {
				// prefix = Prefix_N4313_Protocol;
				prefix = Protocol_N4313_Codeid;
			}
			break;
		case MODEL_SE955:
			MODELS = "SE955";
			if (getScanIsReturnFactory()) {
				prefix = Protocol_SE955_Default;
			} else {
				prefix = Protocol_SE955_Protocol;
			}
			break;
		case MODEL_EM3095:
			MODELS = "EM3095";
			if (getScanIsReturnFactory()) {
				prefix = Protocol_EM3095_Default;
			} else {
				prefix = Protocol_EM3095_Protocol;
			}
			break;
		case MODEL_EM3070:
			MODELS = "EM3070";
			if (getScanIsReturnFactory()) {
				prefix = Protocol_EM3070_Default;
			} else {
				prefix = Protocol_EM3070_Protocol;
			}
			break;
		case MODEL_N3680:
			MODELS = "N3680";
			if (getScanIsReturnFactory()) {
				prefix = Protocol_N3680_Default;
			} else {
				prefix = Protocol_N3680_Codeid;
				// 3680 协议与4313相同
			}
			break;

		default:
			break;
		}
		System.out.println("MODELS ==== " + MODELS);
		return prefix;
	}

	/**
	 * 获取本地内存中扫描头设备类型 注意：使用本方法前 确保已经调用checkScannerModelSetting()方法 获取到了系统配置表中信息
	 * 
	 * @return
	 */
	public int getScannerModel() {
		int scannerModel = -1;
		scannerModel = Preference.getScannerModel(serviceContext);
		return scannerModel;
	}

	/**
	 * 设置扫描头设备类型 model 扫描头类型号
	 */
	private void setScannerModel(int model) {
		// Log.i(TAG, "Scan_model:" + model);
		Preference.setScannerModel(serviceContext, model);
	}

	/**
	 * 设置扫描设备写入协议内容 解码将依据此设置 进行相应解码 <item>-1:无处理</item>
	 * <item>0:N4313还原出厂设置</item> <item>1:N4313防止断码误码</item>
	 * <item>2:N4313返回条码类型</item> <item>3:SE955还原出厂设置</item>
	 * <item>4:SE955返回条码类型</item> <item>5:EM3095还原出厂设置</item>
	 * <item>6:Em3095返回条码类型</item> <item>7:EM3070还原出厂设置</item>
	 * <item>8:Em3070防止断码误码</item>
	 */
	// public void setScanPrefix(int prefix) {
	private void setScanPrefix() {
		int prefix = autoChoiseScanPrefix();
		// Log.i(TAG, "Scan_prefix:" + prefix);
		mHandler.removeCallbacks(checkInitSuccess);
		init_index = 0;
		Preference.setScannerPrefix(serviceContext, prefix);
		switch (prefix) {
		case Protocol_N4313_Codeid:
			setScannerModel(MODEL_N4313);
			break;

		case Protocol_N4313_Default:
			setScannerModel(MODEL_N4313);
			break;

		case Protocol_N4313_Protocol:
			setScannerModel(MODEL_N4313);
			break;

		case Protocol_EM3070_Default:
			setScannerModel(MODEL_EM3070);
			break;

		case Protocol_EM3070_Protocol:
			setScannerModel(MODEL_EM3070);
			break;

		case Protocol_EM3095_Default:
			setScannerModel(MODEL_EM3095);
			break;

		case Protocol_EM3095_Protocol:
			setScannerModel(MODEL_EM3095);
			break;

		case Protocol_SE955_Default:
			setScannerModel(MODEL_SE955);
			break;

		case Protocol_SE955_Protocol:
			setScannerModel(MODEL_SE955);
			break;

		default:
			break;
		}
	}

	/**
	 * 返回扫描设备写入协议内容 解码将依据此设置 进行相应解码 <item>-1:无处理</item>
	 * <item>0:N4313还原出厂设置</item> <item>1:N4313防止断码误码</item>
	 * <item>2:N4313返回条码类型</item> <item>3:SE955还原出厂设置</item>
	 * <item>4:SE955返回条码类型</item> <item>5:EM3095还原出厂设置</item>
	 * <item>6:Em3095返回条码类型</item> <item>7:EM3070还原出厂设置</item>
	 * <item>8:Em3070防止断码误码</item>
	 */
	private int getScanPrefix() {
		return Preference.getScannerPrefix(serviceContext);
	}

	/**
	 * 打开条码设备，调用此方法将初始化扫描设备
	 */
	public void Barcode_Open(Context context, Callback dataReceive) {
		// //发送广播 关闭扫描服务
		// context.sendBroadcast(new Intent("ReleaseCom"));

		serviceContext = context;
		this.onDataListener = dataReceive;
		device_type = Preference.getDeviceType(serviceContext);

		if (device_type == 3) {
			serialPort_Path = "/dev/ttyHSL1";
			checkScannerModelSetting_380K();
			//setScannerModel(MODEL_SE955);
			Log.d(TAG, "Barcode_Open820 test！");
			trigFile = new File("/sys/devices/soc.0/m9_dev.69/start_scan");// 写"0"出光，写“1”关闭
			power = new File("/sys/devices/soc.0/m9_dev.69/scan_pwr_en");
			openCom = new File("/sys/devices/soc.0/m9_dev.69/scan_com_switch");
			trig_scan = "0";
		} else if (device_type == 2) {
			serialPort_Path = "/dev/ttyHSL1";
			checkScannerModelSetting_380K();
			trigFile = new File("/sys/devices/soc.0/m9_dev.69/start_scan");// 写"0"出光，写“1”关闭
			power = new File("/sys/devices/soc.0/m9_dev.69/scan_pwr_en");
			if (!trigFile.canWrite()) {
				System.out.println("m9_dev.69 can not write");
				trigFile = new File("/sys/devices/soc.0/m9_dev.68/start_scan");// 写"0"出光，写“1”关闭
				power = new File("/sys/devices/soc.0/m9_dev.68/scan_pwr_en");
			}
			trig_scan = "0";
		} else {
			checkScannerModelSetting_380A();
			String line = Tools.getHardwareVersion();
			// String line = null;
			if (Tools.isEmpty(line)) {
				// //Log.d(TAG, "获取硬件版本号失败！");
			} else {
				line = line.replace(".", "");
				hardwareVersion = Integer.valueOf(line);
				// Log.d(TAG, "HardwareVersion:" + hardwareVersion);
			}
			if (hardwareVersion < 103) {
				setSerialPort_Path(1);
				trig_scan = "1";
			} else {
				setSerialPort_Path(3);
				if (hardwareVersion <= 103) {
					trig_scan = "1";
				} else {
					trig_scan = "0";
				}
			}
		}

		setScanPrefix();
		trig_last = trig_scan;
		if (Preference.getScanShortCutPressMode(serviceContext) == 2) {
			setScanTime(10000);
		} else {
			setScanTime(3000);
		}

		if (!is_SerialPortOpen) {
			if (getScannerModel() == MODEL_N3680) {
				System.out.println("MODEL_N3680");
				openserialPort(serialPort_Path, 115200, 8, 'N', 1);
			} else {
				openserialPort(serialPort_Path, 9600, 8, 'N', 1);
			}
			is_SerialPortOpen = true;
			mHandler.post(initRunnable);
		}

	}

	/**
	 * 关闭条码设备
	 */
	public void Barcode_Close() {
		Barcode_Continue_Stop();
		serialPortData_buffer = null;
		mHandler.removeCallbacks(checkInitSuccess);
		begin = false;
		com("0");
		// System.out.println("Barcode_Close() Barcode_Stop");
		Barcode_Stop();// 保证HONEYWELL的头上电时不出光
		try {
			Thread.sleep(100);
		} catch (InterruptedException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		power("0");
		this.close();
		if (null != mReadThread) {
			notifyReader();
			mReadThread = null;
		}
		is_SerialPortOpen = false;

		// //发送广播 开启扫描服务
		// if(null != serviceContext)
		// serviceContext.sendBroadcast(new Intent("ReleaseCom"));
	}

	/**
	 * 出光开始扫描
	 */
	public synchronized void Barcode_Continue_Start(long time) {
		// Log.d(TAG, "Barcode_Continue_Start()");
		if (is_SerialPortOpen) {
			if (time > 0) {
				scan_time_limit = time;
			}
			isContinues = true;
			if (scanThread != null) {
				scanThread.interrupt();
				scanThread.run = false;
			}
			scanThread = new ScanThread();
			scanThread.run = true;
			scanThread.start();
		}
	}

	/**
	 * 闭光停止扫描
	 */
	public synchronized void Barcode_Continue_Stop() {
		// Log.d(TAG, "Barcode_Continue_Stop()");
		if (scanThread != null) {
			scanThread.interrupt();
			scanThread.run = false;
		}
		isContinues = false;
		scanThread = null;
	}

	private class ScanThread extends Thread {
		public boolean run;

		@Override
		public void run() {
			// TODO Auto-generated method stub
			while (run) {
				try {
					if (is_SerialPortOpen) {
						Barcode_Start();
						// System.out.println("ScanActivity Barcode_Start");
						sleep(3000);
						if (is_SerialPortOpen) {
							Barcode_Stop();
						}
					}
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block\
					try {
						sleep(scan_time_limit);
					} catch (InterruptedException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
					e.printStackTrace();
				}
			}
			if (is_SerialPortOpen) {
				// System.out.println("ScanActivity Barcode_Stop7");
				Barcode_Stop();
				isContinues = false;
			}
		}
	}

	private void notifyScanReader() {
		if (scanThread != null && scanThread.isAlive()) {
			scanThread.interrupt();
		}
	}

	/**
	 * 出光开始扫描
	 */
	public synchronized void Barcode_Start() {
		Log.d(TAG, "Barcode_Start() hardwareVersion: "+hardwareVersion);
		if (hardwareVersion <= 103) {
			Log.d(TAG, "Barcode_Start() model: "+getScannerModel());
			if (getScannerModel() == MODEL_EM3070) {
				// // 3070trig 指令反过来			
				scan("1");
			} else {
				scan("0");
			}
		} else {
			scan("1");
		}
		setIsScan(true);
	}

	/**
	 * 闭光停止扫描
	 */
	public synchronized void Barcode_Stop() {
		Log.d(TAG, "Barcode_Stop() scan: "+isScan());
		if (isScan()) {
			if (hardwareVersion <= 103) {
				if (getScannerModel() == MODEL_EM3070) {
					// 3070trig 指令反过来
					scan("0");
				} else {
					scan("1");
				}
			} else {
				scan("0");
			}
			setIsScan(false);
		}
	}

	/**
	 * 选择串口地址 默认新版串口3
	 * 
	 * @param i
	 *            1:串口1 主要基于1.03以前的旧�?3:串口3
	 *            主要基于1.04以后的新版（可以进入手机设置—�?关于手机—�?版本号查看）
	 */
	private void setSerialPort_Path(int i) {
		if (i == 1) {
			serialPort_Path = "/dev/ttySAC1";
		} else {
			serialPort_Path = "/dev/ttySAC3";
		}
	}

	public String checkScannerModelSetting_380A() {
		Log.d(TAG, "ScanManager checkScannerModelSetting");
		String text = "";
		int scannerModel = -1;
		// String path = Environment.getDataDirectory().getAbsolutePath();
		// getFiles("/data/");
		String setting_path = "/data/jb_config.xml";
		File file = new File(setting_path);
		if (file.exists()) {
			try {
				XmlPullParserFactory factory = XmlPullParserFactory
						.newInstance();
				XmlPullParser xmlPullParser = factory.newPullParser();
				InputStream stream = new FileInputStream(file);
				xmlPullParser.setInput(stream, "UTF-8");
				int eventType = xmlPullParser.getEventType();
				try {
					while (eventType != XmlPullParser.END_DOCUMENT) {
						String nodeName = xmlPullParser.getName();
						switch (eventType) {
						case XmlPullParser.START_TAG:
							if ("ScanDeviceType".equals(nodeName)) {
								text = xmlPullParser.nextText();
								scannerModel = Integer.parseInt(text);
								setScannerModel(scannerModel);
								Log.d(TAG, setting_path + "存在,ScanDeviceType:"
										+ scannerModel);
								return text;
							}
							break;
						}
						eventType = xmlPullParser.next();
					}
					stream.close();
				} catch (IOException e) {
					stream.close();
					e.printStackTrace();
				}
			} catch (XmlPullParserException e) {
				e.printStackTrace();
			} catch (Exception e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		} else {
			Log.d("getScannerModel", setting_path + "can't find");
		}
		return text;
	}

	public String checkScannerModelSetting_380K() {
		// //////Log.d(TAG, "ScanManager checkScannerModelSetting");
		String text = "";
		int scannerModel = -1;
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		try {
			String setting_path = "/dev/block/bootdevice/by-name/diag";
			File file = new File(setting_path);
			if (file.exists()) {
				InputStream stream = new FileInputStream(file);
				byte[] buffer = new byte[1024];
				byte[] buffer1 = new byte[1024 - 420];
				stream.read(buffer);
				int count = 0;
				for (int i = 420; i < 1024; i++) {//1024-432
					buffer1[count] = buffer[i];
					count++;
				}
				String xml = new String(buffer1, "utf-8").trim()
						+ "\n</HT380k>";
				System.out.println(xml);
				StringBuffer sb = new StringBuffer(xml);
				sb.insert(56, "\n<HT380k>");
				System.out.println(sb.toString());
				DocumentBuilder builder = factory.newDocumentBuilder();
				StringReader sr = new StringReader(sb.toString());
				InputSource is = new InputSource(sr);
				Document doc = (Document) builder.parse(is);
				Element rootElement = doc.getDocumentElement();
				NodeList phones = rootElement
						.getElementsByTagName("ScanDeviceType");
				Node nodes = phones.item(0);
				text = nodes.getFirstChild().getNodeValue();
				scannerModel = Integer.parseInt(text);
				setScannerModel(scannerModel);
				Log.d(TAG, "ScanDeviceType:" + scannerModel);
				stream.close();
			}
		} catch (Exception e) {
                Log.e(TAG, "checkScannerModelSettingK Error! can't find file");
			e.printStackTrace();
		}
		return text;
	}

	/**
	 * 是否恢复出厂设置 默认不恢复
	 * 
	 * @return <code>true</code> 恢复 <code>false</code>不恢复
	 */
	public boolean getScanIsReturnFactory() {
		return Preference.getScannerIsReturnFactory(serviceContext);
	}

	/**
	 * 是否恢复出厂设置 默认不恢复 <code>true</code> 支持 <code>false</code>不支持
	 */
	public void setScanIsReturnFactory(boolean b) {
		Preference.setScanInit(serviceContext, false);
		Preference.setScannerIsReturnFactory(serviceContext, b);
		setScanPrefix();
		initScanProtocol();
	}

	/**
	 * 获取扫描串口打开状态
	 * 
	 * ture:打开状态 <code>false</code>:关闭
	 */
	public boolean isSerialPort_isOpen() {
		return is_SerialPortOpen;
	}

	/**
	 * 设置出光收光状态
	 * 
	 * @param isScan
	 *            ture:出光 <code>false</code>:收光
	 */
	private void setIsScan(boolean isScan) {
		this.isScan = isScan;
	}

	/**
	 * 获取出光收光状态
	 * 
	 * @param isScan
	 *            ture:出光 <code>false</code>:收光
	 */
	private boolean isScan() {
		return isScan;
	}

	/**
	 * 出光、收光
	 * 
	 * @param trig
	 *            1拉高，关闭；0拉低，打开
	 */
	private synchronized void scan(String trig) {
		// 两次触发信号的间隔时间不低于70ms
		scan_curTime = (int) System.currentTimeMillis();
		if (scan_curTime - scan_preTime < 120) {
			if (!trig_last.equals(trig)) {
				try {
					if (trig_last.equals(trig_scan)) {
						Thread.sleep(120);
						Log.d("trig", "scan() sleep(120) " + "trig:" +
						 trig);
					}
					doScan(trig);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			} else {
				// 相同trig指令间隔低于50ms 不做处理
				Log.d("trig", "scan() return " + "trig:" + trig);
			}
		} else {
			Log.d("trig", "scan_curTime - scan_preTime:"
			 + (scan_curTime - scan_preTime) + " trig:" + trig);
			doScan(trig);
		}
	}

	private void doScan(String trig) {
		if (hardwareVersion <= 103) {
			if (Preference.getScannerModel(serviceContext) == MODEL_EM3070) {
				if (trig.equals("1")) {
					// keyF2DownOrNot = true;
					try {
						all_scan_count++;
						Log.e("trig", "scan:" + all_scan_count);
					} catch (Exception e) {
						all_scan_count = 0;
					}
					setIsReceive(true);
					mHandler.removeCallbacks(stopReceice);
					mHandler.postDelayed(countdown, scan_time);
				} else {
					mHandler.postDelayed(stopReceice, 1000);
					mHandler.removeCallbacks(countdown);
				}
			} else {
				if (trig.equals("0")) {
					// keyF2DownOrNot = true;
					try {
						all_scan_count++;
						// ////Log.e("trig", "scan:" + all_scan_count);
					} catch (Exception e) {
						all_scan_count = 0;
					}
					setIsReceive(true);
					mHandler.removeCallbacks(stopReceice);
					mHandler.postDelayed(countdown, scan_time);
				} else {
					// isReceive = false;
					mHandler.postDelayed(stopReceice, 1000);
					mHandler.removeCallbacks(countdown);
				}
			}
		} else {
			if (trig.equals("1")) {
				// keyF2DownOrNot = true;
				try {
					all_scan_count++;
					Log.e("trig", "1254scan:" + all_scan_count);
				} catch (Exception e) {
					all_scan_count = 0;
				}
				setIsReceive(true);
				mHandler.removeCallbacks(stopReceice);
				mHandler.postDelayed(countdown, scan_time);
			} else {
				// isReceive = false;
				mHandler.postDelayed(stopReceice, 1000);
				mHandler.removeCallbacks(countdown);
			}
		}

		notifyReader();
		Log.d("trig", "trig:" + trig);
		Log.d("scan_time", "scan_time:" + scan_time+" trigFile: "+trigFile);
		writeFile(trigFile, trig);
		trig_last = trig;
		scan_preTime = scan_curTime;
	}

	private void notifyReader() {
		if (mReadThread != null && mReadThread.isAlive()) {
			mReadThread.interrupt();
		}
	}

	private SerialPort getSerialPort(String port, int baudrate, int bits,
			char event, int stop) throws SecurityException, IOException,
			InvalidParameterException {
		if (mSerialPort == null) {
			if ((port.length() == 0) || (baudrate == -1)) {
				throw new InvalidParameterException();
			}
			// mSerialPort = new SerialPort(new File(port), baudrate, bits,
			// event,
			// stop, 0);
			// mSerialPort = new SerialPort(new File(port), baudrate, 0, false);
			mSerialPort = new SerialPort(new File(serialPort_Path), baudrate,
					bits, event, stop, 0);
		}
		return mSerialPort;
	}

	/**
	 * 扫描数据回调接口 buffer：扫描数据byte数组 ， codeId : 条码类型
	 */
	public interface Callback {
		/**
		 * 扫描数据返回
		 * 
		 * @param buffer
		 *            条码数据
		 * @param codeId
		 *            条码类型
		 * @param errorCode
		 *            错误码
		 */
		public void Barcode_Read(byte[] buffer, String codeId, int errorCode);
	}

	private class ReadThread extends Thread {
		@Override
		public void run() {
			super.run();
			while (begin) {
				// if (isReceive || keyF2DownOrNot) {
				try {
					// Thread.sleep(5);
				} catch (Exception e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				//Log.d(TAG,"ReadThread_Handle_Start:"+isReceive);
				if (isReceive) {
					int size;
					try {
						if (mInputStream == null)
							return;
						int cout = mInputStream.available();
						if (cout > 0) {
							long nowTime = System.currentTimeMillis();
							Log.d(TAG,"ReadThread_Handle_Start:"+
							nowTime+" mInputStream.available(): "+cout);
							if (hardwareVersion == 104) {
								if (nowTime - lastTime <= 20) {
									isCutData = true;
									mHandler.removeCallbacks(sendBuffer);
									// System.out
									// .println("ReadThread removeCallbacks");
								} else {
									isCutData = false;
								}
							} else {
								if (nowTime - lastTime <= 60) {
									isCutData = true;
									mHandler.removeCallbacks(sendBuffer);
									// System.out
									// .println("ReadThread removeCallbacks");
								} else {
									isCutData = false;
								}
							}

						} else {
							continue;
						}
						int temp = 0;
						while (begin) {
							if (mInputStream == null)
								return;
							try {
								// if (Preference
								// .getScanDeviceType(serviceContext) ==
								// MODEL_EM3095) {
								// Thread.sleep(100);
								// } else {
								// Thread.sleep(10);
								// }
							} catch (Exception e) {
								e.printStackTrace();
								break;
							}
							cout = mInputStream.available();
							if (temp == cout) {
								break;
							}
							temp = cout;
						}

						if (mInputStream == null)
							return;
						cout = mInputStream.available();
						byte[] buffer = new byte[cout];
						size = mInputStream.read(buffer);
						Log.v(TAG,"1390  size: "+size);
						if (size > 0) {
							if (onDataListener != null) {
								// dealData(buffer, size);
								Log.d(TAG,
										"ReadThread buffer: "
												+ Tools.bytesToHexString(buffer)+" isCutData: "+isCutData);
								if (!isCutData) {
									cutData_buffer = buffer;
									cutData_buffer_size = size;
								} else {
									if (null != cutData_buffer) {
										byte[] data3 = new byte[cutData_buffer.length
												+ buffer.length];
										System.arraycopy(cutData_buffer, 0,
												data3, 0, cutData_buffer.length);
										System.arraycopy(buffer, 0, data3,
												cutData_buffer.length,
												buffer.length);
										cutData_buffer = new byte[data3.length];
										cutData_buffer = data3;
										cutData_buffer_size += size;
									}
								}
								isCutData = false;
								System.out
										.println("ReadThread cutData_buffer: "
												+ Tools.bytesToHexString(cutData_buffer)+" hardwareVersion: "+hardwareVersion);

								if (hardwareVersion == 104 && getScannerModel()!=1) {
									// mHandler.postAtTime(sendBuffer,
									// SystemClock.uptimeMillis() + 19);
									// System.out.println("1");
									Message msg = new Message();
									msg.obj = cutData_buffer;
									msg.what = 1112;
									mHandler.sendMessage(msg);
								} else {
									mHandler.postAtTime(sendBuffer,
											SystemClock.uptimeMillis() + 59);
								}
								cout = 0;
							}
							lastTime = System.currentTimeMillis();
							scan_preTime = lastTime;
							// //Log.d("ReadThread_Handle_End:", "" + lastTime);
						}
					} catch (IOException e) {
						e.printStackTrace();
						return;
					} catch (Exception e) {
						// TODO: handle exception
						e.printStackTrace();
					}
				} else {
					// System.out.println("ReadThread isReceive:"+isReceive);
					try {
						Thread.sleep(10000);
						System.out.println("10秒");
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				// ////Log.d(TAG, "readThreadRunning");
			}
		}
	}

	/**
	 * 定时检查缓存区数据，处理缓存区数据
	 */
	private Runnable sendBuffer = new Runnable() {

		@Override
		public void run() {
			// TODO Auto-generated method stub
			long nowTime = System.currentTimeMillis();
			Log.d(TAG, "sendBuffer() isCutData:" + isCutData
			 + "nowTime - lastTime:" + (nowTime - lastTime));
			if (!isCutData) {
				dealData(cutData_buffer, cutData_buffer_size);
				System.out.println("sendBuffersendBuffersendBuffersendBuffer");
			}
		}
	};

	/**
	 * 关闭串口
	 */
	private void close() {
		System.err.println("ScanManager  关闭串口");
		try {
			if (mSerialPort != null) {
				if (mOutputStream != null)
					mOutputStream.close();
				if (mInputStream != null)
					mInputStream.close();
				if (mSerialPort != null)
					mSerialPort.close();
				begin = false;
				mOutputStream = null;
				mInputStream = null;
				mSerialPort = null;
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	// [s] 输出
	private void writeCommand(String msg) {
		try {
			if (allowToWrite()) {
				if (msg == null)
					msg = "";
				mOutputStream.write(msg.getBytes());
				mOutputStream.flush();

			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * 对扫描头写入数据，如扫描头设置参数等
	 * 
	 * @param b
	 */
	public void writeCommand(byte[] b) {
		try {
			if (allowToWrite()) {
				if (b == null)
					return;
				System.out.println(Tools.bytesToHexString(b));
				mOutputStream.write(b);
				mOutputStream.flush(); // 1
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void write(int oneByte) {
		try {
			if (allowToWrite()) {
				mOutputStream.write(oneByte);
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

	/**
	 * 给扫描头上下电
	 * 
	 * @param power
	 *            1 开，0关
	 */
	private void power(String p) {
		try {
			Thread.sleep(100);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		writeFile(power, p);
	}

	/**
	 * COM口开关
	 * 
	 * @param status
	 *            1开，0关
	 */
	private void com(String status) {
		if (device_type != 2) {
			writeFile(openCom, status);
		}
	}

	/**
	 * 根据不同扫描头 初始化协议内容 type 扫描头类型
	 */
	private void init(int type) {
		// Log.v(TAG, "init()");
		try {

			switch (type) {
			case Protocol_SE955_Protocol:
				setIsReceive(true);
				notifyReader();
				writeCommand(wakeUp);
				Thread.sleep(50);
				setIsReceive(true);
				notifyReader();
				writeCommand(pack_code_protocol);

				// mHandler.sendEmptyMessageDelayed(MESSAGE_CHECK_INIT_SE955 ,
				// 2000);
				mHandler.removeCallbacks(checkInitSuccess);
				current_check = MESSAGE_CHECK_INIT_SE955;
				mHandler.postDelayed(checkInitSuccess, 2000);
				// //Log.d(TAG, "init:SE955 Protocol");
				mHandler.removeCallbacks(stopReceice);
				mHandler.postDelayed(stopReceice, 3 * 1000);
				break;

			case Protocol_SE955_Default:
				setIsReceive(true);
				notifyReader();
				writeCommand(wakeUp);
				Thread.sleep(50);
				setIsReceive(true);
				notifyReader();
				writeCommand(pack_code_noProtoc);

				// //Log.d(TAG, "init:SE955 Default");
				mHandler.removeCallbacks(stopReceice);
				mHandler.postDelayed(stopReceice, 3 * 1000);
				break;

			case Protocol_EM3070_Protocol:
				setIsReceive(true);
				notifyReader();
				writeCommand(wakeUp);
				try {
					Thread.sleep(100);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				setIsReceive(true);
				notifyReader();
				writeCommand(newlandPrefix_Protocol_3070);

				// mHandler.sendEmptyMessageDelayed(MESSAGE_CHECK_INIT_EM3070 ,
				// 2000);
				mHandler.removeCallbacks(checkInitSuccess);
				current_check = MESSAGE_CHECK_INIT_EM3070;
				mHandler.postDelayed(checkInitSuccess, 2000);
				// //Log.d(TAG, "init:EM3070 Protocol");
				mHandler.removeCallbacks(stopReceice);
				mHandler.postDelayed(stopReceice, 3 * 1000);
				break;

			case Protocol_EM3070_Default:
				setIsReceive(true);
				notifyReader();
				writeCommand(wakeUp);
				try {
					Thread.sleep(100);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				setIsReceive(true);
				notifyReader();
				writeCommand(newlandPrefix_Default_3070);

				// //Log.d(TAG, "init:EM3070 Default");
				mHandler.removeCallbacks(stopReceice);
				mHandler.postDelayed(stopReceice, 3 * 1000);
				break;

			case Protocol_EM3095_Protocol:
				setIsReceive(true);
				notifyReader();
				writeCommand(wakeUp);
				try {
					Thread.sleep(100);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				setIsReceive(true);
				notifyReader();
				writeCommand(newLandPrefix_Code_id_3095);
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				setIsReceive(true);
				notifyReader();
				writeCommand(newLandPrefix_Tab_3095);
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				setIsReceive(true);
				notifyReader();
				writeCommand(newLandPrefix_Save_3095);

				// mHandler.sendEmptyMessageDelayed(MESSAGE_CHECK_INIT_EM3095 ,
				// 2000);
				mHandler.removeCallbacks(checkInitSuccess);
				current_check = MESSAGE_CHECK_INIT_EM3095;
				mHandler.postDelayed(checkInitSuccess, 2000);
				// //Log.d(TAG, "init:EM3095 Protocol");
				mHandler.removeCallbacks(stopReceice);
				mHandler.postDelayed(stopReceice, 3 * 1000);
				break;

			case Protocol_EM3095_Default:
				setIsReceive(true);
				notifyReader();
				writeCommand(wakeUp);
				try {
					Thread.sleep(100);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				setIsReceive(true);
				notifyReader();
				writeCommand(newLandPrefix_Default_3095);

				// //Log.d(TAG, "init:EM3095 Default");
				mHandler.removeCallbacks(stopReceice);
				mHandler.postDelayed(stopReceice, 3 * 1000);
				break;

			case Protocol_N4313_Codeid:
				// 返回CODEID 协议
				setIsReceive(true);
				notifyReader();
				writeCommand(wakeUp);
				try {
					Thread.sleep(100);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				setIsReceive(true);
				notifyReader();
				// writeCommand(N4313Prefix_Aim_id);
				writeCommand(N4313Prefix_Code_id);
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				setIsReceive(true);
				notifyReader();
				writeCommand(N4313Prefix_end);

				mHandler.removeCallbacks(checkInitSuccess);
				current_check = MESSAGE_CHECK_INIT_N4313;
				mHandler.postDelayed(checkInitSuccess, 2000);
				// //Log.d(TAG, "init:N4313 Code id");
				mHandler.removeCallbacks(stopReceice);
				mHandler.postDelayed(stopReceice, 3 * 1000);
				break;

			case Protocol_N4313_Protocol:
				// 前后缀1113 1214协议 防止断码误码
				setIsReceive(true);
				notifyReader();
				writeCommand(wakeUp);
				try {
					Thread.sleep(100);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				setIsReceive(true);
				notifyReader();
				writeCommand(N4313Prefix_start);
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				setIsReceive(true);
				notifyReader();
				writeCommand(N4313Prefix_end);

				// mHandler.sendEmptyMessageDelayed(MESSAGE_CHECK_INIT_N4313,
				// 2000);
				mHandler.removeCallbacks(checkInitSuccess);
				current_check = MESSAGE_CHECK_INIT_N4313;
				mHandler.postDelayed(checkInitSuccess, 2000);
				// //Log.d(TAG, "init:N4313 Protoco;");
				mHandler.removeCallbacks(stopReceice);
				mHandler.postDelayed(stopReceice, 3 * 1000);
				break;

			case Protocol_N4313_Default:
				setIsReceive(true);
				notifyReader();
				writeCommand(wakeUp);
				try {
					Thread.sleep(100);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				setIsReceive(true);
				notifyReader();
				writeCommand(N4313Prefix_default);

				// //Log.d(TAG, "init:N4313 Default");
				mHandler.removeCallbacks(stopReceice);
				mHandler.postDelayed(stopReceice, 3 * 1000);
				break;

			case Protocol_N3680_Default:
				setIsReceive(true);
				notifyReader();
				writeCommand(N4313Prefix_default);

				// //Log.d(TAG, "init:N3680 Default");
				mHandler.removeCallbacks(stopReceice);
				mHandler.postDelayed(stopReceice, 3 * 1000);
				break;

			case Protocol_N3680_Codeid:
				setIsReceive(true);
				notifyReader();
				writeCommand(wakeUp);
				try {
					Thread.sleep(100);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				setIsReceive(true);
				notifyReader();
				writeCommand(N4313Prefix_Code_id);
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				setIsReceive(true);
				notifyReader();
				writeCommand(N4313Prefix_end);

				mHandler.removeCallbacks(checkInitSuccess);
				current_check = MESSAGE_CHECK_INIT_N4313;
				mHandler.postDelayed(checkInitSuccess, 2000);
				// //Log.d(TAG, "init:N3680 Code id");
				mHandler.removeCallbacks(stopReceice);
				mHandler.postDelayed(stopReceice, 3 * 1000);
				break;

			default:
				// writeCommand(wakeUp);
				// Thread.sleep(50);
				// setIsReceive(true);
				// notifyReader();
				// ////Log.d(TAG, "init:default");
				// mHandler.removeCallbacks(stopReceice);
				// mHandler.postDelayed(stopReceice, 3 * 1000);
				break;
			}
			// scan("1");
			// Barcode_Stop();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
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

	private Runnable countdown = new Runnable() {

		@Override
		public void run() {
			// scan("1");
			Log.e("trig", "1886scan countdown Barcode_Stop");
			// System.out.println("countdown Barcode_Stop");
			Barcode_Stop();
			isScan = false;
		}
	};

	/**
	 * 根据不同扫描头 初始化协议内容
	 */
	private synchronized void initScanProtocol() {
		new Thread(new Runnable() {

			@Override
			public void run() {
				// TODO Auto-generated method stub
				init(getScanPrefix());
			}
		}).start();
	}

	/**
	 * 防止后台扫描 接受数据线程被阻断
	 */
	private static Runnable stopReceice = new Runnable() {

		@Override
		public void run() {
			// ////Log.d(TAG, "keyF2DownOrNot:" + keyF2DownOrNot);
			// keyF2DownOrNot = false;
			setIsReceive(false);
		}
	};

	private Runnable initRunnable = new Runnable() {

		@Override
		public void run() {
			// TODO Auto-generated method stub
			com("1");// 转换串口到扫描模块
			// System.out.println("initRunnable Barcode_Stop");
			IO_OE("1");
			IO_CS0("1");
			IO_CS1("0");
			Barcode_Stop();
			try {
				Thread.sleep(100);
			} catch (InterruptedException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			power("1");// 给扫描头上电
			if (!Preference.getIsScanInit(serviceContext)) {
				initScanProtocol();
			}
			// Barcode_Stop();
			// writeCommand(ean_13_off);
		}
	};

	/**
	 * 返回数据判断处理
	 */
	private void dealData(byte[] buffer, int size) {
		try {
			// Log.i("对数据进行不同扫描头的协议识别： ", Tools.getNowTimeHm());
			String str = Tools.bytesToHexString(buffer, 0, size);
			if (buffer == null) {
				// System.out.println("Barcode_Close() Barcode_Stop1");
				Barcode_Stop();
				return;
			}
			Log.d(TAG, "dealData buffer size:" + size + " buffer lenght:"
			 + buffer.length+" getScannerModel(): "+getScannerModel()+" str: "+str);
			// //Log.d(TAG, "buffer:" + Tools.bytesToHexString(buffer));

			if (getScannerModel() == MODEL_EM3095) {
				// em3095 02000001003331
				if (buffer.length >= 7 && buffer[0] == 2 && buffer[1] == 0
						&& buffer[2] == 0 && buffer[3] == 1 && buffer[4] == 0
						&& buffer[5] == 51 && buffer[6] == 49) {
					// Log.i(TAG, "init em3095 success!");
					init_EM3095_protocol = true;
					// System.out.println("Barcode_Close() Barcode_Stop2");
					Barcode_Stop();
					return;
				}
			}

			if (getScannerModel() == MODEL_SE955) {
				// "0005d1000001ff29"
				if (str.equals("0005d1000001ff29")) {
					// //Log.d(TAG, "init se955 failure!");
					init_SE955_protocol = false;
					// System.out.println("Barcode_Close() Barcode_Stop3");
					Barcode_Stop();
					return;
				}

				// 05d1000001ff29
				if (buffer.length >= 7 && buffer[0] == 5 && buffer[1] == -47
						&& buffer[2] == 0 && buffer[3] == 0 && buffer[4] == 1
						&& buffer[5] == -1 && buffer[6] == 41) {
					init_SE955_protocol = false;
					// if (getScannerModel() == MODEL_SE955) {
					// //Log.d(TAG, "init se955 failure!");
					// }
					// System.out.println("Barcode_Close() Barcode_Stop4");
					Barcode_Stop();
					return;
				}

				// 04d00000ff2c
				if (buffer.length >= 6 && buffer[0] == 4 && buffer[1] == -48
						&& buffer[2] == 0 && buffer[3] == 0 && buffer[4] == -1
						&& buffer[5] == 44) {
					init_SE955_protocol = true;
					// Log.i(TAG, "init se955 success!");
					// System.out.println("Barcode_Close() Barcode_Stop4");
					Barcode_Stop();
					return;
				}

				// 0004d00000ff2c
				if (str.equals("0004d00000ff2c")) {
					init_SE955_protocol = true;
					// Log.i(TAG, "init se955 success!");
					// System.out.println("Barcode_Close() Barcode_Stop12");
					Barcode_Stop();
					return;
				}
			}

			if (getScannerModel() == MODEL_N4313
					|| getScannerModel() == MODEL_N3680) {
				if (str.equals("fc")) {
					// //Log.d(TAG, "N3680 return fc!");
					return;
				}

				if (str.equals("505245424b32393935633830062e")) {
					// //Log.d(TAG, "init N4313 Codeid success!");
					init_N4313_Code_id = true;
					// System.out.println("Barcode_Close() Barcode_Stop13");
					Barcode_Stop();
					return;
				}

				// 5355942d324e8a324c932e
				// if (str.equals("505245424b32393935633831062e")) {
				// if (getScannerModel() == MODEL_N4313)
				// {
				// ////Log.d(TAG, "init N4313 AimId success!");
				// has_N4313Prefix_Aim_id = true;
				// }
				// stopScan();
				//
				// return;
				// }

				// 505245424b32393931313133062e
				if (str.equals("505245424b32393931313133062e")) {
					// //Log.d(TAG, "init N4313 start success!");
					init_N4313_start = true;
					// System.out.println("Barcode_Close() Barcode_Stop14");
					Barcode_Stop();
					return;
				}

				if (str.equals("535546424b32393931323134062e")) {
					// //Log.d(TAG, "init N4313 end success!");
					init_N4313_end = true;
					// System.out.println("Barcode_Close() Barcode_Stop15");
					Barcode_Stop();
					return;
				}

				// 444546414c54062e
				if (str.equals("444546414c54062e")) {
					// //Log.d(TAG, "init N4313 default success!");
					// System.out.println("Barcode_Close() Barcode_Stop16");
					Barcode_Stop();
					return;
				}
			}

			if (getScannerModel() == MODEL_EM3070) {
				if (buffer.length == 1 && buffer[0] == 21) {
					// //Log.d(TAG, "init em3070 failure!");
					init_EM3070_protocol = false;
					// System.out.println("Barcode_Close() Barcode_Stop17");
					Barcode_Stop();
					return;
				}
				// 06060606060606
				if (str.equals("0606060606060606")) {
					// em3070_init = true;
					// Log.v(TAG, "init em3070 succeed");
					init_EM3070_protocol = true;
					// System.out.println("Barcode_Close() Barcode_Stop18");
					Barcode_Stop();
					return;
				}

				// 060606060606
				if (buffer.length <= 7) {
					boolean em3070_init = true;
					for (int i = 0; i < buffer.length; i++) {
						if (buffer[i] != 0x06) {
							em3070_init = false;
							init_EM3070_protocol = false;
							break;
						}
					}
					if (em3070_init) {
						// Log.v(TAG, "init em3070");
						init_EM3070_protocol = true;
						// System.out.println("Barcode_Close() Barcode_Stop19");
						Barcode_Stop();
						return;
					}
				}
			}

			if (buffer.length == 1 && buffer[0] == 0x00) {
				// 兼容新大陆一维引擎
				// 新大陆一维引擎只能上电一次,下次开电源必须在500ms以上
				// se955 = false;
				// //System.out.println("Barcode_Close() Barcode_Stop5");
				// Barcode_Stop();
				try {
					Thread.sleep(500);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				return;
			}

			int Scan_mode = getScanPrefix();
			switch (Scan_mode) {
			case Protocol_SE955_Protocol:
				writeCommand(host_cmd_ack);
				getXunBao_Se955(buffer);
				break;

			case Protocol_EM3095_Protocol:
				// 21323330313439
				if (size < buffer.length) {
					byte[] temp = new byte[size];
					System.arraycopy(buffer, 0, temp, 0, size);
					buffer = null;
					buffer = new byte[size];
					buffer = temp;
					temp = null;
				}
				byte[] buffer2 = delectBytesNull(buffer);
				getNewLand_Em3095(buffer2);
				break;

			case Protocol_EM3070_Protocol:
				getNewLand_Em3070(buffer);
				break;

			case Protocol_N4313_Protocol:
				getHonyWell_N4313_Protocol(buffer);
				break;

			case Protocol_N3680_Codeid:
			case Protocol_N4313_Codeid:
				getHonyWell_N4313_Codeid(buffer);
				System.out.println("codiidididi");
				// getHonyWell_N4313_AimId(buffer);
				break;

			default:
				getDefault(buffer);
				break;
			}
			cutData_buffer = null;
			cutData_buffer_size = 0;
			// System.out.println("Barcode_Close() Barcode_Stop6");
			Barcode_Stop();
		} catch (Exception e) {
			// System.out.println("Barcode_Close() Barcode_Stop7");
			Barcode_Stop();
			e.printStackTrace();
		}
	}

	private void handleXunBaoResult(byte[] xunBaoContent) {
		// //Log.d("根据讯宝协议 处理开始：", Tools.getNowTimeHm());
		if (xunBaoContent != null) {
			// 讯宝数据包协议 ：Length(1)+Opcode(1)+Message Source(1)为0+Status(1)+Bar
			// Code
			// Type(1)+Decode Data(?)+Checksum(2)
			// lenght:数据包真实长度，不包含校验位长度（Checksum(2)）
			// 校验位长度
			// int Checksum_lenght = 0;
			if (null != serialPortData_buffer) {
				if (serialPortData_buffer[2] != 0) {
					// 正确数据第三位为0
					middle_index = 0;
					serialPortData_buffer = null;
					checkXunBaoContent(xunBaoContent);
					// //Log.d("4", Tools.getNowTimeHm());
				} else {
					arraycopy(xunBaoContent);
					// //Log.d("5", Tools.getNowTimeHm());
					checkXunBaoContent(serialPortData_buffer);
					// //Log.d("6", Tools.getNowTimeHm());
				}

			} else {
				// //Log.d("协议检查", Tools.getNowTimeHm());
				checkXunBaoContent(xunBaoContent);
				// //Log.d("结束：", Tools.getNowTimeHm());
			}
		}
	}

	/**
	 * 检查讯宝数据
	 * 
	 * @param xunBaoContent
	 */
	private void checkXunBaoContent(byte[] xunBaoContent) {
		if (xunBaoContent.length > 7 && xunBaoContent[2] == 0) {
			// 长度位 xunBaoContent【0】
			int lenght = Tools.byteToInt(xunBaoContent[0]);
			// 减去校验2位
			if (lenght == (xunBaoContent.length - 2)) {
				// 是完整数据
				// //Log.d("检测为完整数据：", Tools.getNowTimeHm());
				String str = Tools.bytesToHexString(xunBaoContent, 0,
						xunBaoContent.length);
				// //Log.d(TAG, "xunBaoContent:" + str);
				// //Log.d("协议校验数据正确性：", Tools.getNowTimeHm());
				if (isChecksun(str)) {
					// //Log.d("协议校验完成：", Tools.getNowTimeHm());
					ScanResultBean result = getXunBaoBuffer(xunBaoContent);
					if (null != result) {
						// if (mScanListener != null) {
						// // mScanListener.result(xunBaoBuffer);
						// mScanListener.xunbaoResult(result.getCodeType(),
						// result.getContent());
						// ////Log.d("协应用显示完成：", Tools.getNowTimeHm());
						// }
						// mBeepManager.play();
						if (null != onDataListener) {
							onDataListener
									.Barcode_Read(result.getContent(),
											CodeType.getXunbaoType(result
													.getCodeType()), 0);
							notifyScanReader();
						}
						// houtai_result(result.getContent());
						// ////Log.d("后台显示完成：", Tools.getNowTimeHm());
					}
				}
				serialPortData_buffer = null;
				middle_index = 0;
			} else if (lenght > (xunBaoContent.length - 2)) {
				// 不够一条数据
				// //Log.d("检测为不够一条数据：", Tools.getNowTimeHm());
				serialPortData_buffer = new byte[xunBaoContent.length];
				serialPortData_buffer = xunBaoContent;
				// total_lenght = lenght;
			} else if (lenght < (xunBaoContent.length - 2)) {
				// 超过一条数据
				// //Log.d("检测为超过一条数据：", Tools.getNowTimeHm());
				data_cut(MODEL_SE955, xunBaoContent, (lenght + 2));
			}
		} else {
			// 数据不符合协议 丢掉
			serialPortData_buffer = null;
			middle_index = 0;
			// 尝试重写迅宝协议指令 20150403
			// //Log.d(TAG, "checkXunBaoContent SE955协议不符合要求");
			initScanProtocol();
			init_index++;
		}
	}

	private void arraycopy(byte[] portData) {
		if (null != serialPortData_buffer) {
			byte[] data3 = new byte[serialPortData_buffer.length
					+ portData.length];
			System.arraycopy(serialPortData_buffer, 0, data3, 0,
					serialPortData_buffer.length);
			System.arraycopy(portData, 0, data3, serialPortData_buffer.length,
					portData.length);
			serialPortData_buffer = new byte[data3.length];
			serialPortData_buffer = data3;
			middle_index = 0;
		}
	}

	/**
	 * 分割超过一条数据的数据
	 * 
	 * @param srcData
	 *            超出数组
	 * @param lenght
	 *            分割长度
	 */
	private void data_cut(int scanType, byte[] srcData, int lenght) {
		try {
			switch (scanType) {
			case MODEL_SE955:
				if (srcData.length > lenght) {
					byte[] buffer = new byte[lenght];
					System.arraycopy(srcData, 0, buffer, 0, lenght);
					String str = Tools.bytesToHexString(buffer, 0, lenght);
					// //Log.d(TAG, "data_cut buffer:" + str);
					if (isChecksun(str)) {
						ScanResultBean result = getXunBaoBuffer(buffer);
						if (null != result) {
							// if (mScanListener != null) {
							// // mScanListener.result(xunBaoBuffer);
							// mScanListener.xunbaoResult(
							// result.getCodeType(),
							// result.getContent());
							// }
							// houtai_result(result.getContent());
							// mBeepManager.play();
							if (onDataListener != null) {
								onDataListener.Barcode_Read(
										result.getContent(), CodeType
												.getXunbaoType(result
														.getCodeType()), 0);
								notifyScanReader();
							}
						}
					}
					// 分割后剩余数组
					byte[] data3 = new byte[srcData.length - lenght];
					System.arraycopy(srcData, lenght, data3, 0, srcData.length
							- lenght);

					// if (data3.length > lenght) {
					// // handleXunBaoResult(data3);
					// serialPortData_buffer = null;
					// } else {
					middle_index = 0;
					serialPortData_buffer = null;
					serialPortData_buffer = new byte[data3.length];
					serialPortData_buffer = data3;
					checkXunBaoContent(serialPortData_buffer);
					// }
				}

				break;

			default:
				break;
			}

		} catch (ArrayStoreException e) {
			// TODO: handle exception
			e.printStackTrace();
		}
	}

	/**
	 * 校验讯宝包数据是否完整正确
	 * 
	 * @param s
	 *            16进制HexString
	 * @return
	 */
	private boolean isChecksun(String s) {
		String checksun = s.substring(s.length() - 4).toUpperCase();
		Long sum = 0L;
		for (int i = 0; i < s.length() - 4; i += 2) {
			String hex = s.substring(i, i + 2);
			long x = Long.parseLong(hex, 16);
			sum = sum + x;
			// ////Log.d(TAG, hex + "        " + x);
		}
		sum = ~sum + 1;
		String hex_sum = Long.toHexString(sum).toUpperCase();
		// ////Log.d(TAG, sum);
		// 保留四位
		if (hex_sum.length() > 4) {
			hex_sum = hex_sum.substring(hex_sum.length() - 4);
		} else {
			int time = hex_sum.length();
			for (int i = 0; i < 4 - time; i++) {
				hex_sum = "0" + hex_sum;
			}
		}
		// //Log.d(TAG, hex_sum);

		if (checksun.equals(hex_sum)) {
			return true;
		}
		return false;
	}

	/**
	 * 从讯宝协议数据中获取真实扫描数据
	 */
	private ScanResultBean getXunBaoBuffer(byte[] buffer) {
		ScanResultBean scanResult = null;
		if (buffer.length > 7) {
			byte[] byteData = new byte[buffer.length - 7];
			for (int i = 0; i < byteData.length; i++) {
				byteData[i] = buffer[i + 5];

			}
			// String encoding = Tools.guessEncoding(buffer);
			// encoding = encoding.toUpperCase().equals("WINDOWS-1252") ?
			// "GB2312"
			// : encoding;
			//
			// String data = null;
			// try {
			// data = new String(byteData, encoding);
			// } catch (UnsupportedEncodingException e) {
			// // TODO Auto-generated catch block
			// e.printStackTrace();
			// }

			scanResult = new ScanResultBean();
			scanResult.setCodeType(buffer[4]);
			scanResult.setContent(byteData);
			// scanResult.setContent(data);
		}
		return scanResult;
	}

	/**
	 * 扫描内容存储类 codeType 条码类型 content 条码String内存
	 */
	private class ScanResultBean {
		private byte codeType;
		private byte[] content;

		public byte getCodeType() {
			return codeType;
		}

		public void setCodeType(byte codeType) {
			this.codeType = codeType;
		}

		public byte[] getContent() {
			return content;
		}

		public void setContent(byte[] content) {
			this.content = content;
		}

	}

	/**
	 * 对新大陆3095数据处理
	 */
	private void getNewLand_Em3095(byte[] buffer) {
		// 3095
		// //Log.d(TAG, "getNewLand_Em3095:" + Tools.bytesToHexString(buffer));
		if (null == serialPortData_buffer) {
			if (buffer.length >= 2 && buffer[0] <= 35
					&& buffer[buffer.length - 1] == (byte) 0x09) {
				// 完整数据 用线程处理
				// Message message = new Message();
				// message.what = EM3070_CodeDate_total;
				// message.obj = buffer;
				// mHandler.sendMessage(message);
				SerialPortData_total_3095(buffer);
				// Log.i("time",
				// "SerialPortData_total end:" + Tools.getNowTimeHm());
			} else if (buffer.length >= 1 && buffer[0] <= 35) {
				// 数据头
				SerialPortData_head(buffer);
				// Log.i("time", "SerialPortData_head end:" +
				// Tools.getNowTimeHm());
			} else {
				// 错误数据 ， 或者协议可能没写入
				// //Log.d(TAG, "getNewLand_Em3095 错误数据 ， 或者协议可能没写入");
				serialPortData_buffer = null;
				middle_index = 0;
				initScanProtocol();
				init_index++;
			}
		} else {
			if (buffer.length >= 1 && buffer[buffer.length - 1] == (byte) 0x09) {
				// 数据尾
				SerialPortData_ass(buffer);
				// Log.i("time", "SerialPortData_ass end:" +
				// Tools.getNowTimeHm());
			} else {
				// 数据中间
				SerialPortData_middle(buffer);
				// Log.i("time",
				// "SerialPortData_middle end:" + Tools.getNowTimeHm());
			}
		}
	}

	/**
	 * 霍尼 N4313数据处理
	 * 
	 * @param buffer
	 */
	private void getHonyWell_N4313_Protocol(byte[] buffer) {
		byte code_id = 0x00;
		byte[] tempN = null;
		// if (has_N4313Prefix_start && has_N4313Prefix_end) {
		// 添加协议后和em3070协议处理完全相同
		getNewLand_Em3070(buffer);
	}

	/**
	 * 霍尼 N4313数据处理
	 * 
	 * @param buffer
	 */
	private void getHonyWell_N4313_Codeid(byte[] buffer) {

		/*
		 * Q,a,h,j,b,i,d,D,e,m,g,c,E j以外，判断接下来5位都不出现同个字母。
		 */
		if (null != buffer) {
			if (null == serialPortData_buffer) {
				if (buffer.length > 2 && isN4313Code_head(buffer[0])
						&& buffer[buffer.length - 1] == (byte) 0x14
						&& buffer[buffer.length - 2] == (byte) 0x12) {
					SerialPortData_total_4313_CodeId(buffer);
					System.out.println("12");
				} else if (buffer.length >= 1 && isN4313Code_head(buffer[0])) {
					SerialPortData_head(buffer);
					System.out.println("13");
				} else {
					// 错误数据 ， 或者协议可能没写入
					// //Log.d(TAG, "错误数据 ， 或者协议可能没写入");
				}
			} else {
				if (buffer.length > 2 && isN4313Code_head(buffer[0])
						&& buffer[buffer.length - 1] == (byte) 0x14
						&& buffer[buffer.length - 2] == (byte) 0x12) {
					SerialPortData_total_4313_CodeId(buffer);
					System.out.println("14");
					middle_index = 0;
					serialPortData_buffer = null;
				} else if (buffer.length >= 2
						&& buffer[buffer.length - 1] == (byte) 0x14
						&& buffer[buffer.length - 2] == (byte) 0x12) {
					SerialPortData_ass_4313_CodeId(buffer);
				} else {
					// 数据中间
					SerialPortData_middle_4313_CodeId(buffer);
					System.out.println("15");
				}
			}
		}
	}

	/**
	 * 判断是否为4313Coidid头
	 */
	private boolean isN4313Code_head(byte b) {
		// N4313 Q,a,h,j,b,i,d,D,e,m,g,c,E j
		for (int i = 0; i < heads_byte.length; i++) {
			if (heads_byte[i] == b) {
				return true;
			}
		}
		return false;
	}

	/**
	 * 迅宝se955数据处理
	 * 
	 * @param buffer
	 */
	private void getXunBao_Se955(byte[] buffer) {
		try {
			// Log.i("识别为讯宝扫描头返回数据：", Tools.getNowTimeHm());
			handleXunBaoResult(buffer);
			// //Log.d(TAG, "se955_Re_End:" + System.currentTimeMillis());
			Barcode_Stop();
			return;

		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
	}

	/**
	 * 未做协议处理的数据
	 * 
	 * @param buffer
	 */
	private void getDefault(byte[] buffer) {
		// mBeepManager.play();
		if (null != onDataListener) {
			onDataListener.Barcode_Read(buffer, "", 0);
			notifyScanReader();
		}
		// System.out.println("Barcode_Close() Barcode_Stop9");
		Barcode_Stop();
	}

	/**
	 * 新大陆em3070数据处理
	 * 
	 * @param buffer
	 */
	private void getNewLand_Em3070(byte[] buffer) {
		if (buffer.length >= 4 && buffer[0] == (byte) 0x11
				&& buffer[1] == (byte) 0x13
				&& buffer[buffer.length - 1] == (byte) 0x14
				&& buffer[buffer.length - 2] == (byte) 0x12) {
			// 完整数据 用线程处理
			// Message message = new Message();
			// message.what = EM3070_CodeDate_total;
			// message.obj = buffer;
			// mHandler.sendMessage(message);
			SerialPortData_total(buffer);
			// Log.i("time", "SerialPortData_total end:" +
			// Tools.getNowTimeHm());
		} else if (buffer.length >= 2 && buffer[0] == (byte) 0x11
				&& buffer[1] == (byte) 0x13) {
			// 数据头
			SerialPortData_head(buffer);
			// Log.i("time", "SerialPortData_head end:" + Tools.getNowTimeHm());
		} else if (buffer.length >= 4 && buffer[0] != (byte) 0x11
				&& buffer[1] != (byte) 0x13
				&& buffer[buffer.length - 1] != (byte) 0x14
				&& buffer[buffer.length - 2] != (byte) 0x12) {
			// 数据中间
			SerialPortData_middle(buffer);
			// Log.i("time", "SerialPortData_middle end:" +
			// Tools.getNowTimeHm());
		} else if (buffer.length >= 2
				&& buffer[buffer.length - 1] == (byte) 0x14
				&& buffer[buffer.length - 2] == (byte) 0x12) {
			// 数据尾
			SerialPortData_ass(buffer);
			// Log.i("time", "SerialPortData_ass end:" + Tools.getNowTimeHm());
		}
	}

	/**
	 * em3095 完整数据处理
	 * 
	 * @param portData
	 */
	private void SerialPortData_total_3095(byte[] portData) {
		serialPortData_buffer = null;
		middle_index = 0;
		// String val = null;
		byte codeId = 0x00;
		// String codeType = Tools.returnType(portData);
		ArrayList<byte[]> list_byte;

		// //Log.d(TAG, Tools.bytesToHexString(portData));
		list_byte = checkSerialPortData_3095(portData);
		if (null == list_byte || list_byte.size() <= 0) {
			list_byte.add(portData);
		}
		for (int i = 0; i < list_byte.size(); i++) {
			if (list_byte.get(i)[0] <= 35
					&& list_byte.get(i)[list_byte.get(i).length - 1] == (byte) 0x09) {
				codeId = list_byte.get(i)[0];
				byte[] buffer = new byte[list_byte.get(i).length - 2];
				System.arraycopy(list_byte.get(i), 1, buffer, 0, buffer.length);
				// mBeepManager.play();
				if (null != onDataListener) {
					onDataListener.Barcode_Read(buffer,
							CodeType.getNewLandCodeType_3095(codeId), 0);
					notifyScanReader();
				}
			} else {
				continue;
			}
		}
		// System.out.println("Barcode_Close() Barcode_Stop10");
		Barcode_Stop();
	}

	/**
	 * em3095 完整数据处理 AIMID
	 * 
	 * @param portData
	 */
	private void SerialPortData_total(byte[] portData) {
		String val = null;
		String codeId = "";
		String codeType = Tools.returnType(portData);
		ArrayList<byte[]> list_byte;
		list_byte = checkSerialPortData(portData);
		if (null == list_byte || list_byte.size() <= 0) {
			list_byte.add(portData);
		}
		for (int i = 0; i < list_byte.size(); i++) {
			if (list_byte.get(i)[0] == (byte) 0x11
					&& list_byte.get(i)[1] == (byte) 0x13
					&& list_byte.get(i)[list_byte.get(i).length - 1] == (byte) 0x14
					&& list_byte.get(i)[list_byte.get(i).length - 2] == (byte) 0x12) {
				if (getScannerModel() == MODEL_EM3070) {
					if (codeType.equals("default")) {
						val = new String(list_byte.get(i));
						codeId = val.substring(2, 5);
						val = val.substring(5, val.length() - 2);
					} else {
						try {
							val = new String(list_byte.get(i), codeType);
							codeId = val.substring(2, 5);
							val = val.substring(5, val.length() - 2);
						} catch (UnsupportedEncodingException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
					// mBeepManager.play();
					byte[] buffer = new byte[list_byte.get(i).length - 7];
					System.arraycopy(list_byte.get(i), 5, buffer, 0,
							buffer.length);
					if (null != onDataListener) {
						onDataListener.Barcode_Read(
								buffer,
								CodeType.getCodeTypeByAimId_3070(codeId,
										val.length()), 0);
						notifyScanReader();
						// onDataListener.Barcode_Read(buffer,
						// getNewLandCodeType_3070(codeId), 0);
					}
				} else {
					byte[] buffer = new byte[list_byte.get(i).length - 4];
					System.arraycopy(list_byte.get(i), 2, buffer, 0,
							buffer.length);
					if (null != onDataListener) {
						onDataListener.Barcode_Read(buffer, "", 0);
						notifyScanReader();
					}
				}

			} else {
				continue;
			}
			// System.out.println("Barcode_Close() Barcode_Stop11");
			Barcode_Stop();
		}
	}

	/**
	 * 不完整数据头部
	 * 
	 * @param portData
	 */
	private void SerialPortData_head(byte[] portData) {
		middle_index = 0;
		serialPortData_buffer = null;
		serialPortData_buffer = new byte[portData.length];
		serialPortData_buffer = portData;
		// //Log.d(TAG,
		// "SerialPortData_head:"
		// + Tools.bytesToHexString(serialPortData_buffer));
	}

	/**
	 * 不完整数据中部 将数据拼接head数据后面
	 * 
	 * @param portData
	 */
	private void SerialPortData_middle_4313_CodeId(byte[] portData) {
		if (null != serialPortData_buffer) {
			if (middle_index <= 10) {
				byte[] data3 = new byte[serialPortData_buffer.length
						+ portData.length];
				System.arraycopy(serialPortData_buffer, 0, data3, 0,
						serialPortData_buffer.length);
				System.arraycopy(portData, 0, data3,
						serialPortData_buffer.length, portData.length);
				serialPortData_buffer = new byte[data3.length];
				serialPortData_buffer = data3;
				middle_index++;
			} else {
				serialPortData_buffer = null;
				middle_index = 0;
			}
		}
		// //Log.d(TAG,
		// "SerialPortData_middle:"
		// + Tools.bytesToHexString(serialPortData_buffer));
	}

	/**
	 * 不完整数据中部 将数据拼接head数据后面
	 * 
	 * @param portData
	 */
	private void SerialPortData_middle(byte[] portData) {
		if (null != serialPortData_buffer) {
			if (middle_index <= 5) {
				byte[] data3 = new byte[serialPortData_buffer.length
						+ portData.length];
				System.arraycopy(serialPortData_buffer, 0, data3, 0,
						serialPortData_buffer.length);
				System.arraycopy(portData, 0, data3,
						serialPortData_buffer.length, portData.length);
				serialPortData_buffer = new byte[data3.length];
				serialPortData_buffer = data3;
				middle_index++;
			} else {
				serialPortData_buffer = null;
				middle_index = 0;
				if (getScannerModel() == MODEL_EM3095) {
					// 错误数据 ， 或者协议可能没写入
					// //Log.d(TAG,
					// "SerialPortData_middle 错误数据 ， 或者3095协议可能没写入");
					serialPortData_buffer = null;
					middle_index = 0;
					initScanProtocol();
					init_index++;
				}
			}
		}
		// //Log.d(TAG,
		// "SerialPortData_middle:"
		// + Tools.bytesToHexString(serialPortData_buffer));
	}

	/**
	 * 不完整数据尾部
	 * 
	 * @param portData
	 */
	private void SerialPortData_ass(byte[] portData) {
		if (null != serialPortData_buffer) {
			byte[] data3 = new byte[serialPortData_buffer.length
					+ portData.length];
			System.arraycopy(serialPortData_buffer, 0, data3, 0,
					serialPortData_buffer.length);
			System.arraycopy(portData, 0, data3, serialPortData_buffer.length,
					portData.length);
			serialPortData_buffer = new byte[data3.length];
			serialPortData_buffer = data3;
			middle_index = 0;
			// //Log.d(TAG,
			// "SerialPortData_ass:"
			// + Tools.bytesToHexString(serialPortData_buffer));
		}
		if (null != serialPortData_buffer && serialPortData_buffer.length > 0) {
			if (getScannerModel() == MODEL_EM3095) {
				if (serialPortData_buffer[0] <= 35
						&& serialPortData_buffer[serialPortData_buffer.length - 1] == (byte) 0x09) {
					// SerialPortData_total(serialPortData_buffer);
					// 完整数据 用线程处理
					Message message = new Message();
					message.what = MESSAGE_EM3095_CodeDate;
					message.obj = serialPortData_buffer;
					mHandler.sendMessage(message);
				}
			} else {
				if (serialPortData_buffer[0] == (byte) 0x11
						&& serialPortData_buffer[1] == (byte) 0x13
						&& serialPortData_buffer[serialPortData_buffer.length - 1] == (byte) 0x14
						&& serialPortData_buffer[serialPortData_buffer.length - 2] == (byte) 0x12) {
					// SerialPortData_total(serialPortData_buffer);
					// 完整数据 用线程处理
					Message message = new Message();
					message.what = MESSAGE_EM3070_CodeDate;
					message.obj = serialPortData_buffer;
					mHandler.sendMessage(message);
				}
			}
		}
	}

	/**
	 * n4313不完整数据尾部 CodeId
	 * 
	 * @param portData
	 */
	private void SerialPortData_ass_4313_CodeId(byte[] portData) {
		if (null != serialPortData_buffer) {
			byte[] data3 = new byte[serialPortData_buffer.length
					+ portData.length];
			System.arraycopy(serialPortData_buffer, 0, data3, 0,
					serialPortData_buffer.length);
			System.arraycopy(portData, 0, data3, serialPortData_buffer.length,
					portData.length);
			serialPortData_buffer = new byte[data3.length];
			serialPortData_buffer = data3;
			middle_index = 0;
		}
		if (null != serialPortData_buffer && serialPortData_buffer.length > 0) {
			if (serialPortData_buffer.length >= 3
					&& isN4313Code_head(serialPortData_buffer[0])
					&& serialPortData_buffer[serialPortData_buffer.length - 1] == (byte) 0x14
					&& serialPortData_buffer[serialPortData_buffer.length - 2] == (byte) 0x12) {
				// SerialPortData_total(serialPortData_buffer);
				// 完整数据 用线程处理
				Message message = new Message();
				message.what = MESSAGE_N4313_CodeDate;
				message.obj = serialPortData_buffer;
				mHandler.sendMessage(message);
			}
		}
	}

	/**
	 * 4313 完整数据处理
	 * 
	 * @param portData
	 */
	private void SerialPortData_total_4313_CodeId(byte[] portData) {
		serialPortData_buffer = null;
		middle_index = 0;
		// String val = null;
		byte code_id = 0x00;
		// String codeType = Tools.returnType(portData);
		ArrayList<byte[]> list_byte;
		list_byte = checkSerialPortData_N4313_CodeId(portData);
		if (null == list_byte || list_byte.size() <= 0) {
			list_byte.add(portData);
		}
		for (int i = 0; i < list_byte.size(); i++) {
			if (isN4313Code_head(list_byte.get(i)[0])
					&& list_byte.get(i)[list_byte.get(i).length - 1] == (byte) 0x14
					&& list_byte.get(i)[list_byte.get(i).length - 2] == (byte) 0x12) {
				code_id = list_byte.get(i)[0];
				byte[] buffer = new byte[list_byte.get(i).length - 3];
				System.arraycopy(list_byte.get(i), 1, buffer, 0, buffer.length);
				// mBeepManager.play();
				if (null != onDataListener) {
					onDataListener.Barcode_Read(buffer,
							CodeType.getHonyWellType(code_id), 0);
					notifyScanReader();
					System.out.println("SerialPortData_total_4313_CodeId");
				}

			} else {
				continue;
			}
			// System.out.println("SerialPortData_total_4313_CodeId Barcode_Stop");
			Barcode_Stop();

		}
	}

	private ArrayList<byte[]> checkSerialPortData_N4313_CodeId(
			byte[] serialPortData) {
		// Log.i("time", "checkSerialPortData start:" + Tools.getNowTimeHm());
		ArrayList<byte[]> list = new ArrayList<byte[]>();
		int iStart = 0;
		int iEnd = 0;
		boolean isHasHead = false;
		for (int i = 0; i < serialPortData.length; i++) {

			if (!isHasHead) {
				if (i < serialPortData.length
						&& isN4313Code_head(serialPortData[i])) {
					isHasHead = true;
					iStart = i;
				}
			}

			if (isHasHead) {
				if (i > 0 && serialPortData[i - 1] == 0x12
						&& serialPortData[i] == 0x14) {
					iEnd = i;
					byte[] temp = new byte[iEnd - iStart + 1];
					int index = 0;
					for (int j = iStart; j <= iEnd; j++) {
						temp[index] = serialPortData[j];
						index++;
					}
					list.add(temp);
					isHasHead = false;
					iStart = 0;
					iEnd = 0;
				}
			}
		}
		// Log.i("time", "checkSerialPortData end:" + Tools.getNowTimeHm());
		return list;
	}

	// 20150212 em3070协议检查 以防出现乱码
	private static ArrayList<byte[]> checkSerialPortData(byte[] serialPortData) {
		// Log.i("time", "checkSerialPortData start:" + Tools.getNowTimeHm());
		ArrayList<byte[]> list = new ArrayList<byte[]>();
		int iStart = 0;
		int iEnd = 0;
		boolean isHasHead = false;
		for (int i = 0; i < serialPortData.length; i++) {

			if (i < serialPortData.length && serialPortData[i] == 0x11
					&& serialPortData[i + 1] == 0x13) {
				isHasHead = true;
				iStart = i;
			}

			if (isHasHead) {
				if (i > 0 && serialPortData[i - 1] == 0x12
						&& serialPortData[i] == 0x14) {
					iEnd = i;
					byte[] temp = new byte[iEnd - iStart + 1];
					int index = 0;
					for (int j = iStart; j <= iEnd; j++) {
						temp[index] = serialPortData[j];
						index++;
					}
					list.add(temp);
					isHasHead = false;
					iStart = 0;
					iEnd = 0;
				}
			}
		}
		// Log.i("time", "checkSerialPortData end:" + Tools.getNowTimeHm());
		return list;
	}

	private static ArrayList<byte[]> checkSerialPortData_3095(
			byte[] serialPortData) {
		ArrayList<byte[]> list = new ArrayList<byte[]>();
		int iStart = 0;
		int iEnd = 0;
		boolean isHasHead = false;
		for (int i = 0; i < serialPortData.length; i++) {

			if (!isHasHead) {
				if (i < serialPortData.length && serialPortData[i] <= 35) {
					isHasHead = true;
					iStart = i;
				}
			}

			if (isHasHead) {
				if (i > 0 && serialPortData[i] == 0x09) {
					iEnd = i;
					byte[] temp = new byte[iEnd - iStart + 1];
					int index = 0;
					for (int j = iStart; j <= iEnd; j++) {
						temp[index] = serialPortData[j];
						index++;
					}
					list.add(temp);
					isHasHead = false;
					iStart = 0;
					iEnd = 0;
				}
			}
		}
		return list;
	}

	// 4313协议检查 以防出现乱码
	private static ArrayList<byte[]> checkSerialPortData_4313(
			byte[] serialPortData) {
		// Log.i("time", "checkSerialPortData start:" + Tools.getNowTimeHm());
		ArrayList<byte[]> list = new ArrayList<byte[]>();
		int iStart = 0;
		int iEnd = 0;
		boolean isHasHead = false;
		for (int i = 0; i < serialPortData.length; i++) {

			if (!isHasHead && i < serialPortData.length
					&& serialPortData[i] == 0x5D) {
				isHasHead = true;
				iStart = i;
			}

			if (isHasHead) {
				if (i > 0 && serialPortData[i - 1] == 0x12
						&& serialPortData[i] == 0x14) {
					iEnd = i;
					byte[] temp = new byte[iEnd - iStart + 1];
					int index = 0;
					for (int j = iStart; j <= iEnd; j++) {
						temp[index] = serialPortData[j];
						index++;
					}
					list.add(temp);
					isHasHead = false;
					iStart = 0;
					iEnd = 0;
				}
			}
		}
		return list;
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

	/**
	 * 协议写入检查
	 */
	private Runnable checkInitSuccess = new Runnable() {

		@Override
		public void run() {
			// TODO Auto-generated method stub
			if (!is_SerialPortOpen) {
				return;
			}
			switch (current_check) {
			case MESSAGE_CHECK_INIT_N4313:
				// 检查协议是否写入
				if (getScanPrefix() == Protocol_N4313_Protocol) {
					if (init_N4313_start && init_N4313_end) {
						// //Log.d(TAG, "check init N4313 Protocol success!");
						Preference.setScanInit(serviceContext, true);
					} else {
						if (init_index <= 5) {
							// //Log.d(TAG,
							// "check init N4313 Protocol fauile , restart init!");
							if (!init_N4313_start) {
								// //Log.d(TAG,
								// "check init N4313Prefix_start fauile , restart init!");
								setIsReceive(true);
								notifyReader();
								writeCommand(wakeUp);
								try {
									Thread.sleep(100);
								} catch (InterruptedException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								}
								setIsReceive(true);
								notifyReader();
								writeCommand(N4313Prefix_start);
							}
							if (!init_N4313_end) {
								// //Log.d(TAG,
								// "check init N4313Prefix_end fauile , restart init!");
								setIsReceive(true);
								notifyReader();
								writeCommand(wakeUp);
								try {
									Thread.sleep(100);
								} catch (InterruptedException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								}
								setIsReceive(true);
								notifyReader();
								writeCommand(N4313Prefix_end);
							}

							mHandler.removeCallbacks(checkInitSuccess);
							current_check = MESSAGE_CHECK_INIT_N4313;
							mHandler.postDelayed(checkInitSuccess, 2000);
							// mHandler.sendEmptyMessageDelayed(
							// MESSAGE_CHECK_INIT_N4313, 2000);
							// //Log.d(TAG, "init:N4313 Protoco;");
							mHandler.removeCallbacks(stopReceice);
							mHandler.postDelayed(stopReceice, 3 * 1000);
							init_index++;
						} else {
							// //Log.d(TAG,
							// "check init N4313 Protocol fauile 5 time ,please close and restart init!");
						}
					}
				} else if (getScanPrefix() == Protocol_N4313_Codeid
						|| getScanPrefix() == Protocol_N3680_Codeid) {
					if (init_N4313_Code_id && init_N4313_end) {
						// //Log.d(TAG, "check init N4313 Code_id success!");
						Preference.setScanInit(serviceContext, true);
					} else {
						if (init_index <= 5) {
							// //Log.d(TAG,
							// "check init N4313 Code_id fauile , restart init!");
							if (!init_N4313_Code_id) {
								// //Log.d(TAG,
								// "check init N4313Prefix_Code_id fauile , restart init!");
								setIsReceive(true);
								notifyReader();
								writeCommand(wakeUp);
								try {
									Thread.sleep(100);
								} catch (InterruptedException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								}
								setIsReceive(true);
								notifyReader();
								writeCommand(N4313Prefix_Code_id);
							}
							if (!init_N4313_end) {
								// //Log.d(TAG,
								// "check init N4313Prefix_end fauile , restart init!");
								setIsReceive(true);
								notifyReader();
								writeCommand(wakeUp);
								try {
									Thread.sleep(100);
								} catch (InterruptedException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								}
								setIsReceive(true);
								notifyReader();
								writeCommand(N4313Prefix_end);
							}

							mHandler.removeCallbacks(checkInitSuccess);
							current_check = MESSAGE_CHECK_INIT_N4313;
							mHandler.postDelayed(checkInitSuccess, 2000);
							// //Log.d(TAG, "init:N4313 Code id");
							mHandler.removeCallbacks(stopReceice);
							mHandler.postDelayed(stopReceice, 3 * 1000);
							init_index++;
						} else {
							// //Log.d(TAG,
							// "check init N4313 Code_id fauile 5 time ,please close and restart init!");
						}
					}
				}

				break;

			case MESSAGE_CHECK_INIT_EM3070:
				if (init_EM3070_protocol) {
					// //Log.d(TAG, "check init EM3070 success!");
				} else {
					// //Log.d(TAG, "check init EM3070 fauile!");
					if (init_index <= 5) {
						initScanProtocol();
					} else {
						// //Log.d(TAG,
						// "check init EM3070 fauile 5 time ,please close and restart init!");
					}
				}
				break;

			case MESSAGE_CHECK_INIT_EM3095:
				if (init_EM3095_protocol) {
					// //Log.d(TAG, "check init EM3095 success!");
				} else {
					// //Log.d(TAG, "check init EM3095 fauile!");
					if (init_index <= 5) {
						initScanProtocol();
					} else {
						// //Log.d(TAG,
						// "check init EM3095 fauile 5 time ,please close and restart init!");
					}
				}
				break;

			case MESSAGE_CHECK_INIT_SE955:
				if (init_SE955_protocol) {
					// //Log.d(TAG, "check init SE955 success!");
				} else {
					// //Log.d(TAG, "check init SE955 fauile!");
					if (init_index <= 5) {
						initScanProtocol();
					} else {
						// //Log.d(TAG,
						// "check init SE955 fauile 5 time ,please close and restart init!");
					}
				}
				break;

			default:
				break;
			}

		}
	};

	/**
	 * 设置扫描间隔时间 time 间隔时间 单位ms
	 */
	public void setScanTime(long time) {
		scan_time = time;
	}

	public void setCallback(Callback callback) {
		this.onDataListener = callback;
	}

	public synchronized static void setIsReceive(boolean b) {
		isReceive = b;
		// System.out.println("setIsReceive:"+isReceive);
	}

	public int getScan_count() {
		return all_scan_count;
	}

	public void clearScan_count() {
		all_scan_count = 0;
	}
}
