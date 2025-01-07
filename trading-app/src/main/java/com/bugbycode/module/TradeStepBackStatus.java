package com.bugbycode.module;

/**
 * 回踩单交易开关状态
 */
public enum TradeStepBackStatus {

	/**
	 * 开启
	 */
	OPEN(1),
	
	/**
	 * 关闭
	 */
	CLOSE(0);

	private int value;
	
	TradeStepBackStatus(int value) {
		this.value = value;
	}
	
	public int getValue() {
		return this.value;
	}
	
	public static TradeStepBackStatus valueOf(int v) {
		return v == 1 ? OPEN : CLOSE;
	}
}
