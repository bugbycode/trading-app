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
import com.util.StringUtil;

/**
 * 合约价格转折点监控
 */
//@Configuration
//@EnableScheduling
@Deprecated
public class FuturesPriceTransitionTask {

	private final Logger logger = LogManager.getLogger(FuturesPriceTransitionTask.class);

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
	//@Scheduled(cron = "8 0/15 * * * ?")
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
				int lastIndex = size - 1;
				int currentIndex = size - 2;
				Klines currentKlines = klinesList_10_x_15m.get(currentIndex--);
				Klines parrentKlines = klinesList_10_x_15m.get(currentIndex--);
				Klines parrentKlines1 = klinesList_10_x_15m.get(currentIndex--);
				Klines parrentKlines2 = klinesList_10_x_15m.get(currentIndex--);
				
				String text = "";//邮件内容
				String subject = "";//邮件主题
				
				if(parrentKlines1.isRise() && parrentKlines.isRise() && currentKlines.isUplead()) { //前一根k线是阳线并且当前k线出现上引线 表示上涨出现衰竭
					
					subject = pair + "永续合约做空机会 " + DateFormatUtil.format(new Date());
					
					text = String.format("当前%s交易对有做空的机会，多头仓位请做好止盈/止损，卖出价：%s",pair,klinesList_10_x_15m.get(lastIndex).getClosePrice());
				
				} else if(parrentKlines1.isFall() && parrentKlines.isFall() && currentKlines.isDownlead()) { //前一根k线是阴线并且当前k线出现下引线 表示下跌出现衰竭
					
					subject = pair + "永续合约做多机会 " + DateFormatUtil.format(new Date());
					
					text = String.format("当前%s交易对有做多的机会，空头仓位请做好止赢/止损，买入价：%s", pair,klinesList_10_x_15m.get(lastIndex).getClosePrice());
				
				} else if(currentKlines.isFall() && parrentKlines.isFall() && parrentKlines1.isFall()) {//连续下跌
					
					subject = pair + "永续合约持续下跌 " + DateFormatUtil.format(new Date());
					
					text = String.format("当前%s交易对价格持续下跌，当前价格：%s", pair,klinesList_10_x_15m.get(lastIndex).getClosePrice());
					
				} else if(currentKlines.isRise() && parrentKlines.isRise() && parrentKlines1.isRise()) {//连续上涨
					
					subject = pair + "永续合约持续上涨 " + DateFormatUtil.format(new Date());
					
					text = String.format("当前%s交易对价格持续上涨，当前价格：%s", pair,klinesList_10_x_15m.get(lastIndex).getClosePrice());
				
				} else if(currentKlines.isFall() && parrentKlines.isRise() && parrentKlines1.isRise() && parrentKlines2.isRise()) {//连续上涨中断
					
					subject = pair + "永续合约持续上涨中断 " + DateFormatUtil.format(new Date());
					
					text = String.format("当前%s交易对价格持续上涨中断，当前价格：%s", pair,klinesList_10_x_15m.get(lastIndex).getClosePrice());
				
				} else if(currentKlines.isRise() && parrentKlines.isFall() && parrentKlines1.isFall() && parrentKlines2.isFall()) {//连续下跌中断
					
					subject = pair + "永续合约持续下跌中断 " + DateFormatUtil.format(new Date());
					
					text = String.format("当前%s交易对价格持续下跌中断，当前价格：%s", pair,klinesList_10_x_15m.get(lastIndex).getClosePrice());
				}
				
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
