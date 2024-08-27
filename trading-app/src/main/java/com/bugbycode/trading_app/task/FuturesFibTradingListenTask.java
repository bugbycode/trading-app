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
import com.bugbycode.module.FibInfo;
import com.bugbycode.module.FibKlinesData;
import com.bugbycode.module.FibLevel;
import com.bugbycode.module.Klines;
import com.bugbycode.module.QUERY_SPLIT;
import com.bugbycode.module.QuotationMode;
import com.bugbycode.service.KlinesService;
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
	 * 查询k线信息 每五分钟执行一次
	 * 
	 * @throws Exception
	 */
	@Scheduled(cron = "4 0/5 * * * ?")
	public void continuousKlines() throws Exception {
		logger.info("FuturesFibTradingListenTask start.");
		
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
					logger.info("无法获取" + pair + "交易对最近1年日线级别K线信息");
					continue;
				}
				
				//一部分5分钟级别k线信息
				List<Klines> klinesList_hit = klinesService.continuousKlines5M(pair, now, 5, QUERY_SPLIT.NOT_ENDTIME);
				
				if(klinesList_hit.isEmpty()) {
					logger.info("无法获取" + pair + "交易对最近5分钟级别K线信息");
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
				
				if(ObjectUtils.isEmpty(fibHightKlines) || ObjectUtils.isEmpty(fibLowKlines)) {
					logger.debug("无法计算出" + pair + "第一级别斐波那契回撤信息");
					continue;
				}
				
				//修正斐波那契回撤点位
				fibHightKlines = PriceUtil.rectificationFibHightKlines(lconicHighPriceList, fibLowKlines, fibHightKlines);
				fibLowKlines = PriceUtil.rectificationFibLowKlines(lconicLowPriceList, fibLowKlines, fibHightKlines);
				
				//第一级别斐波那契
				//斐波那契回撤信息
				FibInfo fibInfo = new FibInfo(fibLowKlines, fibHightKlines, fibLowKlines.getDecimalNum(),FibLevel.LEVEL_1);
				
				//第一级别趋势
				QuotationMode qm = fibInfo.getQuotationMode();
				//第一级别斐波那契开仓
				switch (qm) {
				case LONG:
					klinesService.openLong(fibInfo, klinesList_hit);
					break;

				default:
					klinesService.openShort(fibInfo, klinesList_hit);
					break;
				}
				
				//开始获取第二级别斐波那契回撤信息
				Klines secondFibHightKlines = null;
				Klines secondFibLowKlines = null;
				
				//开始获取第二级别斐波那契回撤终点
				switch (qm) {
				case LONG:
					secondFibHightKlines = fibHightKlines;
					secondFibLowKlines = PriceUtil.getLowKlinesByStartKlines(klinesList_365_x_day, secondFibHightKlines);
					break;

				default:
					secondFibLowKlines = fibLowKlines;
					secondFibHightKlines = PriceUtil.getHightKlinesByStartKlines(klinesList_365_x_day, secondFibLowKlines);
					break;
				}
				
				if(ObjectUtils.isEmpty(secondFibHightKlines) || ObjectUtils.isEmpty(secondFibLowKlines)) {
					logger.debug("无法计算出" + pair + "第二级别斐波那契回撤信息");
					continue;
				}
				
				FibInfo secondFibInfo = new FibInfo(secondFibLowKlines, secondFibHightKlines, secondFibLowKlines.getDecimalNum(),FibLevel.LEVEL_2);
				
				//第二级别趋势
				QuotationMode secondQm = secondFibInfo.getQuotationMode();
				
				//第二级别斐波那契开仓
				switch (secondQm) {
				case LONG:
					klinesService.openLong(secondFibInfo, klinesList_hit);
					break;

				default:
					klinesService.openShort(secondFibInfo, klinesList_hit);
					break;
				}
				
				
				//开始获取第三级别斐波那契回撤信息
				Klines thirdFibHightKlines = null;
				Klines thirdFibLowKlines = null;
				//开始获取第三级斐波那契回撤
				switch (secondQm) {
				case LONG:
					thirdFibHightKlines = secondFibHightKlines;
					thirdFibLowKlines = PriceUtil.getLowKlinesByStartKlines(klinesList_365_x_day, thirdFibHightKlines);
					break;

				default:
					thirdFibLowKlines = secondFibLowKlines;
					thirdFibHightKlines = PriceUtil.getHightKlinesByStartKlines(klinesList_365_x_day, thirdFibLowKlines);
					break;
				}
				
				if(ObjectUtils.isEmpty(thirdFibHightKlines) || ObjectUtils.isEmpty(thirdFibLowKlines)) {
					logger.debug("无法计算出" + pair + "第三级别斐波那契回撤信息");
					continue;
				}
				
				FibInfo thirdFibInfo = new FibInfo(thirdFibLowKlines, thirdFibHightKlines, thirdFibLowKlines.getDecimalNum(),FibLevel.LEVEL_3);
				
				QuotationMode thirdQm = thirdFibInfo.getQuotationMode();
				
				//第三级别斐波那契开仓
				switch (thirdQm) {
				case LONG:
					klinesService.openLong(thirdFibInfo, klinesList_hit);
					break;

				default:
					klinesService.openShort(thirdFibInfo, klinesList_hit);
					break;
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
