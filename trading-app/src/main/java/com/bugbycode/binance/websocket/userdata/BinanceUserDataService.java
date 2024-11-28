package com.bugbycode.binance.websocket.userdata;

import java.util.List;

import com.bugbycode.module.binance.Balance;

/**
 * 币安用户数据接口 USER_DATA
 */
public interface BinanceUserDataService {

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
