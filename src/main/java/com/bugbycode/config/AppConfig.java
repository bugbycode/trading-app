package com.bugbycode.config;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.bugbycode.module.binance.SymbolExchangeInfo;

public class AppConfig {
	
	public static boolean DEBUG = false;
	
	public static String REST_BASE_URL;
	
	public static String WEBSOCKET_URL;
	
	public static String WEBSOCKET_API_URL;
	
	public static String CACHE_PATH = "/usr/local/cache";//缓存路径
	
	public static final int BINANCE_ALGO_ORDER_LIMIT = 200;//币安条件单数量限制
	
	//交易对交易规则
	public static Map<String,SymbolExchangeInfo> SYMBOL_EXCHANGE_INFO = Collections.synchronizedMap(new HashMap<String, SymbolExchangeInfo>());
	
	public static String RECAPTCHA_SECRET;
	
	//记录15分钟级别k线同步批次
	public static Map<String,Long> SYNC_15M_KLINES_RECORD = Collections.synchronizedMap(new HashMap<String,Long>());
	
	//记录已经执行完同步的15分钟级别k线的交易对
	public static Set<String> SYNC_15M_KLINES_FINISH = new HashSet<String>();
	
}
