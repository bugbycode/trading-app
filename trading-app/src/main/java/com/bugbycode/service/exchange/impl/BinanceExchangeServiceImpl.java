package com.bugbycode.service.exchange.impl;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.bugbycode.config.AppConfig;
import com.bugbycode.module.binance.SymbolExchangeInfo;
import com.bugbycode.service.exchange.BinanceExchangeService;
import com.bugbycode.service.user.UserService;
import com.bugbycode.trading_app.pool.WorkTaskPool;
import com.bugbycode.trading_app.task.email.SendMailTask;
import com.util.DateFormatUtil;
import com.util.StringUtil;

@Service("binanceExchangeService")
public class BinanceExchangeServiceImpl implements BinanceExchangeService {

	private final Logger logger = LogManager.getLogger(BinanceExchangeServiceImpl.class);
	
	@Autowired
	private RestTemplate restTemplate;
	
	@Autowired
	private UserService userDetailsService;
	
	@Autowired
	private WorkTaskPool emailWorkTaskPool;
	
	@Override
	public Set<String> exchangeInfo() {
		Set<String> pairs = new HashSet<String>();
		//
		String resultStr = restTemplate.getForObject(AppConfig.REST_BASE_URL + "/fapi/v1/exchangeInfo", String.class);
		JSONObject result = new JSONObject(resultStr);
		if(result.has("symbols")) {
			JSONArray symbolJsonArray = result.getJSONArray("symbols");
			symbolJsonArray.forEach(item -> {
				if(item instanceof JSONObject) {
					JSONObject symbolJson = (JSONObject) item;
					String contractType = symbolJson.getString("contractType");
					String status = symbolJson.getString("status");
					String symbol = symbolJson.getString("symbol");
					JSONArray filters = symbolJson.getJSONArray("filters");
					if("PERPETUAL".equals(contractType) && "TRADING".equals(status)) {
						pairs.add(symbol);
						SymbolExchangeInfo info = new SymbolExchangeInfo();
						info.setSymbol(symbol);
						
						filters.forEach(filter -> {
							JSONObject f = (JSONObject) filter;
							String filterType = f.getString("filterType");
							if("LOT_SIZE".equals(filterType)) {//限价单交易规则
								info.setLot_stepSize(f.getDouble("stepSize"));
								info.setLot_minQty(f.getDouble("minQty"));
								info.setLot_maxQty(f.getDouble("maxQty"));
							} else if("MARKET_LOT_SIZE".equals(filterType)) {//市价单交易规则
								info.setLot_market_stepSize(f.getDouble("stepSize"));
								info.setLot_market_minQty(f.getDouble("minQty"));
								info.setLot_market_maxQty(f.getDouble("maxQty"));
							} else if("MIN_NOTIONAL".equals(filterType)) {//最小名义价值
								info.setMin_notional(f.getDouble("notional"));
							}
						});
						
						AppConfig.SYMBOL_EXCHANGE_INFO.put(symbol, info);
					}
					
					//待上市代币
					if("PENDING_TRADING".equals(status)) {
						if(!StringUtil.contains(AppConfig.PENDING_TRADING_SET, symbol)) {
							
							AppConfig.PENDING_TRADING_SET.add(symbol);
							
							String dateStr = DateFormatUtil.format(new Date());
							String recEmail = userDetailsService.getAllUserEmail();
							
							String text = String.format("币安即将上市%s %s", symbol, dateStr);
							
							emailWorkTaskPool.add(new SendMailTask(text, text, recEmail));
						}
					}
				}
			});
		}
		
		logger.debug(resultStr);
		logger.debug(pairs);
		return pairs;
	}

}
