package com.bugbycode.module;

public enum PriceActionType {

	/**
	 * 常规看涨看跌吞没
	 */
	DEFAULT,
	
	/**
	 * 可能需要回补的看涨看跌吞没
	 */
	BACK,
	
	/**
	 * 颓势或强势价格行为
	 */
	DECL_POWER
}
