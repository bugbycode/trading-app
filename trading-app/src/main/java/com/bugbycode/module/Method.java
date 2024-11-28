package com.bugbycode.module;

/**
 * Websocket api方法枚举信息
 */
public enum Method {
	
	/**
	 * 账户余额 (USER_DATA)
	 */
	BALANCE("account.balance"),
	
	/**
	 * 账户余额V2 (USER_DATA)
	 */
	BALANCE_V2("v2/account.balance"),
	
	/**
	 * 最新价格
	 */
	TICKER_PRICE("ticker.price"),
	
	/**
	 * 下单 (TRADE)
	 */
	ORDER_PLACE("order.place");

	private String value;
	
	Method(String value) {
		this.value = value;
	}
	
	public String value() {
		return this.value;
	}
}
