package android.jb.uhf;

public class ErrorBean {
	public static final int Error_Find_Card_Failed = 1;	//寻卡失败
	public static final int Error_Check_Failed = 2;		//通信帧校验码错误
	public static final int Error_Temperature_Too_Heigh = 3;	//反射功率过大
	public static final int Error_Reflection_Power_Too_Heigh = 4;	//温度过高
	public int errorCode = -1;	//错误码
}
