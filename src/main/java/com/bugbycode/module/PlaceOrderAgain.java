package com.bugbycode.module;

/**
 * 是否再次下单 用于当下单失败时选择是否再次下单
 */
public enum PlaceOrderAgain {
	
	/**
	 * 开启
	 */
	OPEN(1),
	
	/**
	 * 关闭
	 */
	CLOSE(0);

	private int value;
	
	PlaceOrderAgain(int value) {
		this.value = value;
	}

	public int getValue() {
		return value;
	}
	
	
}
