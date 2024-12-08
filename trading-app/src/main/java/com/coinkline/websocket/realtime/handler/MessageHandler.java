package com.coinkline.websocket.realtime.handler;

import com.coinkline.repository.klines.KlinesRepository;
import com.coinkline.service.klines.KlinesService;
import com.coinkline.trading_app.pool.WorkTaskPool;
import com.coinkline.websocket.realtime.endpoint.PerpetualWebSocketClientEndpoint;

public interface MessageHandler {
	
	public void handleMessage(String message,PerpetualWebSocketClientEndpoint client, KlinesService klinesService, 
			KlinesRepository klinesRepository, WorkTaskPool analysisWorkTaskPool);
	
}
