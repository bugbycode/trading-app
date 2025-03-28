package com.bugbycode.trading_app.task.sync;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

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
 * 连接websocket订阅永续合约k线日线级别定时任务
 */
@Configuration
@EnableScheduling
public class SyncFuturesLastDayKlinesWebSocketTask {

	private final Logger logger = LogManager.getLogger(SyncFuturesLastDayKlinesWebSocketTask.class);

	@Autowired
	private MessageHandler messageHandler;
	
	@Autowired
	private WorkTaskPool analysisWorkTaskPool;
	
	@Autowired
	private WorkTaskPool workTaskPool;
	
	@Autowired
	private KlinesRepository klinesRepository;
	
	@Autowired
	private KlinesService klinesService;
	
	@Autowired
	private BinanceExchangeService binanceExchangeService;
	
	@Autowired
	private OpenInterestHistRepository openInterestHistRepository;
	
	/**
	 * 每天早上 7:59:46 执行一次任务
	 */
	@Scheduled(cron = "46 59 7 * * ?")
	public void runWebsocketClient() {
		logger.debug("SyncFuturesLastDayKlinesWebSocketTask start.");
		
		Inerval inerval = Inerval.INERVAL_1D;
		
		Set<String> pairs = binanceExchangeService.exchangeInfo();
		
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
			new PerpetualWebSocketClientEndpoint(s, messageHandler, klinesService, klinesRepository, openInterestHistRepository, analysisWorkTaskPool, workTaskPool);
		}
		
		logger.debug("SyncFuturesLastDayKlinesWebSocketTask end.");
	}
}
