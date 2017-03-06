package android.jb.barcode;

public class CodeType {

	/**
	 * 根据AIMId获取 条码类型
	 * 
	 * @param AimId
	 * @return
	 */
	public static String getCodeTypeByAimId_3070(String AimId, int lenght) {
		// TODO Auto-generated method stub
		if ("]E0".equals(AimId)) {
			if (lenght == 13) {
				return "EAN-13";
			} else if (lenght == 8 || lenght == 7) {
				return "UPC-E";
			} else if (lenght == 12) {
				return "UPC-A";
			}
			return "EAN-13";
		} else if ("]E4".equals(AimId)) {
			return "EAN-8";
		} else if ("]C0".equals(AimId)) {
			return "Code 128";
		} else if ("]C1".equals(AimId)) {
			// return "GS1-128(UCC/EAN-128)";
			return "EAN-128";
		} else if ("]C2".equals(AimId)) {
			return "AIM-128";
		} else if ("]C4".equals(AimId)) {
			return "ISBT-128";
		} else if ("]I0".equals(AimId) || "]I1".equals(AimId)
				|| "]I3".equals(AimId)) {
			return "Interleaved 2 of 5";
		} else if ("]S0".equals(AimId)) {
			return "Industrial 2 of 5";
		} else if ("]R0".equals(AimId) || "]R8".equals(AimId)
				|| "]R9".equals(AimId)) {
			return "Standard 2 of 5";
		} else if ("]A0".equals(AimId) || "]A1".equals(AimId)
				|| "]A3".equals(AimId) || "]A4".equals(AimId)
				|| "]A5".equals(AimId) || "]A7".equals(AimId)) {
			return "Code 39";
		} else if ("]F0".equals(AimId) || "]F2".equals(AimId)
				|| "]F4".equals(AimId)) {
			return "Codabar";
		} else if ("]G0".equals(AimId)) {
			return "Code 93";
		} else if ("]H1".equals(AimId) || "]H3".equals(AimId)
				|| "]H0".equals(AimId) || "]H9".equals(AimId)) {
			return "Code 11 ";
		} else if ("]e0".equals(AimId)) {
			return "GS1-DataBar(RSS)";
		} else if ("]P0".equals(AimId)) {
			return "Plessey";
		} else if ("]M0".equals(AimId) || "]M1".equals(AimId)
				|| "]M8".equals(AimId) || "]M9".equals(AimId)) {
			return "MSI-Plessey";
		} else if ("]X0".equals(AimId) || "]X1".equals(AimId)
				|| "]X2".equals(AimId) || "]X3".equals(AimId)) {
			return "Matrix 2 of 5";
		} else if ("]X4".equals(AimId)) {
			// return "ISBN";
			return "EAN 13";
		} else if ("]X5".equals(AimId)) {
			// return "ISSN";
			return "EAN 13";
		} else if ("]L0".equals(AimId)) {
			return "PDF417";
		} else if ("]Q0".equals(AimId) || "]Q1".equals(AimId)
				|| "]Q2".equals(AimId) || "]Q3".equals(AimId)
				|| "]Q4".equals(AimId) || "]Q5".equals(AimId)
				|| "]Q6".equals(AimId)) {
			return "QR Code";
		} else if ("]d0".equals(AimId) || "]d1".equals(AimId)
				|| "]d2".equals(AimId) || "]d3".equals(AimId)
				|| "]d4".equals(AimId) || "]d5".equals(AimId)
				|| "]d6".equals(AimId)) {
			return "Data Matrix";
		} else {
			return "Unknown";
		}
	}

	/**
	 * 获取新大陆EM3095 条码类型
	 * 
	 * @param newLandCodeType2
	 * @return
	 */
	public static String getNewLandCodeType_3095(byte newLandCodeType2) {
		// TODO Auto-generated method stub
		switch (newLandCodeType2) {
		case 1:

			return "Code 128 FNC3";

		case 2:

			return "Code 128";
		case 3:
			return "EAN-128";
			// return "UCC/EAN 128";
		case 4:
			return "EAN-8";
		case 5:
			return "EAN-13";
		case 6:
			return "UPC-E";
		case 7:
			return "UPC-A";
		case 8:
			return "Interleaved 2 of 5";
		case 9:
			return "ITF-14";
		case 10:
			return "ITF-6";
		case 13:
			return "Code 39";
		case 15:
			return "Codabar";
		case 16:
			return "Standard 25";
		case 17:
			return "Code 93";
		case 21:
			return "AIM 128";
		case 22:
			return "MSI Plessey";
		case 23:
			// return "ISBN";
			return "EAN 13";
		case 24:
			return "Industrial 25";
		case 25:
			return "Matrix 2 of 5";
		case 26:
			return "RSS-14";
		case 27:
			return "RSS Limited";
		case 28:
			return "RSS Expand";
		case 29:
			return "Code 11";
		case 30:
			return "Plessey";
		case 31:
			// return "ISSN";
			return "EAN 13";
		case 32:
			return "PDF417";
		case 33:
			return "QR";
		case 35:
			return "Data Matrix";
		default:
			break;
		}
		return "";
	}

