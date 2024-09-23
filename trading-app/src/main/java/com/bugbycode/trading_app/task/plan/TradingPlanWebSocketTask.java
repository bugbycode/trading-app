package com.bugbycode.trading_app.task.plan;

import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.util.CollectionUtils;

import com.bugbycode.module.Inerval;
import com.bugbycode.module.TradingPlan;
import com.bugbycode.repository.klines.KlinesRepository;
import com.bugbycode.service.klines.KlinesService;
import com.bugbycode.service.plan.TradingPlanService;
import com.bugbycode.trading_app.pool.WorkTaskPool;
import com.bugbycode.websocket.realtime.endpoint.PerpetualWebSocketClientEndpoint;
import com.bugbycode.websocket.realtime.handler.MessageHandler;
import com.util.CoinPairSet;
import com.util.PlanPairSet;

/**
 * 交易计划任务
 */
@Configuration
@EnableScheduling
public class TradingPlanWebSocketTask {

	private final Logger logger = LogManager.getLogger(TradingPlanWebSocketTask.class);
	
	@Autowired
	private WorkTaskPool analysisWorkTaskPool;
    
    @Autowired
	private MessageHandler messageHandler;
	
	@Autowired
	private TradingPlanService tradingPlanService;
	
	@Autowired
	private KlinesService klinesService;
	
	@Autowired
	private KlinesRepository klinesRepository;
	
	/**
	 * 交易计划定时任务 每5分钟执行一次
	 * 
	 * @throws Exception
	 */
	@Scheduled(cron = "50 4/5 * * * ?")
	public void execute() throws Exception {
		
		logger.info("TradingPlanWebSocketTask start.");
		
		Inerval inerval = Inerval.INERVAL_5M;
		
		//查询所有任务
		List<TradingPlan> list = tradingPlanService.getAllTradingPlan();
		
		if(CollectionUtils.isEmpty(list)) {
			logger.info("当前没有交易计划可执行");
			return;
		}
		
		PlanPairSet planPairSet = new PlanPairSet(); 
		planPairSet.addPair(list);
		
		CoinPairSet set = new CoinPairSet(inerval);
		
		for(String coin : planPairSet) {
			set.add(coin);
			if(set.isFull()) {
				new PerpetualWebSocketClientEndpoint(set, messageHandler, klinesService, klinesRepository, analysisWorkTaskPool);
				set = new CoinPairSet(inerval);
			}
		}
		
		logger.info("TradingPlanWebSocketTask finish.");
	}
}
