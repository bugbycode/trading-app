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
import com.bugbycode.module.FibInfo;
import com.bugbycode.module.Klines;
import com.bugbycode.module.Result;
import com.bugbycode.module.ResultCode;
import com.bugbycode.service.KlinesService;
import com.util.DateFormatUtil;
import com.util.EmailUtil;
import com.util.PriceUtil;
import com.util.StringUtil;

/**
 * 昨日价格点位、以及标志性高低点价格监控
 */
//@Configuration
//@EnableScheduling
public class LastDayPriceListenTask {

	private final Logger logger = LogManager.getLogger(LastDayPriceListenTask.class);
	
	@Autowired
	private KlinesService klinesService;
	
	/**
	 * 查询k线信息 每十五分钟执行一次
	 * 
	 * @throws Exception
	 */
	//@Scheduled(cron = "4 0/15 * * * ?")
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
				
				//昨日15分钟级别K线信息
				List<Klines> klinesList_1_x_day_15m = klinesService.continuousKlines(pair, restBaseUrl, lastDayStartTimeDate.getTime(), 
						lastDayEndTimeDate.getTime(), AppConfig.INERVAL_15M);
				
				//15分钟级别K线起止时间
				Date endTime_15m = DateFormatUtil.parse(DateFormatUtil.format_yyyy_mm_dd_HH_mm_00(now));
				Date startTime_15m = DateFormatUtil.getStartTimeBySetMinute(endTime_15m, -15);//15分钟以前开盘时间
				endTime_15m = DateFormatUtil.getStartTimeBySetSecond(endTime_15m, -1);//收盘时间
				
				//前1根15分钟级别k线信息
				List<Klines> klinesList_1_x_15m = klinesService.continuousKlines(pair, restBaseUrl, startTime_15m.getTime(),
						endTime_15m.getTime(), AppConfig.INERVAL_15M);
				
				if(klinesList_1_x_15m.isEmpty()) {
					logger.info("无法获取" + pair + "交易对最近15分钟K线信息");
					continue;
				}
				
				if(klinesList_1_x_day_15m.isEmpty()) {
					logger.info("无法获取" + pair + "交易对昨日15分钟级别K线信息");
				}
				
				//Klines kline_1d = klinesList_1_x_day.get(0);
				Klines kline_15m = klinesList_1_x_15m.get(0);
				int decimalNum = kline_15m.getDecimalNum();
				/*
				//昨日开盘、收盘、最低、最高价格
				double closePrice_1d = kline_1d.getClosePrice();
				double openPrice_1d = kline_1d.getOpenPrice();
				double lowPrice_1d = kline_1d.getLowPrice();
				double hightPrice_1d = kline_1d.getHighPrice();
				*/
				//15分钟开盘、收盘、最低、最高价格
				double closePrice_15m = kline_15m.getClosePrice();
				//double openPrice_15m = kline_15m.getOpenPrice();
				double lowPrice_15m = kline_15m.getLowPrice();
				double hightPrice_5m = kline_15m.getHighPrice();
				double currentPrice = closePrice_15m;
				
				Klines highKline_1d_15m = PriceUtil.getMaxPriceKLine(klinesList_1_x_day_15m);//昨日最高价k线信息
				Klines lowKline_1d_15m = PriceUtil.getMinPriceKLine(klinesList_1_x_day_15m);//昨日最低价k线信息
				
