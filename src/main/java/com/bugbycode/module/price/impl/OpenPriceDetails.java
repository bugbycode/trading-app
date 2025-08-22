package com.bugbycode.module.price.impl;

import com.bugbycode.module.FibCode;
import com.bugbycode.module.price.OpenPrice;

public class OpenPriceDetails implements OpenPrice {

	private FibCode code;
	
	private double price;
	
	/**
	 * 开仓价格信息
	 * @param code 所处的回撤点
	 * @param price 开仓价
	 */
	public OpenPriceDetails(FibCode code, double price) {
		this.code = code;
		this.price = price;
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
		return code.getValue() + "(" + price + ")";
	}

}
