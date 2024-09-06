package com.bugbycode.module;

/**
 * 斐波那契回撤比例
 */
public enum FibCode {
	
	FIB1_618(1.618,"1.618"),
	FIB1_414(1.414,"1.414"),
	FIB1_272(1.272,"1.272"),
	
	FIB1(1,"1"),
	FIB786(0.786,"0.786"),
	FIB66(0.66,"0.66"),
	FIB618(0.618,"0.618"),
	FIB5(0.5,"0.5"),
	FIB382(0.382,"0.382"),
	FIB236(0.236,"0.236"),
	FIB0(0,"0");
	
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
