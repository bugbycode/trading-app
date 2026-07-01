package com.bugbycode.module;

/**
 * 双向持仓启用状态
 */
public enum DualSidePositionStatus {
	
	/**
	 * 开启
	 */
	OPEN(1),
	
	/**
	 * 关闭
	 */
	CLOSE(0);

	private int value;
	
	DualSidePositionStatus(int value) {
		this.value = value;
	}
	
	public int getValue() {
		return this.value;
	}
	
	public static DualSidePositionStatus valueOf(int v) {
		return v == 1 ? OPEN : CLOSE;
	}
}
