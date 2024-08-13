package com.bugbycode.service.impl;

import java.util.Date;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;

import com.bugbycode.config.AppConfig;
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
	public List<Klines> continuousKlinesDay(String pair, Date now, int limit) {
		
		int hours = DateFormatUtil.getHours(now.getTime());
		Date lastDayStartTimeDate = DateFormatUtil.getStartTime(hours);//前一天K线起始时间 yyyy-MM-dd 08:00:00
		Date lastDayEndTimeDate = DateFormatUtil.getEndTime(hours);//前一天K线结束时间 yyyy-MM-dd 07:59:59
		
		Date firstDayStartTime = DateFormatUtil.getStartTimeBySetDay(lastDayStartTimeDate, -limit);//多少天以前起始时间
		
		return continuousKlines(pair, firstDayStartTime.getTime(), 
				lastDayEndTimeDate.getTime(), AppConfig.INERVAL_1D);
	}

	@Override
	public List<Klines> continuousKlines15M(String pair, Date now, int limit) {
		return null;
	}

}
