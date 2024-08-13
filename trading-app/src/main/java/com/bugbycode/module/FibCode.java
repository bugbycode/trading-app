package com.bugbycode.module;

/**
 * 斐波那契回撤比例
 */
public enum FibCode {
	
	FIB0(0,"0"),
	FIB236(0.236,"0.236"),
	FIB382(0.382,"0.382"),
	FIB5(0.5,"0.5"),
	FIB618(0.618,"0.618"),
	FIB66(0.66,"0.66"),
	FIB786(0.786,"0.786"),
	FIB1(1,"1");
	
	private double value;
	
	private String description;

	FibCode(double value,String description) {
		this.value = value;
		this.description = description;
	}
	
	public double getValue() {
		return this.value;
	}

	public String getDescription() {
		return this.description;
	}
}
