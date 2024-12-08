package com.coinkline.module.binance;

public enum AutoTradeType {
	
	/**
	 * 斐波那契回撤
	 */
	FIB_RET(0,"斐波那契回撤"),
	
	/**
	 * 价格行为
	 */
	PRICE_ACTION(1,"价格行为"),
	
	/**
	 * EMA指标
	 */
	EMA_INDEX(2,"EMA指标"),
	
	/**
	 * 盘整区间
	 */
	AREA_INDEX(3, "盘整区间"),
	
	/**
	 * 画线交易
	 */
	DEFAULT(4, "画线交易");

	private int value;
	
	private String memo;
	
	AutoTradeType(int value, String memo) {
		this.value = value;
		this.memo = memo;
	}
	
	public int value() {
		return this.value;
	}
	
	public String memo() {
		return this.memo;
	}
	
	public static AutoTradeType valueOf(int value) {
		AutoTradeType[] arr = values();
		for(AutoTradeType type : arr) {
			if(type.value() == value) {
				return type;
			}
		}
		return PRICE_ACTION;
	}
}
