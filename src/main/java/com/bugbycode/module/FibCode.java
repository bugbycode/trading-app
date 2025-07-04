package com.bugbycode.module;

/**
 * 斐波那契回撤比例
 */
public enum FibCode {

	//FIB4_764(4.764,"4.764", false),
	FIB4_618(4.618,"4.618", true),
	/*FIB4_414(4.414,"4.414", false),
	FIB4_272(4.272,"4.272", false),
	FIB4_236(4.236,"4.236", false),
	FIB4(4,"4", false),
	*/
	FIB3_618(3.618,"3.618", true),
	/*FIB3_414(3.414,"3.414", false),
	FIB3_272(3.272,"3.272", false),
	FIB3(3,"3", false),
	*/
	FIB2_618(2.618,"2.618", true),
	//FIB2_414(2.414,"2.414", false),
	//FIB2_272(2.272,"2.272", false),
	FIB2(2,"2", true),

	FIB1_618(1.618,"1.618", true),
	//FIB1_414(1.414,"1.414", false),
	FIB1_272(1.272,"1.272", true),
	
	FIB1(1,"1", true),
	FIB786(0.786,"0.786", true),
	FIB66(0.66,"0.66", true),
	FIB618(0.618,"0.618", true),
	FIB5(0.5,"0.5", true),
	FIB382(0.382,"0.382", true),
	FIB236(0.236,"0.236", true),
	FIB0(0,"0", false);
	
	private double value;
	
	private String description;
	
	private boolean trade;

	FibCode(double value,String description, boolean trade) {
		this.value = value;
		this.description = description;
		this.trade = trade;
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
	
	public boolean isTrade() {
		return trade;
	}
}