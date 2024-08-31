package com.bugbycode.trading_app.task;

import java.util.Date;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.util.ObjectUtils;

import com.bugbycode.config.AppConfig;
import com.bugbycode.module.FibKlinesData;
import com.bugbycode.module.Klines;
import com.bugbycode.module.QUERY_SPLIT;
import com.bugbycode.module.Result;
import com.bugbycode.module.ResultCode;
import com.bugbycode.service.KlinesService;
import com.util.DateFormatUtil;
import com.util.EmailUtil;
import com.util.KlinesComparator;
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
		
		KlinesComparator kc = new KlinesComparator();
		
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
				
				FibKlinesData<List<Klines>,List<Klines>> fibKlinesData = PriceUtil.getFibKlinesData(klinesList_365_x_day);
				
				//标志性高点K线信息
				List<Klines> lconicHighPriceList = fibKlinesData.getLconicHighPriceList();
				//标志性低点K线信息
				List<Klines> lconicLowPriceList = fibKlinesData.getLconicLowPriceList();
				
				//昨日K线信息
				Klines lastDayKlines = PriceUtil.getLastKlines(klinesList_365_x_day);
				
				lconicHighPriceList.add(lastDayKlines);
				lconicLowPriceList.add(lastDayKlines);
				
				//排序 按开盘时间升序 从旧到新
				lconicHighPriceList.sort(kc);
				lconicLowPriceList.sort(kc);
				
				List<Klines> klinesList_hit = klinesService.continuousKlines5M(pair, now, 5, QUERY_SPLIT.NOT_ENDTIME);
				if(klinesList_hit.isEmpty()) {
					logger.info("无法获取" + pair + "交易对最近5分钟级别K线信息");
					continue;
				}
				
				Klines hitLowKlines = PriceUtil.getPositionLowKlines(lconicLowPriceList, klinesList_hit);
				Klines hitHighKlines = PriceUtil.getPositionHighKlines(lconicHighPriceList, klinesList_hit);
				
				String dateStr = DateFormatUtil.format(new Date());
				
				String subject = "";
				String text = "";
				
				if(!ObjectUtils.isEmpty(hitLowKlines)) {
					
					double lowPrice = hitLowKlines.getLowPrice();
					
					if(PriceUtil.isLong(lowPrice, klinesList_hit)) {
						
						subject = String.format("%s永续合约跌破%s并收回 %s", pair,PriceUtil.formatDoubleDecimal(lowPrice, hitLowKlines.getDecimalNum()),dateStr);
						
						text = String.format("%s永续合约跌破%s最低价%s并收回", pair, 
								DateFormatUtil.format_yyyy_mm_dd(new Date(hitLowKlines.getStarTime())), 
								PriceUtil.formatDoubleDecimal(lowPrice, hitLowKlines.getDecimalNum()));
					} else if(PriceUtil.isShort(lowPrice, klinesList_hit)) {
						
						subject = String.format("%s永续合约跌破昨%s %s", pair,PriceUtil.formatDoubleDecimal(lowPrice, hitLowKlines.getDecimalNum()),dateStr);
						
						text = String.format("%s永续合约跌破%s最低价%s", pair, 
								DateFormatUtil.format_yyyy_mm_dd(new Date(hitLowKlines.getStarTime())), 
								PriceUtil.formatDoubleDecimal(lowPrice, hitLowKlines.getDecimalNum()));
					}
				
				} else if(!ObjectUtils.isEmpty(hitHighKlines)) {
					
					double highPrice = hitHighKlines.getHighPrice();
					
					if(PriceUtil.isLong(highPrice, klinesList_hit)) {
						
						subject = String.format("%s永续合约突破%s %s", pair,PriceUtil.formatDoubleDecimal(highPrice, hitHighKlines.getDecimalNum()),dateStr);
						
						text = String.format("%s永续合约突破%s最高价%s", pair, 
								DateFormatUtil.format_yyyy_mm_dd(new Date(hitHighKlines.getStarTime())), 
								PriceUtil.formatDoubleDecimal(highPrice, hitHighKlines.getDecimalNum()));
						
					} else if(PriceUtil.isShort(highPrice, klinesList_hit)) {
						
						subject = String.format("%s永续合约突破%s并收回 %s", pair,PriceUtil.formatDoubleDecimal(highPrice, hitHighKlines.getDecimalNum()),dateStr);
						
						text = String.format("%s永续合约突破%s最高价%s并收回", pair,
								DateFormatUtil.format_yyyy_mm_dd(new Date(hitHighKlines.getStarTime())), 
								PriceUtil.formatDoubleDecimal(highPrice, hitHighKlines.getDecimalNum()));
						
					}
					
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
