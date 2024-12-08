package com.coinkline.module.binance;

import java.math.BigDecimal;

/**
 * 价格信息
 */
public class PriceInfo {

	private String symbol;
	
	private String price;
	
	private long time;

	public PriceInfo(String symbol, String price, long time) {
		this.symbol = symbol;
		this.price = price;
		this.time = time;
	}

	public String getSymbol() {
		return symbol;
	}

	public void setSymbol(String symbol) {
		this.symbol = symbol;
	}

	public String getPrice() {
		return price;
	}
	
	public double getPriceDoubleValue() {
		return Double.valueOf(price);
	}
	
	public BigDecimal getPriceBigDecimalValue() {
		return new BigDecimal(price);
	}

	public void setPrice(String price) {
		this.price = price;
	}

	public long getTime() {
		return time;
	}

	public void setTime(long time) {
		this.time = time;
	}
	
}