	/**
	 * 根据AIMId获取 条码类型
	 * 
	 * @param AimId
	 * @return
	 */
	public static String getCodeTypeByAimId_4313(String AimId) {
		// TODO Auto-generated method stub
		if ("]E0".equals(AimId) || "]E3".equals(AimId)) {
			return "EAN-13/UPC-E/UPC-A";
		} else if ("]E4".equals(AimId)) {
			return "EAN-8";
		} else if ("]C0".equals(AimId)) {
			return "Code 128";
		} else if ("]C1".equals(AimId)) {
			// return "GS1-128(UCC/EAN-128)";
			return "EAN-128";
		} else if ("]C2".equals(AimId)) {
			return "AIM-128";
		} else if ("]C4".equals(AimId)) {
			return "ISBT-128";
		} else if ("]I0".equals(AimId) || "]I1".equals(AimId)
				|| "]I3".equals(AimId)) {
			return "Interleaved 2 of 5/ITF-6/ITF-14";
		} else if ("]R0".equals(AimId) || "]R8".equals(AimId)
				|| "]R9".equals(AimId)) {
			return "Standard 2 of 5";
		} else if ("]S0".equals(AimId)) {
			return "Industrial 2 of 5";
		} else if ("]A0".equals(AimId) || "]A1".equals(AimId)
				|| "]A2".equals(AimId) || "]A3".equals(AimId)
				|| "]A4".equals(AimId) || "]A5".equals(AimId)
				|| "]A7".equals(AimId)) {
			return "Code 39";
		} else if ("]F0".equals(AimId) || "]F2".equals(AimId)
				|| "]F4".equals(AimId)) {
			return "Codabar";
		} else if ("]G0".equals(AimId)) {
			return "Code 93";
		} else if ("]H1".equals(AimId) || "]H3".equals(AimId)
				|| "]H0".equals(AimId) || "]H9".equals(AimId)) {
			return "Code 11 ";
		} else if ("]P0".equals(AimId)) {
			return "Plessey";
		} else if ("]M0".equals(AimId) || "]M1".equals(AimId)
				|| "]M8".equals(AimId) || "]M9".equals(AimId)) {
			return "MSI-Plessey";
		} else if ("]X0".equals(AimId) || "]X1".equals(AimId)
				|| "]X2".equals(AimId) || "]X3".equals(AimId)) {
			return "Matrix 2 of 5";
		} else if ("]e0".equals(AimId)) {
			return "GS1 Databar";
		} else if ("]X4".equals(AimId)) {
			// return "ISBN";
			return "EAN 13";
		} else if ("]X5".equals(AimId)) {
			// return "ISSN";
			return "EAN 13";
		} else if ("]L0".equals(AimId)) {
			return "PDF417";
		} else if ("]Q0".equals(AimId) || "]Q1".equals(AimId)
				|| "]Q2".equals(AimId) || "]Q3".equals(AimId)
				|| "]Q4".equals(AimId) || "]Q5".equals(AimId)
				|| "]Q6".equals(AimId)) {
			return "QR Code";
		} else if ("]d0".equals(AimId) || "]d1".equals(AimId)
				|| "]d2".equals(AimId) || "]d3".equals(AimId)
				|| "]d4".equals(AimId) || "]d5".equals(AimId)
				|| "]d6".equals(AimId)) {
			return "Data Matrix";
		} else {
			return "Unknown";
		}
	}

	/**
	 * 获取新大陆EM3070 条码类型
	 * 
	 * @param newLandCodeType2
	 * @return
	 */
	public static String getNewLandCodeType_3070(String newLandCodeType2) {
		// TODO Auto-generated method stub
		if ("j".equals(newLandCodeType2)) {
			return "Code 128";
		} else if ("f".equals(newLandCodeType2)) {
			return "AIM-128";
		} else if ("d".equals(newLandCodeType2)) {
			return "EAN-8";
		} else if ("n".equals(newLandCodeType2)) {
			// return "ISSN";
			return "EAN 13";
		} else if ("B".equals(newLandCodeType2)) {
			// return "ISBN";
			return "EAN 13";
		} else if ("c".equals(newLandCodeType2)) {
			return "UPC-A";
		} else if ("e".equals(newLandCodeType2)) {
			return "Interleaved 2 of 5";
		} else if ("v".equals(newLandCodeType2)) {
			return "Matrix 2 of 5 ";
		} else if ("D".equals(newLandCodeType2)) {
			return "Industrial 2 of 5 ";
		} else if ("s".equals(newLandCodeType2)) {
			return "Standard 2 of 5";
		} else if ("b".equals(newLandCodeType2)) {
			return "Code 39";
		} else if ("a".equals(newLandCodeType2)) {
			return "Codabar";
		} else if ("i".equals(newLandCodeType2)) {
			return "Code 93";
		} else if ("H".equals(newLandCodeType2)) {
			return "Code 11 ";
		} else if ("p".equals(newLandCodeType2)) {
			return "Plessey";
		} else if ("m".equals(newLandCodeType2)) {
			return "MSI-Plessey";
		} else if ("R".equals(newLandCodeType2)) {
			return "GS1 Databar";
		} else if ("r".equals(newLandCodeType2)) {
			return "PDF417";
		} else if ("Q".equals(newLandCodeType2)) {
			return "QR Code";
		} else if ("u".equals(newLandCodeType2)) {
			return "Data Matrix";
		} else {
			return "Unknown";
		}
	}

