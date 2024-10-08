package com.bugbycode.websocket.realtime.handler.impl;

import java.util.Date;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONObject;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import com.bugbycode.module.Inerval;
import com.bugbycode.module.Klines;
import com.bugbycode.module.TradingPlan;
import com.bugbycode.repository.klines.KlinesRepository;
import com.bugbycode.service.klines.KlinesService;
import com.bugbycode.service.plan.TradingPlanService;
import com.bugbycode.service.plan.impl.TradingPlanServiceImpl;
import com.bugbycode.trading_app.pool.WorkTaskPool;
import com.bugbycode.trading_app.task.plan.work.PlanAnalysisKlinesTask;
import com.bugbycode.trading_app.task.sync.work.AnalysisKlinesTask;
import com.bugbycode.trading_app.task.sync.work.SyncKlinesTask;
import com.bugbycode.websocket.realtime.endpoint.PerpetualWebSocketClientEndpoint;
import com.bugbycode.websocket.realtime.handler.MessageHandler;

@Service("messageHandler")
public class MessageHandlerImpl implements MessageHandler{

	private final Logger logger = LogManager.getLogger(MessageHandlerImpl.class);
	
	private TradingPlanService tradingPlanService = new TradingPlanServiceImpl();
	
	@Override
	public void handleMessage(String message, PerpetualWebSocketClientEndpoint client, KlinesService klinesService, 
			KlinesRepository klinesRepository,WorkTaskPool analysisWorkTaskPool) {
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
			
			//交易计划
			if(kline.getInervalType() == Inerval.INERVAL_5M) {
				List<TradingPlan> plan_list = tradingPlanService.getAllTradingPlan();
				if(!CollectionUtils.isEmpty(plan_list)) {
					for(TradingPlan plan : plan_list) {
						if(plan.getPair().equals(kline.getPair())) {
							analysisWorkTaskPool.add(new PlanAnalysisKlinesTask(plan, kline, klinesService, tradingPlanService));
						}
					}
				}
			} else {
				//15分钟k线分析
				long count = klinesRepository.count(kline.getPair(), Inerval.INERVAL_1D.getDescption());
				if(count == 0) {
					analysisWorkTaskPool.add(new SyncKlinesTask(kline.getPair(), new Date(), klinesService, klinesRepository));
					return;
				} else {
					klinesRepository.insert(kline);
					if(kline.getInervalType() == Inerval.INERVAL_15M) {
						analysisWorkTaskPool.add(new AnalysisKlinesTask(kline.getPair(), klinesService, klinesRepository));
					}
				}
			}
		};
		
		if(client.coinCount() == 0) {
			logger.info(client.getStreamName() + "订阅结束，关闭websocket连接");
			client.close();
		}
	}
	
}
