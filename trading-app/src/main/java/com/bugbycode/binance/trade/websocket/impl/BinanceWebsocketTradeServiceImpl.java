package com.bugbycode.binance.trade.websocket.impl;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.bugbycode.binance.trade.websocket.BinanceWebsocketTradeService;
import com.bugbycode.module.Method;
import com.bugbycode.module.binance.Balance;
import com.bugbycode.module.binance.BinanceOrderInfo;
import com.bugbycode.module.binance.PriceInfo;
import com.bugbycode.module.binance.WorkingType;
import com.bugbycode.module.trading.PositionSide;
import com.bugbycode.module.trading.Side;
import com.bugbycode.module.trading.Type;
import com.bugbycode.websocket.trading.endpoint.TradingWebSocketClientEndpoint;
import com.util.MethodDataUtil;
import com.util.StringUtil;

@Service("binanceWebsocketTradeService")
public class BinanceWebsocketTradeServiceImpl implements BinanceWebsocketTradeService {

	private final Logger logger = LogManager.getLogger(BinanceWebsocketTradeServiceImpl.class);
	
	@Autowired
	private TradingWebSocketClientEndpoint websocketApi;

	@Override
	public List<Balance> balance(String apiKey, String secretKey) {
		
		List<Balance> blanceList = new ArrayList<Balance>();
		
		JSONObject method = MethodDataUtil.getMethodJsonObjec(Method.BALANCE);
		JSONObject params = new JSONObject();
		params.put("apiKey", apiKey);
		params.put("timestamp", new Date().getTime());
		
		MethodDataUtil.generateSignature(params, secretKey);
		
		method.put("params", params);
		
		websocketApi.sendMessage(method);
		
		JSONObject result = websocketApi.read(method.getString("id"));
		
		if(result.getInt("status") == 200 && result.has("result")) {
			JSONArray data = result.getJSONArray("result");
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
		} else if(result.has("error")){
			JSONObject errorJson = result.getJSONObject("error");
			String message = errorJson.getInt("code") + ":" + errorJson.getString("msg");
			logger.error(message);
			//throw new RuntimeException(errorJson.getInt("code") + ":" + errorJson.getString("msg"));
		}
		
		return blanceList;
	}

	@Override
	public List<Balance> balance_v2(String apiKey, String secretKey) {
		List<Balance> blanceList = new ArrayList<Balance>();
		
		JSONObject method = MethodDataUtil.getMethodJsonObjec(Method.BALANCE_V2);
		JSONObject params = new JSONObject();
		params.put("apiKey", apiKey);
		params.put("timestamp", new Date().getTime());
		
		MethodDataUtil.generateSignature(params, secretKey);
		
		method.put("params", params);
		
		websocketApi.sendMessage(method);
		
		JSONObject result = websocketApi.read(method.getString("id"));
		
		if(result.getInt("status") == 200 && result.has("result")) {
			JSONArray data = result.getJSONArray("result");
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
		} else if(result.has("error")){
			JSONObject errorJson = result.getJSONObject("error");
			String message = errorJson.getInt("code") + ":" + errorJson.getString("msg");
			logger.error(message);
			//throw new RuntimeException(errorJson.getInt("code") + ":" + errorJson.getString("msg"));
		}
		
		return blanceList;
	}

	@Override
	public PriceInfo getPrice(String symbol) {
		PriceInfo info = null;
		
		JSONObject method = MethodDataUtil.getMethodJsonObjec(Method.TICKER_PRICE);
		JSONObject params = new JSONObject();
		params.put("symbol", symbol);
		method.put("params", params);
		
		websocketApi.sendMessage(method);
		
		JSONObject result = websocketApi.read(method.getString("id"));
		
		if(result.getInt("status") == 200 && result.has("result")) {
			JSONObject data = result.getJSONObject("result");
			info = new PriceInfo(data.getString("symbol"), data.getString("price"), data.getLong("time"));
		}
		return info;
	}

