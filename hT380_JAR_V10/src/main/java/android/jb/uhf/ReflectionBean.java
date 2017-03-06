package android.jb.uhf;

import android.jb.utils.Tools;

public class ReflectionBean {
	public byte errorCode;
	public String power;
	public byte h;
	public byte l;
	private int hexInt[] = { 0x0000, /* -25dBm */
	0x0000, /* -24dBm */
	0x0000, /* -23dBm */
	0x0001, /* -22dBm */
	0x0002, /* -21dBm */
	0x0005, /* -20dBm */
	0x0007, /* -19dBm */
	0x000B, /* -18dBm */
	0x0010, /* -17dBm */
	0x0116, /* -16dBm */
	0x011D, /* -15dBm */
	0x0126, /* -14dBm */
	0x0131, /* -13dBm */
	0x013E, /* -12dBm */
	0x024C, /* -11dBm */
	0x0260, /* -10dBm */
	0x0374, /* -09dBm */
	0x048B, /* -08dBm */
	0x05A5, /* -07dBm */
	0x06C3, /* -06dBm */
	0x08E6, /* -05dBm */
	0x09FF, /* -04dBm */
	0x0BFF, /* -03dBm */
	0x0EFF, /* -02dBm */
	0x10FF, /* -01dBm */
	0x14FF, /* 00dBm */
	0x17FF, /* 01dBm */
	0x1CFF, /* 02dBm */
	0x21FF, /* 03dBm */
	0x26FF, /* 04dBm */
	0x2DFF, /* 05dBm */
	0x34FF, /* 06dBm */
	0x3CFF, /* 07dBm */
	0x46FF, /* 08dBm */
	0x50FF, /* 09dBm */
	0x5DFF, /* 10dBm */
	0x6AFF, /* 11dBm */
	0x7AFF, /* 12dBm */
	0x8BFF, /* 13dBm */
	0x9EFF, /* 14dBm */
	0xB4FF, /* 15dBm */
	0xCCFF, /* 16dBm */
	0xE7FF, /* 17dBm */
	0xFFFF, /* 18dBm */
	};

	public ReflectionBean(byte h, byte l, byte errorCode) {
		this.h = h;
		this.l = l;
		this.errorCode = errorCode;
		calculatePower();
	}

	private void calculatePower() {
		power = "";
		int tmp = ((h & 0x000000ff) << 8) | (l & 0x000000ff);
		for (int i = 0; i < hexInt.length; i++) {
			int iTen = Integer.parseInt(hexInt[i] + "", 10);
			if (tmp < iTen) {
				power = "" + (-25 + i) + "dBm";
				break;
			}
		}
	}

	// public byte h;
	// public byte l;
	// public float power;
	// public byte errorCode;
	//
	// public ReflectionBean(byte h, byte l, byte errorCode) {
	// this.h = h;
	// this.l = l;
	// this.errorCode = errorCode;
	// calculatePower();
	// }
	//
	// public void calculatePower() {
	// // TODO Auto-generated method stub
	// power = getPower(h, l) / 100f;
	//
	// }
	//
	// public int getPower(byte h, byte l) {
	// // TODO Auto-generated method stub
	// int re = 0;
	// if (h > 0) {
	// re = (byte) (l & 0x0F);
	// re = re + (((byte) ((l >> 4) & 0x0F)) * 16);
	// re = re + (((byte) (h & 0x0F)) * 256);
	// re = re + (((byte) ((h >> 4) & 0x0F)) * 4096);
	// } else {
	// re = (byte) ((~l) & 0x0F);
	// re = re + (((byte) (((~l) >> 4) & 0x0F)) * 16);
	// re = re + (((byte) ((~h) & 0x0F)) * 256);
	// re = re + (((byte) (((~h) >> 4) & 0x0F)) * 4096);
	// re = re + 1;
	// }
	// return re;
	// }
	private void hexStringtoInt(String hex) {
		hex.replace("0X", "");
		hex.replace("0x", "");
		System.out.println(hex);

	}
}
