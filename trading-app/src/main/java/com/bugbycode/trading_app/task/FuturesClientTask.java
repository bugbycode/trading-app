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
 * 永续合约消息提示定时任务
 */
//@Configuration
//@EnableScheduling
@Deprecated
public class FuturesClientTask {

	private final Logger logger = LogManager.getLogger(FuturesClientTask.class);

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
	//@Scheduled(cron = "3 0 * * * ?")
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
				//时辰
				int now_day_of_hours = DateFormatUtil.getHours(now.getTime());
				if(now_day_of_hours == 0) {
					now_day_of_hours = 23;
				} else {
					now_day_of_hours = now_day_of_hours - 1;
				}
				
				String text = "";//邮件内容
				String subject = "";//邮件主题
				
				//上一小时k线数据下标
				int rise_fall_offset = 0;
				//价格小数点保留个数
				int decimalNum = 2;
				
				//180天k线信息
				List<Klines> klinesList_180d = klinesService.continuousKlines_180d(pair, restBaseUrl);
				//3小时K线
				List<Klines> klinesList_3h = klinesService.continuousKlines_3h(pair, restBaseUrl);
				
				int klinesSize_180d = klinesList_180d.size();
				int klinesSize_3h = klinesList_3h.size();
				
				for(int index = 0;index < klinesSize_3h;index++) {
					Klines kl = klinesList_3h.get(index);
					if(now_day_of_hours == DateFormatUtil.getHours(kl.getStarTime())) {
						rise_fall_offset = index;
						break;
					}
				}
				
