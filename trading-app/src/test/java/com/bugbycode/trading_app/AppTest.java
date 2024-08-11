package com.bugbycode.trading_app;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.util.DateFormatUtil;

/**
 * Unit test for simple App.
 */
public class AppTest 
{
   public static void main(String[] args) throws Exception  {
	  Date now = new Date();
	  int hours = DateFormatUtil.getHours(now.getTime());
	  Date lastDayStartTimeDate = DateFormatUtil.getStartTime(hours);//前一天K线起始时间 yyyy-MM-dd 08:00:00
	  Date lastDayEndTimeDate = DateFormatUtil.getEndTime(hours);//前一天K线结束时间 yyyy-MM-dd 07:59:59
	  
	  Date oneYearAgo = DateFormatUtil.getStartTimeBySetDay(lastDayStartTimeDate, -365);
	  
	  System.out.println(DateFormatUtil.format(lastDayStartTimeDate));
	  System.out.println(DateFormatUtil.format(lastDayEndTimeDate));
	  
	  System.out.println(DateFormatUtil.format(oneYearAgo));
	  
	  List<Integer> list = new ArrayList<Integer>();
	  list.add(1);
	  list.add(2);
	  
	  System.out.println(list);
	  
	  
   }
}
