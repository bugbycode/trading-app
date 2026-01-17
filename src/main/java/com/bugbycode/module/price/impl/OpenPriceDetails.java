package com.bugbycode.module.price.impl;

import com.bugbycode.module.FibCode;
import com.bugbycode.module.price.OpenPrice;

public class OpenPriceDetails implements OpenPrice {

	private FibCode code;
	
	private double price;
	
	private double stopLossLimit;
	
	private double firstTakeProfit;//第一止盈点
	
	private double secondTakeProfit;//第二止盈点
	
	/**
	 * 开仓价格信息
	 * @param code 所处的回撤点
	 * @param price 开仓价
	 */
	public OpenPriceDetails(FibCode code, double price) {
		this.code = code;
		this.price = price;
		this.stopLossLimit = -1;
	}
	
	/**
	 * 开仓价格信息
	 * @param code 所处的回撤点
	 * @param price 开仓价
	 * @param stopLoss 最佳止损点
	 */
	public OpenPriceDetails(FibCode code, double price, double stopLossLimit) {
		this.code = code;
		this.price = price;
		this.stopLossLimit = stopLossLimit;
	}
	
	/**
	 * 开仓价格信息
	 * @param code 所处的回撤点
	 * @param price 开仓价
	 * @param stopLoss 最佳止损点
	 * @param firstTakeProfit 第一止盈点
	 * @param secondTakeProfit 第二止盈点
	 */
	public OpenPriceDetails(FibCode code, double price, double stopLossLimit,
			double firstTakeProfit, double secondTakeProfit) {
		this.code = code;
		this.price = price;
		this.stopLossLimit = stopLossLimit;
		this.firstTakeProfit = firstTakeProfit;
		this.secondTakeProfit = secondTakeProfit;
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
		return "OpenPrice: " + code.getValue() + "(" + price + "), StopLossLimit: " + stopLossLimit;
	}

	@Override
	public double getStopLossLimit() {
		return this.stopLossLimit;
	}

	public double getFirstTakeProfit() {
		return firstTakeProfit;
	}

	public double getSecondTakeProfit() {
		return secondTakeProfit;
	}

}
