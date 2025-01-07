package com.bugbycode.trading_app.task.crossUnPnl;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

import com.bugbycode.binance.trade.rest.BinanceRestTradeService;
import com.bugbycode.module.RecvCrossUnPnlStatus;
import com.bugbycode.module.binance.Balance;
import com.bugbycode.module.user.User;
import com.bugbycode.service.klines.KlinesService;
import com.bugbycode.service.user.UserService;
import com.util.PriceUtil;

/**
 * 未实现盈亏监控任务
 */
//@Configuration
//@EnableScheduling
public class CrossUnPnlTask {

	private final Logger logger = LogManager.getLogger(CrossUnPnlTask.class);
	
	@Autowired
	private BinanceRestTradeService binanceRestTradeService;
	
	@Autowired
	private UserService userDetailsService;
	
	@Autowired
	private KlinesService klinesService;
	
	/**
	 * 3分30秒开始执行 每15分钟执行一次
	 */
	//@Scheduled(cron = "30 3/5 * * * ?")
	public void monitor() {
		logger.info("CrossUnPnlTask monitor() start.");
		
		List<User> userList = userDetailsService.queryByRecvCrossUnPnl(RecvCrossUnPnlStatus.OPEN);
		for(User u : userList) {
			String binanceApiKey = u.getBinanceApiKey();
			String binanceSecretKey = u.getBinanceSecretKey();
			List<Balance> balanceList = binanceRestTradeService.balance_v2(binanceApiKey, binanceSecretKey);
			for(Balance balance : balanceList) {
				if("USDT".equals(balance.getAsset())) {
					BigDecimal balanceValue = balance.getBalanceBigDecimalValue();
					BigDecimal crossUnPnlValue = balance.getCrossUnPnlBigDecimalValue();
					
					BigDecimal pnlPercentValue = crossUnPnlValue.divide(balanceValue,10, RoundingMode.HALF_UP);
					double pnlPercentDoubleValue = pnlPercentValue.doubleValue();
					
					if(balanceValue.doubleValue() == 0) {
						continue;
					}
					
					String subject = String.format("未实现盈亏提示(PNL:%s)", PriceUtil.formatDoubleDecimal(pnlPercentDoubleValue, 2));
					String text = String.format("用户%s钱包余额：%s，未实现盈亏：%s，可用下单余额：%s", u.getUsername(), balance.getBalance(), balance.getCrossUnPnl(),
							balance.getAvailableBalance());
					
					logger.info(subject);
					logger.info(text);
					
					if(pnlPercentDoubleValue < u.getRecvCrossUnPnlPercent()) {
						continue;
					}
					
					klinesService.sendEmail(subject, text, u.getUsername());
				}
			}
		}
		
		logger.info("CrossUnPnlTask monitor() end.");
	}
}
