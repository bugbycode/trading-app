package com.bugbycode.trading_app.task;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

import com.bugbycode.config.AppConfig;
import com.bugbycode.module.FibCode;
import com.bugbycode.module.FibInfo;
import com.bugbycode.module.Klines;
import com.bugbycode.module.QuotationMode;
import com.bugbycode.module.Result;
import com.bugbycode.module.ResultCode;
import com.bugbycode.service.KlinesService;
import com.util.DateFormatUtil;
import com.util.EmailUtil;
import com.util.PriceUtil;
import com.util.StringUtil;

/**
 * 永续合约日线级别斐波那契回撤交易监听计划任务
 */
@Configuration
@EnableScheduling
public class FuturesFibTradingListenTask {

	private final Logger logger = LogManager.getLogger(FuturesFibTradingListenTask.class);
	
	@Autowired
	private KlinesService klinesService;
	
	/**
	 * 查询k线信息 每十五分钟执行一次
	 * 
	 * @throws Exception
	 */
	@Scheduled(cron = "4 0/5 * * * ?")
	public void continuousKlines() throws Exception {
		logger.info("FuturesFibTradingListenTask start.");
		Date now = new Date();
		String restBaseUrl = AppConfig.REST_BASE_URL;
		String pairs = AppConfig.PAIRS;
		String emailUserName = AppConfig.EMAIL_USDRNAME;
		String emailPassword = AppConfig.EMAIL_PASSWORD;
		String smtpHost = AppConfig.SMTP_HOST;
		int smtpPort = AppConfig.SMTP_PORT;
		String recipient = AppConfig.RECIPIENT;
		
		int hours = DateFormatUtil.getHours(now.getTime());
		Date lastDayStartTimeDate = DateFormatUtil.getStartTime(hours);//前一天K线起始时间 yyyy-MM-dd 08:00:00
		Date lastDayEndTimeDate = DateFormatUtil.getEndTime(hours);//前一天K线结束时间 yyyy-MM-dd 07:59:59
		
		Date oneYearAgo = DateFormatUtil.getStartTimeBySetDay(lastDayStartTimeDate, -365);//1年以前起始时间
		
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
				
				//近1年日线级别k线信息
				List<Klines> klinesList_365_x_day = klinesService.continuousKlines(pair, restBaseUrl, oneYearAgo.getTime(), 
						lastDayEndTimeDate.getTime(), AppConfig.INERVAL_1D);
				
				if(klinesList_365_x_day.isEmpty()) {
					logger.info("无法获取" + pair + "交易对最近1年日线级别K线信息");
					continue;
				}
				
				int klinesSize = klinesList_365_x_day.size();
				
				//昨日K线信息
				Klines lastDayKlines = klinesList_365_x_day.get(klinesSize - 1);
				//标志性高点K线信息
				List<Klines> lconicHighPriceList = new ArrayList<Klines>();
				//标志性低点K线信息
				List<Klines> lconicLowPriceList = new ArrayList<Klines>();
				//获取标志性高低点K线信息
				int index_365day = klinesSize - 2;
				while(index_365day >= 1) {
					Klines klines_1day = klinesList_365_x_day.get(index_365day);
					Klines klines_1day_parent = klinesList_365_x_day.get(index_365day - 1);
					Klines klines_1day_after = klinesList_365_x_day.get(index_365day + 1);
					//判断是否为标志性高点
					if(klines_1day.getHighPrice() >= klines_1day_parent.getHighPrice() 
							&& klines_1day.getHighPrice() >= klines_1day_after.getHighPrice()){
						lconicHighPriceList.add(klines_1day);
					}
					
					//判断是否为标志性低点
					if(klines_1day.getLowPrice() <= klines_1day_parent.getLowPrice() 
							&& klines_1day.getLowPrice() <= klines_1day_after.getLowPrice()){
						lconicLowPriceList.add(klines_1day);
					}
					index_365day--;
				}
				
				//获取斐波那契回撤高点
				Klines fibHightKlines = PriceUtil.getFibHightKlines(lconicHighPriceList,lastDayKlines);
				//获取斐波那契回撤低点
				Klines fibLowKlines = PriceUtil.getFibLowKlines(lconicLowPriceList,lastDayKlines);
				
				if(fibHightKlines == null || fibLowKlines == null) {
					logger.info("无法计算出" + pair + "斐波那契回撤信息");
					continue;
				}
				
				int decimalNum = lastDayKlines.getDecimalNum();
				
				//斐波那契回撤信息
				FibInfo fibInfo = new FibInfo(fibLowKlines, fibHightKlines, decimalNum);
				
				/*
				logger.info("fibHightKlines：" + fibHightKlines.toString());
				logger.info("fibLowKlines：" + fibLowKlines.toString());
				
				logger.info(pair + "斐波那契信息：" + fibInfo.toString());
				*/
				
				double fib1 = fibInfo.getFib1();
				double fib236 = fibInfo.getFib236();
				double fib382 = fibInfo.getFib382();
				double fib5 = fibInfo.getFib5();
				double fib618 = fibInfo.getFib618();
				double fib66 = fibInfo.getFib66();
				double fib786 = fibInfo.getFib786();
				double fib0 = fibInfo.getFib0();
				
				//15分钟级别K线起止时间
				Date endTime_15m = DateFormatUtil.parse(DateFormatUtil.format_yyyy_mm_dd_HH_mm_00(now));
				Date startTime_15m = DateFormatUtil.getStartTimeBySetMinute(endTime_15m, -5 * 4);//15x4分钟以前开盘时间
				endTime_15m = DateFormatUtil.getStartTimeBySetSecond(endTime_15m, -1);//收盘时间
				
				//前4根15分钟级别k线信息
				List<Klines> klinesList_4_x_15m = klinesService.continuousKlines(pair, restBaseUrl, startTime_15m.getTime(),
						endTime_15m.getTime(), AppConfig.INERVAL_5M);
				
				if(klinesList_4_x_15m.isEmpty()) {
					logger.info("无法获取" + pair + "交易对最近15分钟K线信息");
					continue;
				}
				
				Klines kline_15m = klinesList_4_x_15m.get(klinesList_4_x_15m.size() - 1);
				
				//15分钟开盘、收盘、最低、最高价格
				double closePrice_15m = kline_15m.getClosePrice();
				double openPrice_15m = kline_15m.getOpenPrice();
				double lowPrice_15m = kline_15m.getLowPrice();
				double hightPrice_5m = kline_15m.getHighPrice();
				double currentPrice = closePrice_15m;
				
				//logger.info(pair + ":" + fibInfo.toString());
				
				//空头行情
				if(fib0 < fib1) {
					
					subject = pair + "永续合约做空机会 " + DateFormatUtil.format(new Date());
					
					//fib1做空
					if(PriceUtil.isShort(fib1, klinesList_4_x_15m)) {
						
						text = StringUtil.formatShortMessage(pair, currentPrice, fibInfo, hightPrice_5m, FibCode.FIB1,QuotationMode.SHORT);
						
					} else if(PriceUtil.isShort(fib786, klinesList_4_x_15m)) {//0.786做空
						
						text = StringUtil.formatShortMessage(pair, currentPrice, fibInfo, hightPrice_5m, FibCode.FIB786,QuotationMode.SHORT);
						
					} else if(PriceUtil.isShort(fib66, klinesList_4_x_15m)) {//0.66做空
						
						text = StringUtil.formatShortMessage(pair, currentPrice, fibInfo, hightPrice_5m, FibCode.FIB66,QuotationMode.SHORT);
						
					} else if(PriceUtil.isShort(fib618, klinesList_4_x_15m)) {// 0.618做空
						
						text = StringUtil.formatShortMessage(pair, currentPrice, fibInfo, hightPrice_5m, FibCode.FIB618,QuotationMode.SHORT);
						
					} else if(PriceUtil.isShort(fib5, klinesList_4_x_15m)) {//0.5做空
						
						text = StringUtil.formatShortMessage(pair, currentPrice, fibInfo, hightPrice_5m, FibCode.FIB5,QuotationMode.SHORT);
						
					} else if(PriceUtil.isShort(fib382, klinesList_4_x_15m)) {//0.382做空
						
						text = StringUtil.formatShortMessage(pair, currentPrice, fibInfo, hightPrice_5m, FibCode.FIB382,QuotationMode.SHORT);
						
					} else if(PriceUtil.isShort(fib236, klinesList_4_x_15m)) {//0.236做空
						text = StringUtil.formatShortMessage(pair, currentPrice, fibInfo, hightPrice_5m, FibCode.FIB236,QuotationMode.SHORT);
					}
					
					if(StringUtil.isEmpty(text)) {
						subject = pair + "永续合约做多机会 " + DateFormatUtil.format(new Date());
						
						//空头行情做多的情况
						if(PriceUtil.isLong(fib0, klinesList_4_x_15m)) {//fib0 
							
							text = StringUtil.formatLongMessage(pair, currentPrice, fibInfo, lowPrice_15m, FibCode.FIB0, QuotationMode.SHORT);
							
						} else if(PriceUtil.isLong(fib236, klinesList_4_x_15m)) {//fib236 
							
							text = StringUtil.formatLongMessage(pair, currentPrice, fibInfo, lowPrice_15m, FibCode.FIB236, QuotationMode.SHORT);
							
						} else if(PriceUtil.isLong(fib382, klinesList_4_x_15m)) {//fib382 
							
							text = StringUtil.formatLongMessage(pair, currentPrice, fibInfo, lowPrice_15m, FibCode.FIB382, QuotationMode.SHORT);
							
						} else if(PriceUtil.isLong(fib5, klinesList_4_x_15m)) {//fib5 
							
							text = StringUtil.formatLongMessage(pair, currentPrice, fibInfo, lowPrice_15m, FibCode.FIB5, QuotationMode.SHORT);
							
						} else if(PriceUtil.isLong(fib618, klinesList_4_x_15m)) {//fib618 
							
							text = StringUtil.formatLongMessage(pair, currentPrice, fibInfo, lowPrice_15m, FibCode.FIB618, QuotationMode.SHORT);
							
						} else if(PriceUtil.isLong(fib66, klinesList_4_x_15m)) {//fib66 
							
							text = StringUtil.formatLongMessage(pair, currentPrice, fibInfo, lowPrice_15m, FibCode.FIB66, QuotationMode.SHORT);
							
						} else if(PriceUtil.isLong(fib786, klinesList_4_x_15m)) {//fib786 
							
							text = StringUtil.formatLongMessage(pair, currentPrice, fibInfo, lowPrice_15m, FibCode.FIB786, QuotationMode.SHORT);
							
						}
					}
					
				} else if(fib0 > fib1) {//多头行情
					
					subject = pair + "永续合约做多机会 " + DateFormatUtil.format(new Date());
					
					if(PriceUtil.isLong(fib1, klinesList_4_x_15m)) {//FIB1做多
						
						text = StringUtil.formatLongMessage(pair, currentPrice, fibInfo, lowPrice_15m, FibCode.FIB1, QuotationMode.LONG);
					
					} else if(PriceUtil.isLong(fib786, klinesList_4_x_15m)) {
						
						text = StringUtil.formatLongMessage(pair, currentPrice, fibInfo, lowPrice_15m, FibCode.FIB786, QuotationMode.LONG);
					
					} else if(PriceUtil.isLong(fib66, klinesList_4_x_15m)) {
						
						text = StringUtil.formatLongMessage(pair, currentPrice, fibInfo, lowPrice_15m, FibCode.FIB66, QuotationMode.LONG);
					
					} else if(PriceUtil.isLong(fib618, klinesList_4_x_15m)) {
						
						text = StringUtil.formatLongMessage(pair, currentPrice, fibInfo, lowPrice_15m, FibCode.FIB618, QuotationMode.LONG);
					
					} else if(PriceUtil.isLong(fib5, klinesList_4_x_15m)) {
						
						text = StringUtil.formatLongMessage(pair, currentPrice, fibInfo, lowPrice_15m, FibCode.FIB5, QuotationMode.LONG);
					
					} else if(PriceUtil.isLong(fib382, klinesList_4_x_15m)) {
						
						text = StringUtil.formatLongMessage(pair, currentPrice, fibInfo, lowPrice_15m, FibCode.FIB382, QuotationMode.LONG);
					
					} else if(PriceUtil.isLong(fib236, klinesList_4_x_15m)) {
						
						text = StringUtil.formatLongMessage(pair, currentPrice, fibInfo, lowPrice_15m, FibCode.FIB236, QuotationMode.LONG);
					
					}
					
					if(StringUtil.isEmpty(text)) {
						
						subject = pair + "永续合约做空机会 " + DateFormatUtil.format(new Date());
						
						//多头行情做空的情况
						if(PriceUtil.isShort(fib0, klinesList_4_x_15m)) {
							
							text = StringUtil.formatShortMessage(pair, currentPrice, fibInfo, hightPrice_5m, FibCode.FIB0, QuotationMode.LONG);
							
						} else if(PriceUtil.isShort(fib236, klinesList_4_x_15m)) {
							
							text = StringUtil.formatShortMessage(pair, currentPrice, fibInfo, hightPrice_5m, FibCode.FIB236, QuotationMode.LONG);
							
						} else if(PriceUtil.isShort(fib382, klinesList_4_x_15m)) {
							
							text = StringUtil.formatShortMessage(pair, currentPrice, fibInfo, hightPrice_5m, FibCode.FIB382, QuotationMode.LONG);
							
						} else if(PriceUtil.isShort(fib5, klinesList_4_x_15m)) {
							
							text = StringUtil.formatShortMessage(pair, currentPrice, fibInfo, hightPrice_5m, FibCode.FIB5, QuotationMode.LONG);
							
						} else if(PriceUtil.isShort(fib618, klinesList_4_x_15m)) {
							
							text = StringUtil.formatShortMessage(pair, currentPrice, fibInfo, hightPrice_5m, FibCode.FIB618, QuotationMode.LONG);
							
						} else if(PriceUtil.isShort(fib66, klinesList_4_x_15m)) {
							
							text = StringUtil.formatShortMessage(pair, currentPrice, fibInfo, hightPrice_5m, FibCode.FIB66, QuotationMode.LONG);
							
						} else if(PriceUtil.isShort(fib786, klinesList_4_x_15m)) {
							
							text = StringUtil.formatShortMessage(pair, currentPrice, fibInfo, hightPrice_5m, FibCode.FIB786, QuotationMode.LONG);
							
						}
					}
					
				}
				
				if(StringUtil.isNotEmpty(subject) && StringUtil.isNotEmpty(text)) {
					
					text += "\n\nFib：" + fibInfo.toString();
					
					logger.info("邮件主题：" + subject);
					logger.info("邮件内容：" + text);
					
					Result<ResultCode, Exception> result = EmailUtil.send(smtpHost, smtpPort, emailUserName, emailPassword, recipient, subject, text);
					
					switch (result.getResult()) {
					case ERROR:
						
						Exception ex = result.getErr();
						
						logger.info("邮件发送失败！失败原因：" + ex.getLocalizedMessage());
						
						ex.printStackTrace();
						
						break;
						
					default:
						
						logger.info("邮件发送成功！");
						
						break;
					}
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
