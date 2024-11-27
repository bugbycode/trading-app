package com.bugbycode.module.binance;

/**
 * 保证金模式 ISOLATED(逐仓), CROSSED(全仓)
 */
public enum MarginType {

	/**
	 * 逐仓
	 */
	ISOLATED,
	
	/**
	 * 全仓
	 */
	CROSSED
}
