package com.bugbycode.module.trading;

/**
 * 	订单类型 LIMIT, MARKET, STOP, TAKE_PROFIT, STOP_MARKET, TAKE_PROFIT_MARKET, TRAILING_STOP_MARKET
 */
public enum Type {
	
	/**
	 * 限价订单
	 */
	LIMIT("LIMIT", "限价订单"),
	
	/**
	 * 市价订单
	 */
	MARKET("MARKET", "市价订单"), 
	
	/**
	 * 限价止损订单
	 */
	STOP("STOP", "限价止损订单"), 
	
	/**
	 * 限价止盈订单
	 */
	TAKE_PROFIT("TAKE_PROFIT", "限价止盈订单"), 
	
	/**
	 * 市价止损订单
	 */
	STOP_MARKET("STOP_MARKET", "市价止损订单"), 
	
	/**
	 * 市价止盈订单
	 */
	TAKE_PROFIT_MARKET("TAKE_PROFIT_MARKET", "市价止盈订单"), 
	
	/**
	 * 跟踪委托订单
	 */
	TRAILING_STOP_MARKET("TRAILING_STOP_MARKET", "跟踪委托订单");

	private String type;
	
	private String memo;
	
	Type(String type, String memo) {
		this.type = type;
		this.memo = memo;
	}
	
	public String value() {
		return this.type;
	}

	public String getMemo() {
		return memo;
	}
}
