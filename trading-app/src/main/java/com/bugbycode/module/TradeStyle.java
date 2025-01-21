package com.bugbycode.module;

/**
 * 交易风格
 */
public enum TradeStyle {
	
	/**
	 * 保守
	 */
	CONSERVATIVE(0),
	
	/**
	 * 激进
	 */
	RADICAL(1);
	
	private int value;
	
	private TradeStyle(int value) {
		this.value = value;
	}
	
	public int getValue() {
		return this.value;
	}
	
	public static TradeStyle valueOf(int value) {
		return value == 0 ? CONSERVATIVE : RADICAL;
	}
}
