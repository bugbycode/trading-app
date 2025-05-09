package com.bugbycode.trading_app;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.bugbycode.module.Regex;
import com.util.RegexUtil;

public class EmailRegTest {

	public static void main(String[] args) {
        String text = "65535";
        System.out.println(RegexUtil.test(text, Regex.PORT));
	}

}