				if(klinesSize_180d > 0) {
					Klines highKline_180d = PriceUtil.getMaxPriceKLine(klinesList_180d);//本周期最高价
					Klines lowKline_180d = PriceUtil.getMinPriceKLine(klinesList_180d);//本周期最低价
					Klines currentKline = klinesList_3h.get(rise_fall_offset);//前一小时K线
					double currentPrice = klinesList_3h.get(rise_fall_offset).getClosePrice();//前一小时K线收盘价
					
					decimalNum = lowKline_180d.getDecimalNum();
					
					//180天为1个周期斐波那契回撤水平
					FibInfo fibInfo = new FibInfo(lowKline_180d, highKline_180d,decimalNum);
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
							//理想止盈价 fib382
							text = StringUtil.formatShortMessage(pair, currentPrice, fib382, fib5,decimalNum);
						} else if(currentPrice < fib618 && currentKline.getHighPrice() > fib618) {//0.618
							//理想止损价 fib66
							//理想止盈价 fib5
							text = StringUtil.formatShortMessage(pair, currentPrice, fib5, fib66,decimalNum);
						} else if(currentPrice < fib66 && currentKline.getHighPrice() > fib66) {//0.66
							//理想止损价 fib66
							//理想止盈价 fib5
							text = StringUtil.formatShortMessage(pair, currentPrice, fib5, fib66,decimalNum);
						} else if(currentPrice < fib786 && currentKline.getHighPrice() > fib786) {//0.786
							//理想止损价 fib786
							//理想止盈价 fib5
							text = StringUtil.formatShortMessage(pair, currentPrice, fib5, fib786,decimalNum);
						}
						//以下是空头行情中考虑做多的情况
						else if(currentPrice > fib236 && currentKline.getLowPrice() < fib236) {
							//止损fib236
							//止盈fib382
							subject = pair + "永续合约做多机会 " + DateFormatUtil.format(new Date());
							text = StringUtil.formatLongMessage(pair, currentPrice, fib236, fib382,decimalNum);
						} else if(currentPrice > fib382 && currentKline.getLowPrice() < fib382) {
							//止损fib382
							//止盈fib5
							subject = pair + "永续合约做多机会 " + DateFormatUtil.format(new Date());
							text = StringUtil.formatLongMessage(pair, currentPrice, fib382, fib5,decimalNum);
						} else if(currentPrice > fib5 && currentKline.getLowPrice() < fib5) {
							//止损fib5
							//止盈fib618
							subject = pair + "永续合约做多机会 " + DateFormatUtil.format(new Date());
							text = StringUtil.formatLongMessage(pair, currentPrice, fib5, fib618,decimalNum);
						} else if(currentPrice > fib618 && currentKline.getLowPrice() < fib618) {
							//止损fib618
							//止盈fib786
							subject = pair + "永续合约做多机会 " + DateFormatUtil.format(new Date());
							text = StringUtil.formatLongMessage(pair, currentPrice, fib618, fib786,decimalNum);
						} else if(currentPrice > fib66 && currentKline.getLowPrice() < fib66) {
							//止损fib618
							//止盈fib786
							subject = pair + "永续合约做多机会 " + DateFormatUtil.format(new Date());
							text = StringUtil.formatLongMessage(pair, currentPrice, fib618, fib786,decimalNum);
						} else if(currentPrice > fib786 && currentKline.getLowPrice() < fib786) {
							//止损fib786
							//止盈fib1
							subject = pair + "永续合约做多机会 " + DateFormatUtil.format(new Date());
							text = StringUtil.formatLongMessage(pair, currentPrice, fib786, fib1,decimalNum);
						}
						//行情新低后出现买盘行为
						else if((//创出新低后的阳线
								klinesList_3h.get(rise_fall_offset).getLowPrice() == fib0
								&& klinesList_3h.get(rise_fall_offset).isRise()
								) || ( //前一根k线为新低 并且当前k线为阳线
								klinesList_3h.get(rise_fall_offset - 1).getLowPrice() == fib0)
								&& klinesList_3h.get(rise_fall_offset).isRise()) {
							//止损 fib0
							//止盈 fib236
							subject = pair + "永续合约做多机会 " + DateFormatUtil.format(new Date());
							text = StringUtil.formatLongMessage(pair, currentPrice, fib0, fib236,decimalNum);
						}
						
					} else if(fib0 > fib1) {//多头行情
						
						subject = pair + "永续合约做多机会 " + DateFormatUtil.format(new Date());
						
						if(currentPrice > fib236 && currentKline.getLowPrice() < fib236) {
							//理想止盈价 fib0
							//理想止损价 fib236
							text = StringUtil.formatLongMessage(pair, currentPrice, fib236, fib0,decimalNum);
						} else if(currentPrice > fib382 && currentKline.getLowPrice() < fib382) {
							//理想止盈价 fib236
							//理想止损价 fib382
							text = StringUtil.formatLongMessage(pair, currentPrice, fib382, fib236,decimalNum);
						} else if(currentPrice > fib5 && currentKline.getLowPrice() < fib5) {
							//理想止盈价 fib382
							//理想止损价 fib5
							text = StringUtil.formatLongMessage(pair, currentPrice, fib5, fib382,decimalNum);
						} else if(currentPrice > fib618 && currentKline.getLowPrice() < fib618) {
							//理想止盈价 fib5
							//理想止损价 fib66
							text = StringUtil.formatLongMessage(pair, currentPrice, fib66, fib5,decimalNum);
						} else if(currentPrice > fib66 && currentKline.getLowPrice() < fib66) {
							//理想止盈价 fib5
							//理想止损价 fib66
							text = StringUtil.formatLongMessage(pair, currentPrice, fib66, fib5,decimalNum);
						} else if(currentPrice > fib786 && currentKline.getLowPrice() < fib786) {
							//理想止盈价 fib5
							//理想止损价 fib786
							text = StringUtil.formatLongMessage(pair, currentPrice, fib786, fib5,decimalNum);
						}
						//以下是多头行情中考虑做空的情况
						else if(currentPrice < fib786 && currentKline.getHighPrice() > fib786) {
							//理想止盈价 fib1
							//理想止损价 fib786
							subject = pair + "永续合约做空机会 " + DateFormatUtil.format(new Date());
							text = StringUtil.formatShortMessage(pair, currentPrice, fib1, fib786,decimalNum);
						} else if(currentPrice < fib618 && currentKline.getHighPrice() > fib618) {
							//理想止盈价 fib786
							//理想止损价 fib618
							subject = pair + "永续合约做空机会 " + DateFormatUtil.format(new Date());
							text = StringUtil.formatShortMessage(pair, currentPrice, fib786, fib618,decimalNum);
						} else if(currentPrice < fib66 && currentKline.getHighPrice() > fib66) {
							//理想止盈价 fib786
							//理想止损价 fib618
							subject = pair + "永续合约做空机会 " + DateFormatUtil.format(new Date());
							text = StringUtil.formatShortMessage(pair, currentPrice, fib786, fib618,decimalNum);
						} else if(currentPrice < fib5 && currentKline.getHighPrice() > fib5) {
							//理想止盈价 fib618
							//理想止损价 fib5
							subject = pair + "永续合约做空机会 " + DateFormatUtil.format(new Date());
							text = StringUtil.formatShortMessage(pair, currentPrice, fib618, fib5,decimalNum);
						} else if(currentPrice < fib382 && currentKline.getHighPrice() > fib382) {
							//理想止盈价 fib5
							//理想止损价 fib382
							subject = pair + "永续合约做空机会 " + DateFormatUtil.format(new Date());
							text = StringUtil.formatShortMessage(pair, currentPrice, fib5, fib382,decimalNum);
						} else if(currentPrice < fib236 && currentKline.getHighPrice() > fib236) {
							//理想止盈价 fib382
							//理想止损价 fib236
							subject = pair + "永续合约做空机会 " + DateFormatUtil.format(new Date());
							text = StringUtil.formatShortMessage(pair, currentPrice, fib382, fib236,decimalNum);
						}
						
						//多头行情中价格新高后出现抛售行为
						else if((//当前K线为ATH 并且为阴线
								klinesList_3h.get(rise_fall_offset).getHighPrice() == fib0 
								&& klinesList_3h.get(rise_fall_offset).isFall()
								) || (
										//当前k线收阴线并且前一根k线为新高的k线
										klinesList_3h.get(rise_fall_offset).isFall()
										&& klinesList_3h.get(rise_fall_offset - 1).getHighPrice() == fib0
										)) {
							//理想止盈价 fib236
							//理想止损价 fib0
							subject = pair + "永续合约做空机会 " + DateFormatUtil.format(new Date());
							text = StringUtil.formatShortMessage(pair, currentPrice, fib236, fib0,decimalNum);
						}
					}
					
					//logger.info("Fib：" + fibInfo.toString());
					
					if(StringUtil.isNotEmpty(subject) && StringUtil.isNotEmpty(text)) {
						
						text += "\n\nFib：" + fibInfo.toString();
						
						logger.info("邮件主题：" + subject);
						logger.info("邮件内容：" + text);
						
						EmailUtil.send(smtpHost, smtpPort, emailUserName, emailPassword, recipient, subject, text);
					} /*else {
						
						if(klinesList_3h.get(rise_fall_offset).isRise() && klinesList_3h.get(rise_fall_offset-1).isRise()
								&& klinesList_3h.get(rise_fall_offset-2).isRise()) {
							
							subject = pair + "永续合约持续上涨" + DateFormatUtil.format(new Date());
							text =  pair + "永续合约持续上涨，请注意风险";
							text += "\n\nFib：" + fibInfo.toString();
							
							logger.info("邮件主题：" + subject);
							logger.info("邮件内容：" + text);
							
							EmailUtil.send(smtpHost, smtpPort, emailUserName, emailPassword, recipient, subject, text);
							
						} else if(klinesList_3h.get(rise_fall_offset).isFall() && klinesList_3h.get(rise_fall_offset-1).isFall()
								&& klinesList_3h.get(rise_fall_offset-2).isFall()) {
							
							subject = pair + "永续合约持续下跌" + DateFormatUtil.format(new Date());
							text =  pair + "永续合约持续下跌，请注意风险";
							text += "\n\nFib：" + fibInfo.toString();
							
							logger.info("邮件主题：" + subject);
							logger.info("邮件内容：" + text);
							
							EmailUtil.send(smtpHost, smtpPort, emailUserName, emailPassword, recipient, subject, text);
							
						}
					}*/
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			EmailUtil.send(smtpHost, smtpPort, emailUserName, emailPassword, recipient, "程序运行出现异常", e.getLocalizedMessage());
		}
	}
}
