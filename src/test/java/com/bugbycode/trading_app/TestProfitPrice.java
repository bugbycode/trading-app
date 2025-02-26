package com.bugbycode.trading_app;

import com.util.PriceUtil;

public class TestProfitPrice {

	public static void main(String[] args) {
		System.out.println(PriceUtil.getLongTakeProfitForPercent(100, 0.04));
		System.out.println(PriceUtil.getShortTakeProfitForPercent(100, 0.04));
	}

}
