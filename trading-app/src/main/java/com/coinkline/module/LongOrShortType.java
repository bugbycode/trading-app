package com.coinkline.module;

public enum LongOrShortType {
	
	SHORT(0,"SHORT","做空"),
	LONG(1,"LONG","做多"),;
	
	private int value;
	private String label;
	private String memo;

	LongOrShortType(int value, String label, String memo) {
		this.value = value;
		this.label = label;
		this.memo = memo;
	}

	public int getValue() {
		return value;
	}

	public String getLabel() {
		return label;
	}

	public String getMemo() {
		return memo;
	}
	
	public static LongOrShortType resolve(int value) {
		LongOrShortType[] arr = values();
		for(LongOrShortType type : arr) {
			if(type.getValue() == value) {
				return type;
			}
		}
		throw new RuntimeException("仓位类型错误");
	}
	
	public static LongOrShortType resolve(String typeStr) {
		LongOrShortType[] arr = values();
		for(LongOrShortType type : arr) {
			if(type.getLabel().equals(typeStr)) {
				return type;
			}
		}
		throw new RuntimeException("仓位类型错误");
	}
}
