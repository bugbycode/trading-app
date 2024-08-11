package com.bugbycode.service.impl;

import java.util.Date;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;

import com.bugbycode.module.Klines;
import com.bugbycode.service.KlinesService;
import com.util.CommandUtil;
import com.util.DateFormatUtil;

@Service("klinesService")
public class KlinesServiceImpl implements KlinesService {

	private final Logger logger = LogManager.getLogger(KlinesServiceImpl.class);
	
	@Override
	public List<Klines> continuousKlines_180d(String pair,String restBaseUrl) {
		
		String command = String.format("curl -G -d 'limit=180&pair=%s&contractType=PERPETUAL"
				+ "&interval=1d' %s/fapi/v1/continuousKlines", pair, restBaseUrl);
		
		logger.debug(command);
		
		String result = CommandUtil.run(command);
		
		return CommandUtil.format(pair, result);
	}

	@Override
	public List<Klines> continuousKlines_3h(String pair, String restBaseUrl) {
		String command = String.format("curl -G -d 'limit=4&pair=%s&contractType=PERPETUAL"
				+ "&interval=1h' %s/fapi/v1/continuousKlines", pair, restBaseUrl);
		
		logger.debug(command);
		
		String result = CommandUtil.run(command);

		return CommandUtil.format(pair, result);
	}

	@Override
	public List<Klines> continuousKlines_last_day_15m(String pair, String restBaseUrl) {
		
		long now = new Date().getTime();
		int hours = DateFormatUtil.getHours(now);
		
		long startTime = DateFormatUtil.getStartTime(hours).getTime();
		long endTime = DateFormatUtil.getEndTime(hours).getTime();
		
		String command = String.format("curl -G -d 'pair=%s&contractType=PERPETUAL&startTime=%s&endTime=%s"
				+ "&interval=15m' %s/fapi/v1/continuousKlines", pair,startTime,endTime, restBaseUrl);
		
		logger.debug(command);
		
		String result = CommandUtil.run(command);

		return CommandUtil.format(pair, result);
	}

	@Override
	public List<Klines> continuousKlines_last_4_x_15m(String pair, String restBaseUrl) {
		
		String command = String.format("curl -G -d 'limit=4&pair=%s&contractType=PERPETUAL"
				+ "&interval=15m' %s/fapi/v1/continuousKlines", pair, restBaseUrl);
		
		logger.debug(command);
		
		String result = CommandUtil.run(command);

		return CommandUtil.format(pair, result);
	}

	@Override
	public List<Klines> continuousKlines_last_2_x_1w(String pair, String restBaseUrl) {
		String command = String.format("curl -G -d 'limit=2&pair=%s&contractType=PERPETUAL"
				+ "&interval=1w' %s/fapi/v1/continuousKlines", pair, restBaseUrl);
		
		logger.debug(command);
		
		String result = CommandUtil.run(command);

		return CommandUtil.format(pair, result);
	}

	@Override
	public List<Klines> continuousKlines_lastweek_1h(String pair, String restBaseUrl) {
		List<Klines> weekKlineList = continuousKlines_last_2_x_1w(pair,restBaseUrl);
		Klines weekKline = weekKlineList.get(0);
		long startTime = weekKline.getStarTime();
		long endTime = weekKline.getEndTime();
		
		String command = String.format("curl -G -d 'pair=%s&contractType=PERPETUAL&startTime=%s&endTime=%s"
				+ "&interval=1h' %s/fapi/v1/continuousKlines", pair,startTime,endTime, restBaseUrl);
		
		logger.debug(command);
		
		String result = CommandUtil.run(command);

		return CommandUtil.format(pair, result);
	}

	@Override
	public List<Klines> continuousKlines_last_7_x_15m(String pair, String restBaseUrl) {
		String command = String.format("curl -G -d 'limit=8&pair=%s&contractType=PERPETUAL"
				+ "&interval=15m' %s/fapi/v1/continuousKlines", pair, restBaseUrl);
		
		logger.debug(command);
		
		String result = CommandUtil.run(command);

		return CommandUtil.format(pair, result);
	}

	@Override
	public List<Klines> continuousKlines_last_10_x_15m(String pair, String restBaseUrl) {
		String command = String.format("curl -G -d 'limit=11&pair=%s&contractType=PERPETUAL"
				+ "&interval=15m' %s/fapi/v1/continuousKlines", pair, restBaseUrl);
		
		logger.debug(command);
		
		String result = CommandUtil.run(command);

		return CommandUtil.format(pair, result);
	}

	@Override
	public List<Klines> continuousKlines_last_2_x_1d(String pair, String restBaseUrl) {
		String command = String.format("curl -G -d 'limit=2&pair=%s&contractType=PERPETUAL"
				+ "&interval=1d' %s/fapi/v1/continuousKlines", pair, restBaseUrl);
		
		logger.debug(command);
		
		String result = CommandUtil.run(command);

		return CommandUtil.format(pair, result);
	}

	@Override
	public List<Klines> continuousKlines_last_15m(String pair, String restBaseUrl, long startTime, long endTime) {
		String command = String.format("curl -G -d 'pair=%s&contractType=PERPETUAL&startTime=%s&endTime=%s"
				+ "&interval=15m' %s/fapi/v1/continuousKlines", pair, startTime,endTime, restBaseUrl);
		
		logger.debug(command);
		
		String result = CommandUtil.run(command);

		return CommandUtil.format(pair, result);
	}

	@Override
	public List<Klines> continuousKlines(String pair, String restBaseUrl, long startTime, long endTime,
			String interval) {
		String command = String.format("curl -G -d 'pair=%s&contractType=PERPETUAL&startTime=%s&endTime=%s"
				+ "&interval=%s' %s/fapi/v1/continuousKlines", pair, startTime, endTime, interval, restBaseUrl);
		
		logger.debug(command);
		
		String result = CommandUtil.run(command);

		return CommandUtil.format(pair, result);
	}

}
