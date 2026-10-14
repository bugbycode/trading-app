package com.bugbycode.module.trading;

/**
 * 买卖方向 SELL, BUY
 */
public enum Side {

	BUY("BUY"),
	SELL("SELL");

	private String side;
	
	Side(String side) {
		this.side = side;
	}
	
	public String value() {
		return this.side;
	}
	
	public static Side parse(String value) {
		Side result = BUY;
		Side[] arr = Side.values();
		for(Side side : arr) {
			if(side.value().equals(value)) {
				result = side;
				break;
			}
		}
		return result;
	}
}
