package com.bugbycode.module;

/**
 * 交易风格
 */
public enum TradeStyle {
	
	/**
	 * 保守
	 */
	CONSERVATIVE(0,"保守"),
	
	/**
	 * 激进
	 */
	RADICAL(1,"激进");
	
	private int value;
	
	private String memo;
	
	private TradeStyle(int value, String memo) {
		this.value = value;
		this.memo = memo;
	}
	
	public int getValue() {
		return this.value;
	}
	
	public String getMemo() {
		return memo;
	}

	public static TradeStyle valueOf(int value) {
		return value == 0 ? CONSERVATIVE : RADICAL;
	}
}
