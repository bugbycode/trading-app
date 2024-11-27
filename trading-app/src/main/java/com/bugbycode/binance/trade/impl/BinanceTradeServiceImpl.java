package com.bugbycode.binance.trade.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.bugbycode.binance.trade.BinanceTradeService;
import com.bugbycode.config.AppConfig;
import com.bugbycode.module.ResultCode;
import com.bugbycode.module.binance.BinanceOrderInfo;
import com.bugbycode.module.binance.Leverage;
import com.bugbycode.module.binance.MarginType;
import com.bugbycode.module.binance.Result;
import com.util.HmacSHA256Util;
import com.util.StringUtil;
import com.util.UrlQueryStringUtil;

@Service("binanceTradeService")
public class BinanceTradeServiceImpl implements BinanceTradeService {

	private final Logger logger = LogManager.getLogger(BinanceTradeServiceImpl.class);
	
	@Autowired
	private RestTemplate restTemplate;
	
	@Override
	public Result dualSidePosition(String binanceApiKey,String binanceSecretKey,boolean dualSidePosition) {
		ResultCode code = ResultCode.ERROR;
		String queryString = String.format("dualSidePosition=%s&timestamp=%s", dualSidePosition, new Date().getTime());
		String signature = HmacSHA256Util.generateSignature(queryString, binanceSecretKey);
		queryString += "&signature=" + signature;
		
		HttpHeaders headers = new HttpHeaders();
		headers.add("X-MBX-APIKEY", binanceApiKey);
		HttpEntity<String> entity = new HttpEntity<>(headers);
		
		ResponseEntity<String> result = restTemplate.exchange(AppConfig.REST_BASE_URL + "/fapi/v1/positionSide/dual?" + queryString, HttpMethod.POST, entity, String.class);
		
		JSONObject json = new JSONObject(result.getBody());
		
		if(json.has("code") && json.getInt("code") == 200) {
			code = ResultCode.SUCCESS;
		}
		
		return new Result(code, json.getInt("code"), json.getString("msg"));
	}
	
	@Override
	public boolean dualSidePosition(String binanceApiKey,String binanceSecretKey) {
		
		boolean dualSidePosition = false;
		
		String queryString = String.format("timestamp=%s", new Date().getTime());
		String signature = HmacSHA256Util.generateSignature(queryString, binanceSecretKey);
		queryString += "&signature=" + signature;
		
		HttpHeaders headers = new HttpHeaders();
		headers.add("X-MBX-APIKEY", binanceApiKey);
		HttpEntity<String> entity = new HttpEntity<>(headers);
		
		ResponseEntity<String> result = restTemplate.exchange(AppConfig.REST_BASE_URL + "/fapi/v1/positionSide/dual?" + queryString, HttpMethod.GET, entity, String.class);
		
		JSONObject json = new JSONObject(result.getBody());
		
		if(json.has("dualSidePosition")) {
			dualSidePosition = json.getBoolean("dualSidePosition");
		}
		
		return dualSidePosition;
	}

	@Override
	public Leverage leverage(String binanceApiKey,String binanceSecretKey,String symbol, int leverage) {
		Leverage lr = new Leverage(0, null, symbol);
		
		String queryString = String.format("symbol=%s&leverage=%s&timestamp=%s", symbol, leverage, new Date().getTime());
		String signature = HmacSHA256Util.generateSignature(queryString, binanceSecretKey);
		queryString += "&signature=" + signature;
		
		HttpHeaders headers = new HttpHeaders();
		headers.add("X-MBX-APIKEY", binanceApiKey);
		HttpEntity<String> entity = new HttpEntity<>(headers);
		
		ResponseEntity<String> result = restTemplate.exchange(AppConfig.REST_BASE_URL + "/fapi/v1/leverage?" + queryString, HttpMethod.POST, entity, String.class);
		HttpStatus status = HttpStatus.resolve(result.getStatusCode().value());
		
		if(status == HttpStatus.OK) {
			JSONObject json = new JSONObject(result.getBody());
			lr.setLeverage(json.getInt("leverage"));
			lr.setMaxNotionalValue(json.getString("maxNotionalValue"));
		}
		
		return lr;
	}

	@Override
	public Result marginType(String binanceApiKey,String binanceSecretKey,String symbol, MarginType marginType) {
		ResultCode code = ResultCode.ERROR;
		String queryString = String.format("symbol=%s&marginType=%s&timestamp=%s", symbol, marginType, new Date().getTime());
		String signature = HmacSHA256Util.generateSignature(queryString, binanceSecretKey);
		queryString += "&signature=" + signature;
		
		HttpHeaders headers = new HttpHeaders();
		headers.add("X-MBX-APIKEY", binanceApiKey);
		HttpEntity<String> entity = new HttpEntity<>(headers);
		
		ResponseEntity<String> result = restTemplate.exchange(AppConfig.REST_BASE_URL + "/fapi/v1/marginType?" + queryString, HttpMethod.POST, entity, String.class);
		
		JSONObject json = new JSONObject(result.getBody());
		
		if(json.has("code") && json.getInt("code") == 200) {
			code = ResultCode.SUCCESS;
		}
		
		return new Result(code, json.getInt("code"), json.getString("msg"));
	}

