package com.bugbycode.module;

public enum EMAType {

	EMA7(7),
	EMA25(25),
	EMA99(99);
	
	private int value;

	EMAType(int value) {
		this.value = value;
	}

	public int getValue() {
		return value;
	}
	
}
