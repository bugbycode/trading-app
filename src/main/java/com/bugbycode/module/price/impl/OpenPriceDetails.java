package com.bugbycode.module.price.impl;

import com.bugbycode.module.FibCode;
import com.bugbycode.module.price.OpenPrice;

public class OpenPriceDetails implements OpenPrice {

	private FibCode code;
	
	private double price;
	
	private double stopLoss;
	
	/**
	 * 开仓价格信息
	 * @param code 所处的回撤点
	 * @param price 开仓价
	 */
	public OpenPriceDetails(FibCode code, double price) {
		this.code = code;
		this.price = price;
		this.stopLoss = -1;
	}
	
	/**
	 * 开仓价格信息
	 * @param code 所处的回撤点
	 * @param price 开仓价
	 * @param stopLoss 最佳止损点
	 */
	public OpenPriceDetails(FibCode code, double price, double stopLoss) {
		this.code = code;
		this.price = price;
		this.stopLoss = stopLoss;
	}

	@Override
	public double getPrice() {
		return price;
	}

	@Override
	public FibCode getCode() {
		return code;
	}

	@Override
	public String toString() {
		return "OpenPrice: " + code.getValue() + "(" + price + "), StopLossLimit: " + stopLoss;
	}

	@Override
	public double getStopLoss() {
		return this.stopLoss;
	}

}