				//昨日15分钟级别为1个周期斐波那契回撤水平
				FibInfo fibInfo = new FibInfo(lowKline_1d_15m, highKline_1d_15m,decimalNum);
				double fib1 = fibInfo.getFib1();
				double fib236 = fibInfo.getFib236();
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
					else if(currentPrice >= fib0 && lowPrice_15m <= fib0) {//最低价做多
						subject = pair + "永续合约【极好的】做多机会 " + DateFormatUtil.format(new Date());
						//理想止盈价 fib618
						//理想止损价 lowPrice_5m
						text = StringUtil.formatLongMessage(pair, currentPrice, lowPrice_15m, fib618,decimalNum);
					} else if(currentPrice >= fib236 && lowPrice_15m <= fib236) {//0.236做多
						subject = pair + "永续合约【不错的】做多机会 " + DateFormatUtil.format(new Date());
						//理想止盈价 fib618
						//理想止损价 lowPrice_5m
						text = StringUtil.formatLongMessage(pair, currentPrice, lowPrice_15m, fib618,decimalNum);
					}
					
				} else if(fib0 > fib1) {//多头行情
					subject = pair + "永续合约做多机会 " + DateFormatUtil.format(new Date());
					
					//未能跌破最低价的价格行为
					if(currentPrice >= fib1 && lowPrice_15m <= fib1) {//最低价做多
						//理想止盈价 fib382
						//理想止损价 lowPrice_5m
						text = StringUtil.formatLongMessage(pair, currentPrice, lowPrice_15m, fib382,decimalNum);
						
						subject = pair + "永续合约【极好的】做多机会 " + DateFormatUtil.format(new Date());
						
					} else if(currentPrice >= fib786 && lowPrice_15m <= fib786) {//0.786做多
						//理想止盈价 fib382
						//理想止损价 lowPrice_5m
						text = StringUtil.formatLongMessage(pair, currentPrice, lowPrice_15m, fib382,decimalNum);
						
						subject = pair + "永续合约【不错的】做多机会 " + DateFormatUtil.format(new Date());
						
					} else if(currentPrice >= fib66 && lowPrice_15m <= fib66) {//0.66做多
						//理想止盈价 fib382
						//理想止损价 lowPrice_5m
						text = StringUtil.formatLongMessage(pair, currentPrice, lowPrice_15m, fib382,decimalNum);
					} else if(currentPrice >= fib618 && lowPrice_15m <= fib618) {//0.618做多
						//理想止盈价 fib382
						//理想止损价 lowPrice_5m
						text = StringUtil.formatLongMessage(pair, currentPrice, lowPrice_15m, fib382,decimalNum);
					}
					
					//多头行情可做空的情况 最高价
					else if(currentPrice <= fib0 && hightPrice_5m >= fib0) {
						subject = pair + "永续合约【极好的】做空机会 " + DateFormatUtil.format(new Date());
						//理想止盈价 fib618
						//理想止损价 hightPrice_5m
						text = StringUtil.formatShortMessage(pair, currentPrice, fib618, hightPrice_5m,decimalNum);
					} else if(currentPrice <= fib236 && hightPrice_5m >= fib236) {//0.236做空
						subject = pair + "永续合约【不错的】做空机会 " + DateFormatUtil.format(new Date());
						//理想止盈价 fib618
						//理想止损价 hightPrice_5m
						text = StringUtil.formatShortMessage(pair, currentPrice, fib618, hightPrice_5m,decimalNum);
					}
				}
				
				if (!(StringUtil.isNotEmpty(subject) && StringUtil.isNotEmpty(text))) {
					
					subject = "";
					text = "";
					
					//logger.debug("开始匹配" + pair + "近1年标志性高低点价格");
					
					//近三年日线级别K线信息
					List<Klines> klinesList_365_x_day = klinesService.continuousKlines(pair, restBaseUrl, oneYearAgo.getTime(), 
							lastDayEndTimeDate.getTime(), AppConfig.INERVAL_1D);
					
					if(klinesList_365_x_day.isEmpty()) {
						logger.info("无法获取" + pair + "交易对最近1年日线级别K线信息");
						continue;
					}
					
					//标志性高点K线信息
					List<Klines> lconicHighPriceList = new ArrayList<Klines>();
					//标志性低点K线信息
					List<Klines> lconicLowPriceList = new ArrayList<Klines>();
					//获取标志性高低点K线信息
					int index_365day = klinesList_365_x_day.size() - 2;
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
					
					//logger.info("开始匹配" + pair + "标志性高点=================================");
					
					double maxLconicHighPrice = 0;
					//判断是否突破标志性高点
					for(int index = 0;index < lconicHighPriceList.size();index++) {
						Klines klines = lconicHighPriceList.get(index);
						
						if(index > 0) {
							//从已匹配的项找出最高值 如果已匹配的项中最高值大于当前K线最高价则继续匹配下一个
							Klines maxPriceKlines = PriceUtil.getMaxPriceKLine(lconicHighPriceList, 0, index);
							if(klines.getHighPrice() < maxPriceKlines.getHighPrice()) {
								continue;
							}
						}
						
						//永远放最大值
						if(klines.getHighPrice() >= maxLconicHighPrice || maxLconicHighPrice == 0) {
							maxLconicHighPrice = klines.getHighPrice();
						}
						
						//logger.info("匹配K线：" + klines.toString());
						
						if(maxLconicHighPrice > hightPrice_5m) {//如果遇到更高的高点且无法匹配直接退出
							//logger.info("当前K线最高价大于当前五分钟k线最高价，因此无需继续匹配");
							break;
						}
						
						//价格假突破高点行为 收盘价小于等于标志性高点 最高价大于等于标志性高点
						if(closePrice_15m <= maxLconicHighPrice) {
							//理想止盈价 fib382
							//理想止损价 hightPrice_5m
							text = StringUtil.formatShortMessage(pair, currentPrice, fib382, hightPrice_5m,decimalNum);
							
							subject = pair + "永续合约【难得的】做空机会 " + DateFormatUtil.format(new Date());
							
							//logger.info("命中的K线信息：" + klines.toString());
							
							break;
						}
					}
					
					//判断是否跌破标志性低点
					if(StringUtil.isEmpty(subject)) {
						
						//logger.info("开始匹配" + pair + "标志性低点++++++++++++++++++++++++++++++++++++++++++++++++++");
						
						double minLconicLowPrice = 0;
						for(int index = 0;index < lconicLowPriceList.size();index++) {
							Klines klines = lconicLowPriceList.get(index);
							
							if(index > 0) {
								//从已匹配的项中找出最低价的k线信息 如果已匹配的项中出现比当前匹配的K线最低价还低则继续匹配下一个
								Klines minPriceKlines = PriceUtil.getMinPriceKLine(lconicLowPriceList, 0, index);
								if(klines.getLowPrice() > minPriceKlines.getLowPrice()) {
									continue;
								}
							}
							
							//永远放最小值
							if(klines.getLowPrice() < minLconicLowPrice || minLconicLowPrice == 0) {
								minLconicLowPrice = klines.getLowPrice();
							}
							
							//logger.info("匹配K线：" + klines.toString());
							
							if(minLconicLowPrice < lowPrice_15m) {//遇到更低的低点时直接退出
								//logger.info("当前k线最低价小于当前五分钟k线最低价，因此无需继续匹配");
								break;
							}
							
							//假跌破 收盘价大于等于标志性低点 最低价小于等于标志性低点
							if(closePrice_15m >= minLconicLowPrice) {
								//理想止盈价 fib382
								//理想止损价 lowPrice_5m
								text = StringUtil.formatLongMessage(pair, currentPrice, lowPrice_15m, fib382,decimalNum);
								
								subject = pair + "永续合约【难得的】做多机会 " + DateFormatUtil.format(new Date());
								
								//logger.info("命中的K线信息：" + klines.toString());
								
								break;
							}
						}
					}
				}
				
				if(StringUtil.isNotEmpty(subject) && StringUtil.isNotEmpty(text)) {
					
					//text += "\n\nFib：" + fibInfo.toString();
					
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
