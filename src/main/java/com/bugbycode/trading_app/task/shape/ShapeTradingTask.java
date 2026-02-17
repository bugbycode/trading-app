package com.bugbycode.trading_app.task.shape;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.util.CollectionUtils;

import com.bugbycode.config.AppConfig;
import com.bugbycode.module.Inerval;
import com.bugbycode.module.ShapeInfo;
import com.bugbycode.module.binance.ContractType;
import com.bugbycode.module.binance.SymbolExchangeInfo;
import com.bugbycode.repository.shape.ShapeRepository;
import com.bugbycode.service.exchange.BinanceExchangeService;
import com.bugbycode.service.klines.KlinesService;
import com.bugbycode.trading_app.pool.WorkTaskPool;
import com.bugbycode.websocket.client.endpoint.PerpetualWebSocketClientEndpoint;
import com.bugbycode.websocket.client.handler.impl.ShapeMessageHandler;
import com.util.CoinPairSet;

/**
 * 绘图交易计划任务
 */
@Configuration
@EnableScheduling
public class ShapeTradingTask {
	
	private final Logger logger = LogManager.getLogger(ShapeTradingTask.class);
	
	@Autowired
	private ShapeRepository shapeRepository;
	
	@Autowired
	private KlinesService klinesService;
	
	@Autowired
    private BinanceExchangeService binanceExchangeService;
	
	@Autowired
	public WorkTaskPool workTaskPool;
	
	@Autowired
	public WorkTaskPool analysisWorkTaskPool;
	
	/**
	 * 交易计划定时任务 每5分钟执行一次
	 * 
	 * @throws Exception
	 */
	@Scheduled(cron = "45 4/5 * * * ?")
	public void executeShapeTask() {
		
		if(AppConfig.DEBUG) {
			return;
		}
		
		logger.debug("ShapeTradingTask executeShapeTask start.");
		
		binanceExchangeService.exchangeInfo();
		
		Inerval inerval = Inerval.INERVAL_5M;
		
		List<ShapeInfo> shapeList = shapeRepository.query();
		//所有交易对
		Set<SymbolExchangeInfo> pairSet = new HashSet<SymbolExchangeInfo>();
		if(!CollectionUtils.isEmpty(shapeList)) {
			for(ShapeInfo shape : shapeList) {
				SymbolExchangeInfo info = AppConfig.SYMBOL_EXCHANGE_INFO.get(shape.getSymbol());
				if(info == null) {
					logger.info("未找到{}交易规则信息", shape.getSymbol());
					continue;
				}
				pairSet.add(info);
			}
			CoinPairSet set = new CoinPairSet(inerval, ContractType.PERPETUAL);
			List<CoinPairSet> coinList = new ArrayList<CoinPairSet>();
			for(SymbolExchangeInfo info : pairSet) {
				set.add(info);
				if(set.isFull()) {
					coinList.add(set);
					set = new CoinPairSet(inerval, ContractType.PERPETUAL);
				}
			}
			
			if(!set.isEmpty()) {
				coinList.add(set);
			}
			
			for(CoinPairSet coin : coinList) {
				//建立连接订阅交易对实时行情信息
				PerpetualWebSocketClientEndpoint client = new PerpetualWebSocketClientEndpoint(coin);
				client.setMessageHandler(new ShapeMessageHandler(client, klinesService,shapeRepository,workTaskPool,analysisWorkTaskPool));
				client.connectToServer();
			}
		}
		
		logger.debug("ShapeTradingTask executeShapeTask end.");
	}
}
