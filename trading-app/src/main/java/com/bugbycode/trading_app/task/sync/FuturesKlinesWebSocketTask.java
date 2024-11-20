package com.bugbycode.trading_app.task.sync;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

import com.bugbycode.config.AppConfig;
import com.bugbycode.module.Inerval;
import com.bugbycode.repository.klines.KlinesRepository;
import com.bugbycode.service.exchange.BinanceExchangeService;
import com.bugbycode.service.klines.KlinesService;
import com.bugbycode.service.user.UserService;
import com.bugbycode.trading_app.pool.WorkTaskPool;
import com.bugbycode.trading_app.task.email.SendMailTask;
import com.bugbycode.websocket.realtime.endpoint.PerpetualWebSocketClientEndpoint;
import com.bugbycode.websocket.realtime.handler.MessageHandler;
import com.util.CoinPairSet;
import com.util.DateFormatUtil;
import com.util.StringUtil;

/**
 * 连接websocket订阅永续合约k线15分钟级别定时任务
 */
@Configuration
@EnableScheduling
public class FuturesKlinesWebSocketTask {
	
	private final Logger logger = LogManager.getLogger(FuturesKlinesWebSocketTask.class);
	
	@Autowired
	private MessageHandler messageHandler;
	
	@Autowired
	private WorkTaskPool analysisWorkTaskPool;
	
	@Autowired
	private WorkTaskPool emailWorkTaskPool;
	
	@Autowired
	private KlinesRepository klinesRepository;
	
	@Autowired
	private KlinesService klinesService;
	
	@Autowired
	private BinanceExchangeService binanceExchangeService;
	
	@Autowired
	private UserService userDetailsService;
	
	/**
	 * 14分47秒开始执行 每15分钟执行一次
	 */
	@Scheduled(cron = "47 14/15 * * * ?")
	public void runWebsocketClient() {
		
		logger.info("FuturesKlinesWebSocketTask start.");
		
		Date now = new Date();
		
		Inerval inerval = Inerval.INERVAL_15M;
		
		Set<String> pairs = binanceExchangeService.exchangeInfo();
		
		try {
			String dateStr = DateFormatUtil.format(now);
			String recEmail = userDetailsService.getAllUserEmail();
			
			if(AppConfig.PAIRS == null) {//程序第一次启动
				AppConfig.PAIRS = pairs;
			} else {
				//判断合约是否出现新交易对
				for(String coin : pairs) {
					if(!StringUtil.contains(AppConfig.PAIRS, coin)) {
						
						String text = String.format("币安即将开放%s永续合约交易 %s", coin, dateStr);
						
						emailWorkTaskPool.add(new SendMailTask(text, text, recEmail));
						
					}
				}
				
				AppConfig.PAIRS = pairs;
			}
		}catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
		
		CoinPairSet set = new CoinPairSet(inerval);
		List<CoinPairSet> coinList = new ArrayList<CoinPairSet>();
		for(String coin : pairs) {
			set.add(coin);
			if(set.isFull()) {
				coinList.add(set);
				set = new CoinPairSet(inerval);
			}
		}
		
		if(!set.isEmpty()) {
			coinList.add(set);
		}
		
		for(CoinPairSet s : coinList) {
			new PerpetualWebSocketClientEndpoint(s, messageHandler, klinesService, klinesRepository, analysisWorkTaskPool);
		}
		
		logger.info("FuturesKlinesWebSocketTask end.");
	}
}
