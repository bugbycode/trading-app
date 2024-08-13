package com.bugbycode.service.impl;

import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;

import com.bugbycode.module.Klines;
import com.bugbycode.service.KlinesService;
import com.util.CommandUtil;

@Service("klinesService")
public class KlinesServiceImpl implements KlinesService {

	private final Logger logger = LogManager.getLogger(KlinesServiceImpl.class);

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
