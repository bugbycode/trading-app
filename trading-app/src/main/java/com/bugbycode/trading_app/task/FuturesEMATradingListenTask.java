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
import com.bugbycode.module.EMAType;
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
 * 永续合约EMA指标交易监听计划任务
 */
//@Configuration
//@EnableScheduling
public class FuturesEMATradingListenTask {

	private final Logger logger = LogManager.getLogger(FuturesEMATradingListenTask.class);
	
	@Autowired
	private KlinesService klinesService;
	
	/**
	 * 查询k线信息 每五分钟执行一次
	 * 
	 * @throws Exception
	 */
	//@Scheduled(cron = "4 0/15 * * * ?")
	public void continuousKlines() throws Exception {
		logger.info("FuturesFibTradingListenTask start.");
		
		Date now = new Date();
		
		try {
			
			for(String pair : AppConfig.PAIRS) {
				pair = pair.trim();
				if(StringUtil.isEmpty(pair)) {
					continue;
				}
				
				List<Klines> klinesList_hit = klinesService.continuousKlines15M(pair, now, 3 * 365, QUERY_SPLIT.NOT_ENDTIME);
				
				int size = klinesList_hit.size();
				
				PriceUtil.calculateEMAArray(klinesList_hit,EMAType.EMA7);
				PriceUtil.calculateEMAArray(klinesList_hit,EMAType.EMA25);
				PriceUtil.calculateEMAArray(klinesList_hit,EMAType.EMA99);
				
				Klines lastKlines = klinesList_hit.get(size - 1);
				
				String subject = "";
				String text = "";
				
				if(PriceUtil.isOpenLongEMA(klinesList_hit)) {
					subject = String.format("%s永续合约做多机会 %s", pair,DateFormatUtil.format(new Date()));
				} else if(PriceUtil.isOpenShortEMA(klinesList_hit)) {
					subject = String.format("%s永续合约做空机会 %s", pair,DateFormatUtil.format(new Date()));
				}
				
				text = lastKlines.toString();
				
				if(StringUtil.isNotEmpty(subject) && StringUtil.isNotEmpty(text)) {
					
					logger.info("邮件主题：" + subject);
					logger.info("邮件内容：" + text);
					
					Result<ResultCode, Exception> result = EmailUtil.send(subject, text);
					
					switch (result.getResult()) {
					case ERROR:
						
						Exception ex = result.getErr();
						
						logger.info("邮件发送失败！失败原因：" + ex.getLocalizedMessage());
						
						AppConfig.nexEmailAuth();
						
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
