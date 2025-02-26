package com.bugbycode.module;

/**
 * 接收PNL通知开关状态
 */
public enum RecvCrossUnPnlStatus {

	/**
	 * 开启
	 */
	OPEN(1),
	
	/**
	 * 关闭
	 */
	CLOSE(0);

	private int value;
	
	RecvCrossUnPnlStatus(int value) {
		this.value = value;
	}
	
	public int getValue() {
		return this.value;
	}
	
	public static RecvCrossUnPnlStatus valueOf(int v) {
		return v == 1 ? OPEN : CLOSE;
	}
}
