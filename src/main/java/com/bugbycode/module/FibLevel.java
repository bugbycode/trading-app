package com.bugbycode.module;

/**
 * 斐波那契回撤级别
 */
public enum FibLevel {

	LEVEL_1(1,"Lv1",FibCode.FIB382),
	LEVEL_2(2,"Lv2",FibCode.FIB5),
	LEVEL_3(3,"Lv3",FibCode.FIB618),
	LEVEL_4(4,"Lv4",FibCode.FIB618),;

	private int value;
	
	private String label;
	
	private FibCode startFibCode;
	
	FibLevel(int value, String label,FibCode startFibCode) {
		this.value = value;
		this.label = label;
		this.startFibCode = startFibCode;
	}

	public int getValue() {
		return value;
	}

	public String getLabel() {
		return label;
	}

	public FibCode getStartFibCode() {
		return startFibCode;
	}
	
}
