package com.bugbycode.websocket.realtime.handler.impl;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONObject;
import org.springframework.stereotype.Service;

import com.bugbycode.config.AppConfig;
import com.bugbycode.module.Inerval;
import com.bugbycode.module.Klines;
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
			KlinesRepository klinesRepository, OpenInterestHistRepository openInterestHistRepository, WorkTaskPool analysisWorkTaskPool) {
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
		
		if(finish && client.putFinishPair(kline.getPair())) {
			
			logger.debug(kline);
			
			//15分钟k线分析
			long count = klinesRepository.count(kline.getPair(), Inerval.INERVAL_1D);
			if(count == 0) {
				analysisWorkTaskPool.add(new SyncKlinesTask(kline.getPair(), new Date(), klinesService, klinesRepository));
				return;
			} else {
				klinesRepository.insert(kline);
				if(kline.getInervalType() == Inerval.INERVAL_15M) {
					analysisWorkTaskPool.add(new AnalysisKlinesTask(kline.getPair(), klinesService, klinesRepository));
				}
			}
			
			if(kline.getInervalType() == Inerval.INERVAL_15M) {
				//移除批次
				AppConfig.SYNC_15M_KLINES_RECORD.remove(pair);
				//添加同步完成的交易对
				AppConfig.SYNC_15M_KLINES_FINISH.add(pair);
			}
		};
		
		if(client.isFinish()) {
			client.close();
			//全部同步完成时执行
			if(AppConfig.SYNC_15M_KLINES_RECORD.isEmpty()) {
				try {
					List<OpenInterestHist> list = openInterestHistRepository.query();
					for(OpenInterestHist oih : list) {
						logger.info(oih);
					}
					logger.info("总共同步了15分钟级别{}种交易对k线信息", AppConfig.SYNC_15M_KLINES_FINISH.size());
				} catch (Exception e) {
					logger.error("处理同步k线结果时出现异常", e);
				}
			}
		}
	}
	
}
