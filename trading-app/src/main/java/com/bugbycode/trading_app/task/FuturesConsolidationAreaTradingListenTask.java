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
import com.bugbycode.module.FibInfo;
import com.bugbycode.module.FibLevel;
import com.bugbycode.module.Inerval;
import com.bugbycode.module.Klines;
import com.bugbycode.module.QUERY_SPLIT;
import com.bugbycode.service.KlinesService;
import com.util.EmailUtil;
import com.util.PriceUtil;
import com.util.StringUtil;

/**
 * 盘整区价格监控
 */
@Configuration
@EnableScheduling
public class FuturesConsolidationAreaTradingListenTask {
	
	private final Logger logger = LogManager.getLogger(FuturesConsolidationAreaTradingListenTask.class);
	
	@Autowired
	private KlinesService klinesService;
	
	/**
	 * 查询k线信息 每五分钟执行一次
	 * 
	 * @throws Exception
	 */
	@Scheduled(cron = "4 0/15 * * * ?")
	public void continuousKlines() throws Exception {
		logger.info("FuturesConsolidationAreaTradingListenTask start.");
		
		Date now = new Date();
		
		try {
			/*
			AppConfig.PAIRS.clear();
			AppConfig.PAIRS.add("BTCUSDT");
			AppConfig.PAIRS.add("XRPUSDT");
			*/
			for(String pair : AppConfig.PAIRS) {
				pair = pair.trim();
				if(StringUtil.isEmpty(pair)) {
					continue;
				}
				
				//近1年日线级别k线信息
				List<Klines> klinesList_365_x_day = klinesService.continuousKlines1Day(pair, now, 4 * 365, QUERY_SPLIT.NOT_ENDTIME);
				
				if(klinesList_365_x_day.isEmpty()) {
					logger.info("无法获取" + pair + "交易对最近4年日线级别K线信息");
					continue;
				}
				
				int offset = klinesList_365_x_day.size() - 1;
				
				Klines firstKlines = PriceUtil.getConsolidationAreaFirstKlines(klinesList_365_x_day);
				Klines lastKlines = klinesList_365_x_day.get(offset);
				
				long fibStartTime = firstKlines.getStarTime();
				long fibEndTime = lastKlines.getEndTime();
				
				List<Klines> fibKlinesList = klinesService.continuousKlines(pair, fibStartTime, fibEndTime, Inerval.INERVAL_4H.getDescption(), QUERY_SPLIT.NOT_ENDTIME);
				
				if(fibKlinesList.isEmpty()) {
					logger.info("无法获取" + pair + "交易对最近4小时级别K线信息");
					continue;
				}
				
				Klines fibLowKlines = PriceUtil.getMinPriceKLine(fibKlinesList,fibStartTime, fibEndTime);
				Klines fibHightKlines = PriceUtil.getMaxPriceKLine(fibKlinesList,fibStartTime, fibEndTime);
				
				FibInfo fibInfo = new FibInfo(fibLowKlines, fibHightKlines, fibLowKlines.getDecimalNum(),FibLevel.LEVEL_1);
				
				//logger.info(fibInfo);
				
				//查询最近k线信息
				List<Klines> list_5x_5m = klinesService.continuousKlines15M(pair, now, 5, QUERY_SPLIT.NOT_ENDTIME);
				
				klinesService.openLongConsolidationArea(fibInfo, list_5x_5m);
				klinesService.openShortConsolidationArea(fibInfo, list_5x_5m);
				
			}
		} catch (Exception e) {
			e.printStackTrace();
			EmailUtil.send("程序运行出现异常", e.getLocalizedMessage());
		} finally {
			logger.info("FuturesConsolidationAreaTradingListenTask finish.");
		}
		
	}
}
