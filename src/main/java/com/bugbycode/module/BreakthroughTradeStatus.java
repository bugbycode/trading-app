package com.bugbycode.module;

/**
 * 突破交易开关状态
 */
public enum BreakthroughTradeStatus {

	/**
	 * 开启
	 */
	OPEN(1),
	
	/**
	 * 关闭
	 */
	CLOSE(0);

	private int value;
	
	BreakthroughTradeStatus(int value) {
		this.value = value;
	}
	
	public int getValue() {
		return this.value;
	}
	
	public static BreakthroughTradeStatus valueOf(int v) {
		return v == 1 ? OPEN : CLOSE;
	}
}
