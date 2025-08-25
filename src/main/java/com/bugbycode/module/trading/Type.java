package com.bugbycode.module.trading;

/**
 * 	订单类型 LIMIT, MARKET, STOP, TAKE_PROFIT, STOP_MARKET, TAKE_PROFIT_MARKET, TRAILING_STOP_MARKET
 */
public enum Type {
	
	/**
	 * 限价单
	 */
	LIMIT("LIMIT"),
	
	/**
	 * 市价单
	 */
	MARKET("MARKET"), 
	
	/**
	 * 止损限价单
	 */
	STOP("STOP"), 
	
	/**
	 * 止盈限价单
	 */
	TAKE_PROFIT("TAKE_PROFIT"), 
	
	/**
	 * 止损市价单
	 */
	STOP_MARKET("STOP_MARKET"), 
	
	/**
	 * 止盈市价单
	 */
	TAKE_PROFIT_MARKET("TAKE_PROFIT_MARKET"), 
	
	/**
	 * 跟踪止损单
	 */
	TRAILING_STOP_MARKET("TRAILING_STOP_MARKET");

	private String type;
	
	Type(String type) {
		this.type = type;
	}
	
	public String value() {
		return this.type;
	}
}
