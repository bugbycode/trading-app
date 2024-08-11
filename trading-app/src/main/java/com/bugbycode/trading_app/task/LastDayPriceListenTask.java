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
import com.bugbycode.module.Klines;
import com.bugbycode.service.KlinesService;
import com.util.DateFormatUtil;
import com.util.EmailUtil;
import com.util.PriceUtil;
import com.util.StringUtil;

/**
 * 昨日价格点位监控
 */
@Configuration
@EnableScheduling
public class LastDayPriceListenTask {

	private final Logger logger = LogManager.getLogger(LastDayPriceListenTask.class);
	
	@Autowired
	private KlinesService klinesService;
	
	/**
	 * 查询k线信息
	 * 
	 * @throws Exception
	 */
	@Scheduled(cron = "4 0/5 * * * ?")
	public void continuousKlines() throws Exception {
		logger.info("LconicHighAndLowPricesListenTask start.");
		Date now = new Date();
		String restBaseUrl = AppConfig.REST_BASE_URL;
		String pairs = AppConfig.PAIRS;
		String emailUserName = AppConfig.EMAIL_USDRNAME;
		String emailPassword = AppConfig.EMAIL_PASSWORD;
		String smtpHost = AppConfig.SMTP_HOST;
		int smtpPort = AppConfig.SMTP_PORT;
		String recipient = AppConfig.RECIPIENT;
		
		try {
			String[] pairArr = { pairs };
			
			if(pairs.contains(",")) {
				pairArr = pairs.split(",");
			}
			
			for(String pair : pairArr) {
				pair = pair.trim();
				if(StringUtil.isEmpty(pair)) {
					continue;
				}
				
				String text = "";//邮件内容
				String subject = "";//邮件主题
				
				int hours = DateFormatUtil.getHours(now.getTime());
				Date lastDayStartTimeDate = DateFormatUtil.getStartTime(hours);//前一天K线起始时间 yyyy-MM-dd 08:00:00
				Date lastDayEndTimeDate = DateFormatUtil.getEndTime(hours);//前一天K线结束时间 yyyy-MM-dd 07:59:59
				//昨日K线信息
				/*
				List<Klines> klinesList_1_x_day = klinesService.continuousKlines(pair, restBaseUrl, lastDayStartTimeDate.getTime(), 
						lastDayEndTimeDate.getTime(), AppConfig.INERVAL_1D);
						*/
				//昨日15分钟级别K线信息
				List<Klines> klinesList_1_x_day_15m = klinesService.continuousKlines(pair, restBaseUrl, lastDayStartTimeDate.getTime(), 
						lastDayEndTimeDate.getTime(), AppConfig.INERVAL_15M);
				/*
				if(klinesList_1_x_day.isEmpty()) {
					logger.info("无法获取" + pair + "交易对昨日K线信息");
					continue;
				}*/
				
				//5分钟级别K线起止时间
				Date endTime_5m = DateFormatUtil.parse(DateFormatUtil.format_yyyy_mm_dd_HH_mm_00(now));
				Date startTime_5m = DateFormatUtil.getStartTimeBySetMinute(endTime_5m, -5);//5分钟以前开盘时间
				endTime_5m = DateFormatUtil.getStartTimeBySetSecond(endTime_5m, -1);//收盘时间
				
				//前1根5分钟级别k线信息
				List<Klines> klinesList_1_x_5m = klinesService.continuousKlines(pair, restBaseUrl, startTime_5m.getTime(),
						endTime_5m.getTime(), AppConfig.INERVAL_5M);
				
				if(klinesList_1_x_5m.isEmpty()) {
					logger.info("无法获取" + pair + "交易对最近5分钟K线信息");
					continue;
				}
				
				if(klinesList_1_x_day_15m.isEmpty()) {
					logger.info("无法获取" + pair + "交易对昨日15分钟级别K线信息");
				}
				
				//Klines kline_1d = klinesList_1_x_day.get(0);
				Klines kline_5m = klinesList_1_x_5m.get(0);
				int decimalNum = kline_5m.getDecimalNum();
				/*
				//昨日开盘、收盘、最低、最高价格
				double closePrice_1d = kline_1d.getClosePrice();
				double openPrice_1d = kline_1d.getOpenPrice();
				double lowPrice_1d = kline_1d.getLowPrice();
				double hightPrice_1d = kline_1d.getHighPrice();
				*/
				//5分钟开盘、收盘、最低、最高价格
				double closePrice_5m = kline_5m.getClosePrice();
				//double openPrice_5m = kline_5m.getOpenPrice();
				double lowPrice_5m = kline_5m.getLowPrice();
				double hightPrice_5m = kline_5m.getHighPrice();
				double currentPrice = closePrice_5m;
				
				Klines highKline_1d_15m = PriceUtil.getMaxPriceKLine(klinesList_1_x_day_15m);//昨日最高价k线信息
				Klines lowKline_1d_15m = PriceUtil.getMinPriceKLine(klinesList_1_x_day_15m);//昨日最低价k线信息
				
				//昨日15分钟级别为1个周期斐波那契回撤水平
				FibInfo fibInfo = new FibInfo(lowKline_1d_15m, highKline_1d_15m,decimalNum);
				double fib1 = fibInfo.getFib1();
				//double fib236 = fibInfo.getFib236();
				double fib382 = fibInfo.getFib382();
				//double fib5 = fibInfo.getFib5();
				double fib618 = fibInfo.getFib618();
				double fib66 = fibInfo.getFib66();
				double fib786 = fibInfo.getFib786();
				double fib0 = fibInfo.getFib0();
				
				//空头行情
				if(fib0 < fib1) {
					subject = pair + "永续合约做空机会 " + DateFormatUtil.format(new Date());
					
					//未能突破最高价的价格行为
					if(currentPrice <= fib1 && hightPrice_5m >= fib1) {//最高价做空
						//理想止盈价 fib382
						//理想止损价 hightPrice_5m
						text = StringUtil.formatShortMessage(pair, currentPrice, fib382, hightPrice_5m,decimalNum);
						
						subject = pair + "永续合约【极好的】做空机会 " + DateFormatUtil.format(new Date());
						
					} else if(currentPrice <= fib786 && hightPrice_5m >= fib786) {//0.786做空
						//理想止盈价 fib382
						//理想止损价 hightPrice_5m
						text = StringUtil.formatShortMessage(pair, currentPrice, fib382, hightPrice_5m,decimalNum);
						
						subject = pair + "永续合约【不错的】做空机会 " + DateFormatUtil.format(new Date());
						
					} else if(currentPrice <= fib66 && hightPrice_5m >= fib66) {//0.66做空
						//理想止盈价 fib382
						//理想止损价 hightPrice_5m
						text = StringUtil.formatShortMessage(pair, currentPrice, fib382, hightPrice_5m,decimalNum);
					} else if(currentPrice <= fib618 && hightPrice_5m >= fib618) {//0.618做空
						//理想止盈价 fib382
						//理想止损价 hightPrice_5m
						text = StringUtil.formatShortMessage(pair, currentPrice, fib382, hightPrice_5m,decimalNum);
					}
					
					//空头行情中可做多的情况 最低价
					else if(currentPrice >= fib0 && lowPrice_5m <= fib0) {//最低价做多
						subject = pair + "永续合约【极好的】做多机会 " + DateFormatUtil.format(new Date());
						//理想止盈价 fib618
						//理想止损价 lowPrice_5m
						text = StringUtil.formatLongMessage(pair, currentPrice, lowPrice_5m, fib618,decimalNum);
					} else if(currentPrice >= fib382 && lowPrice_5m <= fib382) {//0.382做多
						subject = pair + "永续合约做多机会 " + DateFormatUtil.format(new Date());
						//理想止盈价 fib618
						//理想止损价 lowPrice_5m
						text = StringUtil.formatLongMessage(pair, currentPrice, lowPrice_5m, fib618,decimalNum);
					}
					
				} else if(fib0 > fib1) {//多头行情
					subject = pair + "永续合约做多机会 " + DateFormatUtil.format(new Date());
					
					//未能跌破最低价的价格行为
					if(currentPrice >= fib1 && lowPrice_5m <= fib1) {//最低价做多
						//理想止盈价 fib382
						//理想止损价 lowPrice_5m
						text = StringUtil.formatLongMessage(pair, currentPrice, lowPrice_5m, fib382,decimalNum);
						
						subject = pair + "永续合约【极好的】做多机会 " + DateFormatUtil.format(new Date());
						
					} else if(currentPrice >= fib786 && lowPrice_5m <= fib786) {//0.786做多
						//理想止盈价 fib382
						//理想止损价 lowPrice_5m
						text = StringUtil.formatLongMessage(pair, currentPrice, lowPrice_5m, fib382,decimalNum);
						
						subject = pair + "永续合约【不错的】做多机会 " + DateFormatUtil.format(new Date());
						
					} else if(currentPrice >= fib66 && lowPrice_5m <= fib66) {//0.66做多
						//理想止盈价 fib382
						//理想止损价 lowPrice_5m
						text = StringUtil.formatLongMessage(pair, currentPrice, lowPrice_5m, fib382,decimalNum);
					} else if(currentPrice >= fib618 && lowPrice_5m <= fib618) {//0.618做多
						//理想止盈价 fib382
						//理想止损价 lowPrice_5m
						text = StringUtil.formatLongMessage(pair, currentPrice, lowPrice_5m, fib382,decimalNum);
					}
					
					//多头行情可做空的情况 最高价
					else if(currentPrice <= fib0 && hightPrice_5m >= fib0) {
						subject = pair + "永续合约【极好的】做空机会 " + DateFormatUtil.format(new Date());
						//理想止盈价 fib618
						//理想止损价 hightPrice_5m
						text = StringUtil.formatShortMessage(pair, currentPrice, fib618, hightPrice_5m,decimalNum);
					} else if(currentPrice <= fib382 && hightPrice_5m >= fib382) {//0.382做空
						subject = pair + "永续合约做空机会 " + DateFormatUtil.format(new Date());
						//理想止盈价 fib618
						//理想止损价 hightPrice_5m
						text = StringUtil.formatShortMessage(pair, currentPrice, fib618, hightPrice_5m,decimalNum);
					}
				}
				
				if(StringUtil.isNotEmpty(subject) && StringUtil.isNotEmpty(text)) {
					
					//text += "\n\nFib：" + fibInfo.toString();
					
					logger.info("邮件主题：" + subject);
					logger.info("邮件内容：" + text);
					
					EmailUtil.send(smtpHost, smtpPort, emailUserName, emailPassword, recipient, subject, text);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			EmailUtil.send(smtpHost, smtpPort, emailUserName, emailPassword, recipient, "程序运行出现异常", e.getLocalizedMessage());
		} finally {
			logger.info("LconicHighAndLowPricesListenTask finish.");
		}
	}
}
