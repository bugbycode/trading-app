package com.coinkline.websocket.realtime.handler.impl;

import java.math.BigDecimal;
import java.util.Date;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONObject;
import org.springframework.stereotype.Service;

import com.coinkline.module.Inerval;
import com.coinkline.module.Klines;
import com.coinkline.repository.klines.KlinesRepository;
import com.coinkline.service.klines.KlinesService;
import com.coinkline.trading_app.pool.WorkTaskPool;
import com.coinkline.trading_app.task.close.CloseLongOrShortTask;
import com.coinkline.trading_app.task.sync.work.AnalysisKlinesTask;
import com.coinkline.trading_app.task.sync.work.SyncKlinesTask;
import com.coinkline.websocket.realtime.endpoint.PerpetualWebSocketClientEndpoint;
import com.coinkline.websocket.realtime.handler.MessageHandler;

@Service("messageHandler")
public class MessageHandlerImpl implements MessageHandler{

	private final Logger logger = LogManager.getLogger(MessageHandlerImpl.class);
	
	@Override
	public void handleMessage(String message, PerpetualWebSocketClientEndpoint client, KlinesService klinesService, 
			KlinesRepository klinesRepository,WorkTaskPool analysisWorkTaskPool) {
		JSONObject result = new JSONObject(message);
		JSONObject klinesJson = result.getJSONObject("k");
		String openPriceStr = klinesJson.getString("o");
		int decimalNum = new BigDecimal(openPriceStr).scale();
		
		Klines kline = new Klines(result.getString("ps"), klinesJson.getLong("t"), klinesJson.getString("o"), 
				klinesJson.getString("h"), klinesJson.getString("l"), klinesJson.getString("c"), klinesJson.getLong("T"),
				client.getCoinPairSet().getInerval().getDescption(), decimalNum);
		
		boolean finish = klinesJson.getBoolean("x");
		
		if(finish) {
			logger.debug(message);
			client.subCount();
			
			//15分钟k线分析
			long count = klinesRepository.count(kline.getPair(), Inerval.INERVAL_1D.getDescption());
			if(count == 0) {
				analysisWorkTaskPool.add(new SyncKlinesTask(kline.getPair(), new Date(), klinesService, klinesRepository));
				return;
			} else {
				klinesRepository.insert(kline);
				if(kline.getInervalType() == Inerval.INERVAL_15M) {
					analysisWorkTaskPool.add(new AnalysisKlinesTask(kline.getPair(), klinesService, klinesRepository));
					analysisWorkTaskPool.add(new CloseLongOrShortTask(kline, klinesService));
				}
			}
		};
		
		if(client.coinCount() == 0) {
			client.close();
		}
	}
	
}
