package com.bugbycode.module;

/**
 * 交易频率枚举信息类
 */
public enum TradeFrequency {

	/**
	 * 低频交易
	 */
	LOW("低频交易"),
	
	/**
	 * 高频交易
	 */
	HIGH("高频交易");
	
	private String memo;

	TradeFrequency(String memo) {
		this.memo = memo;
	}

	public String getMemo() {
		return memo;
	}
	
}
