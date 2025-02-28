package com.bugbycode.websocket.client.handler.impl;

import java.math.BigDecimal;

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
		logger.debug(message);
		JSONObject result = new JSONObject(message);
		JSONObject klinesJson = result.getJSONObject("k");
		String openPriceStr = klinesJson.getString("o");
		int decimalNum = new BigDecimal(openPriceStr).scale();
		
		Klines kline = new Klines(result.getString("ps"), klinesJson.getLong("t"), klinesJson.getString("o"), 
				klinesJson.getString("h"), klinesJson.getString("l"), klinesJson.getString("c"), klinesJson.getLong("T"),
				client.getCoinPairSet().getInerval().getDescption(), decimalNum, klinesJson.getString("v"), klinesJson.getLong("n"),
				klinesJson.getString("q"), klinesJson.getString("V"), klinesJson.getString("Q"));
		
		boolean finish = klinesJson.getBoolean("x");
		
		if(finish && client.putFinishPair(kline.getPair())) {
			
			logger.debug(kline);
			
			this.workTaskPool.add(new ShapeDistributeTask(kline, klinesService, shapeRepository,analysisWorkTaskPool));
		}
		
		if(client.isFinish()) {
			logger.debug(client.getStreamName() + "订阅结束，关闭websocket连接");
			client.close();
		}
	}

}
