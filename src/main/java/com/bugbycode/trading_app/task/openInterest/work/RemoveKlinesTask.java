package com.bugbycode.trading_app.task.openInterest.work;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.bugbycode.module.Inerval;
import com.bugbycode.repository.klines.KlinesRepository;

/**
 * 删除k线信息任务
 */
public class RemoveKlinesTask implements Runnable {

	private final Logger logger = LogManager.getLogger(RemoveKlinesTask.class);
	
	private String pair;
	
	private long time;
	
	private Inerval interval;
	
	private KlinesRepository klinesRepository;
	
	/**
	 * 删除k线信息任务
	 * @param pair 交易对
	 * @param time 时间点
	 * @param interval 时间级别
	 * @param klinesRepository
	 */
	public RemoveKlinesTask(String pair, long time, Inerval interval,
			KlinesRepository klinesRepository) {
		this.pair = pair;
		this.time = time;
		this.interval = interval;
		this.klinesRepository = klinesRepository;
	}

	@Override
	public void run() {
		try {
			klinesRepository.removeBeforeKlinesByTime(pair, time, interval);
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
		
	}

}
