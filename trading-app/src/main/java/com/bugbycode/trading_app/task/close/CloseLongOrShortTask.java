package com.bugbycode.trading_app.task.close;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.bugbycode.module.Klines;
import com.bugbycode.service.klines.KlinesService;

/**
 * 平仓任务
 */
public class CloseLongOrShortTask implements Runnable {

	private final Logger logger = LogManager.getLogger(CloseLongOrShortTask.class);
	
	private final Klines klines;
	
	private final KlinesService klinesService;
	
	public CloseLongOrShortTask(Klines klines, KlinesService klinesService) {
		this.klines = klines;
		this.klinesService = klinesService;
	}

	@Override
	public void run() {
		//logger.info("CloseLongOrShortTask.run() start.");
		try {
			klinesService.closeOrder(klines);
		}catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
		//logger.info("CloseLongOrShortTask.run() end.");
	}

}
