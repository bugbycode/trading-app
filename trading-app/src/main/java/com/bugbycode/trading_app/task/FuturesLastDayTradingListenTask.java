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
import com.bugbycode.module.Result;
import com.bugbycode.module.ResultCode;
import com.bugbycode.service.KlinesService;
import com.util.DateFormatUtil;
import com.util.EmailUtil;
import com.util.PriceUtil;
import com.util.StringUtil;

/**
 * 昨日最高价和最低价监控
 */
@Configuration
@EnableScheduling
public class FuturesLastDayTradingListenTask {

	private final Logger logger = LogManager.getLogger(FuturesLastDayTradingListenTask.class);
	
	@Autowired
	private KlinesService klinesService;
	
	/**
	 * 查询k线信息 每十五分钟执行一次
	 * 
	 * @throws Exception
	 */
	@Scheduled(cron = "10 0/5 * * * ?")
	public void continuousKlines() throws Exception {
		
		logger.info("FuturesLastDayTradingListenTask start.");
		
		Date now = new Date();
		
		try {
			
			for(String pair : AppConfig.PAIRS) {
				pair = pair.trim();
				if(StringUtil.isEmpty(pair)) {
					continue;
				}
				
				//一部分15分钟级别k线信息 最近两天
				//List<Klines> klinesList_hit = klinesService.continuousKlines15M(pair, now, 192, QUERY_SPLIT.NOT_ENDTIME);
				List<Klines> klinesList_hit = klinesService.continuousKlines5M(pair, now, 577, QUERY_SPLIT.NOT_ENDTIME);
				if(klinesList_hit.isEmpty()) {
					logger.info("无法获取" + pair + "交易对最近5分钟级别K线信息");
					continue;
				}
				
				List<Klines> lastDayKlinesList = PriceUtil.getLastDayKlines(klinesList_hit);
				
				if(lastDayKlinesList.isEmpty()) {
					logger.info("无法获取昨日" + pair + "交易对5分钟级别K线信息");
					continue;
				}
				
				Klines newStartKlines = lastDayKlinesList.get(0);
				Klines newEndKlines = PriceUtil.getLastKlines(lastDayKlinesList);
				
				Klines highPriceKlines = PriceUtil.getMaxPriceKLine(lastDayKlinesList);
				Klines lowPriceKlines = PriceUtil.getMinPriceKLine(lastDayKlinesList);
				
				double highPrice = highPriceKlines.getHighPrice();
				double lowPrice = lowPriceKlines.getLowPrice();
				
				Klines lastDayKlines = new Klines(pair, newStartKlines.getStarTime(), 
						newStartKlines.getOpenPrice(), highPrice, lowPrice, newEndKlines.getClosePrice(), 
						newEndKlines.getEndTime(), newEndKlines.getDecimalNum());
				
				String subject = "";
				String text = lastDayKlines.toString();
				
				String dateStr = DateFormatUtil.format(new Date());
				
				if(PriceUtil.isLong(lowPrice, klinesList_hit)) {
					
					subject = String.format("%s永续合约跌破昨日最低价%s并收回 %s", pair,lowPrice,dateStr);
					
				} else if(PriceUtil.isLong(highPrice, klinesList_hit)) {
					
					subject = String.format("%s永续合约突破昨日最高价%s %s", pair,highPrice,dateStr);
					
				} else if(PriceUtil.isShort(highPrice, klinesList_hit)) {
					
					subject = String.format("%s永续合约突破昨日最高价%s并收回 %s", pair,highPrice,dateStr);
					
				} else if(PriceUtil.isShort(lowPrice, klinesList_hit)) {
					
					subject = String.format("%s永续合约跌破昨日最低价%s %s", pair,lowPrice,dateStr);
					
				}
				
				if(StringUtil.isNotEmpty(subject)) {
					
					text += "\n\n" + dateStr;
					
					logger.info("邮件主题：" + subject);
					logger.info("邮件内容：" + text);
					
					Result<ResultCode, Exception> result = EmailUtil.send(subject, text);
					
					switch (result.getResult()) {
					case ERROR:
						
						Exception ex = result.getErr();
						
						logger.info("邮件发送失败！失败原因：" + ex.getLocalizedMessage());
						
						break;
						
					default:
						
						logger.info("邮件发送成功！");
						
						break;
					}
				}
			}
			
		} catch (Exception e) {
			e.printStackTrace();
			EmailUtil.send("程序运行出现异常", e.getLocalizedMessage());
		} finally {
			logger.info("FuturesLastDayTradingListenTask finish.");
		}
	}
}
