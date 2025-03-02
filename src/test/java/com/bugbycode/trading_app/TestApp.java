package com.bugbycode.trading_app;

import java.text.ParseException;
import java.util.HashSet;
import java.util.Set;

import com.bugbycode.module.FibCode;
import com.bugbycode.module.FibInfo;
import com.bugbycode.module.FibLevel;
import com.util.PriceUtil;
public class TestApp {

	public static void main(String[] args) throws ParseException {
		Set<String> set = new HashSet<String>();
		set.add("BTCUSDT");
		set.add("DOGEUSDT");
		
		System.out.println(set.contains(new String("BTCUSDT")));
		System.out.println(set.contains(new String("LTCUSDT")));
	}

}
