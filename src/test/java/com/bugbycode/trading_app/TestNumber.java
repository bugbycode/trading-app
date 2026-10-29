package com.bugbycode.trading_app;

import java.math.BigDecimal;
import java.util.Date;

import com.bugbycode.module.binance.PriceInfo;
import com.util.LeverageBracketUtil;

public class TestNumber {

	public static void main(String[] args) {
		PriceInfo priceInfo = new PriceInfo("BTCUSDT", "100000", new Date().getTime());
		int resetLeverage = LeverageBracketUtil.resetLeverage(150, priceInfo, new BigDecimal("101300"));
		System.out.println(resetLeverage);
	}

}
