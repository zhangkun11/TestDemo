package android.jb.uhf;

import java.util.ArrayList;
import java.util.List;


public class TransmittedPowerMsgBean {
	public class Antenna {
		public byte wL;
		public byte wH;
		public byte rL;
		public byte rH;
		/**
		 * antenna number
		 */
		public int number;
		/**
		 * write power dBm
		 */
		public float writePower;
		/**
		 * read power dBm
		 */
		public float readPower;

		public void calculatePower() {
			// TODO Auto-generated method stub
			writePower = getPower(rH, rL)/100f;
			readPower = getPower(wH, wL)/100f;

		}
	}

	/**
	 * 0--open loop; 1--close loop; othervalue--error
	 */
	public int status;
	public List<Antenna> antennaList = new ArrayList<TransmittedPowerMsgBean.Antenna>();

	public int getPower(byte h, byte l) {
		// TODO Auto-generated method stub
		int re = 0;
		if (h > 0) {
			re = (byte) (l & 0x0F);
			re = re + (((byte) ((l >> 4) & 0x0F)) * 16);
			re = re + (((byte) (h & 0x0F)) * 256);
			re = re + (((byte) ((h >> 4) & 0x0F)) * 4096);
		} else {
			re = (byte) ((~l) & 0x0F);
			re = re + (((byte) (((~l) >> 4) & 0x0F)) * 16);
			re = re + (((byte) ((~h) & 0x0F)) * 256);
			re = re + (((byte) (((~h) >> 4) & 0x0F)) * 4096);
			re = re + 1;
		}
		return re;
	}
}
