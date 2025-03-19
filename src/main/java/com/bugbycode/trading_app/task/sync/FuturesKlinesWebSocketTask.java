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
import com.bugbycode.repository.openInterest.OpenInterestHistRepository;
import com.bugbycode.service.exchange.BinanceExchangeService;
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
	
	@Autowired
	private BinanceExchangeService binanceExchangeService;
	
	@Autowired
	private OpenInterestHistRepository openInterestHistRepository;
	
	/**
	 * 14分47秒开始执行 每15分钟执行一次
	 */
	@Scheduled(cron = "47 14/15 * * * ?")
	public void runWebsocketClient() {
		
		logger.debug("FuturesKlinesWebSocketTask start.");
		
		AppConfig.SYNC_15M_KLINES_RECORD.clear();
		AppConfig.SYNC_15M_KLINES_FINISH.clear();
		
		Inerval inerval = Inerval.INERVAL_15M;
		
		Set<String> pairs = binanceExchangeService.exchangeInfo();
		
		CoinPairSet set = new CoinPairSet(inerval);
		List<CoinPairSet> coinList = new ArrayList<CoinPairSet>();
		for(String coin : pairs) {
			
			AppConfig.SYNC_15M_KLINES_RECORD.put(coin, new Date().getTime());
			
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
			new PerpetualWebSocketClientEndpoint(s, messageHandler, klinesService, klinesRepository, openInterestHistRepository, analysisWorkTaskPool);
		}
		
		logger.debug("FuturesKlinesWebSocketTask end.");
	}
}
