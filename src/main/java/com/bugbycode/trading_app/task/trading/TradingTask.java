package com.bugbycode.trading_app.task.trading;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.bugbycode.config.AppConfig;
import com.bugbycode.module.binance.ContractType;
import com.bugbycode.module.binance.SymbolExchangeInfo;
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
	
	private OpenPrice openPrice;
	
	private int decimalNum;
	
	/**
	 * 
	 * @param klinesService
     * @param pair 交易对
     * @param ps 持仓方向 LONG / SHORT 
     * @param openPrice 开仓价
     * @param decimalNum 价格小数点个数
	 */
	public TradingTask(KlinesService klinesService,String pair, PositionSide ps, OpenPrice openPrice, int decimalNum) {
		this.klinesService = klinesService;
		this.pair = pair;
		this.ps = ps;
		this.openPrice = openPrice;
		this.decimalNum = decimalNum;
	}
	
	@Override
	public void run() {
		try {
			
			SymbolExchangeInfo info = AppConfig.SYMBOL_EXCHANGE_INFO.get(pair);
			if(info != null && info.getContractType() == ContractType.TRADIFI_PERPETUAL) {//不交易传统金融交易对
				return;
			}
			
			klinesService.marketPlace(pair, ps, openPrice, decimalNum);
		} catch (Exception e) {
			logger.error("执行自动交易任务时出现异常", e);
		}
	}

}
