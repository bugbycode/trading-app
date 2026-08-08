package com.bugbycode.module;

public enum AutoClosePosition {
	
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
	
	AutoClosePosition(int value,String memo) {
		this.value = value;
		this.memo = memo;
	}
	
	public int value() {
		return this.value;
	}
	
	public String memo() {
		return this.memo;
	}
	
	public static AutoClosePosition valueOf(int value) {
		AutoClosePosition[] arr = values();
		for(AutoClosePosition at : arr) {
			if(at.value() == value) {
				return at;
			}
		}
		return CLOSE;
	}
}
