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
import com.bugbycode.module.FibKlinesData;
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
		
		int hours = DateFormatUtil.getHours(now.getTime());
		Date lastDayStartTimeDate = DateFormatUtil.getStartTime(hours);//前一天K线起始时间 yyyy-MM-dd 08:00:00
		Date lastDayEndTimeDate = DateFormatUtil.getEndTime(hours);//前一天K线结束时间 yyyy-MM-dd 07:59:59
		
		Date oneYearAgo = DateFormatUtil.getStartTimeBySetDay(lastDayStartTimeDate, -365);//1年以前起始时间
		
		try {
			
			for(String pair : AppConfig.PAIRS) {
				pair = pair.trim();
				if(StringUtil.isEmpty(pair)) {
					continue;
				}
				
				String text = "";//邮件内容
				String subject = "";//邮件主题
				
				//近1年日线级别k线信息
				List<Klines> klinesList_365_x_day = klinesService.continuousKlines(pair, oneYearAgo.getTime(), 
						lastDayEndTimeDate.getTime(), AppConfig.INERVAL_1D);
				
				if(klinesList_365_x_day.isEmpty()) {
					logger.info("无法获取" + pair + "交易对最近1年日线级别K线信息");
					continue;
				}
				
				FibKlinesData<List<Klines>,List<Klines>> fibKlinesData = PriceUtil.getFibKlinesData(klinesList_365_x_day);
				
				//标志性高点K线信息
				List<Klines> lconicHighPriceList = fibKlinesData.getLconicHighPriceList();
				//标志性低点K线信息
				List<Klines> lconicLowPriceList = fibKlinesData.getLconicLowPriceList();
				
				int klinesSize = klinesList_365_x_day.size();
				
				//昨日K线信息
				Klines lastDayKlines = klinesList_365_x_day.get(klinesSize - 1);
				
				//获取斐波那契回撤高点
				Klines fibHightKlines = PriceUtil.getFibHightKlines(lconicHighPriceList,lastDayKlines);
				//获取斐波那契回撤低点
				Klines fibLowKlines = PriceUtil.getFibLowKlines(lconicLowPriceList,lastDayKlines);
				
				if(fibHightKlines == null || fibLowKlines == null) {
					logger.info("无法计算出" + pair + "斐波那契回撤信息");
					continue;
				}
				
				//斐波那契回撤信息
				FibInfo fibInfo = new FibInfo(fibLowKlines, fibHightKlines, fibLowKlines.getDecimalNum());
				
				//15分钟级别K线起止时间
				Date endTime_5m = DateFormatUtil.parse(DateFormatUtil.format_yyyy_mm_dd_HH_mm_00(now));
				Date startTime_5m = DateFormatUtil.getStartTimeBySetMinute(endTime_5m, -5 * 4);//5x4分钟以前开盘时间
				endTime_5m = DateFormatUtil.getStartTimeBySetSecond(endTime_5m, -1);//收盘时间
				
				//前4根5分钟级别k线信息
				List<Klines> klinesList_4_x_5m = klinesService.continuousKlines(pair, startTime_5m.getTime(),
						endTime_5m.getTime(), AppConfig.INERVAL_5M);
				
				if(klinesList_4_x_5m.isEmpty()) {
					logger.info("无法获取" + pair + "交易对最近5分钟级别K线信息");
					continue;
				}
				
				Klines kline_5m = klinesList_4_x_5m.get(klinesList_4_x_5m.size() - 1);
				
				//15分钟开盘、收盘、最低、最高价格
				double closePrice_5m = kline_5m.getClosePrice();
				double openPrice_5m = kline_5m.getOpenPrice();
				double lowPrice_5m = kline_5m.getLowPrice();
				double hightPrice_5m = kline_5m.getHighPrice();
				double currentPrice = closePrice_5m;
				
				FibCode[] codes = FibCode.values();
				
				QuotationMode qm = fibInfo.getQuotationMode();
				
				switch (qm) {
				
				case SHORT:
					//空头行情做空 fib1~fib236
					for(int offset = codes.length - 1;offset > 0;offset--) {
						
						FibCode code = codes[offset];
						
						if(PriceUtil.isShort(fibInfo.getFibValue(code), klinesList_4_x_5m)) {
							
							FibCode closePpositionCode = codes[offset - 1];
							
							switch (closePpositionCode) {
							case FIB66:
								closePpositionCode = codes[offset - 2];
								break;
							case FIB618:
								closePpositionCode = codes[offset - 2];
								break;

							default:
								
								closePpositionCode = codes[offset - 1];
								
								break;
							}
							
							subject = String.format("%s永续合约%s(%s)做空机会 %s", pair, code.getDescription(),
									PriceUtil.formatDoubleDecimal(fibInfo.getFibValue(code),fibInfo.getDecimalPoint()),
											DateFormatUtil.format(new Date()));
							
							text = StringUtil.formatShortMessage(pair, currentPrice, fibInfo, hightPrice_5m, closePpositionCode);
							
							break;
						}
					}
					
					if(StringUtil.isEmpty(text)) {
						
						//空头行情做多 fib0~fib786
						for(int offset = 0; offset < codes.length - 1;offset++) {
							
							FibCode code = codes[offset];
							
							FibCode closePpositionCode = codes[offset + 1];
							
							switch (closePpositionCode) {
							case FIB66:
								closePpositionCode = codes[offset + 2];
								break;
							case FIB618:
								closePpositionCode = codes[offset + 2];
								break;

							default:
								
								closePpositionCode = codes[offset + 1];
								
								break;
							}
							
							if(PriceUtil.isLong(fibInfo.getFibValue(code), klinesList_4_x_5m)) {//fib0 
								
								subject = String.format("%s永续合约%s(%s)做多机会 %s", pair, code.getDescription(),
										PriceUtil.formatDoubleDecimal(fibInfo.getFibValue(code),fibInfo.getDecimalPoint()),
										DateFormatUtil.format(new Date()));
								
								text = StringUtil.formatLongMessage(pair, currentPrice, fibInfo, lowPrice_5m, closePpositionCode);
								
							}
						}
					}
					
					break;
					
				case LONG:
					
					//多头行情做多 fib1~fib236
					for(int offset = codes.length - 1;offset > 0;offset--) {
						
						FibCode code = codes[offset];
						
						FibCode closePpositionCode = codes[offset - 1];
						
						switch (closePpositionCode) {
						case FIB66:
							closePpositionCode = codes[offset - 2];
							break;
						case FIB618:
							closePpositionCode = codes[offset - 2];
							break;

						default:
							
							closePpositionCode = codes[offset - 1];
							
							break;
						}
						
						if(PriceUtil.isLong(fibInfo.getFibValue(code), klinesList_4_x_5m)) {//FIB1做多

							subject = String.format("%s永续合约%s(%s)做多机会 %s", pair, code.getDescription(),
									PriceUtil.formatDoubleDecimal(fibInfo.getFibValue(code),fibInfo.getDecimalPoint()),
									DateFormatUtil.format(new Date()));
							
							text = StringUtil.formatLongMessage(pair, currentPrice, fibInfo, lowPrice_5m, closePpositionCode);
						
						}
					}
					
					if(StringUtil.isEmpty(text)) {
						
						//多头行情做空 fib0~fib786
						for(int offset = 0; offset < codes.length - 1;offset++) {
							
							FibCode code = codes[offset];
							
							FibCode closePpositionCode = codes[offset + 1];
							
							switch (closePpositionCode) {
							case FIB66:
								closePpositionCode = codes[offset + 2];
								break;
							case FIB618:
								closePpositionCode = codes[offset + 2];
								break;

							default:
								
								closePpositionCode = codes[offset + 1];
								
								break;
							}
							
							if(PriceUtil.isShort(fibInfo.getFibValue(code), klinesList_4_x_5m)) {

								subject = String.format("%s永续合约%s(%s)做空机会 %s", pair, code.getDescription(),
										PriceUtil.formatDoubleDecimal(fibInfo.getFibValue(code),fibInfo.getDecimalPoint()),
										DateFormatUtil.format(new Date()));
								
								text = StringUtil.formatShortMessage(pair, currentPrice, fibInfo, hightPrice_5m, closePpositionCode);
								
							}
						}
						
					}
					break;
					
				default:
					break;
					
				}
				
				if(StringUtil.isNotEmpty(subject) && StringUtil.isNotEmpty(text)) {
					
					logger.info("邮件主题：" + subject);
					logger.info("邮件内容：" + text);
					
					text += "\n\nFib：" + fibInfo.toString();
					
					Result<ResultCode, Exception> result = EmailUtil.send(subject, text);
					
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
			EmailUtil.send("程序运行出现异常", e.getLocalizedMessage());
		} finally {
			logger.info("LconicHighAndLowPricesListenTask finish.");
		}
	}
}
