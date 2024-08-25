package com.bugbycode.config;

import java.util.List;
import java.util.Set;

import com.bugbycode.module.EmailAuth;

public class AppConfig {
	
	public static String REST_BASE_URL;
	
	public static Set<String> PAIRS;
	
	private static List<EmailAuth> EMAIL_AUTH;//邮件认证信息
	
	public static String SMTP_HOST;//服务器
	
	public static int SMTP_PORT;//端口
	
	public static Set<String> RECIPIENT;//收件人
	
	public static String CACHE_PATH = "/usr/local/cache";//缓存路径
	
	private static int EMAIL_AUTH_USE_OFFSET = 0;//邮件认证使用
	
	public static EmailAuth getEmailAuth() {
		return EMAIL_AUTH.get(EMAIL_AUTH_USE_OFFSET);
	}
	
	//获取下一项认证信息
	public static void nexEmailAuth() {
		
		EMAIL_AUTH_USE_OFFSET++;
		
		if(EMAIL_AUTH_USE_OFFSET == EMAIL_AUTH.size()) {
			EMAIL_AUTH_USE_OFFSET = 0;
		}
		
	}
	
	public static void setEmailAuth(List<EmailAuth> authList) {
		if(EMAIL_AUTH == null) {
			EMAIL_AUTH = authList;
		}
	}
}
