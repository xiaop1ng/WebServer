package com.xiaoping.util;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Log 管理
 * @author xiaopng
 *
 */
public class Log {
	
	private Log() {
		throw new UnsupportedOperationException("cannot be instantiated");
	}
	
	public static boolean isDebug = true;
	
	private static SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	
	private static final String TAG = ":) ";
	
	/**
	 * print with TAG
	 * @param msg
	 */
	public static void i(String msg) {
		if(isDebug) {
			
			System.out.println(TAG + sdf.format(new Date()) + " -> " +  msg);
		}
	}
	
	/**
	 * print without TAG
	 * @param msg
	 */
	public static void m(String msg) {
		if(isDebug) {
			System.out.println(msg);
		}
	}
	
}
