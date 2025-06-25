package com.bugbycode.trading_app.task.operations;

import java.util.Date;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

import com.bugbycode.config.AppConfig;
import com.bugbycode.module.Inerval;
import com.bugbycode.repository.klines.KlinesRepository;
import com.bugbycode.service.exchange.BinanceExchangeService;
import com.bugbycode.trading_app.pool.WorkTaskPool;
import com.bugbycode.trading_app.task.openInterest.work.RemoveKlinesTask;
import com.util.DateFormatUtil;

/**
 * 自动维护数据定时任务
 */
@Configuration
@EnableScheduling
public class OperationsTask {

	private final Logger logger = LogManager.getLogger(OperationsTask.class);
	
	@Autowired
	private BinanceExchangeService binanceExchangeService;

	@Autowired
	private WorkTaskPool removeKlinesTaskPool;
	
	@Autowired
	private KlinesRepository klinesRepository;
	
	/**
	 * 每10分钟执行一次
	 * 
	 * @throws Exception
	 */
	@Scheduled(cron = "10 7/10 * * * ?")
	public void executeTask() {
		
		if(AppConfig.DEBUG) {
			return;
		}
		
		Date now = new Date();
		
		int total = 1 * 10000;// 保留1万条数据
		
		try {

			//计算时间点
			long before_15m_time = DateFormatUtil.getStartTimeBySetMinute(now, - Inerval.INERVAL_15M.getNumber() * total).getTime();
			long before_1h_time = DateFormatUtil.getStartTimeBySetHour(now, - Inerval.INERVAL_1H.getNumber() * total).getTime();
			long before_4h_time = DateFormatUtil.getStartTimeBySetHour(now, - Inerval.INERVAL_4H.getNumber() * 4 * total).getTime();
			Set<String> pairSet = binanceExchangeService.exchangeInfo();
			
			for(String pair : pairSet) {
				this.removeKlinesTaskPool.add(new RemoveKlinesTask(pair, before_15m_time, Inerval.INERVAL_15M, klinesRepository));
				this.removeKlinesTaskPool.add(new RemoveKlinesTask(pair, before_1h_time, Inerval.INERVAL_1H, klinesRepository));
				this.removeKlinesTaskPool.add(new RemoveKlinesTask(pair, before_4h_time, Inerval.INERVAL_4H, klinesRepository));
			}
			
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
		
	}
	
}
