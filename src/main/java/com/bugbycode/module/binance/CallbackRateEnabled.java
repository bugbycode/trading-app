package com.bugbycode.module.binance;

/**
 * 追踪止损启用状态
 */
public enum CallbackRateEnabled {
	/**
	 * 开启
	 */
	OPEN(1),
	
	/**
	 * 关闭
	 */
	CLOSE(0);

	private int value;
	
	CallbackRateEnabled(int value) {
		this.value = value;
	}
	
	public int getValue() {
		return this.value;
	}
	
	public static CallbackRateEnabled valueOf(int v) {
		return v == 1 ? OPEN : CLOSE;
	}
}
