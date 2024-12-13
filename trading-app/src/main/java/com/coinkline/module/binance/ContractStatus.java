package com.coinkline.module.binance;

/**
 * 合约状态
 */
public enum ContractStatus {
	
	/**
	 * 待上市
	 */
	PENDING_TRADING("PENDING_TRADING","待上市"),
	
	/**
	 * 交易中
	 */
	TRADING("TRADING","交易中"),
	
	/**
	 * 预交割
	 */
	PRE_DELIVERING("PRE_DELIVERING","预交割"),
	
	/**
	 * 交割中
	 */
	DELIVERING("DELIVERING","交割中"),
	
	/**
	 * 已交割
	 */
	DELIVERED("DELIVERED","已交割"),
	
	/**
	 * 预结算
	 */
	PRE_SETTLE("PRE_SETTLE","预结算"),
	
	/**
	 * 结算中
	 */
	SETTLING("SETTLING","结算中"),
	
	/**
	 * 已下架
	 */
	CLOSE("CLOSE","已下架"),
	
	/**
	 * 未知状态
	 */
	UNKNOWN("UNKNOWN","未知状态");

	private String value;
	
	private String memo;
	
	ContractStatus(String value,String memo) {
		this.value = value;
		this.memo = memo;
	}

	public String getValue() {
		return value;
	}
	
	public String getMemo() {
		return memo;
	}

	public static ContractStatus resolve(String value) {
		ContractStatus[] arr = values();
		for(ContractStatus status : arr) {
			if(status.getValue().equals(value)) {
				return status;
			}
		}
		return UNKNOWN;
	}
}
