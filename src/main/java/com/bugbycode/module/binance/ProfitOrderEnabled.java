package com.bugbycode.module.binance;

/**
 * 止盈订单启用状态
 */
public enum ProfitOrderEnabled {

	/**
	 * 开启
	 */
	OPEN(1),
	
	/**
	 * 关闭
	 */
	CLOSE(0);

	private int value;
	
	ProfitOrderEnabled(int value) {
		this.value = value;
	}
	
	public int getValue() {
		return this.value;
	}
	
	public static ProfitOrderEnabled valueOf(int v) {
		return v == 1 ? OPEN : CLOSE;
	}
}
