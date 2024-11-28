package com.bugbycode.module;

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
	TICKER_PRICE("ticker.price");

	private String value;
	
	Method(String value) {
		this.value = value;
	}
	
	public String value() {
		return this.value;
	}
}
