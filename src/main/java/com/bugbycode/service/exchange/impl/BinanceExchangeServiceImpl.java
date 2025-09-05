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
		long now = new Date().getTime();
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
					long deliveryDate = symbolJson.getLong("deliveryDate");
					JSONArray filters = symbolJson.getJSONArray("filters");
					
					ContractType type = ContractType.resolve(contractType);
					ContractStatus status = ContractStatus.resolve(statusStr);
					
					//交割到期/即将下架天数
					long deliveryDay = (deliveryDate - now) / 1000 / 60 / 60 / 24;
					
					/*if(status == ContractStatus.TRADING && deliveryDay < 1000) {
						logger.info("{}交易对{}天后交割或下架", symbol, deliveryDay);
					}*/
					
					if(type == ContractType.PERPETUAL && status == ContractStatus.TRADING && !symbol.endsWith("USDC") && deliveryDay > 30) {
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
							} else if("PRICE_FILTER".equals(filterType)) {// 订单最小价格间隔
								info.setTickSize(f.getString("tickSize"));
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
