package com.bugbycode.trading_app.task.sync;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

import com.bugbycode.binance.module.eoptions.EoptionContracts;
import com.bugbycode.binance.trade.rest.BinanceRestTradeService;
import com.bugbycode.config.AppConfig;
import com.bugbycode.module.Inerval;
import com.bugbycode.module.binance.SymbolExchangeInfo;
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
	private WorkTaskPool workTaskPool;
	
	@Autowired
	private KlinesRepository klinesRepository;
	
	@Autowired
	private KlinesService klinesService;
	
	@Autowired
	private BinanceExchangeService binanceExchangeService;
	
	@Autowired
	private OpenInterestHistRepository openInterestHistRepository;
	
	@Autowired
    private BinanceRestTradeService binanceRestTradeService;
	
	/**
	 * 14分37秒开始执行 每15分钟执行一次
	 */
	@Scheduled(cron = "37 14/15 * * * ?")
	public void runWebsocketClient() {
		
		if(AppConfig.DEBUG) {
			return;
		}
		
		logger.debug("FuturesKlinesWebSocketTask start.");
		
		try {
			synchronized (AppConfig.LEVERAGE_BRACKET) {
				AppConfig.LEVERAGE_BRACKET.clear();
			}
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
		
		try {
			synchronized (AppConfig.SYMBOL_CONFIG_INFO) {
				AppConfig.SYMBOL_CONFIG_INFO.clear();
			}
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
		
		try {
			binanceRestTradeService.fundingInfo();
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
		
		long exec_time = new Date().getTime();
		
		Inerval inerval = Inerval.INERVAL_15M;
		
		try {
			List<EoptionContracts> ecList = binanceExchangeService.eOptionsExchangeInfo();
			AppConfig.EOPTION_EXCHANGE_INFO.clear();
			for(EoptionContracts ec : ecList) {
				AppConfig.EOPTION_EXCHANGE_INFO.put(ec.getUnderlying(), ec);
			}
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
		
		List<SymbolExchangeInfo> pairs = binanceExchangeService.exchangeInfo();
		
		Map<String,SymbolExchangeInfo> symbol_exchange_info_map = new HashMap<String,SymbolExchangeInfo>();
		
		CoinPairSet set = new CoinPairSet(inerval);
		List<CoinPairSet> coinList = new ArrayList<CoinPairSet>();
		for(SymbolExchangeInfo coin : pairs) {
			
			set.add(coin);
			if(set.isFull()) {
				coinList.add(set);
				set = new CoinPairSet(inerval);
			}
			
			symbol_exchange_info_map.put(coin.getSymbol(), coin);
		}
		
		if(!set.isEmpty()) {
			coinList.add(set);
		}
		
		List<PerpetualWebSocketClientEndpoint> clients = new ArrayList<PerpetualWebSocketClientEndpoint>();
		for(CoinPairSet s : coinList) {
			PerpetualWebSocketClientEndpoint client = new PerpetualWebSocketClientEndpoint(exec_time, s, symbol_exchange_info_map, messageHandler, klinesService, klinesRepository, 
					openInterestHistRepository, analysisWorkTaskPool, workTaskPool);
			clients.add(client);
		}
		
		AppConfig.SYNC_FINISH_WEBSOCKET_CLIENT.put(exec_time, clients);
		
		//开始连接websocket订阅k线
		for(PerpetualWebSocketClientEndpoint c : clients) {
			try {
				c.connectToServer();
	        } catch (Exception e) {
				logger.error(e.getLocalizedMessage(), e);
			}
		}
		
		logger.debug("FuturesKlinesWebSocketTask end.");
	}
}
