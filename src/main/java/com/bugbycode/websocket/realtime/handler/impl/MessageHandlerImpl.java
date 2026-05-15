package com.bugbycode.websocket.realtime.handler.impl;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONObject;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import com.bugbycode.config.AppConfig;
import com.bugbycode.module.Inerval;
import com.bugbycode.module.Klines;
import com.bugbycode.module.binance.SymbolExchangeInfo;
import com.bugbycode.module.open_interest.OpenInterestHist;
import com.bugbycode.repository.klines.KlinesRepository;
import com.bugbycode.repository.openInterest.OpenInterestHistRepository;
import com.bugbycode.service.klines.KlinesService;
import com.bugbycode.trading_app.pool.WorkTaskPool;
import com.bugbycode.trading_app.task.sync.work.AnalysisKlinesTask;
import com.bugbycode.trading_app.task.sync.work.SyncKlinesTask;
import com.bugbycode.websocket.realtime.endpoint.PerpetualWebSocketClientEndpoint;
import com.bugbycode.websocket.realtime.handler.MessageHandler;

@Service("messageHandler")
public class MessageHandlerImpl implements MessageHandler{

	private final Logger logger = LogManager.getLogger(MessageHandlerImpl.class);
	
	@Override
	public void handleMessage(String message, PerpetualWebSocketClientEndpoint client, KlinesService klinesService, 
			KlinesRepository klinesRepository, OpenInterestHistRepository openInterestHistRepository, WorkTaskPool analysisWorkTaskPool,
			WorkTaskPool workTaskPool) {
		
		JSONObject result = new JSONObject(message);
		JSONObject klinesJson = result.getJSONObject("k");
		String openPriceStr = klinesJson.getString("o");
		String pair = result.getString("ps");
		
		int decimalNum = new BigDecimal(openPriceStr).scale();
		
		if(AppConfig.SYMBOL_EXCHANGE_INFO.get(pair) != null) {
			decimalNum = AppConfig.SYMBOL_EXCHANGE_INFO.get(pair).getDecimalNum();
		}
		
		Klines kline = new Klines(pair, klinesJson.getLong("t"), klinesJson.getString("o"), 
				klinesJson.getString("h"), klinesJson.getString("l"), klinesJson.getString("c"), klinesJson.getLong("T"),
				client.getCoinPairSet().getInerval().getDescption(), decimalNum, klinesJson.getString("v"), klinesJson.getLong("n"),
				klinesJson.getString("q"), klinesJson.getString("V"), klinesJson.getString("Q"));
		
		boolean finish = klinesJson.getBoolean("x");
		
		if(finish) {
			
			logger.debug(kline);
			
			klinesRepository.insert(kline);
			
			//15分钟k线分析
			/*boolean isEmpty = klinesRepository.isEmpty(pair, Inerval.INERVAL_1D);
			if(isEmpty) {
				workTaskPool.add(new SyncKlinesTask(pair, new Date(), klinesService, klinesRepository));
			} else {
				klinesRepository.insert(kline);
				analysisWorkTaskPool.add(new AnalysisKlinesTask(pair, klinesService, klinesRepository, openInterestHistRepository));
			}*/
			
			client.putFinishPair(pair);
		};
		
		if(client.isFinish()) {
			client.close();
			synchronized (AppConfig.SYNC_FINISH_WEBSOCKET_CLIENT) {
				
				List<PerpetualWebSocketClientEndpoint> clients = AppConfig.SYNC_FINISH_WEBSOCKET_CLIENT.get(client.getExec_time());
				
				if(CollectionUtils.isEmpty(clients)) {
					return;
				}

				boolean sync_finish = true;
				for(PerpetualWebSocketClientEndpoint c : clients) {
					if(!c.isFinish()) {
						sync_finish = false;
					}
				}
				
				if(sync_finish) {
					logger.debug("已同步的交易对：");
					List<OpenInterestHist> oihList = openInterestHistRepository.query();
					for(OpenInterestHist oih : oihList) {
						SymbolExchangeInfo info = client.getSymbolExchangeInfo(oih.getSymbol());
						if(info == null) {
							continue;
						}
						
						String symbol = info.getSymbol();
						
						logger.debug(symbol);
						
						boolean isEmpty = klinesRepository.isEmpty(symbol, Inerval.INERVAL_1D);
						
						if(isEmpty) {
							workTaskPool.add(new SyncKlinesTask(symbol, new Date(), klinesService, klinesRepository));
						} else {
							analysisWorkTaskPool.add(new AnalysisKlinesTask(symbol, klinesService, klinesRepository, openInterestHistRepository));
						}
					}
					
					logger.debug("K线订阅批次{}已同步完成.", client.getExec_time());
					
					AppConfig.SYNC_FINISH_WEBSOCKET_CLIENT.remove(client.getExec_time());
				}
			}
		}
	}
	
}
