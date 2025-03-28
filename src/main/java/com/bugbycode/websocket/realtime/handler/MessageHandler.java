package com.bugbycode.websocket.realtime.handler;

import com.bugbycode.repository.klines.KlinesRepository;
import com.bugbycode.repository.openInterest.OpenInterestHistRepository;
import com.bugbycode.service.klines.KlinesService;
import com.bugbycode.trading_app.pool.WorkTaskPool;
import com.bugbycode.websocket.realtime.endpoint.PerpetualWebSocketClientEndpoint;

public interface MessageHandler {
	
	public void handleMessage(String message,PerpetualWebSocketClientEndpoint client, KlinesService klinesService, 
			KlinesRepository klinesRepository, OpenInterestHistRepository openInterestHistRepository, 
			WorkTaskPool analysisWorkTaskPool, WorkTaskPool workTaskPool);
	
}
