package com.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class DateFormatUtil {

	private static SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	
	private static SimpleDateFormat sdf_yyyy_mm_dd_08_00_00 = new SimpleDateFormat("yyyy-MM-dd 08:00:00");
	
	private static SimpleDateFormat sdf_yyyy_mm_dd_07_59_59 = new SimpleDateFormat("yyyy-MM-dd 07:59:59");
	
	private static SimpleDateFormat sdf_yyyy_mm_dd_HH_mm_00 = new SimpleDateFormat("yyyy-MM-dd HH:mm:00");
	
	public static String format(long time) {
		return format(new Date(time));
	}
	
	public static int getHours(long time) {
		Date date = new Date(time);
		Calendar c = Calendar.getInstance();
		c.setTime(date);
		return c.get(Calendar.HOUR_OF_DAY);
	}
	
	public static String format(Date date) {
		return sdf.format(date);
	}
	
	public static String format_yyyy_mm_dd_08_00_00(Date date) {
		return sdf_yyyy_mm_dd_08_00_00.format(date);
	}
	
	public static String format_yyyy_mm_dd_07_59_59(Date date) {
		return sdf_yyyy_mm_dd_07_59_59.format(date);
	}
	
	public static String format_yyyy_mm_dd_HH_mm_00(Date date) {
		return sdf_yyyy_mm_dd_HH_mm_00.format(date);
	}
	
	public static Date parse(String date) throws ParseException {
		return sdf.parse(date);
	}
	
	/**
	 * 将时间更新到多少天以前或之后
	 * @param now 当前时间
	 * @param day 多少分天
	 * @return
	 */
	public static Date getStartTimeBySetDay(Date now,int day) {
		Calendar c = Calendar.getInstance();
		c.setTime(now);
		c.add(Calendar.DAY_OF_YEAR, day);
		return c.getTime();
	}
	
	/**
	 * 将时间更新到多少分钟以前或之后
	 * @param now 当前时间
	 * @param minute 多少分钟
	 * @return
	 */
	public static Date getStartTimeBySetMinute(Date now,int minute) {
		Calendar c = Calendar.getInstance();
		c.setTime(now);
		c.add(Calendar.MINUTE, minute);
		return c.getTime();
	}
	
	/**
	 * 将时间更新到多少秒以前或之后
	 * @param now 当前时间
	 * @param minute 多少秒
	 * @return
	 */
	public static Date getStartTimeBySetSecond(Date now,int second) {
		Calendar c = Calendar.getInstance();
		c.setTime(now);
		c.add(Calendar.SECOND, second);
		return c.getTime();
	}
	
	/**
	 * 前一天K线起始时间 东八区早上八点整
	 * @param hours
	 * @return
	 * @throws RuntimeException
	 */
	public static Date getStartTime(int hours) throws RuntimeException {
		Calendar c = Calendar.getInstance();
		if(hours >= 8 && hours <= 23) {
			c.add(Calendar.HOUR_OF_DAY, -24 - (hours - 8));
		} else if(hours >= 0 && hours <= 7 ) {
			c.add(Calendar.HOUR_OF_DAY, - (hours + 1) - 15 - 24);
		}
		try {
			return sdf.parse(format_yyyy_mm_dd_08_00_00(c.getTime()));
		} catch (ParseException e) {
			throw new RuntimeException(e.getLocalizedMessage());
		}
	}
	
	/**
	 * 前一天K线结束时间 东八区早上七点五十九分五十九秒
	 * @param hours
	 * @return
	 * @throws RuntimeException
	 */
	public static Date getEndTime(int hours) throws RuntimeException {
		Calendar c = Calendar.getInstance();
		if(hours >= 8 && hours <= 23) {
			c.add(Calendar.HOUR_OF_DAY, - (hours - 8));
		} else if(hours >= 0 && hours <= 7 ) {
			c.add(Calendar.HOUR_OF_DAY, - (hours + 1) - 16);
		}
		try {
			return sdf.parse(format_yyyy_mm_dd_07_59_59(c.getTime()));
		} catch (ParseException e) {
			throw new RuntimeException(e.getLocalizedMessage());
		}
	}
}
