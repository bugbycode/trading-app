package com.bugbycode.trading_app;

import com.util.PriceUtil;

public class TestProfitPrice {

	public static void main(String[] args) {
		System.out.println(PriceUtil.calculateLongActivationPrice_v2(37.273, 7.5, 39.037));
		System.out.println(PriceUtil.calculateShortActivationPrice_v2(100, 7.5, 60));
	}

}
