package com.bugbycode.module;

public enum FollowMasterStatus {
	
	/**
	 * 开启
	 */
	OPEN(1),
	
	/**
	 * 关闭
	 */
	CLOSE(0);

	private int value;
	
	FollowMasterStatus(int value) {
		this.value = value;
	}
	
	public int getValue() {
		return this.value;
	}
	
	public static FollowMasterStatus valueOf(int value) {
		return value == 1 ? OPEN : CLOSE;
	}
}
