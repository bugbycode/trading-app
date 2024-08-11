package com.bugbycode.trading_app.task;


import org.apache.logging.log4j.Logger;
import org.json.JSONArray;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

import com.bugbycode.module.FibInfo;
import com.bugbycode.module.Klines;
import com.util.CommandUtil;
import com.util.DateFormatUtil;
import com.util.EmailUtil;
import com.util.PriceUtil;
import com.util.StringUtil;

/**
 * 比特币永续合约定时任务
 */
//@Configuration
//@EnableScheduling
@Deprecated
public class BTCUSDTFuturesClientTask{

	private final Logger logger = LogManager.getLogger(BTCUSDTFuturesClientTask.class);

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
	
	/**
	 * 查询k线信息
	 * 
	 * @throws Exception
	 */
	//@Scheduled(cron = "3 0 * * * ?")
	public void continuousKlines() throws Exception {

		try {
			//pairs = "BTCUSDT";
			Date now = new Date();
			String[] pairArr = { pairs };
			
			if(pairs.contains(",")) {
				pairArr = pairs.split(",");
			}
			
			logger.info(new JSONArray(pairArr).toString());
			
			for(String pair : pairArr) {
				//时辰
				int now_day_of_hours = DateFormatUtil.getHours(now.getTime());
				if(now_day_of_hours == 0) {
					now_day_of_hours = 23;
				} else {
					now_day_of_hours = now_day_of_hours - 1;
				}
				
				//上一小时k线数据下标
				int rise_fall_offset = 0; 
				
				String command = String.format("curl -G -d 'limit=180&pair=%s&contractType=PERPETUAL"
						+ "&interval=1d' %s/fapi/v1/continuousKlines", pair, restBaseUrl);
				
				logger.info(command);
				
				String result = CommandUtil.run(command);
				
				//logger.info(result);
				
				List<Klines> klinesList = new ArrayList<Klines>();
				
				int decimalNum = 0;//价格小数点的数量
				
				if(StringUtil.isNotEmpty(result)) {
					
					JSONArray jsonArr = new JSONArray(result);
					
					for(int index = 0;index < jsonArr.length();index++) {
						
						JSONArray klJson = jsonArr.getJSONArray(index);
						decimalNum = klJson.getString(1).substring(klJson.getString(1).indexOf(".") + 1).length();
						Klines kl = new Klines(pair,klJson.getLong(0),
								klJson.getDouble(1),klJson.getDouble(2),
								klJson.getDouble(3),klJson.getDouble(4),
								klJson.getLong(6),decimalNum);
						//logger.info("永续合约" + kl.toString());
						klinesList.add(kl);
						
						if(now_day_of_hours == DateFormatUtil.getHours(kl.getStarTime())) {
							rise_fall_offset = index;
						}
					};
				}
				
				int klinesSize = klinesList.size();
				
				//==========================================

				String text = "";
				String subject = "";
				
				if(klinesSize > 0) {
					Klines highKline = PriceUtil.getMaxPriceKLine(klinesList);//本周期最高价
					Klines lowKline = PriceUtil.getMinPriceKLine(klinesList);//本周期最低价
					
					//logger.info(lowKline.toString());
					
					FibInfo fibInfo = new FibInfo(lowKline, highKline,decimalNum);
					Klines currentKline = klinesList.get(rise_fall_offset);//前一小时K线
					double fib1 = fibInfo.getFib1();
					double fib236 = fibInfo.getFib236();
					double fib382 = fibInfo.getFib382();
					double fib5 = fibInfo.getFib5();
					double fib618 = fibInfo.getFib618();
					double fib66 = fibInfo.getFib66();
					double fib786 = fibInfo.getFib786();
					double fib0 = fibInfo.getFib0();
					
					double currentPrice = klinesList.get(rise_fall_offset).getClosePrice();//前一小时K线收盘价
					
					//空头行情
					if(fib0 < fib1) {
						subject = pair + "永续合约做空机会 " + DateFormatUtil.format(new Date());
						//做空的情况
						if(currentPrice < fib236 && currentKline.getHighPrice() > fib236) {//0.236
							//理想止损价 fib236
							//理想止盈价 fib0
							text = StringUtil.formatShortMessage(pair, currentPrice, fib0, fib236,decimalNum);
						} else if(currentPrice < fib382 && currentKline.getHighPrice() > fib382) {//0.382
							//理想止损价 fib382
							//理想止盈价 fib236
							text = StringUtil.formatShortMessage(pair, currentPrice, fib236, fib382,decimalNum);
						} else if(currentPrice < fib5 && currentKline.getHighPrice() > fib5) {//0.5
							//理想止损价 fib5
							//理想止盈价 fib236
							text = StringUtil.formatShortMessage(pair, currentPrice, fib236, fib5,decimalNum);
						} else if(currentPrice < fib618 && currentKline.getHighPrice() > fib618) {//0.618
							//理想止损价 fib66
							//理想止盈价 fib236
							text = StringUtil.formatShortMessage(pair, currentPrice, fib236, fib66,decimalNum);
						} else if(currentPrice < fib66 && currentKline.getHighPrice() > fib66) {//0.66
							//理想止损价 fib66
							//理想止盈价 fib236
							text = StringUtil.formatShortMessage(pair, currentPrice, fib236, fib66,decimalNum);
						} else if(currentPrice < fib786 && currentKline.getHighPrice() > fib786) {//0.786
							//理想止损价 fib786
							//理想止盈价 fib236
							text = StringUtil.formatShortMessage(pair, currentPrice, fib236, fib786,decimalNum);
						}
					} else if(fib0 > fib1) {//多头行情
						
						subject = pair + "永续合约做多机会 " + DateFormatUtil.format(new Date());
						
						if(currentPrice > fib382 && currentKline.getLowPrice() < fib382) {
							//理想止盈价 fib0
							//理想止损价 fib382
							text = StringUtil.formatLongMessage(pair, currentPrice, fib382, fib236,decimalNum);
						} else if(currentPrice > fib5 && currentKline.getLowPrice() < fib5) {
							//理想止盈价 fib382
							//理想止损价 fib5
							text = StringUtil.formatLongMessage(pair, currentPrice, fib5, fib236,decimalNum);
						} else if(currentPrice > fib618 && currentKline.getLowPrice() < fib618) {
							//理想止盈价 fib382
							//理想止损价 fib66
							text = StringUtil.formatLongMessage(pair, currentPrice, fib66, fib236,decimalNum);
						} else if(currentPrice > fib66 && currentKline.getLowPrice() < fib66) {
							//理想止盈价 fib382
							//理想止损价 fib66
							text = StringUtil.formatLongMessage(pair, currentPrice, fib66, fib236,decimalNum);
						} else if(currentPrice > fib786 && currentKline.getLowPrice() < fib786) {
							//理想止盈价 fib382
							//理想止损价 fib786
							text = StringUtil.formatLongMessage(pair, currentPrice, fib786, fib236,decimalNum);
						}
					}
					
					if(StringUtil.isNotEmpty(subject) && StringUtil.isNotEmpty(text)) {
						
						text += "\n\n斐波那契回撤水平：" + fibInfo.toString();
						
						logger.info("邮件主题：" + subject);
						logger.info("邮件内容：" + text);
						
						EmailUtil.send(smtpHost, smtpPort, emailUserName, emailPassword, recipient, subject, text);
					}
				}
				Thread.sleep(1000);
			}
			
		} catch (Exception e) {
			e.printStackTrace();
			EmailUtil.send(smtpHost, smtpPort, emailUserName, emailPassword, recipient, "程序运行出现异常", e.getLocalizedMessage());
		}
	}

}
