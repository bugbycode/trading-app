package com.bugbycode.trading_app.task.sync;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

import com.bugbycode.config.AppConfig;
import com.bugbycode.module.Inerval;
import com.bugbycode.repository.klines.KlinesRepository;
import com.bugbycode.service.klines.KlinesService;
import com.bugbycode.trading_app.pool.WorkTaskPool;
import com.bugbycode.websocket.realtime.endpoint.PerpetualWebSocketClientEndpoint;
import com.bugbycode.websocket.realtime.handler.MessageHandler;
import com.util.CoinPairSet;

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
	private KlinesRepository klinesRepository;
	
	@Autowired
	private KlinesService klinesService;
	
	/**
	 * 14分50秒开始执行 每15分钟执行一次
	 */
	@Scheduled(cron = "50 14/15 * * * ?")
	public void runWebsocketClient() {
		
		logger.info("FuturesKlinesWebSocketTask start.");
		
		Inerval inerval = Inerval.INERVAL_15M;
		
		CoinPairSet set = new CoinPairSet(inerval);
		
		for(String coin : AppConfig.PAIRS) {
			set.add(coin);
			if(set.isFull()) {
				new PerpetualWebSocketClientEndpoint(set, messageHandler, klinesService, klinesRepository, analysisWorkTaskPool);
				set = new CoinPairSet(inerval);
			}
		}
		
		logger.info("FuturesKlinesWebSocketTask end.");
	}
}
