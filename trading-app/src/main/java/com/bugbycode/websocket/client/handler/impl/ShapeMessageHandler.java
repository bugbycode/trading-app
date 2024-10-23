package com.bugbycode.websocket.client.handler.impl;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONObject;

import com.bugbycode.module.Klines;
import com.bugbycode.repository.shape.ShapeRepository;
import com.bugbycode.service.klines.KlinesService;
import com.bugbycode.trading_app.pool.WorkTaskPool;
import com.bugbycode.trading_app.task.shape.work.ShapeDistributeTask;
import com.bugbycode.websocket.client.endpoint.PerpetualWebSocketClientEndpoint;
import com.bugbycode.websocket.client.handler.MessageHandler;

/**
 * 绘图交易计划分析接口
 */
public class ShapeMessageHandler implements MessageHandler {

	private final Logger logger = LogManager.getLogger(ShapeMessageHandler.class);
	
	private final KlinesService klinesService;
	
	private final ShapeRepository shapeRepository;
	
	private final WorkTaskPool workTaskPool;
	
	private final WorkTaskPool analysisWorkTaskPool;
	
	private final PerpetualWebSocketClientEndpoint client;
	
	public ShapeMessageHandler(PerpetualWebSocketClientEndpoint client,KlinesService klinesService,
			ShapeRepository shapeRepository,WorkTaskPool workTaskPool,WorkTaskPool analysisWorkTaskPool) {
		this.client = client;
		this.klinesService = klinesService;
		this.shapeRepository = shapeRepository;
		this.workTaskPool = workTaskPool;
		this.analysisWorkTaskPool = analysisWorkTaskPool;
	}
	
	@Override
	public void handleMessage(String message) {
		//logger.info(message);
		JSONObject result = new JSONObject(message);
		JSONObject klinesJson = result.getJSONObject("k");
		String openPriceStr = klinesJson.getString("o");
		int decimalNum = openPriceStr.substring(openPriceStr.indexOf(".") + 1).length();
		
		Klines kline = new Klines(result.getString("ps"), klinesJson.getLong("t"), klinesJson.getDouble("o"), 
				klinesJson.getDouble("h"), klinesJson.getDouble("l"), klinesJson.getDouble("c"), klinesJson.getLong("T"),
				client.getCoinPairSet().getInerval().getDescption(), decimalNum);
		
		boolean finish = klinesJson.getBoolean("x");
		
		if(finish) {
			client.subCount();
			
			logger.info(kline);
			
			this.workTaskPool.add(new ShapeDistributeTask(kline, klinesService, shapeRepository,analysisWorkTaskPool));
		}
		
		if(client.coinCount() == 0) {
			logger.info(client.getStreamName() + "订阅结束，关闭websocket连接");
			client.close();
		}
	}

}
