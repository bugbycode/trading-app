package com.bugbycode.config;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.bugbycode.binance.module.eoptions.EoptionContracts;
import com.bugbycode.binance.module.fundingInfo.FundingInfo;
import com.bugbycode.module.binance.SymbolConfig;
import com.bugbycode.module.binance.SymbolExchangeInfo;
import com.bugbycode.websocket.realtime.endpoint.PerpetualWebSocketClientEndpoint;

public class AppConfig {
	
	public static boolean DEBUG = false;
	
	public static String REST_BASE_URL;
	
	public static String EOPTIONS_BASE_URL;
	
	public static String WEBSOCKET_URL;
	
	public static String WEBSOCKET_API_URL;
	
	public static String CACHE_PATH = "/usr/local/cache";//缓存路径
	
	public static final int BINANCE_REST_API_WEIGHT = 35;
	
	public static Map<String,FundingInfo> FUNDING_INFO = Collections.synchronizedMap(new HashMap<String,FundingInfo>());
	
	public static Map<String,List<SymbolConfig>> SYMBOL_CONFIG_INFO = Collections.synchronizedMap(new HashMap<String,List<SymbolConfig>>());
	
	//交易对交易规则
	public static Map<String,SymbolExchangeInfo> SYMBOL_EXCHANGE_INFO = Collections.synchronizedMap(new HashMap<String, SymbolExchangeInfo>());
	
	//期权底层资产信息
	public static Map<String,EoptionContracts> EOPTION_EXCHANGE_INFO = Collections.synchronizedMap(new HashMap<String, EoptionContracts>());
		
	public static String RECAPTCHA_SECRET;
	
	//记录已订阅完成的websocket连接
	public static Map<Long, List<PerpetualWebSocketClientEndpoint>> SYNC_FINISH_WEBSOCKET_CLIENT = Collections.synchronizedMap(new HashMap<Long, List<PerpetualWebSocketClientEndpoint>>());
}
