package com.bugbycode.websocket.realtime.handler.impl;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONObject;
import org.springframework.stereotype.Service;

import com.bugbycode.module.Klines;
import com.bugbycode.websocket.realtime.handler.MessageHandler;

@Service("messageHandler")
public class MessageHandlerImpl implements MessageHandler{

	private final Logger logger = LogManager.getLogger(MessageHandlerImpl.class);
	
	@Override
	public void handleMessage(String message) {
		//logger.info(message);
		JSONObject result = new JSONObject(message);
		JSONObject klinesJson = result.getJSONObject("k");
		String openPriceStr = klinesJson.getString("o");
		int decimalNum = openPriceStr.substring(openPriceStr.indexOf(".") + 1).length();
		Klines kline = new Klines(result.getString("ps"), klinesJson.getLong("t"), klinesJson.getDouble("o"), 
				klinesJson.getDouble("h"), klinesJson.getDouble("l"), klinesJson.getDouble("c"), klinesJson.getLong("T"), decimalNum);
		
		logger.info(kline);
		
	}
	
}
