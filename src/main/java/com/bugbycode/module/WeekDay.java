package com.bugbycode.module;

public enum WeekDay {
	
	SUNDAY(1, "星期日"), 
	MONDAY(2, "星期一") , 
	TUESDAY(3, "星期二") , 
	WEDNESDAY(4, "星期三") , 
	THURSDAY(5, "星期四") , 
	FRIDAY(6, "星期五") , 
	SATURDAY(7, "星期六");
	
	private int value;
	
	private String memo;
	
	private WeekDay(int value, String memo) {
		this.value = value;
		this.memo = memo;
	}
	
	public static WeekDay valueOf(int value) {
		WeekDay result = SUNDAY;
		WeekDay[] weeks = WeekDay.values();
		for(WeekDay w : weeks) {
			if(w.value() == value) {
				result = w;
				break;
			}
		}
		return result;
	}
	
	public int value() {
		return this.value;
	}
	
	public String memo() {
		return this.memo;
	}
}
