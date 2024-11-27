package com.bugbycode.websocket.trading.handler.base;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONObject;

import com.bugbycode.websocket.trading.endpoint.TradingWebSocketClientEndpoint;
import com.bugbycode.websocket.trading.handler.impl.APIMessageHandlerImpl;
import com.util.HmacSHA256Util;

public abstract class BaseAPIMessageHandler {
	
	protected final Logger logger = LogManager.getLogger(APIMessageHandlerImpl.class);

	protected LinkedList<String> recvMessageQueue;
	
	protected TradingWebSocketClientEndpoint client;
	
	protected void setClient(TradingWebSocketClientEndpoint client) {
		if(this.client == null) {
			this.client = client;
	        this.recvMessageQueue = new LinkedList<String>();
		}
	}
	
	/**
	 * 对数据签名
	 * @param data
	 */
	protected void generateSignature(JSONObject data) {
		List<String> keyList = new ArrayList<String>(data.keySet());
		Collections.sort(keyList);
		
		StringBuffer buff = new StringBuffer();
		for(String key : keyList) {
			if(buff.length() > 0) {
				buff.append("&");
			}
			buff.append(key + "=" + data.get(key));
		}
		logger.info(buff.toString());
		String signature = HmacSHA256Util.generateSignature(buff.toString(), this.client.getSecretKey());
		data.put("signature", signature);
	}
	
	protected void sendMessage(String message) {
		this.client.sendMessage(message);
	}
}
