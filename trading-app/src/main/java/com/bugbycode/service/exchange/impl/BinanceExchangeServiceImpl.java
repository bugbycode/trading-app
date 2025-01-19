package com.bugbycode.service.exchange.impl;

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
import com.bugbycode.module.binance.ContractStatus;
import com.bugbycode.module.binance.ContractType;
import com.bugbycode.module.binance.SymbolExchangeInfo;
import com.bugbycode.service.exchange.BinanceExchangeService;

@Service("binanceExchangeService")
public class BinanceExchangeServiceImpl implements BinanceExchangeService {

	private final Logger logger = LogManager.getLogger(BinanceExchangeServiceImpl.class);
	
	@Autowired
	private RestTemplate restTemplate;
	
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
					String statusStr = symbolJson.getString("status");
					String symbol = symbolJson.getString("symbol");
					//long onboardDate = symbolJson.getLong("onboardDate");
					JSONArray filters = symbolJson.getJSONArray("filters");
					
					ContractType type = ContractType.resolve(contractType);
					ContractStatus status = ContractStatus.resolve(statusStr);
					
					if(type == ContractType.PERPETUAL && status == ContractStatus.TRADING) {
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
				}
			});
		}
		
		logger.debug(resultStr);
		logger.debug(pairs);
		return pairs;
	}

}
