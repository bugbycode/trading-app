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
 * 使用EMA移动平均值
 */
//@Configuration
//@EnableScheduling
@Deprecated
public class FuturesEMATask {
	
	private final Logger logger = LogManager.getLogger(FuturesEMATask.class);

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
	//@Scheduled(cron = "03 0/15 * * * ?")
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
				
				Klines currentKlines = klinesList_10_x_15m.get(size - 2);//收盘K线信息
				//Klines parentKlines = klinesList_10_x_15m.get(size - 3);//前一根k线
				//价格小数点保留个数
				int decimalNum = currentKlines.getDecimalNum();
				String text = "";//邮件内容
				String subject = "";//邮件主题
				
				double ema7 = PriceUtil.getEma7(klinesList_10_x_15m);
				//double ema7_parent = PriceUtil.getEma7_parent(klinesList_10_x_15m);
				double openPrice = currentKlines.getOpenPrice();//开盘价
				double closePrice = currentKlines.getClosePrice();//收盘价
				//double parentClosePrice = parentKlines.getClosePrice();
				
				if(closePrice < ema7 && openPrice > ema7) { //做空
					subject = pair + "永续合约做空机会 " + DateFormatUtil.format(new Date());
					text = String.format("当前%s交易对有做空的机会，多头仓位请做好止盈/止损，卖出价：%s，止损价：%s，EMA7：%s", pair,PriceUtil.formatDoubleDecimal(closePrice,decimalNum)
							,PriceUtil.formatDoubleDecimal(openPrice,decimalNum),PriceUtil.formatDoubleDecimal(ema7,decimalNum));
				} else if(closePrice > ema7 && openPrice < ema7){//做多
					subject = pair + "永续合约做多机会 " + DateFormatUtil.format(new Date());
					text = String.format("当前%s交易对有做多的机会，空头仓位请做好止赢/止损，买入价：%s，止损价：%s，EMA7：%s", pair,PriceUtil.formatDoubleDecimal(closePrice,decimalNum)
							,PriceUtil.formatDoubleDecimal(openPrice,decimalNum),PriceUtil.formatDoubleDecimal(ema7,decimalNum));
				}
				
				if(StringUtil.isNotEmpty(subject) && StringUtil.isNotEmpty(text)) {
					logger.info("邮件主题：" + subject);
					logger.info("邮件内容：" + text);
					//text += "\n\n" + String.format("PARENT_EMA：%s，EMA：%s", PriceUtil.formatDoubleDecimal(ema7_parent,decimalNum),PriceUtil.formatDoubleDecimal(ema7,decimalNum));
					EmailUtil.send(smtpHost, smtpPort, emailUserName, emailPassword, recipient, subject, text);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			EmailUtil.send(smtpHost, smtpPort, emailUserName, emailPassword, recipient, "程序运行出现异常", e.getLocalizedMessage());
		}
	}
}
