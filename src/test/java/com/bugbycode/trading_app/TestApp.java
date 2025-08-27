package com.bugbycode.trading_app;

import java.text.ParseException;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import com.bugbycode.module.BreakthroughTradeStatus;
import com.bugbycode.module.FibCode;
import com.bugbycode.module.FibInfo;
import com.bugbycode.module.FibLevel;
import com.bugbycode.module.Inerval;
import com.util.DateFormatUtil;
import com.util.PriceUtil;
public class TestApp {

	public static void main(String[] args) throws ParseException {
		System.out.println(PriceUtil.calculateLongActivationPrice(100, 3));
		System.out.println(PriceUtil.calculateShortActivationPrice(100, 3));
	}

}
