package com.bugbycode.trading_app.task;

import java.util.Date;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONArray;
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
 * 永续合约开仓点位监控定时任务 
 * 周线级别
 */
//@Configuration
//@EnableScheduling
@Deprecated
public class FuturesTradingByLastDayTask {
	
	private final Logger logger = LogManager.getLogger(FuturesTradingByLastDayTask.class);

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
	//@Scheduled(cron = "5 0/15 * * * ?")
	public void continuousKlines() throws Exception {
		try {
			//pairs = "BTCUSDT";
			String[] pairArr = { pairs };
			
			if(pairs.contains(",")) {
				pairArr = pairs.split(",");
			}
			
			logger.info(new JSONArray(pairArr).toString());
			for(String pair : pairArr) {
				pair = pair.trim();
				if(StringUtil.isEmpty(pair)) {
					continue;
				}
				
				//最近7根15分钟级别k线信息
				List<Klines> klinesList_10_x_15m = klinesService.continuousKlines_last_10_x_15m(pair, restBaseUrl);
				
				int size = klinesList_10_x_15m.size();
				int lastIndex = size - 2;
				Klines currentKlines = klinesList_10_x_15m.get(lastIndex);//收盘K线信息
				double closePrice = currentKlines.getClosePrice();
				//Klines parentKlines = klinesList_10_x_15m.get(size - 3);//前一根k线
				//价格小数点保留个数
				int decimalNum = currentKlines.getDecimalNum();
				String text = "";//邮件内容
				String subject = "";//邮件主题
				
				//前3根K线信息
				Klines kline_0 = klinesList_10_x_15m.get(lastIndex - 1);
				Klines kline_1 = klinesList_10_x_15m.get(lastIndex - 2);
				Klines kline_2 = klinesList_10_x_15m.get(lastIndex - 3);
				
				//判断反转行情
				if(currentKlines.isFall() && kline_0.isRise() && kline_1.isRise() && kline_2.isRise()) {//即将下跌
					subject = pair + "永续合约做空机会 " + DateFormatUtil.format(new Date());
					text = String.format("当前%s交易对有做空的机会，多头仓位请做好止盈/止损，当前价格：%s", pair,PriceUtil.formatDoubleDecimal(closePrice,decimalNum));
				} else if(currentKlines.isRise() && kline_0.isFall() && kline_1.isFall() && kline_2.isFall()){//即将上涨
					subject = pair + "永续合约做多机会 " + DateFormatUtil.format(new Date());
					text = String.format("当前%s交易对有做多的机会，空头仓位请做好止盈/止损，当前价格：%s", pair,PriceUtil.formatDoubleDecimal(closePrice,decimalNum));
				}
				
				if(StringUtil.isNotEmpty(subject) && StringUtil.isNotEmpty(text)) {
					EmailUtil.send(smtpHost, smtpPort, emailUserName, emailPassword, recipient, subject, text);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			EmailUtil.send(smtpHost, smtpPort, emailUserName, emailPassword, recipient, "程序运行出现异常", e.getLocalizedMessage());
		}

	}
}
