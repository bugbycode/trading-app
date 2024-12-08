package com.coinkline.module;

public enum Inerval {

	INERVAL_5M(5,"5m","5分钟"),

	INERVAL_15M(15,"15m","15分钟"),
	
	INERVAL_1H(1,"1h","1小时"),
	
	INERVAL_4H(1,"4h","4小时"),
	
	INERVAL_1D(1,"1d","日线"),
	
	INERVAL_1W(1,"1w","周线");
	
	private int number;
	
	private String descption;
	
	private String memo;
	
	private Inerval(int number,String descption,String memo) {
		this.number = number;
		this.descption = descption;
		this.memo = memo;
	}

	public int getNumber() {
		return number;
	}

	public String getDescption() {
		return descption;
	}

	public String getMemo() {
		return memo;
	}
	
	public static Inerval resolve(String des) {
		Inerval[] arr = values();
		for(int index = 0; index < arr.length; index++) {
			if(arr[index].getDescption().equals(des)) {
				return arr[index];
			}
		}
		throw new RuntimeException("k线级别信息错误");
	}
}
