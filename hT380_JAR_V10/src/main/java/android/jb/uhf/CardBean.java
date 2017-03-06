package android.jb.uhf;

public class CardBean {

	private boolean isEnd = false;
	private String pcId;
	private String epc;
	private String rssi;
	private int findTime;
	public String getPcId() {
		return pcId;
	}
	public void setPcId(String pcId) {
		this.pcId = pcId;
	}
	public String getEpc() {
		return epc;
	}
	public void setEpc(String epc) {
		this.epc = epc;
	}
	public String getRssi() {
		return rssi;
	}
	public void setRssi(String rssi) {
		this.rssi = rssi;
	}
	public int getFindTime() {
		return findTime;
	}
	public void setFindTime(int findTime) {
		this.findTime = findTime;
	}
	public boolean isEnd() {
		return isEnd;
	}
	public void setEnd(boolean isEnd) {
		this.isEnd = isEnd;
	}

	
}
