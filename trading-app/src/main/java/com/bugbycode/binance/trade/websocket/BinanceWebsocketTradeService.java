package com.bugbycode.binance.trade.websocket;

import java.util.List;

import com.bugbycode.module.binance.Balance;

/**
 * 币安交易管理接口
 */
public interface BinanceWebsocketTradeService {

	/**
	 * 账户余额 (USER_DATA)
	 * @param apiKey
	 * @param secretKey
	 */
	public List<Balance> balance(String apiKey,String secretKey);
	
	/**
	 * 账户余额V2 (USER_DATA)
	 * @param apiKey
	 * @param secretKey
	 */
	public List<Balance> balance_v2(String apiKey,String secretKey);
}
