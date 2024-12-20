package com.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.bugbycode.module.Regex;

public class RegexUtil {

	public static boolean test(String text, Regex regex) {
		return test(text, regex.getRegex());
	}
	
	public static boolean test(String text, String regex) {
		Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(text);
        return matcher.matches();
	}
}
