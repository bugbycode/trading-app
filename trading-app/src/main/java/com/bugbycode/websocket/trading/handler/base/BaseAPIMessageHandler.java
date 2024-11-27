package com.bugbycode.websocket.trading.handler.base;

import java.util.LinkedList;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.bugbycode.websocket.trading.endpoint.TradingWebSocketClientEndpoint;
import com.bugbycode.websocket.trading.handler.impl.APIMessageHandlerImpl;

public abstract class BaseAPIMessageHandler {
	
	protected final Logger logger = LogManager.getLogger(APIMessageHandlerImpl.class);

	protected LinkedList<String> recvMessageQueue;
	
	protected TradingWebSocketClientEndpoint client;
	
	protected String apiKey;
	
	protected String secretKey;
	
	protected void setClient(TradingWebSocketClientEndpoint client) {
		if(this.client == null) {
			this.client = client;
			this.apiKey = client.getApiKey();
			this.secretKey = client.getSecretKey();
	        this.recvMessageQueue = new LinkedList<String>();
		}
	}
	
	protected void sendMessage(String message) {
		logger.info(message);
		this.client.sendMessage(message);
	}
}
