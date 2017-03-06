package android.jb.utils;

import java.math.BigDecimal;

public class ArithUtil {

	 private static final int DEF_DIV_SCALE = 10;
	    
	    /**
	     * 两个Double数相加
	     * @param v1
	     * @param v2
	     * @return Double
	     */
	    public static Double add(Double v1,Double v2){
	        BigDecimal b1 = new BigDecimal(v1.toString());
	        BigDecimal b2 = new BigDecimal(v2.toString());
	        return b1.add(b2).doubleValue();
	    }
	    
	    /**
	     * 两个Double数相加 去除小数点后的零
	     * @param v1
	     * @param v2
	     * @param t 是否去除小数点
	     * @return String
	     */
	    public static String add(Double v1,Double v2,boolean t){
	        BigDecimal b1 = new BigDecimal(v1.toString());
	        BigDecimal b2 = new BigDecimal(v2.toString());
	        double number = b1.add(b2).doubleValue();
	        String Number = String.valueOf(number);
	        if(t){
	        	BigDecimal bg = new BigDecimal(number);
				number = bg.setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();
				if(number % 1.0 == 0){
				        Number =  String.valueOf((long)number);
			    }else {
					Number =  String.valueOf(number);
				}
	        }
	        return Number;
	    }
	    
	    /**
	     * 两个Double数相减  去除小数点后的零
	     * @param v1
	     * @param v2
	     * @param t 是否去除小数点
	     * @return String
	     */
	    public static String sub(Double v1,Double v2,boolean t){
	        BigDecimal b1 = new BigDecimal(v1.toString());
	        BigDecimal b2 = new BigDecimal(v2.toString());
	        double number = b1.subtract(b2).doubleValue();
	        String Number = String.valueOf(number);
	        if(t){
	        	BigDecimal bg = new BigDecimal(number);
				number = bg.setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();
				if(number % 1.0 == 0){
				        Number =  String.valueOf((long)number);
			    }else {
					Number =  String.valueOf(number);
				}
	        }
	        return Number;
	    }
	    
	    /**
	     * 两个Double数相减
	     * @param v1
	     * @param v2
	     * @return Double
	     */
	    public static Double sub(Double v1,Double v2){
	        BigDecimal b1 = new BigDecimal(v1.toString());
	        BigDecimal b2 = new BigDecimal(v2.toString());
	        return b1.subtract(b2).doubleValue();
	    }
	    
	    /**
	     * 两个Double数相乘
	     * @param v1
	     * @param v2
	     * @return Double
	     */
	    public static Double mul(Double v1,Double v2){
	        BigDecimal b1 = new BigDecimal(v1.toString());
	        BigDecimal b2 = new BigDecimal(v2.toString());
	        return b1.multiply(b2).doubleValue();
	    }
	    
	    /**
	     * 两个Double数相除
	     * @param v1
	     * @param v2
	     * @return Double
	     */
	    public static Double div(Double v1,Double v2){
	        BigDecimal b1 = new BigDecimal(v1.toString());
	        BigDecimal b2 = new BigDecimal(v2.toString());
	        return b1.divide(b2,DEF_DIV_SCALE,BigDecimal.ROUND_HALF_UP).doubleValue();
	    }
	    
	    /**
	     * 两个Double数相除，并保留scale位小数
	     * @param v1
	     * @param v2
	     * @param scale
	     * @return Double
	     */
	    public static Double div(Double v1,Double v2,int scale){
	        if(scale<0){
	            throw new IllegalArgumentException(
	            "The scale must be a positive integer or zero");
	        }
	        BigDecimal b1 = new BigDecimal(v1.toString());
	        BigDecimal b2 = new BigDecimal(v2.toString());
	        return b1.divide(b2,scale,BigDecimal.ROUND_HALF_UP).doubleValue();
	    }

}
