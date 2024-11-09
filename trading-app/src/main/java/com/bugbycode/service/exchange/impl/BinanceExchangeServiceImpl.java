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
					String status = symbolJson.getString("status");
					if("PERPETUAL".equals(contractType) && "TRADING".equals(status)) {
						pairs.add(symbolJson.getString("symbol"));
					}
				}
			});
		}
		
		logger.info(resultStr);
		logger.info(pairs);
		return pairs;
	}

}
