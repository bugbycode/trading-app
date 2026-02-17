package com.bugbycode.module.binance;

/**
 * 合约类型
 */
public enum ContractType {

	/**
	 * 永续合约
	 */
	PERPETUAL("PERPETUAL","永续合约"),
	
	/**
	 * 传统金融永续合约
	 */
	TRADIFI_PERPETUAL("TRADIFI_PERPETUAL", "传统金融永续合约"),
	
	/**
	 * 当月交割合约
	 */
	CURRENT_MONTH("CURRENT_MONTH","当月交割合约"),
	
	/**
	 * 次月交割合约
	 */
	NEXT_MONTH("NEXT_MONTH","次月交割合约"),
	
	/**
	 * 当季交割合约
	 */
	CURRENT_QUARTER("CURRENT_QUARTER","当季交割合约"),
	
	/**
	 * 次季交割合约
	 */
	NEXT_QUARTER("NEXT_QUARTER","次季交割合约"),
	
	/**
	 * 交割结算中合约
	 */
	PERPETUAL_DELIVERING("PERPETUAL_DELIVERING","交割结算中合约"),
	
	/**
	 * 欧式期权
	 */
	E_OPTIONS("E_OPTIONS","欧式期权"),
	
	/**
	 * 未知合约
	 */
	UNKNOWN("UNKNOWN","未知合约"),
	;
	
	private String value;
	
	private String memo;
	
	ContractType(String value,String memo) {
		this.value = value;
		this.memo = memo;
	}

	public String getValue() {
		return value;
	}
	
	public String getMemo() {
		return memo;
	}

	public static ContractType resolve(String value) {
		ContractType[] arr = values();
		for(ContractType type : arr) {
			if(type.getValue().equals(value)) {
				return type;
			}
		}
		return UNKNOWN;
	}
}
