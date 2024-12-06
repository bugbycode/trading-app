package com.bugbycode.module.binance;

/**
 * 画线交易
 */
public enum DrawTrade {

	OPEN(1,"开启"),
	CLOSE(1,"开启");

	private int value;
	
	private String memo;
	
	DrawTrade(int value, String memo) {
		this.value = value;
		this.memo = memo;
	}

	public int getValue() {
		return value;
	}

	public String getMemo() {
		return memo;
	}
	
	public static DrawTrade valueOf(int value) {
		DrawTrade[] arr = values();
		for(DrawTrade dt : arr) {
			if(dt.getValue() == value) {
				return dt;
			}
		}
		return CLOSE;
	}
	
}
