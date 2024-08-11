package com.bugbycode.trading_app.task;

import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONArray;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.util.ObjectUtils;

import com.bugbycode.module.EmailInfo;
import com.bugbycode.module.Klines;
import com.bugbycode.service.KlinesService;
import com.util.EmailUtil;
import com.util.StringUtil;

/**
 * 永续合约监控
 */
//@Configuration
//@EnableScheduling
@Deprecated
public class BuyOrSellFuturesTradingTask {

	private final Logger logger = LogManager.getLogger(BuyOrSellFuturesTradingTask.class);

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
				//最近2根1天级别k线信息
				List<Klines> klinesList_2_x_1d = klinesService.continuousKlines_last_2_x_1d(pair, restBaseUrl);
				
				int index_last_15m = klinesList_10_x_15m.size() - 2;
				int index_last_1d = klinesList_2_x_1d.size() - 2;
				
				Klines kline_15m = klinesList_10_x_15m.get(index_last_15m);
				Klines kline_1d = klinesList_2_x_1d.get(index_last_1d);
				
				//15分钟开盘、收盘、最低、最高价格
				double closePrice_15m = kline_15m.getClosePrice();
				double openPrice_15m = kline_15m.getOpenPrice();
				double lowPrice_15m = kline_15m.getLowPrice();
				double hightPrice_15m = kline_15m.getHighPrice();
				//昨日开盘、收盘、最低、最高价格
				double closePrice_1d = kline_1d.getClosePrice();
				double openPrice_1d = kline_1d.getOpenPrice();
				double lowPrice_1d = kline_1d.getLowPrice();
				double hightPrice_1d = kline_1d.getHighPrice();
				
				EmailInfo emailInfo = null;
				
				if(kline_1d.isRise()) {//阳线
					if(kline_15m.isRise()) {//15分钟收阳
						//买入判断
						if(closePrice_15m >= openPrice_1d && openPrice_15m <= openPrice_1d) {
							emailInfo = StringUtil.formatBuyFuturesMessage(pair, closePrice_15m, openPrice_1d);
						} else if(closePrice_15m >= lowPrice_1d && openPrice_15m <= lowPrice_1d) {
							emailInfo = StringUtil.formatBuyFuturesMessage(pair, closePrice_15m, lowPrice_1d);
						}
						//卖出判断
						else if(closePrice_15m <= closePrice_1d && hightPrice_15m >= closePrice_1d) {
							emailInfo = StringUtil.formatSellFuturesMessage(pair, closePrice_15m, closePrice_1d);
						} else if(closePrice_15m <= hightPrice_1d && hightPrice_15m >= hightPrice_1d) {
							emailInfo = StringUtil.formatSellFuturesMessage(pair, closePrice_15m, hightPrice_1d);
						}
					} else if(kline_15m.isFall()) { //15分钟收阴
						//买入判断
						if(closePrice_15m >= openPrice_1d && lowPrice_15m <= openPrice_1d) {
							emailInfo = StringUtil.formatBuyFuturesMessage(pair, closePrice_15m, openPrice_1d);
						} else if(closePrice_15m >= lowPrice_1d && lowPrice_15m <= lowPrice_1d) {
							emailInfo = StringUtil.formatBuyFuturesMessage(pair, closePrice_15m, lowPrice_1d);
						}
						//卖出判断
						else if(closePrice_15m <= closePrice_1d && hightPrice_15m >= closePrice_1d) {
							emailInfo = StringUtil.formatSellFuturesMessage(pair, closePrice_15m, closePrice_1d);
						} else if(closePrice_15m <= hightPrice_1d && hightPrice_15m >= hightPrice_1d) {
							emailInfo = StringUtil.formatSellFuturesMessage(pair, closePrice_15m, hightPrice_1d);
						}
					}
				} else if(kline_1d.isFall()) {//阴线
					if(kline_15m.isRise()) {//15分钟收阳
						//买入判断
						if(closePrice_15m >= closePrice_1d && openPrice_15m <= closePrice_1d) {
							emailInfo = StringUtil.formatBuyFuturesMessage(pair, closePrice_15m, closePrice_1d);
						} else if(closePrice_15m >= lowPrice_1d && openPrice_15m <= lowPrice_1d) {
							emailInfo = StringUtil.formatBuyFuturesMessage(pair, closePrice_15m, lowPrice_1d);
						}
						//卖出判断
						else if(closePrice_15m <= openPrice_1d && hightPrice_15m >= openPrice_1d) {
							emailInfo = StringUtil.formatSellFuturesMessage(pair, closePrice_15m, openPrice_1d);
						} else if(closePrice_15m <= hightPrice_1d && hightPrice_15m >= hightPrice_1d) {
							emailInfo = StringUtil.formatSellFuturesMessage(pair, closePrice_15m, hightPrice_1d);
						}
					} else if(kline_15m.isFall()) { //15分钟收阴
						//买入判断
						if(closePrice_15m >= closePrice_1d && lowPrice_15m <= closePrice_1d) {
							emailInfo = StringUtil.formatBuyFuturesMessage(pair, closePrice_15m, closePrice_1d);
						} else if(closePrice_15m >= lowPrice_1d && lowPrice_15m <= lowPrice_1d) {
							emailInfo = StringUtil.formatBuyFuturesMessage(pair, closePrice_15m, lowPrice_1d);
						}
						//卖出判断
						else if(closePrice_15m <= openPrice_1d && hightPrice_15m >= openPrice_1d) {
							emailInfo = StringUtil.formatSellFuturesMessage(pair, closePrice_15m, openPrice_1d);
						} else if(closePrice_15m <= hightPrice_1d && hightPrice_15m >= hightPrice_1d) {
							emailInfo = StringUtil.formatSellFuturesMessage(pair, closePrice_15m, hightPrice_1d);
						}
					}
				}
				
				if(!ObjectUtils.isEmpty(emailInfo)) {
					EmailUtil.send(smtpHost, smtpPort, emailUserName, emailPassword, recipient, emailInfo.getSubject(), emailInfo.getText());
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			EmailUtil.send(smtpHost, smtpPort, emailUserName, emailPassword, recipient, "程序运行出现异常", e.getLocalizedMessage());
		}
	}
}