	@Override
	public List<BinanceOrderInfo> openOrders(String binanceApiKey, String binanceSecretKey, String symbol) {
		
		List<BinanceOrderInfo> orders = new ArrayList<BinanceOrderInfo>();
		
		String queryString = String.format("symbol=%s&timestamp=%s", symbol, new Date().getTime());
		String signature = HmacSHA256Util.generateSignature(queryString, binanceSecretKey);
		queryString += "&signature=" + signature;
		
		HttpHeaders headers = new HttpHeaders();
		headers.add("X-MBX-APIKEY", binanceApiKey);
		HttpEntity<String> entity = new HttpEntity<>(headers);
		
		ResponseEntity<String> result = restTemplate.exchange(AppConfig.REST_BASE_URL + "/fapi/v1/openOrders?" + queryString, HttpMethod.GET, entity, String.class);
		HttpStatus status = HttpStatus.resolve(result.getStatusCode().value());
		
		if(status == HttpStatus.OK) {
			JSONArray jsonArr = new JSONArray(result.getBody());
			jsonArr.forEach(obj -> {
				JSONObject o = (JSONObject) obj;
				BinanceOrderInfo order = new BinanceOrderInfo();
				order.setAvgPrice(o.getString("avgPrice"));
				order.setClientOrderId(o.getString("clientOrderId"));
				order.setCumQuote(o.getString("cumQuote"));
				order.setExecutedQty(o.getString("executedQty"));
				order.setOrderId(o.getLong("orderId"));
				order.setOrigQty(o.getString("origQty"));
				order.setOrigType(o.getString("origType"));
				order.setPrice(o.getString("price"));
				order.setReduceOnly(o.getBoolean("reduceOnly"));
				order.setSide(o.getString("side"));
				order.setPositionSide(o.getString("positionSide"));
				order.setStatus(o.getString("status"));
				order.setStopPrice(o.getString("stopPrice"));
				order.setClosePosition(o.getBoolean("closePosition"));
				order.setSymbol(o.getString("symbol"));
				order.setTime(o.getLong("time"));
				order.setTimeInForce(o.getString("timeInForce"));
				order.setType(o.getString("type"));
				if(o.has("activatePrice")) {
					order.setActivatePrice(o.getString("activatePrice"));
				}
				if(o.has("priceRate")) {
					order.setPriceRate(o.getString("priceRate"));
				}
				order.setUpdateTime(o.getLong("updateTime"));
				order.setWorkingType(o.getString("workingType"));
				order.setPriceProtect(o.getBoolean("priceProtect"));
				order.setPriceMatch(o.getString("priceMatch"));
				order.setSelfTradePreventionMode(o.getString("selfTradePreventionMode"));
				order.setGoodTillDate(o.getLong("goodTillDate"));
				orders.add(order);
			});
		}
		
		return orders;
	}

