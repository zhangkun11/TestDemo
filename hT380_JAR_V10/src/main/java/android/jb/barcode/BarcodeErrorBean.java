package android.jb.barcode;

public class BarcodeErrorBean {
	public static final int Error_Not = 0;	//无错误
	public static final int Error_Init_Failed = 1;	//扫描头协议写入失败
	public int errorCode;	//错误码
}
