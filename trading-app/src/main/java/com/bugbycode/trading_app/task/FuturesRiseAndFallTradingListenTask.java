package com.bugbycode.trading_app.task;

import java.util.Date;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.util.CollectionUtils;

import com.bugbycode.config.AppConfig;
import com.bugbycode.module.Klines;
import com.bugbycode.module.QUERY_SPLIT;
import com.bugbycode.service.KlinesService;
import com.util.DateFormatUtil;
import com.util.EmailUtil;
import com.util.PriceUtil;
import com.util.StringUtil;

/**
 * 涨跌监控
 */
@Configuration
@EnableScheduling
public class FuturesRiseAndFallTradingListenTask {

	private final Logger logger = LogManager.getLogger(FuturesRiseAndFallTradingListenTask.class);
	
	@Autowired
	private KlinesService klinesService;
	
	/**
	 * 查询k线信息 每15分钟执行一次
	 * 
	 * @throws Exception
	 */
	@Scheduled(cron = "5 0/15 * * * ?")
	public void continuousKlines() throws Exception {
		logger.info("FuturesRiseAndFallTradingListenTask start.");
		
		Date now = new Date();
		
		try {
			
			for(String pair : AppConfig.PAIRS) {
				pair = pair.trim();
				if(StringUtil.isEmpty(pair)) {
					continue;
				}
				
			 	List<Klines> klinesList_15m = klinesService.continuousKlines15M(pair, now, 20, QUERY_SPLIT.NOT_ENDTIME);
			 	
			 	if(CollectionUtils.isEmpty(klinesList_15m)) {
			 		logger.info("无法获取" + pair + "15分钟级别K线信息");
			 		continue;
			 	}
			 	
			 	int size = klinesList_15m.size();
				int lastIndex = size - 1;
				Klines currentKlines = klinesList_15m.get(lastIndex);
				
				String percentageStr = PriceUtil.formatDoubleDecimal(PriceUtil.getPriceFluctuationPercentage(klinesList_15m, lastIndex), 2);
				
				double pricePercentage = Double.valueOf(percentageStr);
				
				String text = "";//邮件内容
				String subject = "";//邮件主题
				String dateStr = DateFormatUtil.format(new Date());
				if(currentKlines.isFall()) {//下跌
					
					if(pair.equals("BTCUSDT") || pair.equals("ETHUSDT")) {
						if(pricePercentage >= 5) {
							subject = pair + "永续合约价格大暴跌";
						} else if(pricePercentage >= 3) {
							subject = pair + "永续合约价格暴跌";
						}else if(pricePercentage >= 1.5) {
							subject = pair + "永续合约价格大跌";
						}
					} else {
						if(pricePercentage >= 10) {
							subject = pair + "永续合约价格大暴跌";
						} else if(pricePercentage >= 5) {
							subject = pair + "永续合约价格暴跌";
						}else if(pricePercentage >= 3) {
							subject = pair + "永续合约价格大跌";
						}
					}
					
				} else if(currentKlines.isRise()) {
					if(pair.equals("BTCUSDT") || pair.equals("ETHUSDT")) {
						if(pricePercentage >= 5) {
							subject = pair + "永续合约价格大暴涨";
						} else if(pricePercentage >= 3) {
							subject = pair + "永续合约价格暴涨";
						}else if(pricePercentage >= 1.5) {
							subject = pair + "永续合约价格大涨";
						}
					} else {
						if(pricePercentage >= 10) {
							subject = pair + "永续合约价格大暴涨";
						} else if(pricePercentage >= 5) {
							subject = pair + "永续合约价格暴涨";
						}else if(pricePercentage >= 3) {
							subject = pair + "永续合约价格大涨";
						}
					}
				}
				
				if(StringUtil.isNotEmpty(subject)) {
					
					subject += percentageStr + "% " + dateStr;
					
					text = subject;
					
					klinesService.sendEmail(subject, text, null);
				}
				
			}
			
		} catch (Exception e) {
			e.printStackTrace();
			EmailUtil.send("程序运行出现异常", e.getLocalizedMessage());
		} finally {
			logger.info("FuturesRiseAndFallTradingListenTask finish.");
		}
	}
}
