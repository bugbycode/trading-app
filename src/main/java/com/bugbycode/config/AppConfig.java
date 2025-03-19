package com.bugbycode.config;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.bugbycode.module.EmailAuth;
import com.bugbycode.module.binance.SymbolExchangeInfo;

public class AppConfig {
	
	public static String REST_BASE_URL;
	
	public static String WEBSOCKET_URL;
	
	public static String WEBSOCKET_API_URL;
	
	private static List<EmailAuth> EMAIL_AUTH;//邮件认证信息
	
	public static String SMTP_HOST;//服务器
	
	public static int SMTP_PORT;//端口
	
	public static Set<String> RECIPIENT;//收件人
	
	public static String CACHE_PATH = "/usr/local/cache";//缓存路径
	
	private static int EMAIL_AUTH_USE_OFFSET = 0;//邮件认证使用
	
	//交易对交易规则
	public static Map<String,SymbolExchangeInfo> SYMBOL_EXCHANGE_INFO = Collections.synchronizedMap(new HashMap<String, SymbolExchangeInfo>());
	
	public static String RECAPTCHA_SECRET;
	
	//记录15分钟级别k线同步批次
	public static Map<String,Long> SYNC_15M_KLINES_RECORD = Collections.synchronizedMap(new HashMap<String,Long>());
	
	//记录已经执行完同步的15分钟级别k线的交易对
	public static Set<String> SYNC_15M_KLINES_FINISH = new HashSet<String>();
	
	public static EmailAuth getEmailAuth() {
		return EMAIL_AUTH == null ? null : EMAIL_AUTH.get(EMAIL_AUTH_USE_OFFSET);
	}
	
	//获取下一项认证信息
	public static void nexEmailAuth() {
		if(EMAIL_AUTH != null) {
			
			EMAIL_AUTH_USE_OFFSET++;
			
			if(EMAIL_AUTH_USE_OFFSET >= EMAIL_AUTH.size()) {
				EMAIL_AUTH_USE_OFFSET = 0;
			}
			
		}
		
	}
	
	public static void setEmailAuth(List<EmailAuth> authList) {
		EMAIL_AUTH = authList;
	}
}
