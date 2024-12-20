package com.bugbycode.module;

/**
 * 行情订阅状态
 */
public enum MonitorStatus {
	/**
	 * 开启
	 */
	OPEN(1),
	
	/**
	 * 关闭
	 */
	CLOSE(0);
	
	private int value;

	MonitorStatus(int value) {
		this.value = value;
	}
	
	public int getValue() {
		return this.value;
	}
}
