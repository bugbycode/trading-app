package com.bugbycode.trading_app;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

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

@SpringBootTest
public class WebsocketSyncKlinesTest {

	private final Logger logger = LogManager.getLogger(WebsocketSyncKlinesTest.class);
	
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
	
	@BeforeEach
	public void befor() {
		AppConfig.DEBUG = true;
		System.setProperty("https.proxyHost", "localhost");
		System.setProperty("https.proxyPort", "50000");
	}
	
	@Test
	public void testSync() throws UnsupportedEncodingException, InterruptedException {
		
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
			new PerpetualWebSocketClientEndpoint(s, messageHandler, klinesService, klinesRepository, openInterestHistRepository, analysisWorkTaskPool, workTaskPool);
		}
		
		Thread.sleep(60 * 60 * 1000);
	}
}
