package com.bugbycode.module;

/**
 * 斐波那契回撤级别
 */
public enum FibLevel {

	LEVEL_1(1,"Lv1"),
	LEVEL_2(1,"Lv2"),
	LEVEL_3(1,"Lv3"),
	LEVEL_4(1,"Lv4");

	private int value;
	
	private String label;
	
	FibLevel(int value, String label) {
		this.value = value;
		this.label = label;
	}

	public int getValue() {
		return value;
	}

	public String getLabel() {
		return label;
	}
	
	
}
