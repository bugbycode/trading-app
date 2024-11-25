package com.bugbycode.module.trading;

/**
 * 	订单类型 LIMIT, MARKET, STOP, TAKE_PROFIT, STOP_MARKET, TAKE_PROFIT_MARKET, TRAILING_STOP_MARKET
 */
public enum Type {
	LIMIT("LIMIT"), 
	MARKET("MARKET"), 
	STOP("STOP"), 
	TAKE_PROFIT("TAKE_PROFIT"), 
	STOP_MARKET("STOP_MARKET"), 
	TAKE_PROFIT_MARKET("TAKE_PROFIT_MARKET"), 
	TRAILING_STOP_MARKET("TRAILING_STOP_MARKET");

	private String type;
	
	Type(String type) {
		this.type = type;
	}
	
	public String value() {
		return this.type;
	}
}
