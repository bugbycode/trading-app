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

import com.bugbycode.binance.module.eoptions.EoptionContracts;
import com.bugbycode.config.AppConfig;
import com.bugbycode.module.Inerval;
import com.bugbycode.module.binance.ContractType;
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
	
	/**
	 * 14分47秒开始执行 每15分钟执行一次
	 */
	@Scheduled(cron = "47 14/15 * * * ?")
	public void runWebsocketClient() {
		
		if(AppConfig.DEBUG) {
			return;
		}
		
		logger.debug("FuturesKlinesWebSocketTask start.");
		
		AppConfig.SYNC_15M_KLINES_RECORD.clear();
		AppConfig.SYNC_15M_KLINES_FINISH.clear();
		AppConfig.EOPTION_EXCHANGE_INFO.clear();
		
		Inerval inerval = Inerval.INERVAL_15M;
		
		//欧式期权
		/*List<CoinPairSet> e_coinList = new ArrayList<CoinPairSet>();
		List<SymbolExchangeInfo> list = binanceExchangeService.eOptionsExchangeInfoSymbol();
		CoinPairSet e_set = new CoinPairSet(inerval, ContractType.E_OPTIONS);
		for(SymbolExchangeInfo e_coin : list) {
			AppConfig.SYNC_15M_KLINES_RECORD.put(e_coin.getSymbol(), new Date().getTime());
			e_set.add(e_coin);
			if(e_set.isFull()) {
				e_coinList.add(e_set);
				e_set = new CoinPairSet(inerval, ContractType.E_OPTIONS);
			}
		}
		if(!e_set.isEmpty()) {
			e_coinList.add(e_set);
		}
		
		for(CoinPairSet e_s : e_coinList) {
			new PerpetualWebSocketClientEndpoint(e_s, messageHandler, klinesService, klinesRepository, openInterestHistRepository, analysisWorkTaskPool, workTaskPool, ContractType.E_OPTIONS);
		}
		*/
		//欧式期权 END
		
		List<EoptionContracts> ecList = binanceExchangeService.eOptionsExchangeInfo();
		for(EoptionContracts ec : ecList) {
			AppConfig.EOPTION_EXCHANGE_INFO.put(ec.getUnderlying(), ec);
		}
		
		Set<SymbolExchangeInfo> pairs = binanceExchangeService.exchangeInfo();
		
		CoinPairSet set = new CoinPairSet(inerval, ContractType.PERPETUAL);
		List<CoinPairSet> coinList = new ArrayList<CoinPairSet>();
		for(SymbolExchangeInfo coin : pairs) {
			
			AppConfig.SYNC_15M_KLINES_RECORD.put(coin.getSymbol(), new Date().getTime());
			
			set.add(coin);
			if(set.isFull()) {
				coinList.add(set);
				set = new CoinPairSet(inerval, ContractType.PERPETUAL);
			}
		}
		
		if(!set.isEmpty()) {
			coinList.add(set);
		}
		
		for(CoinPairSet s : coinList) {
			new PerpetualWebSocketClientEndpoint(s, messageHandler, klinesService, klinesRepository, openInterestHistRepository, analysisWorkTaskPool, workTaskPool, ContractType.PERPETUAL);
		}
		
		
		logger.debug("FuturesKlinesWebSocketTask end.");
	}
}
