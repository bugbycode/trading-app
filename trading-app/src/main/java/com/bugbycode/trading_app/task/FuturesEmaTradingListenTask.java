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
import com.bugbycode.module.EMAType;
import com.bugbycode.module.Klines;
import com.bugbycode.module.QUERY_SPLIT;
import com.bugbycode.service.KlinesService;
import com.util.DateFormatUtil;
import com.util.EmailUtil;
import com.util.KlinesUtil;
import com.util.PriceUtil;
import com.util.StringUtil;

/**
 * EMA指标监控
 */
@Configuration
@EnableScheduling
public class FuturesEmaTradingListenTask {

	private final Logger logger = LogManager.getLogger(FuturesEmaTradingListenTask.class);
	
	@Autowired
	private KlinesService klinesService;
	
	/**
	 * 查询k线信息 每15分钟执行一次
	 * 
	 * @throws Exception
	 */
	@Scheduled(cron = "5 0/15 * * * ?")
	public void continuousKlines() throws Exception {
		logger.info("FuturesEmaTradingListenTask start.");
		
		Date now = new Date();
		
		try {
			
			for(String pair : AppConfig.PAIRS) {
				pair = pair.trim();
				if(StringUtil.isEmpty(pair)) {
					continue;
				}
				
			 	List<Klines> klinesList_4_x_365_15m = klinesService.continuousKlines15M(pair, now, 4 * 365, QUERY_SPLIT.NOT_ENDTIME);
			 	
			 	if(CollectionUtils.isEmpty(klinesList_4_x_365_15m)) {
			 		logger.info("无法获取" + pair + "15分钟级别K线信息");
			 		continue;
			 	}
			 	
			 	PriceUtil.calculateEMAArray(klinesList_4_x_365_15m, EMAType.EMA7);
			 	PriceUtil.calculateEMAArray(klinesList_4_x_365_15m, EMAType.EMA25);
			 	PriceUtil.calculateEMAArray(klinesList_4_x_365_15m, EMAType.EMA99);
			 	
			 	KlinesUtil ku = new KlinesUtil(klinesList_4_x_365_15m);
			 	Klines lastKlines = ku.removeLast();
			 	Klines parentKlines = ku.removeLast();
			 	
			 	int decimalNum = lastKlines.getDecimalNum();
			 	
			 	String subject = "";
			 	String text = "";
			 	String dateStr = DateFormatUtil.format(new Date());
			 	
			 	//开始上涨
			 	if(parentKlines.getEma7() < parentKlines.getEma25() && 
			 			lastKlines.getEma7() >= lastKlines.getEma25()) {
			 		subject = String.format("%s永续合约开始上涨 %s", pair, dateStr);
			 	}
			 	//开始下跌
			 	else if(parentKlines.getEma7() > parentKlines.getEma25() && 
			 			lastKlines.getEma7() <= lastKlines.getEma25()) {
			 		subject = String.format("%s永续合约开始下跌 %s", pair, dateStr);
			 	}
			 	
		 		text = lastKlines.toString() + "\n\n";
		 		text += String.format("EMA7: %s, EMA25: %s, EMA99: %s ", PriceUtil.formatDoubleDecimal(lastKlines.getEma7(), decimalNum),
		 				PriceUtil.formatDoubleDecimal(lastKlines.getEma25(), decimalNum),
		 				PriceUtil.formatDoubleDecimal(lastKlines.getEma99(), decimalNum));
		 		
		 		//logger.info(text);
		 		
		 		klinesService.sendEmail(subject, text, null);
			}
			
		} catch (Exception e) {
			e.printStackTrace();
			EmailUtil.send("程序运行出现异常", e.getLocalizedMessage());
		} finally {
			logger.info("FuturesEmaTradingListenTask finish.");
		}
	}
}
