package com.bugbycode.module;

/**
 * 交易逆势单开关状态
 */
public enum CountertrendTradingStatus {
	
	/**
	 * 开启
	 */
	OPEN(1),
	
	/**
	 * 关闭
	 */
	CLOSE(0);

	private int value;
	
	CountertrendTradingStatus(int value) {
		this.value = value;
	}
	
	public int getValue() {
		return this.value;
	}
	
	public static CountertrendTradingStatus valueOf(int value) {
		return value == 1 ? OPEN : CLOSE;
	}
}