	@Override
	public BinanceOrderInfo order_place(String binanceApiKey, String binanceSecretKey, String symbol, Side side,
			PositionSide ps, Type type, String newClientOrderId, BigDecimal quantity, BigDecimal price,
			BigDecimal stopPrice, Boolean closePosition, WorkingType workingType) {
		BinanceOrderInfo order = new BinanceOrderInfo();
		JSONObject method = MethodDataUtil.getMethodJsonObjec(Method.ORDER_PLACE);
		JSONObject params = new JSONObject();
		params.put("apiKey", binanceApiKey);
		//限价订单
		if(type == Type.LIMIT) {
			params.put("symbol", symbol);
			params.put("side", side);
			params.put("positionSide", ps);
			params.put("type", type);
			if(StringUtil.isNotEmpty(newClientOrderId)) {
				params.put("newClientOrderId", newClientOrderId);
			}
			params.put("quantity", quantity);//委托数量
			params.put("price", price);//委托价格
			params.put("timeinforce", "GTC");
		} else if(type == Type.MARKET) {//市价订单
			params.put("symbol", symbol);
			params.put("side", side);
			params.put("positionSide", ps);
			params.put("type", type);
			if(StringUtil.isNotEmpty(newClientOrderId)) {
				params.put("newClientOrderId", newClientOrderId);
			}
			params.put("quantity", quantity);//委托数量
		} else if(type == Type.STOP) {//限价止损
			params.put("symbol", symbol);
			params.put("side", side);
			params.put("positionSide", ps);
			params.put("type", type);
			if(StringUtil.isNotEmpty(newClientOrderId)) {
				params.put("newClientOrderId", newClientOrderId);
			}
			params.put("quantity", quantity);//委托数量
			params.put("price", price);//委托价
			params.put("stopPrice", stopPrice);//触发价
			params.put("workingType", workingType);//触发价格类型 最新价或标记价
			params.put("timeInForce", "GTE_GTC");
		} else if(type == Type.STOP_MARKET) { //市价止损
			params.put("symbol", symbol);
			params.put("side", side);
			params.put("positionSide", ps);
			params.put("type", type);
			if(StringUtil.isNotEmpty(newClientOrderId)) {
				params.put("newClientOrderId", newClientOrderId);
			}
			params.put("stopPrice", stopPrice);//触发价
			params.put("closePosition", closePosition);//市价止损是否全部平仓
			params.put("workingType", workingType);//触发价格类型 最新价或标记价
			params.put("timeInForce", "GTE_GTC");
		} else if(type == Type.TAKE_PROFIT) {//限价止盈
			params.put("symbol", symbol);
			params.put("side", side);
			params.put("positionSide", ps);
			params.put("type", type);
			if(StringUtil.isNotEmpty(newClientOrderId)) {
				params.put("newClientOrderId", newClientOrderId);
			}
			params.put("quantity", quantity);//委托数量
			params.put("price", price);//委托价格
			params.put("stopPrice", stopPrice);//触发价
			params.put("workingType", workingType);//触发价格类型 最新价或标记价
			params.put("timeInForce", "GTE_GTC");
		} else if(type == Type.TAKE_PROFIT_MARKET) {//市价止盈
			params.put("symbol", symbol);
			params.put("side", side);
			params.put("positionSide", ps);
			params.put("type", type);
			if(StringUtil.isNotEmpty(newClientOrderId)) {
				params.put("newClientOrderId", newClientOrderId);
			}
			//params.put("quantity", quantity);//委托数量
			params.put("stopPrice", stopPrice);//触发价
			params.put("closePosition", closePosition);//市价止损是否全部平仓
			params.put("workingType", workingType);
			params.put("timeInForce", "GTE_GTC");
		}
		params.put("timestamp", new Date().getTime());
		
		MethodDataUtil.generateSignature(params, binanceSecretKey);
		
		method.put("params", params);
		
		logger.info(method);
		
		websocketApi.sendMessage(method);
		
		JSONObject result = websocketApi.read(method.getString("id"));
		if(result.getInt("status") == 200 && result.has("result")) {
			logger.info(result);
			JSONObject o = result.getJSONObject("result");
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
			throw new RuntimeException(result.toString());
		}
		return order;
	}

	@Override
	public String availableBalance(String apiKey, String secretKey, String asset) {
		List<Balance> list = balance(apiKey, secretKey);
		Balance balance = null;
		for(Balance bl : list) {
			if(bl.getAsset().equals(asset)) {
				balance = bl;
				break;
			}
		}
		return balance == null ? "0.0" : balance.getAvailableBalance();
	}

	@Override
	public List<BinanceOrderInfo> tradeMarket(String binanceApiKey, String binanceSecretKey, String symbol,
			PositionSide ps, BigDecimal quantity, BigDecimal stopLoss, BigDecimal takeProfit) {
		List<BinanceOrderInfo> orders = new ArrayList<BinanceOrderInfo>();
		if(ps == PositionSide.LONG) {//做多
			BinanceOrderInfo order = order_place(binanceApiKey, binanceSecretKey, 
			        symbol, Side.BUY, PositionSide.LONG, Type.MARKET, 
			        null, quantity, null, 
			        null, null, null);
			
			BinanceOrderInfo slOrder = order_place(binanceApiKey, binanceSecretKey, 
			        symbol, Side.SELL, PositionSide.LONG, Type.STOP_MARKET, 
			        null, new BigDecimal(order.getOrigQty()), null, 
			        stopLoss, true, WorkingType.CONTRACT_PRICE);
			
			BinanceOrderInfo tpOrder = order_place(binanceApiKey, binanceSecretKey, 
			        symbol, Side.SELL, PositionSide.LONG, Type.TAKE_PROFIT_MARKET, 
			        null, new BigDecimal(order.getOrigQty()), null, 
			        takeProfit, true, WorkingType.CONTRACT_PRICE);
			
			orders.add(order);
			orders.add(slOrder);
			orders.add(tpOrder);
			
		} else {//做空
			BinanceOrderInfo order = order_place(binanceApiKey, binanceSecretKey, 
			        symbol, Side.SELL, PositionSide.SHORT, Type.MARKET, 
			        null, quantity, null, 
			        null, null, null);
			
			BinanceOrderInfo slOrder = order_place(binanceApiKey, binanceSecretKey, 
			        symbol, Side.BUY, PositionSide.SHORT, Type.STOP_MARKET, 
			        null, new BigDecimal(order.getOrigQty()), null, 
			        stopLoss, true, WorkingType.CONTRACT_PRICE);
			
			BinanceOrderInfo tpOrder = order_place(binanceApiKey, binanceSecretKey, 
			        symbol, Side.BUY, PositionSide.SHORT, Type.TAKE_PROFIT_MARKET, 
			        null, new BigDecimal(order.getOrigQty()), null, 
			        takeProfit, true, WorkingType.CONTRACT_PRICE);
			orders.add(order);
			orders.add(slOrder);
			orders.add(tpOrder);
		}
		return orders;
	}

}
