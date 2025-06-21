package com.bugbycode.trading_app.task.openInterest.work;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.bugbycode.module.Inerval;
import com.bugbycode.repository.klines.KlinesRepository;
import com.util.DateFormatUtil;

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
			logger.info("开始删除{}{}之前{}级别所有k线信息", pair, DateFormatUtil.format(time), interval.getMemo());
			klinesRepository.removeBeforeKlinesByTime(pair, time, interval);
			logger.info("{}{}之前{}级别所有k线信息已删除完成", pair, DateFormatUtil.format(time), interval.getMemo());
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
		
	}

}
