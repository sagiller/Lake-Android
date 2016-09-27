package com.capgemini.psclient.utils;

import java.text.SimpleDateFormat;
import java.util.Date;

public class TimeUtils{
	private TimeUtils(){
		throw new UnsupportedOperationException("cannot be instantiated");
	}

	public static long getCurrentTimeMillis(){
		return System.currentTimeMillis();
	}

	public static Date getCurrentDate(){
		return new Date(getCurrentTimeMillis());
	}

	public static String getFormatDate(Date date){
		SimpleDateFormat df = new SimpleDateFormat("yyyy_MM_dd_HH_mm_ss");
		return df.format(date);
	}

	public static String getFormatCurrentDate(){
		Date date = new Date(getCurrentTimeMillis());
		SimpleDateFormat df = new SimpleDateFormat("yyyyMMddHHmmss");
		return df.format(date);
	}
}
