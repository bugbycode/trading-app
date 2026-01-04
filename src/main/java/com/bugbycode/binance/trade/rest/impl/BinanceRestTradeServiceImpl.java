package com.bugbycode.binance.trade.rest.impl;

import java.math.BigDecimal;
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

import com.bugbycode.binance.module.position.PositionInfo;
import com.bugbycode.binance.trade.rest.BinanceRestTradeService;
import com.bugbycode.config.AppConfig;
import com.bugbycode.exception.OrderPlaceException;
import com.bugbycode.module.AlgoType;
import com.bugbycode.module.ResultCode;
import com.bugbycode.module.binance.Balance;
import com.bugbycode.module.binance.BinanceOrderInfo;
import com.bugbycode.module.binance.CallbackRateEnabled;
import com.bugbycode.module.binance.Leverage;
import com.bugbycode.module.binance.MarginType;
import com.bugbycode.module.binance.ProfitOrderEnabled;
import com.bugbycode.module.binance.Result;
import com.bugbycode.module.binance.SymbolConfig;
import com.bugbycode.module.binance.WorkingType;
import com.bugbycode.module.trading.PositionSide;
import com.bugbycode.module.trading.Side;
import com.bugbycode.module.trading.Type;
import com.util.HmacSHA256Util;
import com.util.StringUtil;
import com.util.UrlQueryStringUtil;

@Service("binanceTradeService")
public class BinanceRestTradeServiceImpl implements BinanceRestTradeService {

	private final Logger logger = LogManager.getLogger(BinanceRestTradeServiceImpl.class);
	
	@Autowired
	private RestTemplate restTemplate;
	
	@Override
	public Result dualSidePosition(String binanceApiKey,String binanceSecretKey,boolean dualSidePosition) {
		ResultCode code = ResultCode.ERROR;
		String queryString = String.format("dualSidePosition=%s&timestamp=%s", dualSidePosition, getLocalTime());
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
		
		String queryString = String.format("timestamp=%s", getLocalTime());
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
		
		String queryString = String.format("symbol=%s&leverage=%s&timestamp=%s", StringUtil.urlEncoder(symbol), leverage, getLocalTime());
		String signature = HmacSHA256Util.generateSignature(queryString, binanceSecretKey);
		
		queryString = StringUtil.urlDecoder(queryString);
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
		String queryString = String.format("symbol=%s&marginType=%s&timestamp=%s", StringUtil.urlEncoder(symbol), marginType, getLocalTime());
		String signature = HmacSHA256Util.generateSignature(queryString, binanceSecretKey);
		
		queryString = StringUtil.urlDecoder(queryString);
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
		
		String queryString = String.format("symbol=%s&timestamp=%s", StringUtil.urlEncoder(symbol), getLocalTime());
		String signature = HmacSHA256Util.generateSignature(queryString, binanceSecretKey);
		
		queryString = StringUtil.urlDecoder(queryString);
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
				//order.setAvgPrice(o.getString("avgPrice"));
				order.setClientOrderId(o.getString("clientAlgoId"));
				//order.setCumQuote(o.getString("cumQuote"));
				//order.setExecutedQty(o.getString("executedQty"));
				order.setOrderId(o.getLong("algoId"));
				order.setOrigQty(o.getString("quantity"));
				order.setOrigType(o.getString("orderType"));
				order.setPrice(o.getString("price"));
				order.setReduceOnly(o.getBoolean("reduceOnly"));
				order.setSide(o.getString("side"));
				order.setPositionSide(o.getString("positionSide"));
				order.setStatus(o.getString("algoStatus"));
				order.setStopPrice(o.getString("triggerPrice"));
				order.setClosePosition(o.getBoolean("closePosition"));
				order.setSymbol(o.getString("symbol"));
				order.setTime(o.getLong("createTime"));
				order.setTimeInForce(o.getString("timeInForce"));
				order.setType(o.getString("orderType"));
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
	public BinanceOrderInfo openOrder(String binanceApiKey, String binanceSecretKey, String symbol, long orderId,
			String origClientOrderId) {
		
		Map<String,Object> params = new HashMap<String,Object>();
		params.put("symbol", StringUtil.urlEncoder(symbol));
		if(orderId > 0) {
			params.put("orderId", orderId);
		}
		if(StringUtil.isNotEmpty(origClientOrderId)) {
			params.put("origClientOrderId", origClientOrderId);
		}
		
		params.put("timestamp", getLocalTime());
		
		String queryString = UrlQueryStringUtil.parse(params);
		
		String signature = HmacSHA256Util.generateSignature(queryString, binanceSecretKey);
		
		queryString = StringUtil.urlDecoder(queryString);
		queryString += "&signature=" + signature;
		
		HttpHeaders headers = new HttpHeaders();
		headers.add("X-MBX-APIKEY", binanceApiKey);
		HttpEntity<String> entity = new HttpEntity<>(headers);
		
		ResponseEntity<String> result = restTemplate.exchange(AppConfig.REST_BASE_URL + "/fapi/v1/openOrder?" + queryString, HttpMethod.GET, entity, String.class);
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
		}
		return order;
	}

