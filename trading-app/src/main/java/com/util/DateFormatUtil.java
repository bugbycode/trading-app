package com.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class DateFormatUtil {

	private static final ThreadLocal<SimpleDateFormat> sdf = ThreadLocal.withInitial(() -> new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"));
	
	private static final ThreadLocal<SimpleDateFormat> sdf_yyyy_mm_dd_08_00_00 = ThreadLocal.withInitial(() -> new SimpleDateFormat("yyyy-MM-dd 08:00:00"));
	
	private static final ThreadLocal<SimpleDateFormat> sdf_yyyy_mm_dd_07_59_59 = ThreadLocal.withInitial(() -> new SimpleDateFormat("yyyy-MM-dd 07:59:59"));
	
	private static final ThreadLocal<SimpleDateFormat> sdf_yyyy_mm_dd_HH_mm_00 = ThreadLocal.withInitial(() -> new SimpleDateFormat("yyyy-MM-dd HH:mm:00"));
	
	private static final ThreadLocal<SimpleDateFormat> sdf_yyyy_mm_dd_HH_00_00 = ThreadLocal.withInitial(() -> new SimpleDateFormat("yyyy-MM-dd HH:00:00"));
	
	private static final ThreadLocal<SimpleDateFormat> sdf_yyyy_mm_dd = ThreadLocal.withInitial(() -> new SimpleDateFormat("yyyy-MM-dd"));
	
	public static String format(long time) {
		return format(new Date(time));
	}
	
	public static int getHours(long time) {
		Date date = new Date(time);
		Calendar c = Calendar.getInstance();
		c.setTime(date);
		return c.get(Calendar.HOUR_OF_DAY);
	}
	
	public static int getMinute(long time) {
		Date date = new Date(time);
		Calendar c = Calendar.getInstance();
		c.setTime(date);
		return c.get(Calendar.MINUTE);
	}
	
	public static int getSecond(long time) {
		Date date = new Date(time);
		Calendar c = Calendar.getInstance();
		c.setTime(date);
		return c.get(Calendar.SECOND);
	}
	
	public static String format(Date date) {
		return sdf.get().format(date);
	}
	
	public static String format_yyyy_mm_dd(Date date) {
		return sdf_yyyy_mm_dd.get().format(date);
	}
	
	public static String format_yyyy_mm_dd_08_00_00(Date date) {
		return sdf_yyyy_mm_dd_08_00_00.get().format(date);
	}
	
	public static String format_yyyy_mm_dd_07_59_59(Date date) {
		return sdf_yyyy_mm_dd_07_59_59.get().format(date);
	}
	
	public static String format_yyyy_mm_dd_HH_mm_00(Date date) {
		return sdf_yyyy_mm_dd_HH_mm_00.get().format(date);
	}
	
	public static String format_yyyy_mm_dd_HH_00_00(Date date) {
		return sdf_yyyy_mm_dd_HH_00_00.get().format(date);
	}
	
	public static Date parse(String date) {
		try {
			return sdf.get().parse(date);
		} catch (ParseException e) {
			throw new RuntimeException(e);
		}
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
	 * 将时间更新到多少毫秒以前或之后
	 * @param now 当前时间
	 * @param minute 多少秒
	 * @return
	 */
	public static Date getStartTimeBySetMillisecond(Date now,int second) {
		Calendar c = Calendar.getInstance();
		c.setTime(now);
		c.add(Calendar.MILLISECOND, second);
		return c.getTime();
	}
	
	/**
	 * 将时间更新到多少小时以前或之后
	 * @param now 当前时间
	 * @param hour 多少小时
	 * @return
	 */
	public static Date getStartTimeBySetHour(Date now,int hour) {
		Calendar c = Calendar.getInstance();
		c.setTime(now);
		c.add(Calendar.HOUR, hour);
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
			return sdf.get().parse(format_yyyy_mm_dd_08_00_00(c.getTime()));
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
			return sdf.get().parse(format_yyyy_mm_dd_07_59_59(c.getTime()));
		} catch (ParseException e) {
			throw new RuntimeException(e.getLocalizedMessage());
		}
	}
	
	/**
	 * 获取今天的开始时间
	 * @param date 当前时间
	 * @return
	 */
	public static Date getTodayStartTime(Date date) {
		int hours = getHours(date.getTime());
		Date lastDayEndTimeDate = DateFormatUtil.getEndTime(hours);
		try {
			return parse(format_yyyy_mm_dd_08_00_00(lastDayEndTimeDate));
		} catch (Exception e) {
			throw new RuntimeException(e.getLocalizedMessage());
		}
	}
	
	/**
	 * 校验date是否为今日开盘时间
	 * @param date
	 * @return
	 */
	public static boolean verifyLastDayStartTime(Date date) {
		Date now = new Date();
		Date todayStartDate = getTodayStartTime(now);
		return date.getTime() == todayStartDate.getTime();
	}
}
