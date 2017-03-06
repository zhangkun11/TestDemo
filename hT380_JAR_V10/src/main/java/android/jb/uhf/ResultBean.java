package android.jb.uhf;

public class ResultBean {
	private int errorCode;
	private byte[] resultData;
	
	public int getErrorCode() {
		return errorCode;
	}
	public void setErrorCode(int errorCode) {
		this.errorCode = errorCode;
	}
	public byte[] getResultData() {
		return resultData;
	}
	public void setResultData(byte[] resultData) {
		this.resultData = resultData;
	}
	
}
