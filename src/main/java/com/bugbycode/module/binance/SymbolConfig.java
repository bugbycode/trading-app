package com.bugbycode.module.binance;

/**
 * 交易对配置
 */
public class SymbolConfig {

	private String symbol;
	
	private String marginType;
	
	private boolean isAutoAddMargin;
	
	private int leverage;
	
	private String maxNotionalValue;

	public SymbolConfig(String symbol, String marginType, boolean isAutoAddMargin, int leverage,
			String maxNotionalValue) {
		this.symbol = symbol;
		this.marginType = marginType;
		this.isAutoAddMargin = isAutoAddMargin;
		this.leverage = leverage;
		this.maxNotionalValue = maxNotionalValue;
	}

	public String getSymbol() {
		return symbol;
	}

	public void setSymbol(String symbol) {
		this.symbol = symbol;
	}

	public String getMarginType() {
		return marginType;
	}

	public void setMarginType(String marginType) {
		this.marginType = marginType;
	}

	public boolean isAutoAddMargin() {
		return isAutoAddMargin;
	}

	public void setAutoAddMargin(boolean isAutoAddMargin) {
		this.isAutoAddMargin = isAutoAddMargin;
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
	
	
}