	/**
	 * 获取迅宝SE955一维码编码类型
	 * 
	 * @param typeNum
	 *            CODE ID
	 */
	public static String getXunbaoType(byte typeNum) {
		switch (typeNum) {
		case 0x01:
			return "Code 39";

		case 0x02:
			return "Codabar";

		case 0x03:
			return "Code 128";

		case 0x04:
			return "discrete";

		case 0x05:
			return "IATA";

		case 0x06:
			return "Interleaved";

		case 0x07:
			return "Code 93";

		case 0x08:
		case 0x48:
		case (byte) 0x88:
			return "UPC A";

		case 0x09:
		case 0x49:
		case (byte) 0x89:
			return "UPC E";

		case 0x0A:
			return "EAN 8";

		case (byte) 0x8B:
		case 0x0B:
		case 0x4B:
			return "EAN 13";

		case 0x0E:
			return "MSI";

		case 0x0F:
			return "EAN 128";

		case 0x10:
		case 0x50:
		case (byte) 0x90:
			return "UPC E1";
		case 0x15:
			return "Trioptic Code";

		case 0x16:
			return "Bookland EAN";

		case 0x17:
			return "Coupon Code";

		case 0x23:
			return "RSS-Limited";

		case 0x24:
			return "RSS-14";

		case 0x25:
			return "RSS-Expanded";

		default:
			return "Unknown";
		}
	}

	/**
	 * 获取N4313一维码编码类型
	 * 
	 * @param typeNum
	 *            CODE ID
	 */
	public static String getHonyWellType(byte typeNum) {
		switch (typeNum) {

		case 0x41:
			return "Australian Post";

		case 0x7A:
			return "Aztec Code";

		case 0x42:
			return "British Post";

		case 0x43:
			return "Canadian Post";

		case 0x51:
			return "China Post";

		case 0x48:
			return "Chinese Sensible Code (Han Xin Code)";

		case 0x61:
			return "Codabar";

		case 0x56:
			return "Codablock A";

		case 0x71:
			return "Codablock F";

		case 0x68:
			return "Code 11";

		case 0x6A:
			return "Code 128";

		case 0x49:
			// return "GS1-128";
			return "EAN-128";

		case (byte) 0x3C:
			return "Code 32 Pharmaceutical (PARAF)";

		case 0x62:
			return "Code 39";

		case 0x6C:
			return "Code 49";

		case 0x69:
			return "Code 93";

		case 0x77:
			return "Data Matrix";

		case 0x64:
			return "EAN-13";

		case 0x44:
			return "EAN-8";

		case 0x79:
			return "GS1 Composite";

		case 0x7B:
			return "GS1 DataBar Limited";

		case (byte) 0x7D:
			return "GS1 DataBar Expanded";

		case 0x2c:
			return "InfoMail";

		case 0x4D:
			return "Intelligent Mail Bar Code ";

		case 0x65:
			return "Interleaved 2 of 5";

		case 0x4A:
			return "Japanese Post";

		case 0x4B:
			return "KIX (Netherlands) Post";
		case 0x3F:
			return "Korea Post";
		case 0x6D:
			return "Matrix 2 of 5";
		case 0x78:
			return "MaxiCode";
		case 0x52:
			return "MicroPDF417";
		case 0x67:
			return "MSI";
		case 0x59:
			return "NEC 2 of 5";
		case 0x4F:
			return "OCR";
		case 0x72:
			return "PDF417";
		case 0x4C:
			return "Planet Code";
		case 0x6E:
			return "Plessey Code";
		case 0x4E:
			return "Postal-4i";
		case 0x50:
			return "Postnet";
		case 0x73:
			return "QR Code";
		case 0x66:
			return "Straight 2 of 5";
		case 0x54:
			return "TCIF Linked Code 39";
		case 0x74:
			return "Telepen";
		case 0x63:
			return "UPC-A";
		case 0x45:
			return "UPC-E";

		default:
			return "Unknown";
		}
	}
}
