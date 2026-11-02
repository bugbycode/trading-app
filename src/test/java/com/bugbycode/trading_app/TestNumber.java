package com.bugbycode.trading_app;

import java.math.BigDecimal;
import java.util.Date;

import com.bugbycode.module.binance.PriceInfo;
import com.util.LeverageBracketUtil;

public class TestNumber {

	public static void main(String[] args) {
		//PriceInfo priceInfo = new PriceInfo("BTCUSDT", "0.4", new Date().getTime());
		int leverage = 150;
		double price = 0.3009;
		double stopLoss = 0.2993;
		int resetLeverage = LeverageBracketUtil.resetLeverage(leverage, price, stopLoss);
		System.out.println(resetLeverage);
	}

}
