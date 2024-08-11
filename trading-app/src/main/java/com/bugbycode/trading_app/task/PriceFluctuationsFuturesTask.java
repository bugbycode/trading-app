package com.bugbycode.trading_app.task;

import java.util.Date;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

import com.bugbycode.module.Klines;
import com.bugbycode.service.KlinesService;
import com.util.DateFormatUtil;
import com.util.EmailUtil;
import com.util.PriceUtil;
import com.util.StringUtil;

/**
 * 15分钟价格波动监控
 */
//@Configuration
//@EnableScheduling
@Deprecated
public class PriceFluctuationsFuturesTask {
	
	private final Logger logger = LogManager.getLogger(PriceFluctuationsFuturesTask.class);

	@Value("${binance.baseUrl.rest}")
	private String restBaseUrl;
	
	@Value("${binance.pair}")
	private String pairs;
	
	@Value("${email.auth.user}")
	private String emailUserName;//发件人
	
	@Value("${email.auth.password}")
	private String emailPassword;//密码
	
	@Value("${email.smtp.host}")
	private String smtpHost;//服务器
	
	@Value("${email.smtp.port}")
	private int smtpPort;//端口
	
	@Value("${email.recipient}")
	private String recipient;//收件人
	
	@Autowired
	private KlinesService klinesService;
	
	/**
	 * 查询k线信息
	 * 
	 * @throws Exception
	 */
	//@Scheduled(cron = "4 0/15 * * * ?")
	public void continuousKlines() throws Exception {
		
		Date now = new Date();
		
		try {
			
			Date endTime = DateFormatUtil.parse(DateFormatUtil.format_yyyy_mm_dd_HH_mm_00(now));
			Date startTime = DateFormatUtil.getStartTimeBySetMinute(endTime, -15 * 11);
			endTime = DateFormatUtil.getStartTimeBySetSecond(endTime, -1);
			
			String[] pairArr = { pairs };
			
			if(pairs.contains(",")) {
				pairArr = pairs.split(",");
			}
			
			//logger.info(new JSONArray(pairArr).toString());
			for(String pair : pairArr) {
				pair = pair.trim();
				if(StringUtil.isEmpty(pair)) {
					continue;
				}
				
				//最近10根15分钟级别k线信息
				List<Klines> klinesList_x_15m = klinesService.continuousKlines_last_15m(pair, restBaseUrl,startTime.getTime(),endTime.getTime());
				
				//logger.info(klinesList_x_15m.toString());
				
				int size = klinesList_x_15m.size();
				int lastIndex = size - 1;
				int currentIndex = lastIndex; //size - 2;
				Klines lastKlines = klinesList_x_15m.get(lastIndex);
				Klines currentKlines = klinesList_x_15m.get(currentIndex--);
				//Klines parrentKlines = klinesList_x_15m.get(currentIndex--);
				//Klines parrentKlines1 = klinesList_x_15m.get(currentIndex--);
				//Klines parrentKlines2 = klinesList_x_15m.get(currentIndex--);
				
				
				//String percentageStr = PriceUtil.formatDoubleDecimal(currentKlines.getPriceFluctuationPercentage(), 2);
				String percentageStr = PriceUtil.formatDoubleDecimal(PriceUtil.getPriceFluctuationPercentage(klinesList_x_15m, lastIndex), 2);
				
				double pricePercentage = Double.valueOf(percentageStr);
				
				String text = "";//邮件内容
				String subject = "";//邮件主题
				
				if(currentKlines.isFall()) {//下跌
					
					if(pair.equals("BTCUSDT") || pair.equals("ETHUSDT")) {
						if(pricePercentage >= 5) {
							subject = pair + "永续合约价格大暴跌 " + DateFormatUtil.format(new Date());
						} else if(pricePercentage >= 3) {
							subject = pair + "永续合约价格暴跌 " + DateFormatUtil.format(new Date());
						}else if(pricePercentage >= 1.5) {
							subject = pair + "永续合约价格大跌 " + DateFormatUtil.format(new Date());
						}
					} else {
						if(pricePercentage >= 10) {
							subject = pair + "永续合约价格大暴跌 " + DateFormatUtil.format(new Date());
						} else if(pricePercentage >= 5) {
							subject = pair + "永续合约价格暴跌 " + DateFormatUtil.format(new Date());
						}else if(pricePercentage >= 3) {
							subject = pair + "永续合约价格大跌 " + DateFormatUtil.format(new Date());
						}
					}
					
				} else if(currentKlines.isRise()) {
					if(pair.equals("BTCUSDT") || pair.equals("ETHUSDT")) {
						if(pricePercentage >= 5) {
							subject = pair + "永续合约价格大暴涨 " + DateFormatUtil.format(new Date());
						} else if(pricePercentage >= 3) {
							subject = pair + "永续合约价格暴涨 " + DateFormatUtil.format(new Date());
						}else if(pricePercentage >= 1.5) {
							subject = pair + "永续合约价格大涨 " + DateFormatUtil.format(new Date());
						}
					} else {
						if(pricePercentage >= 10) {
							subject = pair + "永续合约价格大暴涨 " + DateFormatUtil.format(new Date());
						} else if(pricePercentage >= 5) {
							subject = pair + "永续合约价格暴涨 " + DateFormatUtil.format(new Date());
						}else if(pricePercentage >= 3) {
							subject = pair + "永续合约价格大涨 " + DateFormatUtil.format(new Date());
						}
					}
				}
				
				text = currentKlines.toString() + "，当前价格：" + lastKlines.getClosePrice() + "，k线连续振幅：" + percentageStr + "%";
				
				logger.info(currentKlines.toString());
				
				if(StringUtil.isNotEmpty(subject) && StringUtil.isNotEmpty(text)) {
					logger.info("邮件主题：" + subject);
					logger.info("邮件内容：" + text);
					EmailUtil.send(smtpHost, smtpPort, emailUserName, emailPassword, recipient, subject, text);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			EmailUtil.send(smtpHost, smtpPort, emailUserName, emailPassword, recipient, "程序运行出现异常", e.getLocalizedMessage());
		}
	}
}
