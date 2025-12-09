package com.bugbycode.module;

public enum AlgoType {
	
	CONDITIONAL("CONDITIONAL");

	private String type;
	
	AlgoType(String type) {
		this.type = type;
	}
	
	public String value() {
		return this.type;
	}
	
}
