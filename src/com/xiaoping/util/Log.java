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
	 * @param i
	 */
	public static void i(Object msg) {
		if(isDebug) {
			System.out.println(TAG + sdf.format(new Date()) + " -> " +  (null!=msg ? msg.toString() : "NULL"));
		}
	}
	
	/**
	 * print without TAG
	 * @param msg
	 */
	public static void m(Object msg) {
		if(isDebug) {
			System.out.println(null!=msg ? msg.toString() : "NULL");
		}
	}
	
}
