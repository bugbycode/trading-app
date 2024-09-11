package com.bugbycode.trading_app.task;

import java.util.Date;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

import com.bugbycode.config.AppConfig;
import com.bugbycode.module.Klines;
import com.bugbycode.module.QUERY_SPLIT;
import com.bugbycode.service.KlinesService;
import com.util.EmailUtil;
import com.util.StringUtil;

/**
 * 标志性高低点监控
 */
//@Configuration
//@EnableScheduling
public class FuturesLastDayTradingListenTask {

	private final Logger logger = LogManager.getLogger(FuturesLastDayTradingListenTask.class);
	
	@Autowired
	private KlinesService klinesService;
	
	/**
	 * 查询k线信息 每15分钟执行一次
	 * 
	 * @throws Exception
	 */
	//@Scheduled(cron = "5 0/15 * * * ?")
	public void continuousKlines() throws Exception {
		
		logger.info("FuturesLastDayTradingListenTask start.");
		
		Date now = new Date();
		
		try {
			
			for(String pair : AppConfig.PAIRS) {
				pair = pair.trim();
				if(StringUtil.isEmpty(pair)) {
					continue;
				}
				
				//近1年日线级别k线信息
				List<Klines> klinesList_365_x_day = klinesService.continuousKlines1Day(pair, now, 4 * 365, QUERY_SPLIT.NOT_ENDTIME);
				
				if(klinesList_365_x_day.isEmpty()) {
					logger.info("无法获取" + pair + "交易对最近4年日线级别K线信息");
					return;
				}
				
				List<Klines> klinesList_hit = klinesService.continuousKlines15M(pair, now, 1, QUERY_SPLIT.NOT_ENDTIME);
				if(klinesList_hit.isEmpty()) {
					logger.info("无法获取" + pair + "交易对最近15分钟级别K线信息");
					continue;
				}
				
				klinesService.futuresHighOrLowMonitor(klinesList_365_x_day, klinesList_hit);
			}
			
		} catch (Exception e) {
			e.printStackTrace();
			EmailUtil.send("程序运行出现异常", e.getLocalizedMessage());
		} finally {
			logger.info("FuturesLastDayTradingListenTask finish.");
		}
	}
}
