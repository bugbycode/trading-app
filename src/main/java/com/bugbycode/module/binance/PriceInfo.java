package com.bugbycode.module.binance;

import java.math.BigDecimal;

/**
 * 价格信息
 */
public class PriceInfo {

	private final String symbol;
	
	private final String price;
	
	private final long time;

	public PriceInfo(String symbol, String price, long time) {
		this.symbol = symbol;
		this.price = price;
		this.time = time;
	}

	public String getSymbol() {
		return symbol;
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

	public long getTime() {
		return time;
	}

}
