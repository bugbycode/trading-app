package com.bugbycode.module.binance;

/**
 * 保证金模式 ISOLATED(逐仓), CROSSED(全仓)
 */
public enum MarginType {

	/**
	 * 逐仓
	 */
	ISOLATED("ISOLATED"),
	
	/**
	 * 全仓
	 */
	CROSSED("CROSSED");

	private String value;
	
	MarginType(String value) {
		this.value = value;
	}
	
	public String value() {
		return this.value;
	}
	
	public static MarginType resolve(String value) {
		MarginType[] arr = values();
		for(MarginType type : arr) {
			if(type.value().equals(value)) {
				return type;
			}
		}
		return ISOLATED;
	}
}
