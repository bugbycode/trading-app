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
import com.bugbycode.module.Klines;
import com.bugbycode.service.KlinesService;
import com.util.DateFormatUtil;
import com.util.EmailUtil;
import com.util.StringUtil;

/**
 * 标志性高低价点位监听
 */
//@Configuration
//@EnableScheduling
@Deprecated
public class LconicHighAndLowPricesListenTask {

	private final Logger logger = LogManager.getLogger(LconicHighAndLowPricesListenTask.class);
	
	@Autowired
	private KlinesService klinesService;
	
	/**
	 * 查询k线信息
	 * 
	 * @throws Exception
	 */
	//@Scheduled(cron = "5 0/15 * * * ?")
	public void continuousKlines() throws Exception {
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
				Date oneYearAgo = DateFormatUtil.getStartTimeBySetDay(lastDayStartTimeDate, -365);//365天以前起始时间
				
				//365天K线信息
				List<Klines> klinesList_x_365day = klinesService.continuousKlines(pair, restBaseUrl, oneYearAgo.getTime(), 
						lastDayEndTimeDate.getTime(), AppConfig.INERVAL_1D);
				
				//15分钟级别K线起止时间
				Date endTime_15m = DateFormatUtil.parse(DateFormatUtil.format_yyyy_mm_dd_HH_mm_00(now));
				Date startTime_15m = DateFormatUtil.getStartTimeBySetMinute(endTime_15m, -15 * 4);//60分钟以前开盘时间
				endTime_15m = DateFormatUtil.getStartTimeBySetSecond(endTime_15m, -1);//收盘时间
				
				//最近4根15分钟级别k线信息
				List<Klines> klinesList_4_x_15m = klinesService.continuousKlines(pair, restBaseUrl, startTime_15m.getTime(),
						endTime_15m.getTime(), AppConfig.INERVAL_15M);
				
				Klines last_15m_klines = klinesList_4_x_15m.get(klinesList_4_x_15m.size() - 1);
				
				//标志性高点K线信息
				List<Klines> lconicHighPriceList = new ArrayList<Klines>();
				//标志性低点K线信息
				List<Klines> lconicLowPriceList = new ArrayList<Klines>();
				/*
				for(int index_365day = 1;index_365day < klinesList_x_365day.size() - 1;index_365day++) {
					Klines klines_1day = klinesList_x_365day.get(index_365day);
					Klines klines_1day_parent = klinesList_x_365day.get(index_365day - 1);
					Klines klines_1day_after = klinesList_x_365day.get(index_365day + 1);
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
				}*/
				
				//获取标志性高低点K线信息
				int index_365day = klinesList_x_365day.size() - 2;
				while(index_365day >= 1) {
					Klines klines_1day = klinesList_x_365day.get(index_365day);
					Klines klines_1day_parent = klinesList_x_365day.get(index_365day - 1);
					Klines klines_1day_after = klinesList_x_365day.get(index_365day + 1);
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
				
				//logger.info(lconicHighPriceList.toString());
				//logger.info(lconicLowPriceList.toString());
				
				double maxLconicHighPrice = 0;
				//判断是否突破标志性高点
				for(int index = 0;index < lconicHighPriceList.size();index++) {
					Klines klines = lconicHighPriceList.get(index);
					if(klines.getHighPrice() >= maxLconicHighPrice) {
						maxLconicHighPrice = klines.getHighPrice();
					}
					//价格突破高点行为 最低价小于标志性高点 最高价大于标志性高点
					if(last_15m_klines.getLowPrice() <= klines.getHighPrice() && last_15m_klines.getHighPrice() >= klines.getHighPrice()
							&& klines.getHighPrice() >= maxLconicHighPrice) {
						//假突破 收盘价小于标志性高点
						if(last_15m_klines.getClosePrice() <= klines.getHighPrice()) {
							subject = pair + "永续合约价格突破" + klines.getHighPrice() + "并收回 " + DateFormatUtil.format(new Date());
							break;
						}//真突破 收盘价大于标志性高点
						else if(last_15m_klines.getClosePrice() > klines.getHighPrice()) {
							subject = pair + "永续合约价格突破" + klines.getHighPrice() + " " + DateFormatUtil.format(new Date());
							break;
						}
					}
				}
				
				//判断是否跌破标志性低点
				if(StringUtil.isEmpty(subject)) {
					double minLconicLowPrice = 0;
					for(int index = 0;index < lconicLowPriceList.size();index++) {
						Klines klines = lconicLowPriceList.get(index);
						
						if(klines.getLowPrice() <= minLconicLowPrice || minLconicLowPrice == 0) {
							minLconicLowPrice = klines.getLowPrice();
						}
						
						//价格跌破低点行为 最低价小于标志性低点 最高价大于标志性低点
						if(last_15m_klines.getLowPrice() <= klines.getLowPrice() && last_15m_klines.getHighPrice() >= klines.getLowPrice()
								&& klines.getLowPrice() <= minLconicLowPrice) {
							//假跌破 收盘价大于标志性低点
							if(last_15m_klines.getClosePrice() >= klines.getLowPrice()) {
								subject = pair + "永续合约价格跌破" + klines.getLowPrice() + "并收回 " + DateFormatUtil.format(new Date());
								break;
							}
							//真跌破 收盘价小于标志性低点
							else if(last_15m_klines.getClosePrice() < klines.getLowPrice()) {
								subject = pair + "永续合约价格跌破" + klines.getLowPrice() + " " + DateFormatUtil.format(new Date());
								break;
							}
						}
					}
				}
				
				text = last_15m_klines.toString() + "，当前价格：" + last_15m_klines.getClosePrice();
				
				if(StringUtil.isNotEmpty(subject) && StringUtil.isNotEmpty(text)) {
					logger.info("邮件主题：" + subject);
					logger.info("邮件内容：" + text);
					EmailUtil.send(smtpHost, smtpPort, emailUserName, emailPassword, recipient, subject, text);
				}
			}
			
		} catch (Exception e) {
			e.printStackTrace();
			EmailUtil.send(smtpHost, smtpPort, emailUserName, emailPassword, recipient, "程序运行出现异常", e.getLocalizedMessage());
		} finally {
			logger.info("LconicHighAndLowPricesListenTask run finish.");
		}
	}
}
