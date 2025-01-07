package com.bugbycode.module;

/**
 * 接收交易通知开关状态
 */
public enum RecvTradeStatus {

	/**
	 * 开启
	 */
	OPEN(1),
	
	/**
	 * 关闭
	 */
	CLOSE(0);

	private int value;
	
	RecvTradeStatus(int value) {
		this.value = value;
	}
	
	public int getValue() {
		return this.value;
	}
	
	public static RecvTradeStatus valueOf(int v) {
		return v == 1 ? OPEN : CLOSE;
	}
}
