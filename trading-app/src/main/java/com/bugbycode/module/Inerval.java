package com.bugbycode.module;

public enum Inerval {

	INERVAL_5M(5,"5m"),

	INERVAL_15M(5,"15m"),
	
	INERVAL_1H(1,"1h"),
	
	INERVAL_4H(1,"4h"),
	
	INERVAL_1D(1,"1d"),
	
	INERVAL_1W(1,"1w");
	
	private int number;
	
	private String descption;
	
	private Inerval(int number,String descption) {
		this.number = number;
		this.descption = descption;
	}

	public int getNumber() {
		return number;
	}

	public String getDescption() {
		return descption;
	}
}
