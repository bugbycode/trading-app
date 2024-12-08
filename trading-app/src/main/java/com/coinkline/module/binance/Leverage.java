package com.coinkline.module.binance;

/**
 * 交易对扛杆信息
 */
public class Leverage {

	private int leverage;
	
	private String maxNotionalValue;
	
	private String symbol;

	public Leverage(int leverage, String maxNotionalValue, String symbol) {
		this.leverage = leverage;
		this.maxNotionalValue = maxNotionalValue;
		this.symbol = symbol;
	}

	public int getLeverage() {
		return leverage;
	}

	public void setLeverage(int leverage) {
		this.leverage = leverage;
	}

	public String getMaxNotionalValue() {
		return maxNotionalValue;
	}

	public void setMaxNotionalValue(String maxNotionalValue) {
		this.maxNotionalValue = maxNotionalValue;
	}

	public String getSymbol() {
		return symbol;
	}

	public void setSymbol(String symbol) {
		this.symbol = symbol;
	}
	
	
}
