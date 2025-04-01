package com.bugbycode.module;

/**
 * 斐波那契回撤比例
 */
public enum FibCode {

	//FIB4_764(4.764,"4.764"),
	FIB4_618(4.618,"4.618"),
	/*FIB4_414(4.414,"4.414"),
	FIB4_272(4.272,"4.272"),
	FIB4_236(4.236,"4.236"),
	FIB4(4,"4"),

	FIB3_618(3.618,"3.618"),
	FIB3_414(3.414,"3.414"),
	FIB3_272(3.272,"3.272"),
	FIB3(3,"3"),*/

	FIB2_618(2.618,"2.618"),
	/*FIB2_414(2.414,"2.414"),
	FIB2_272(2.272,"2.272"),*/
	FIB2(2,"2"),

	FIB1_618(1.618,"1.618"),
	/*FIB1_414(1.414,"1.414"),*/
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
	
	/**
	 * 小于
	 * @param code
	 * @return
	 */
	public boolean lt(FibCode code) {
		return this.getValue() < code.getValue();
	}
	
	/**
	 * 小于等于
	 * @param code
	 * @return
	 */
	public boolean lte(FibCode code) {
		return this.getValue() <= code.getValue();
	}
	
	/**
	 * 大于
	 * @param code
	 * @return
	 */
	public boolean gt(FibCode code) {
		return this.getValue() > code.getValue();
	}
	
	/**
	 * 大于等于
	 * @param code
	 * @return
	 */
	public boolean gte(FibCode code) {
		return this.getValue() >= code.getValue();
	}
	
	/**
	 * 比较是否相等
	 * @param code
	 * @return
	 */
	public boolean equalsValue(FibCode code) {
		return this.getValue() == code.getValue();
	}
}
