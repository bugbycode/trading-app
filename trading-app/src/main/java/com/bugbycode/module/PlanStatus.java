package com.bugbycode.module;

public enum PlanStatus {

	VALID(0,"未触发"),
	IN_VALID(1,"已触发");
	
	private int value;
	
	private String memo;

	private PlanStatus(int value, String memo) {
		this.value = value;
		this.memo = memo;
	}

	public int getValue() {
		return value;
	}

	public String getMemo() {
		return memo;
	}
	
	public static PlanStatus resolve(int value) {
		PlanStatus[] arr = values();
		for(PlanStatus status : arr) {
			if(status.getValue() == value) {
				return status;
			}
		}
		throw new RuntimeException("计划状态信息错误");
	}
}
