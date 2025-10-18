package com.bugbycode.module;

/**
 * 策略类型
 */
public enum PolicyType {
	
	ALLOW(1, "允许"),//允许
	
	DENY(0, "拒绝") //拒绝
;

	private int value;
	
	private String memo;
	
	PolicyType(int value,String memo) {
		this.value = value;
		this.memo = memo;
	}

	public String getMemo() {
		return memo;
	}

	public int getValue() {
		return value;
	}
	
	public static PolicyType valueOf(int value) {
		return value == 1 ? ALLOW : DENY;
	}
}
