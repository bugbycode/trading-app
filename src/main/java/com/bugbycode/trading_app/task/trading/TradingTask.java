package com.bugbycode.trading_app.task.trading;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.bugbycode.module.FibInfo;
import com.bugbycode.module.binance.AutoTradeType;
import com.bugbycode.module.price.OpenPrice;
import com.bugbycode.module.trading.PositionSide;
import com.bugbycode.service.klines.KlinesService;

/**
 * 自动交易任务
 */
public class TradingTask implements Runnable {
	
	private final Logger logger = LogManager.getLogger(TradingTask.class);

	private KlinesService klinesService;
	
	private String pair;
	
	private PositionSide ps;
	
	private double stopLossDoubleValue; 
	
	private double takeProfitDoubleValue;
	
	private OpenPrice openPrice;
	
	private FibInfo fibInfo;
	
	private AutoTradeType autoTradeType;
	
	private int decimalNum;
	
	/**
	 * 
	 * @param klinesService
     * @param pair 交易对
     * @param ps 持仓方向 LONG / SHORT 
     * @param stopLossDoubleValue 止损价 fibInfo为null时使用
     * @param takeProfitDoubleValue 止盈价 fibInfo为null时使用
     * @param openPrice 开仓价
     * @param fibInfo 斐波那契回撤点位信息
     * @param autoTradeType 自动交易参考指标
     * @param decimalNum 价格小数点个数
	 */
	public TradingTask(KlinesService klinesService,String pair,PositionSide ps, double stopLossDoubleValue, double takeProfitDoubleValue, OpenPrice openPrice, 
			FibInfo fibInfo, AutoTradeType autoTradeType, int decimalNum) {
		this.klinesService = klinesService;
		this.pair = pair;
		this.ps = ps;
		this.stopLossDoubleValue = stopLossDoubleValue;
		this.takeProfitDoubleValue = takeProfitDoubleValue;
		this.openPrice = openPrice;
		this.fibInfo = fibInfo;
		this.autoTradeType = autoTradeType;
		this.decimalNum = decimalNum;
	}
	
	@Override
	public void run() {
		try {
			klinesService.marketPlace(pair, ps, stopLossDoubleValue, takeProfitDoubleValue, openPrice, fibInfo, autoTradeType, decimalNum);
		} catch (Exception e) {
			logger.error("执行自动交易任务时出现异常", e);
		}
	}

}