	@Override
	public List<BinanceOrderInfo> allOrders(String binanceApiKey, String binanceSecretKey, String symbol, long orderId,
			long startTime, long endTime, int limit) {
		
		List<BinanceOrderInfo> orders = new ArrayList<BinanceOrderInfo>();
		
		Map<String,Object> params = new HashMap<String, Object>();
		params.put("symbol", symbol);
		params.put("timestamp", new Date().getTime());
		if(orderId > 0) {
			params.put("orderId", orderId);
		}
		if(startTime > 0) {
			params.put("startTime", startTime);
		}
		if(startTime > 0) {
			params.put("endTime", endTime);
		}
		
		if(limit > 0) {
			params.put("limit", limit);
		}
		
		String queryString = UrlQueryStringUtil.parse(params);
		
		String signature = HmacSHA256Util.generateSignature(queryString, binanceSecretKey);
		
		queryString += "&signature=" + signature;
		
		HttpHeaders headers = new HttpHeaders();
		headers.add("X-MBX-APIKEY", binanceApiKey);
		HttpEntity<String> entity = new HttpEntity<>(headers);
		
		ResponseEntity<String> result = restTemplate.exchange(AppConfig.REST_BASE_URL + "/fapi/v1/allOrders?" + queryString, HttpMethod.GET, entity, String.class);
		HttpStatus status = HttpStatus.resolve(result.getStatusCode().value());
		
		if(status == HttpStatus.OK) {
			JSONArray jsonArr = new JSONArray(result.getBody());
			jsonArr.forEach(obj -> {
				JSONObject o = (JSONObject) obj;
				BinanceOrderInfo order = new BinanceOrderInfo();
				order.setAvgPrice(o.getString("avgPrice"));
				order.setClientOrderId(o.getString("clientOrderId"));
				order.setCumQuote(o.getString("cumQuote"));
				order.setExecutedQty(o.getString("executedQty"));
				order.setOrderId(o.getLong("orderId"));
				order.setOrigQty(o.getString("origQty"));
				order.setOrigType(o.getString("origType"));
				order.setPrice(o.getString("price"));
				order.setReduceOnly(o.getBoolean("reduceOnly"));
				order.setSide(o.getString("side"));
				order.setPositionSide(o.getString("positionSide"));
				order.setStatus(o.getString("status"));
				if(o.has("stopPrice")) {
					order.setStopPrice(o.getString("stopPrice"));
				}
				order.setClosePosition(o.getBoolean("closePosition"));
				order.setSymbol(o.getString("symbol"));
				order.setTime(o.getLong("time"));
				order.setTimeInForce(o.getString("timeInForce"));
				order.setType(o.getString("type"));
				if(o.has("activatePrice")) {
					order.setActivatePrice(o.getString("activatePrice"));
				}
				if(o.has("priceRate")) {
					order.setPriceRate(o.getString("priceRate"));
				}
				order.setUpdateTime(o.getLong("updateTime"));
				order.setWorkingType(o.getString("workingType"));
				order.setPriceProtect(o.getBoolean("priceProtect"));
				order.setPriceMatch(o.getString("priceMatch"));
				order.setSelfTradePreventionMode(o.getString("selfTradePreventionMode"));
				order.setGoodTillDate(o.getLong("goodTillDate"));
				orders.add(order);
			});
		}
		
		return orders;
	}

	@Override
	public BinanceOrderInfo order(String binanceApiKey, String binanceSecretKey, String symbol, long orderId,
			String origClientOrderId) {
		
		Map<String,Object> params = new HashMap<String, Object>();
		params.put("symbol", symbol);
		params.put("timestamp", new Date().getTime());
		if(orderId > 0) {
			params.put("orderId", orderId);
		}
		
		if(StringUtil.isNotEmpty(origClientOrderId)) {
			params.put("origClientOrderId", origClientOrderId);
		}
		
		String queryString = UrlQueryStringUtil.parse(params);
		
		String signature = HmacSHA256Util.generateSignature(queryString, binanceSecretKey);
		
		queryString += "&signature=" + signature;
		
		HttpHeaders headers = new HttpHeaders();
		headers.add("X-MBX-APIKEY", binanceApiKey);
		HttpEntity<String> entity = new HttpEntity<>(headers);
		
		ResponseEntity<String> result = restTemplate.exchange(AppConfig.REST_BASE_URL + "/fapi/v1/order?" + queryString, HttpMethod.GET, entity, String.class);
		HttpStatus status = HttpStatus.resolve(result.getStatusCode().value());

		BinanceOrderInfo order = new BinanceOrderInfo();
		
		if(status == HttpStatus.OK) {
			JSONObject o = new JSONObject(result.getBody());
			order.setAvgPrice(o.getString("avgPrice"));
			order.setClientOrderId(o.getString("clientOrderId"));
			order.setCumQuote(o.getString("cumQuote"));
			order.setExecutedQty(o.getString("executedQty"));
			order.setOrderId(o.getLong("orderId"));
			order.setOrigQty(o.getString("origQty"));
			order.setOrigType(o.getString("origType"));
			order.setPrice(o.getString("price"));
			order.setReduceOnly(o.getBoolean("reduceOnly"));
			order.setSide(o.getString("side"));
			order.setPositionSide(o.getString("positionSide"));
			order.setStatus(o.getString("status"));
			if(o.has("stopPrice")) {
				order.setStopPrice(o.getString("stopPrice"));
			}
			order.setClosePosition(o.getBoolean("closePosition"));
			order.setSymbol(o.getString("symbol"));
			order.setTime(o.getLong("time"));
			order.setTimeInForce(o.getString("timeInForce"));
			order.setType(o.getString("type"));
			if(o.has("activatePrice")) {
				order.setActivatePrice(o.getString("activatePrice"));
			}
			if(o.has("priceRate")) {
				order.setPriceRate(o.getString("priceRate"));
			}
			order.setUpdateTime(o.getLong("updateTime"));
			order.setWorkingType(o.getString("workingType"));
			order.setPriceProtect(o.getBoolean("priceProtect"));
			order.setPriceMatch(o.getString("priceMatch"));
			order.setSelfTradePreventionMode(o.getString("selfTradePreventionMode"));
			order.setGoodTillDate(o.getLong("goodTillDate"));
		}
		
		return order;
	}

}
