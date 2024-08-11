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

import com.bugbycode.module.FibInfo;
import com.bugbycode.module.Klines;
import com.bugbycode.service.KlinesService;
import com.util.DateFormatUtil;
import com.util.EmailUtil;
import com.util.PriceUtil;
import com.util.StringUtil;

/**
 * 永续合约开仓点位监控定时任务
 */
//@Configuration
//@EnableScheduling
@Deprecated
public class FuturesTask {

	private final Logger logger = LogManager.getLogger(FuturesTask.class);

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
	@Scheduled(cron = "5 0/15 * * * ?")
	public void continuousKlines() throws Exception {
		try {
			Date now = new Date();
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
				//价格小数点保留个数
				int decimalNum = 2;
				String text = "";//邮件内容
				String subject = "";//邮件主题
				List<Klines> klinesList_96_x_15m = klinesService.continuousKlines_last_day_15m(pair, restBaseUrl);
				Klines highKline_96_x_15m = PriceUtil.getMaxPriceKLine(klinesList_96_x_15m);//本周期最高价
				Klines lowKline_96_x_15m = PriceUtil.getMinPriceKLine(klinesList_96_x_15m);//本周期最低价
				decimalNum = highKline_96_x_15m.getDecimalNum();
				
				//获取最近1小时15分钟级别k线信息
				List<Klines> klinesList_4_x_15m = klinesService.continuousKlines_last_4_x_15m(pair, restBaseUrl);
				int klinesList_4_x_15m_size = klinesList_4_x_15m.size();
				Klines lastKlines = klinesList_4_x_15m.get(klinesList_4_x_15m_size - 1);//最后一根k线
				double closePrice = lastKlines.getClosePrice();
				double openPrice = lastKlines.getOpenPrice();
				double highPrice = lastKlines.getHighPrice();
				double lowPrice = lastKlines.getLowPrice();
				
				
				//1天为1个周期斐波那契回撤水平
				FibInfo fibInfo = new FibInfo(lowKline_96_x_15m, highKline_96_x_15m,decimalNum);
				double fib1 = fibInfo.getFib1();
				double fib236 = fibInfo.getFib236();
				double fib382 = fibInfo.getFib382();
				double fib5 = fibInfo.getFib5();
				double fib618 = fibInfo.getFib618();
				double fib66 = fibInfo.getFib66();
				double fib786 = fibInfo.getFib786();
				double fib0 = fibInfo.getFib0();
				
				//空头行情
				if(fib0 < fib1) {
					subject = pair + "永续合约做空机会 " + DateFormatUtil.format(new Date());
					if((highPrice >= fib66 || highPrice >= fib618 ) && (closePrice <= fib66 && closePrice > fib5)) {//0.618~0.66做空
						//理想止盈价 fib382
						//理想止损价 fib66
						text = StringUtil.formatShortMessage(pair, closePrice, fib382, fib66,decimalNum);
					} else if(closePrice <= fib786 && closePrice > fib66 && highPrice >= fib786) {//0.786做空 收盘在0.66~0.786
						//理想止盈价 fib382
						//理想止损价 fib786
						text = StringUtil.formatShortMessage(pair, closePrice, fib382, fib786,decimalNum);
					} else if(closePrice <= fib1 && closePrice > fib786 && highPrice >= fib1) {//收盘在1~0.786 开盘大于fib1 通常是上涨假突破fib1
						//理想止盈价 fib382
						//理想止损价 fib1
						text = StringUtil.formatShortMessage(pair, closePrice, fib382, fib1,decimalNum);
					}
					else if(closePrice >= fib0 && closePrice < fib236 && lowPrice <= fib0) {//空头行情做多的情况 触底反弹
						subject = pair + "永续合约做多机会 " + DateFormatUtil.format(new Date());
						//理想止盈价 fib618
						//理想止损价 lowPrice
						text = StringUtil.formatLongMessage(pair, closePrice, lowPrice, fib618,decimalNum);
					}
				} else if(fib0 > fib1) {//多头行情
					subject = pair + "永续合约做多机会 " + DateFormatUtil.format(new Date());
					if(closePrice > fib786 && closePrice < fib66 && lowPrice < fib786) { //0.786~0.66
						//理想止损价 fib786
						//理想止盈价 fib382
						text = StringUtil.formatLongMessage(pair, closePrice, fib786, fib382,decimalNum);
					} else if((lowPrice <= fib66 || lowPrice <= fib618) && (closePrice >= fib66 && closePrice < fib5)) { //0.66~0.618
						//理想止损价 fib66
						//理想止盈价 fib382
						text = StringUtil.formatLongMessage(pair, closePrice, fib66, fib382,decimalNum);
					} else if(closePrice >= fib1 && closePrice <= fib786 && lowPrice <= fib1) {//1~0.786 触底反弹
						//理想止损价 fib1
						//理想止盈价 fib382
						text = StringUtil.formatLongMessage(pair, closePrice, fib1, fib382,decimalNum);
					} else if(closePrice <= fib0 && closePrice > fib236 && highPrice >= fib0) {//多头行情做空的情况 假突破与触底反弹同理
						subject = pair + "永续合约做空机会 " + DateFormatUtil.format(new Date());
						//理想止损价 highPrice
						//理想止盈价 fib618
						text = StringUtil.formatShortMessage(pair, closePrice, fib618, highPrice,decimalNum);
					}
				}
				
				logger.info(pair + ": " + fibInfo.toString());
				
				if(StringUtil.isNotEmpty(subject) && StringUtil.isNotEmpty(text)) {
					
					text += "\n\nFib：" + fibInfo.toString();
					
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
