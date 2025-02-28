package com.bugbycode.module;

/**
 * 成交量监控状态
 */
public enum VolumeMonitorStatus {

	/**
	 * 开启
	 */
	OPEN(1),
	
	/**
	 * 关闭
	 */
	CLOSE(0);

	private int value;

	VolumeMonitorStatus(int value) {
		this.value = value;
	}

	public int getValue() {
		return value;
	}
	
	public static VolumeMonitorStatus valueOf(int value) {
		return value == 1 ? OPEN : CLOSE;
	}
}
