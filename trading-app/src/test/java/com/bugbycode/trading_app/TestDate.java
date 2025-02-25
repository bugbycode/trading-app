package com.bugbycode.trading_app;

import java.text.ParseException;
import java.util.Date;

import com.util.DateFormatUtil;

public class TestDate {

	public static void main(String[] args) throws ParseException {
		Date now = new Date();
		Date today = DateFormatUtil.parse(DateFormatUtil.format_yyyy_mm_dd_08_00_00(now));
		System.out.println(DateFormatUtil.format(today));
		System.out.println(DateFormatUtil.format(DateFormatUtil.getTodayStartTime(today)));
	}

}
