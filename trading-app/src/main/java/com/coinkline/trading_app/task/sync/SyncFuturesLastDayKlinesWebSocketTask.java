package com.coinkline.trading_app.task.sync;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

import com.coinkline.module.Inerval;
import com.coinkline.repository.klines.KlinesRepository;
import com.coinkline.service.exchange.BinanceExchangeService;
import com.coinkline.service.klines.KlinesService;
import com.coinkline.trading_app.pool.WorkTaskPool;
import com.coinkline.websocket.realtime.endpoint.PerpetualWebSocketClientEndpoint;
import com.coinkline.websocket.realtime.handler.MessageHandler;
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
	private KlinesRepository klinesRepository;
	
	@Autowired
	private KlinesService klinesService;
	
	@Autowired
	private BinanceExchangeService binanceExchangeService;
	
	/**
	 * 每天早上 7:59:46 执行一次任务
	 */
	@Scheduled(cron = "46 59 7 * * ?")
	public void runWebsocketClient() {
		logger.info("SyncFuturesLastDayKlinesWebSocketTask start.");
		
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
			new PerpetualWebSocketClientEndpoint(s, messageHandler, klinesService, klinesRepository, analysisWorkTaskPool);
		}
		
		logger.info("SyncFuturesLastDayKlinesWebSocketTask end.");
	}
}
