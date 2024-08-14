package com.bugbycode.service.impl;

import java.text.ParseException;
import java.util.Date;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;

import com.bugbycode.config.AppConfig;
import com.bugbycode.module.Inerval;
import com.bugbycode.module.Klines;
import com.bugbycode.service.KlinesService;
import com.util.CommandUtil;
import com.util.DateFormatUtil;

@Service("klinesService")
public class KlinesServiceImpl implements KlinesService {

	private final Logger logger = LogManager.getLogger(KlinesServiceImpl.class);

	@Override
	public List<Klines> continuousKlines(String pair, long startTime, long endTime,
			String interval) {
		String command = String.format("curl -G -d 'pair=%s&contractType=PERPETUAL&startTime=%s&endTime=%s"
				+ "&interval=%s' %s/fapi/v1/continuousKlines", pair, startTime, endTime, interval, AppConfig.REST_BASE_URL);
		
		logger.debug(command);
		
		String result = CommandUtil.run(command);

		return CommandUtil.format(pair, result);
	}

	@Override
	public List<Klines> continuousKlines1Day(String pair, Date now, int limit) {
		
		int hours = DateFormatUtil.getHours(now.getTime());
		Date lastDayStartTimeDate = DateFormatUtil.getStartTime(hours);//前一天K线起始时间 yyyy-MM-dd 08:00:00
		Date lastDayEndTimeDate = DateFormatUtil.getEndTime(hours);//前一天K线结束时间 yyyy-MM-dd 07:59:59
		
		Date firstDayStartTime = DateFormatUtil.getStartTimeBySetDay(lastDayStartTimeDate, -limit);//多少天以前起始时间
		
		return continuousKlines(pair, firstDayStartTime.getTime(), 
				lastDayEndTimeDate.getTime(), Inerval.INERVAL_1D.getDescption());
	}

	@Override
	public List<Klines> continuousKlines5M(String pair, Date now, int limit) {
		List<Klines> result = null;
		try {
			
			Date endTime_5m = DateFormatUtil.parse(DateFormatUtil.format_yyyy_mm_dd_HH_mm_00(now));
			Date startTime_5m = DateFormatUtil.getStartTimeBySetMinute(endTime_5m, -Inerval.INERVAL_5M.getNumber() * limit);//limit根k线
			endTime_5m = DateFormatUtil.getStartTimeBySetSecond(endTime_5m, -1);//收盘时间
			
			result = continuousKlines(pair, startTime_5m.getTime(),
					endTime_5m.getTime(), Inerval.INERVAL_5M.getDescption());
			
		} catch (ParseException e) {
			e.printStackTrace();
		}
		
		return result;
	}

}
