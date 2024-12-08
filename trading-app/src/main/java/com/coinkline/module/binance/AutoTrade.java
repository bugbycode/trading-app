package com.coinkline.module.binance;

/**
 * 是否开启自动交易
 */
public enum AutoTrade {
	/**
	 * 开启
	 */
	OPEN(1,"开启"),
	
	/**
	 * 关闭
	 */
	CLOSE(0,"关闭");

	private int value;
	
	private String memo;
	
	AutoTrade(int value,String memo) {
		this.value = value;
		this.memo = memo;
	}
	
	public int value() {
		return this.value;
	}
	
	public String memo() {
		return this.memo;
	}
	
	public static AutoTrade valueOf(int value) {
		AutoTrade[] arr = values();
		for(AutoTrade at : arr) {
			if(at.value() == value) {
				return at;
			}
		}
		return CLOSE;
	}
}