	@Override
	public List<BinanceOrderInfo> allOrders(String binanceApiKey, String binanceSecretKey, String symbol, long orderId,
			long startTime, long endTime, int limit) {
		
		List<BinanceOrderInfo> orders = new ArrayList<BinanceOrderInfo>();
		
		Map<String,Object> params = new HashMap<String, Object>();
		params.put("symbol",  StringUtil.urlEncoder(symbol));
		params.put("timestamp", getLocalTime());
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
		
		queryString = StringUtil.urlDecoder(queryString);
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
		params.put("symbol", StringUtil.urlEncoder(symbol));
		params.put("timestamp", getLocalTime());
		if(orderId > 0) {
			params.put("orderId", orderId);
		}
		
		if(StringUtil.isNotEmpty(origClientOrderId)) {
			params.put("origClientOrderId", origClientOrderId);
		}
		
		String queryString = UrlQueryStringUtil.parse(params);
		
		String signature = HmacSHA256Util.generateSignature(queryString, binanceSecretKey);
		
		queryString = StringUtil.urlDecoder(queryString);
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
	
	@Override
	public BinanceOrderInfo orderPost(String binanceApiKey,String binanceSecretKey,String symbol,Side side,PositionSide ps,Type type,String newClientOrderId,
			BigDecimal quantity,BigDecimal price,BigDecimal stopPrice,Boolean closePosition,WorkingType workingType,
			BigDecimal activationPrice, BigDecimal callbackRate) {
		Map<String,Object> params = new HashMap<String, Object>();
		
		String method = "/fapi/v1/algoOrder?";
		if(type == Type.LIMIT || type == Type.MARKET) {//非条件单
			method = "/fapi/v1/order?";
		} else {
			params.put("algoType", AlgoType.CONDITIONAL);
		}
		
		//限价订单
		if(type == Type.LIMIT) {
			params.put("symbol", StringUtil.urlEncoder(symbol));
			params.put("side", side);
			params.put("positionSide", ps.value());
			params.put("type", type.value());
			if(StringUtil.isNotEmpty(newClientOrderId)) {
				params.put("newClientOrderId", newClientOrderId);
			}
			params.put("quantity", quantity);//委托数量
			params.put("price", price);//委托价格
			params.put("timeinforce", "GTC");
		} else if(type == Type.MARKET) {//市价订单
			params.put("symbol", StringUtil.urlEncoder(symbol));
			params.put("side", side);
			params.put("positionSide", ps.value());
			params.put("type", type.value());
			if(StringUtil.isNotEmpty(newClientOrderId)) {
				params.put("newClientOrderId", newClientOrderId);
			}
			params.put("quantity", quantity);//委托数量
		} else if(type == Type.STOP) {//限价止损
			params.put("symbol", StringUtil.urlEncoder(symbol));
			params.put("side", side);
			params.put("positionSide", ps.value());
			params.put("type", type.value());
			if(StringUtil.isNotEmpty(newClientOrderId)) {
				params.put("newClientOrderId", newClientOrderId);
			}
			params.put("quantity", quantity);//委托数量
			params.put("price", price);//委托价
			params.put("triggerPrice", stopPrice);//触发价
			params.put("workingType", workingType);//触发价格类型 最新价或标记价
			params.put("timeInForce", "GTE_GTC");
		} else if(type == Type.STOP_MARKET) { //市价止损
			params.put("symbol", StringUtil.urlEncoder(symbol));
			params.put("side", side);
			params.put("positionSide", ps.value());
			params.put("type", type.value());
			if(StringUtil.isNotEmpty(newClientOrderId)) {
				params.put("newClientOrderId", newClientOrderId);
			}
			params.put("triggerPrice", stopPrice);//触发价
			params.put("closePosition", closePosition);//市价止损是否全部平仓
			params.put("workingType", workingType);//触发价格类型 最新价或标记价
			params.put("timeInForce", "GTE_GTC");
		} else if(type == Type.TAKE_PROFIT) {//限价止盈
			params.put("symbol", StringUtil.urlEncoder(symbol));
			params.put("side", side);
			params.put("positionSide", ps.value());
			params.put("type", type.value());
			if(StringUtil.isNotEmpty(newClientOrderId)) {
				params.put("newClientOrderId", newClientOrderId);
			}
			params.put("quantity", quantity);//委托数量
			params.put("price", price);//委托价格
			params.put("triggerPrice", stopPrice);//触发价
			params.put("workingType", workingType);//触发价格类型 最新价或标记价
			params.put("timeInForce", "GTE_GTC");
		} else if(type == Type.TAKE_PROFIT_MARKET) {//市价止盈
			params.put("symbol", StringUtil.urlEncoder(symbol));
			params.put("side", side);
			params.put("positionSide", ps.value());
			params.put("type", type.value());
			if(StringUtil.isNotEmpty(newClientOrderId)) {
				params.put("newClientOrderId", newClientOrderId);
			}
			//params.put("quantity", quantity);//委托数量
			params.put("triggerPrice", stopPrice);//触发价
			params.put("closePosition", closePosition);//市价止损是否全部平仓
			params.put("workingType", workingType);
			params.put("timeInForce", "GTE_GTC");
		} else if(type == Type.TRAILING_STOP_MARKET) {//追踪委托止损
			params.put("symbol", StringUtil.urlEncoder(symbol));
			params.put("side", side);
			params.put("positionSide", ps.value());
			params.put("type", type.value());
			if(StringUtil.isNotEmpty(newClientOrderId)) {
				params.put("newClientOrderId", newClientOrderId);
			}
			
			params.put("activatePrice", activationPrice.toString());//追踪止损激活价格，仅TRAILING_STOP_MARKET 需要此参数, 默认为下单当前市场价格(支持不同workingType)
			params.put("callbackRate", callbackRate.toString());//追踪止损回调比例，可取值范围[0.1, 10],其中 1代表1% ,仅TRAILING_STOP_MARKET 需要此参数
			params.put("quantity", quantity.toString());//委托数量
			//params.put("closePosition", closePosition);//市价止损是否全部平仓
			params.put("workingType", workingType);//触发价格类型 最新价或标记价
			params.put("timeInForce", "GTE_GTC");
		}
		
		params.put("timestamp", getLocalTime());
		
		String queryString = UrlQueryStringUtil.parse(params);
		String signature = HmacSHA256Util.generateSignature(queryString, binanceSecretKey);
		
		queryString = StringUtil.urlDecoder(queryString);
		queryString += "&signature=" + signature;
		
		HttpHeaders headers = new HttpHeaders();
		headers.add("X-MBX-APIKEY", binanceApiKey);
		HttpEntity<String> entity = new HttpEntity<>(headers);
		
		String url = AppConfig.REST_BASE_URL + method + queryString;
		logger.debug(url);
		
		ResponseEntity<String> result = restTemplate.exchange(AppConfig.REST_BASE_URL + method + queryString, HttpMethod.POST, entity, String.class);
		HttpStatus status = HttpStatus.resolve(result.getStatusCode().value());

		BinanceOrderInfo order = new BinanceOrderInfo();
		
		if(status == HttpStatus.OK) {
			logger.info(result.getBody());
			JSONObject o = new JSONObject(result.getBody());
			if(o.has("avgPrice")) {
				order.setAvgPrice(o.getString("avgPrice"));
			}
			
			if(o.has("clientAlgoId")) {
				order.setClientOrderId(o.getString("clientAlgoId"));
			} else if(o.has("clientOrderId")) {
				order.setClientOrderId(o.getString("clientOrderId"));
			}
			
			if(o.has("cumQuote")) {
				order.setCumQuote(o.getString("cumQuote"));
			}
			
			if(o.has("executedQty")) {
				order.setExecutedQty(o.getString("executedQty"));
			}
			
			if(o.has("algoId")) {
				order.setOrderId(o.getLong("algoId"));
			} else if(o.has("orderId")) {
				order.setOrderId(o.getLong("orderId"));
			}
			
			if(o.has("origQty")) {
				order.setOrigQty(o.getString("origQty"));
			}
			
			if(o.has("origType")) {
				order.setOrigType(o.getString("origType"));
			}
			
			order.setPrice(o.getString("price"));
			order.setReduceOnly(o.getBoolean("reduceOnly"));
			order.setSide(o.getString("side"));
			if(o.has("positionSide")) {
				order.setPositionSide(o.getString("positionSide"));
			}
			
			if(o.has("status")) {
				order.setStatus(o.getString("status"));
			} else if(o.has("algoStatus")) {
				order.setStatus(o.getString("algoStatus"));
			}
			
			if(o.has("stopPrice")) {
				order.setStopPrice(o.getString("stopPrice"));
			} else if(o.has("triggerPrice")) {
				order.setStopPrice(o.getString("triggerPrice"));
			}
			
			order.setClosePosition(o.getBoolean("closePosition"));
			order.setSymbol(o.getString("symbol"));
			if(o.has("time")) {
				order.setTime(o.getLong("time"));
			}
			order.setTimeInForce(o.getString("timeInForce"));
			
			if(o.has("type")) {
				order.setType(o.getString("type"));
			} else if(o.has("orderType")) {
				order.setType(o.getString("orderType"));
			}
			
			if(o.has("activatePrice")) {
				order.setActivatePrice(o.getString("activatePrice"));
			}
			if(o.has("priceRate")) {
				order.setPriceRate(o.getString("priceRate"));
			}
			order.setUpdateTime(o.getLong("updateTime"));
			if(o.has("workingType")) {
				order.setWorkingType(o.getString("workingType"));
			}
			order.setPriceProtect(o.getBoolean("priceProtect"));
			if(o.has("priceMatch")) {
				order.setPriceMatch(o.getString("priceMatch"));
			}
			if(o.has("selfTradePreventionMode")) {
				order.setSelfTradePreventionMode(o.getString("selfTradePreventionMode"));
			}
			if(o.has("goodTillDate")) {
				order.setGoodTillDate(o.getLong("goodTillDate"));
			}
		} else {
			//logger.error(result.getBody());
			String title = "下单" + symbol + ps.getMemo() + type.getMemo() + "出现异常";
			String message = type.value() + "_" + side + " \r\n " + method.toString() + "\r\n" + result.toString();
			throw new OrderPlaceException(title, message);
		}
		
		return order;
	}
	
	@Override
	public List<BinanceOrderInfo> tradeMarket(String binanceApiKey,String binanceSecretKey,String symbol,PositionSide ps,
			BigDecimal quantity,BigDecimal stopLoss,BigDecimal takeProfit, CallbackRateEnabled callbackRateEnabled, 
			BigDecimal activationPrice, BigDecimal callbackRate, ProfitOrderEnabled profitOrderEnabled) {
		List<BinanceOrderInfo> orders = new ArrayList<BinanceOrderInfo>();
		if(ps == PositionSide.LONG) {//做多
			BinanceOrderInfo order = orderPost(binanceApiKey, binanceSecretKey, 
			        symbol, Side.BUY, PositionSide.LONG, Type.MARKET, 
			        null, quantity, null, 
			        null, null, null, null, null);
			
			BinanceOrderInfo slOrder = orderPost(binanceApiKey, binanceSecretKey, 
			        symbol, Side.SELL, PositionSide.LONG, Type.STOP_MARKET, 
			        null, new BigDecimal(order.getOrigQty()), null, 
			        stopLoss, true, WorkingType.CONTRACT_PRICE, null, null);
			
			orders.add(order);
			orders.add(slOrder);
			
			if(profitOrderEnabled == ProfitOrderEnabled.OPEN) {
				BinanceOrderInfo tpOrder = orderPost(binanceApiKey, binanceSecretKey, 
				        symbol, Side.SELL, PositionSide.LONG, Type.TAKE_PROFIT_MARKET, 
				        null, new BigDecimal(order.getOrigQty()), null, 
				        takeProfit, true, WorkingType.CONTRACT_PRICE, null, null);
				orders.add(tpOrder);
			}

			if(callbackRateEnabled == CallbackRateEnabled.OPEN) {
				BinanceOrderInfo cbOrder = orderPost(binanceApiKey, binanceSecretKey, 
				        symbol, Side.SELL, PositionSide.LONG, Type.TRAILING_STOP_MARKET, 
				        null, new BigDecimal(order.getOrigQty()), null, 
				        takeProfit, true, WorkingType.CONTRACT_PRICE, activationPrice, callbackRate);
				orders.add(cbOrder);
			}
			
		} else {//做空
			BinanceOrderInfo order = orderPost(binanceApiKey, binanceSecretKey, 
			        symbol, Side.SELL, PositionSide.SHORT, Type.MARKET, 
			        null, quantity, null, 
			        null, null, null, null, null);
			
			BinanceOrderInfo slOrder = orderPost(binanceApiKey, binanceSecretKey, 
			        symbol, Side.BUY, PositionSide.SHORT, Type.STOP_MARKET, 
			        null, new BigDecimal(order.getOrigQty()), null, 
			        stopLoss, true, WorkingType.CONTRACT_PRICE, null, null);
			
			orders.add(order);
			orders.add(slOrder);
			
			if(profitOrderEnabled == ProfitOrderEnabled.OPEN) {
				BinanceOrderInfo tpOrder = orderPost(binanceApiKey, binanceSecretKey, 
				        symbol, Side.BUY, PositionSide.SHORT, Type.TAKE_PROFIT_MARKET, 
				        null, new BigDecimal(order.getOrigQty()), null, 
				        takeProfit, true, WorkingType.CONTRACT_PRICE, null, null);
				orders.add(tpOrder);
				
			}
			
			if(callbackRateEnabled == CallbackRateEnabled.OPEN) {
				BinanceOrderInfo cbOrder = orderPost(binanceApiKey, binanceSecretKey, 
				        symbol, Side.BUY, PositionSide.SHORT, Type.TRAILING_STOP_MARKET, 
				        null, new BigDecimal(order.getOrigQty()), null, 
				        takeProfit, true, WorkingType.CONTRACT_PRICE, activationPrice, callbackRate);
				orders.add(cbOrder);
			}
		}
		return orders;
	}
	
	@Override
	public BinanceOrderInfo orderDelete(String binanceApiKey,String binanceSecretKey,String symbol,long orderId,String origClientOrderId) {
		Map<String,Object> params = new HashMap<String,Object>();
		params.put("symbol", StringUtil.urlEncoder(symbol));
		if(orderId > 0) {
			params.put("orderId", orderId);
		}
		if(StringUtil.isNotEmpty(origClientOrderId)) {
			params.put("origClientOrderId", origClientOrderId);
		}
		params.put("timestamp", getLocalTime());
		
		String queryString = UrlQueryStringUtil.parse(params);
		String signature = HmacSHA256Util.generateSignature(queryString, binanceSecretKey);
		
		queryString = StringUtil.urlDecoder(queryString);
		queryString += "&signature=" + signature;
		
		HttpHeaders headers = new HttpHeaders();
		headers.add("X-MBX-APIKEY", binanceApiKey);
		HttpEntity<String> entity = new HttpEntity<>(headers);
		
		ResponseEntity<String> result = restTemplate.exchange(AppConfig.REST_BASE_URL + "/fapi/v1/order?" + queryString, HttpMethod.DELETE, entity, String.class);
		HttpStatus status = HttpStatus.resolve(result.getStatusCode().value());

		BinanceOrderInfo order = new BinanceOrderInfo();
		
		if(status == HttpStatus.OK) {
			JSONObject o = new JSONObject(result.getBody());
			if(o.has("avgPrice")) {
				order.setAvgPrice(o.getString("avgPrice"));
			}
			order.setClientOrderId(o.getString("clientOrderId"));
			order.setCumQuote(o.getString("cumQuote"));
			order.setExecutedQty(o.getString("executedQty"));
			order.setOrderId(o.getLong("orderId"));
			order.setOrigQty(o.getString("origQty"));
			order.setOrigType(o.getString("origType"));
			order.setPrice(o.getString("price"));
			order.setReduceOnly(o.getBoolean("reduceOnly"));
			order.setSide(o.getString("side"));
			if(o.has("positionSide")) {
				order.setPositionSide(o.getString("positionSide"));
			}
			order.setStatus(o.getString("status"));
			if(o.has("stopPrice")) {
				order.setStopPrice(o.getString("stopPrice"));
			}
			order.setClosePosition(o.getBoolean("closePosition"));
			order.setSymbol(o.getString("symbol"));
			if(o.has("time")) {
				order.setTime(o.getLong("time"));
			}
			order.setTimeInForce(o.getString("timeInForce"));
			order.setType(o.getString("type"));
			if(o.has("activatePrice")) {
				order.setActivatePrice(o.getString("activatePrice"));
			}
			if(o.has("priceRate")) {
				order.setPriceRate(o.getString("priceRate"));
			}
			order.setUpdateTime(o.getLong("updateTime"));
			if(o.has("workingType")) {
				order.setWorkingType(o.getString("workingType"));
			}
			order.setPriceProtect(o.getBoolean("priceProtect"));
			if(o.has("priceMatch")) {
				order.setPriceMatch(o.getString("priceMatch"));
			}
			if(o.has("selfTradePreventionMode")) {
				order.setSelfTradePreventionMode(o.getString("selfTradePreventionMode"));
			}
			if(o.has("goodTillDate")) {
				order.setGoodTillDate(o.getLong("goodTillDate"));
			}
		} else {
			logger.error(result.getBody());
		}
		return order;
	}

	@Override
	public List<Balance> balance_v3(String binanceApiKey,String binanceSecretKey) {
		
		List<Balance> blanceList = new ArrayList<Balance>();
		
		Map<String,Object> params = new HashMap<String,Object>();
		params.put("timestamp", getLocalTime());
		
		String queryString = UrlQueryStringUtil.parse(params);
		String signature = HmacSHA256Util.generateSignature(queryString, binanceSecretKey);
		
		queryString += "&signature=" + signature;
		
		HttpHeaders headers = new HttpHeaders();
		headers.add("X-MBX-APIKEY", binanceApiKey);
		HttpEntity<String> entity = new HttpEntity<>(headers);
		
		ResponseEntity<String> result = restTemplate.exchange(AppConfig.REST_BASE_URL + "/fapi/v3/balance?" + queryString, HttpMethod.GET, entity, String.class);
		HttpStatus status = HttpStatus.resolve(result.getStatusCode().value());

		if(status == HttpStatus.OK) {
			JSONArray data = new JSONArray(result.getBody());
			for(int index = 0;index < data.length();index++) {
				JSONObject item = data.getJSONObject(index);
				Balance balance = new Balance();
				balance.setAccountAlias(item.getString("accountAlias"));
				balance.setAsset(item.getString("asset"));
				balance.setBalance(item.getString("balance"));
				balance.setCrossWalletBalance(item.getString("crossWalletBalance"));
				balance.setCrossUnPnl(item.getString("crossUnPnl"));
				balance.setAvailableBalance(item.getString("availableBalance"));
				balance.setMaxWithdrawAmount(item.getString("maxWithdrawAmount"));
				balance.setMarginAvailable(item.getBoolean("marginAvailable"));
				balance.setUpdateTime(item.getLong("updateTime"));
				blanceList.add(balance);
			}
		}
		
		return blanceList;
	}

	@Override
	public List<SymbolConfig> getSymbolConfig(String binanceApiKey, String binanceSecretKey, String symbol) {
		
		List<SymbolConfig> scList = new ArrayList<SymbolConfig>();
		
		String queryString = String.format("symbol=%s&timestamp=%s", StringUtil.urlEncoder(symbol), getLocalTime());
		String signature = HmacSHA256Util.generateSignature(queryString, binanceSecretKey);
		
		queryString = StringUtil.urlDecoder(queryString);
		queryString += "&signature=" + signature;
		
		HttpHeaders headers = new HttpHeaders();
		headers.add("X-MBX-APIKEY", binanceApiKey);
		HttpEntity<String> entity = new HttpEntity<>(headers);
		
		ResponseEntity<String> result = restTemplate.exchange(AppConfig.REST_BASE_URL + "/fapi/v1/symbolConfig?" + queryString, HttpMethod.GET, entity, String.class);
		HttpStatus status = HttpStatus.resolve(result.getStatusCode().value());
		if(status == HttpStatus.OK) {
			JSONArray array = new JSONArray(result.getBody());
			array.forEach(obj -> {
				JSONObject o = (JSONObject) obj;
				SymbolConfig sc = new SymbolConfig(symbol, o.getString("marginType"), o.getBoolean("isAutoAddMargin"), o.getInt("leverage"), o.getString("maxNotionalValue"));
				scList.add(sc);
			});
			return scList;
		} else {
			throw new RuntimeException(result.getBody());
		}
	}

	@Override
	public SymbolConfig getSymbolConfigBySymbol(String binanceApiKey, String binanceSecretKey, String symbol) {
		List<SymbolConfig> scList = getSymbolConfig(binanceApiKey, binanceSecretKey, symbol);
		for(SymbolConfig sc : scList) {
			if(sc.getSymbol().equals(symbol)) {
				return sc;
			}
		}
		return null;
	}

	@Override
	public long getTime() {
		long t = 0;
		String result = restTemplate.getForObject(AppConfig.REST_BASE_URL + "/fapi/v1/time", String.class);
		if(StringUtil.isNotEmpty(result)) {
			JSONObject json = new JSONObject(result);
			if(json.has("serverTime")) {
				t = json.getLong("serverTime");
			}
		}
		return t;
	}
	
	@Override
	public long getLocalTime() {
		return new Date().getTime();
	}

	@Override
	public List<BinanceOrderInfo> openAlgoOrders(String binanceApiKey,String binanceSecretKey, String symbol) {
		List<BinanceOrderInfo> orders = new ArrayList<BinanceOrderInfo>();
		
		String queryString = String.format("timestamp=%s", getLocalTime());
		
		if(StringUtil.isNotEmpty(symbol)) {
			queryString = String.format("symbol=%s&timestamp=%s", StringUtil.urlEncoder(symbol), getLocalTime());
		}
		
		String signature = HmacSHA256Util.generateSignature(queryString, binanceSecretKey);
		
		queryString = StringUtil.urlDecoder(queryString);
		queryString += "&signature=" + signature;
		
		HttpHeaders headers = new HttpHeaders();
		headers.add("X-MBX-APIKEY", binanceApiKey);
		HttpEntity<String> entity = new HttpEntity<>(headers);
		
		ResponseEntity<String> result = restTemplate.exchange(AppConfig.REST_BASE_URL + "/fapi/v1/openAlgoOrders?" + queryString, HttpMethod.GET, entity, String.class);
		HttpStatus status = HttpStatus.resolve(result.getStatusCode().value());
		
		if(status == HttpStatus.OK) {
			JSONArray jsonArr = new JSONArray(result.getBody());
			jsonArr.forEach(obj -> {
				JSONObject o = (JSONObject) obj;
				BinanceOrderInfo order = new BinanceOrderInfo();
				//order.setAvgPrice(o.getString("avgPrice"));
				order.setClientOrderId(o.getString("clientAlgoId"));
				//order.setCumQuote(o.getString("cumQuote"));
				//order.setExecutedQty(o.getString("executedQty"));
				order.setOrderId(o.getLong("algoId"));
				order.setOrigQty(o.getString("quantity"));
				order.setOrigType(o.getString("orderType"));
				order.setPrice(o.getString("price"));
				order.setReduceOnly(o.getBoolean("reduceOnly"));
				order.setSide(o.getString("side"));
				order.setPositionSide(o.getString("positionSide"));
				order.setStatus(o.getString("algoStatus"));
				order.setStopPrice(o.getString("triggerPrice"));
				order.setClosePosition(o.getBoolean("closePosition"));
				order.setSymbol(o.getString("symbol"));
				order.setTime(o.getLong("createTime"));
				order.setTimeInForce(o.getString("timeInForce"));
				order.setType(o.getString("orderType"));
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
	public int allCountOpenAlgoOrders(String binanceApiKey, String binanceSecretKey) {
		return openAlgoOrders(binanceApiKey, binanceSecretKey, null).size();
	}
	
	@Override
	public List<PositionInfo> positionRisk_v3(String binanceApiKey, String binanceSecretKey, String symbol) {
		
		List<PositionInfo> list = new ArrayList<PositionInfo>();
		
		String queryString = String.format("timestamp=%s", getLocalTime());
		
		if(StringUtil.isNotEmpty(symbol)) {
			queryString = String.format("symbol=%s&timestamp=%s", StringUtil.urlEncoder(symbol), getLocalTime());
		}
		
		String signature = HmacSHA256Util.generateSignature(queryString, binanceSecretKey);
		
		queryString = StringUtil.urlDecoder(queryString);
		queryString += "&signature=" + signature;
		
		HttpHeaders headers = new HttpHeaders();
		headers.add("X-MBX-APIKEY", binanceApiKey);
		HttpEntity<String> entity = new HttpEntity<>(headers);
		
		String url = AppConfig.REST_BASE_URL + "/fapi/v3/positionRisk?" + queryString;
		
		logger.debug(url);
		
		ResponseEntity<String> result = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);
		HttpStatus status = HttpStatus.resolve(result.getStatusCode().value());
		
		if(status == HttpStatus.OK) {
			
			logger.debug(result.getBody());
			
			JSONArray jsonArr = new JSONArray(result.getBody());
			jsonArr.forEach(obj -> {
				JSONObject o = (JSONObject) obj;
				PositionInfo info = new PositionInfo();
				info.setSymbol(o.getString("symbol"));
				info.setPositionSide(o.getString("positionSide"));
				info.setPositionAmt(o.getString("positionAmt"));
				info.setEntryPrice(o.getString("entryPrice"));
				info.setBreakEvenPrice(o.getString("breakEvenPrice"));
				info.setMarkPrice(o.getString("markPrice"));
				info.setUnRealizedProfit(o.getString("unRealizedProfit"));
				info.setLiquidationPrice(o.getString("liquidationPrice"));
				info.setIsolatedMargin(o.getString("isolatedMargin"));
				info.setNotional(o.getString("notional"));
				info.setMarginAsset(o.getString("marginAsset"));
				info.setIsolatedWallet(o.getString("isolatedWallet"));
				info.setInitialMargin(o.getString("initialMargin"));
				info.setMaintMargin(o.getString("maintMargin"));
				info.setPositionInitialMargin(o.getString("positionInitialMargin"));
				info.setOpenOrderInitialMargin(o.getString("openOrderInitialMargin"));
				info.setAdl(o.getInt("adl"));
				info.setBidNotional(o.getString("bidNotional"));
				info.setAskNotional(o.getString("askNotional"));
				info.setUpdateTime(o.getLong("updateTime"));
				list.add(info);
			});
		}
		
		return list;
	}
	
	@Override
	public List<PositionInfo> getPositionInfo(String binanceApiKey, String binanceSecretKey, String symbol, PositionSide side) {
		List<PositionInfo> list = positionRisk_v3(binanceApiKey, binanceSecretKey, symbol);
		List<PositionInfo> result = new ArrayList<PositionInfo>();
		for(PositionInfo info : list) {
			if(side.value().equals(info.getPositionSide())) {
				result.add(info);
			}
		}
		return result;
	}
}
