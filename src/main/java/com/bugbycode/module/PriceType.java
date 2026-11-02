package com.bugbycode.module;

/**
 * 价格类型
 */
public enum PriceType {

	HIGH("高点"),
	
	LOW("低点");

	private final String memo;
	
	PriceType(String memo) {
		this.memo = memo;
	}
	
	public String getMemo() {
		return this.memo;
	}
}
