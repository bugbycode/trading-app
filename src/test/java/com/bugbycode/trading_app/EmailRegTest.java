package com.bugbycode.trading_app;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.bugbycode.module.Regex;
import com.util.RegexUtil;

public class EmailRegTest {

	public static void main(String[] args) {
		
		String email = "bugbycode@gmail.com";
        String regex = "^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$";
        
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(email);
        
        if (matcher.matches()) {
            System.out.println("邮箱地址有效");
        } else {
            System.out.println("邮箱地址无效");
        }
        
        System.out.println(RegexUtil.test(email, Regex.EMAIL));
	}

}
