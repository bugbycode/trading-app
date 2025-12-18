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

import com.bugbycode.binance.module.order_cancel.OrderCancelInfo;
import com.bugbycode.binance.module.order_cancel.OrderCancelResult;
import com.bugbycode.binance.trade.rest.BinanceRestTradeService;
import com.bugbycode.binance.trade.websocket.BinanceWebsocketTradeService;
import com.bugbycode.config.AppConfig;
import com.bugbycode.exception.OrderCancelException;
import com.bugbycode.exception.OrderPlaceException;
import com.bugbycode.module.AlgoType;
import com.bugbycode.module.Method;
import com.bugbycode.module.binance.Balance;
import com.bugbycode.module.binance.BinanceOrderInfo;
import com.bugbycode.module.binance.CallbackRateEnabled;
import com.bugbycode.module.binance.PriceInfo;
import com.bugbycode.module.binance.ProfitOrderEnabled;
import com.bugbycode.module.binance.SymbolExchangeInfo;
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
	
	@Autowired
	private BinanceRestTradeService binanceRestTradeService;

	@Override
	public List<Balance> balance_v2(String apiKey, String secretKey) {
		List<Balance> blanceList = new ArrayList<Balance>();
		
		JSONObject method = MethodDataUtil.getMethodJsonObjec(Method.BALANCE_V2);
		JSONObject params = new JSONObject();
		params.put("apiKey", apiKey);
		params.put("timestamp", getTime());
		
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
			BigDecimal stopPrice, Boolean closePosition, WorkingType workingType,
			BigDecimal activationPrice, BigDecimal callbackRate) {
		BinanceOrderInfo order = new BinanceOrderInfo();
		
		JSONObject method = MethodDataUtil.getMethodJsonObjec(Method.ALGO_ORDER_PLACE);
		
		JSONObject params = new JSONObject();
		if(type == Type.LIMIT || type == Type.MARKET) {//非条件单
			method = MethodDataUtil.getMethodJsonObjec(Method.ORDER_PLACE);
		} else {
			params.put("algoType", AlgoType.CONDITIONAL);
		}
		
		params.put("apiKey", binanceApiKey);
		//限价订单
		if(type == Type.LIMIT) {
			params.put("symbol", symbol);
			params.put("side", side);
			params.put("positionSide", ps.value());
			params.put("type", type.value());
			if(StringUtil.isNotEmpty(newClientOrderId)) {
				params.put("newClientOrderId", newClientOrderId);
			}
			params.put("quantity", quantity.toString());//委托数量
			params.put("price", price.toString());//委托价格
			params.put("timeinforce", "GTC");
		} else if(type == Type.MARKET) {//市价订单
			params.put("symbol", symbol);
			params.put("side", side);
			params.put("positionSide", ps.value());
			params.put("type", type.value());
			if(StringUtil.isNotEmpty(newClientOrderId)) {
				params.put("newClientOrderId", newClientOrderId);
			}
			params.put("quantity", quantity.toString());//委托数量
		} else if(type == Type.STOP) {//限价止损
			params.put("symbol", symbol);
			params.put("side", side);
			params.put("positionSide", ps.value());
			params.put("type", type.value());
			if(StringUtil.isNotEmpty(newClientOrderId)) {
				params.put("newClientOrderId", newClientOrderId);
			}
			params.put("quantity", quantity.toString());//委托数量
			params.put("price", price.toString());//委托价
			params.put("triggerPrice", stopPrice.toString());//触发价
			params.put("workingType", workingType);//触发价格类型 最新价或标记价
			params.put("timeInForce", "GTE_GTC");
		} else if(type == Type.STOP_MARKET) { //市价止损
			params.put("symbol", symbol);
			params.put("side", side);
			params.put("positionSide", ps.value());
			params.put("type", type.value());
			if(StringUtil.isNotEmpty(newClientOrderId)) {
				params.put("newClientOrderId", newClientOrderId);
			}
			params.put("triggerPrice", stopPrice.toString());//触发价
			params.put("closePosition", closePosition);//市价止损是否全部平仓
			params.put("workingType", workingType);//触发价格类型 最新价或标记价
			params.put("timeInForce", "GTE_GTC");
		} else if(type == Type.TAKE_PROFIT) {//限价止盈
			params.put("symbol", symbol);
			params.put("side", side);
			params.put("positionSide", ps.value());
			params.put("type", type.value());
			if(StringUtil.isNotEmpty(newClientOrderId)) {
				params.put("newClientOrderId", newClientOrderId);
			}
			params.put("quantity", quantity.toString());//委托数量
			params.put("price", price.toString());//委托价格
			params.put("triggerPrice", stopPrice.toString());//触发价
			params.put("workingType", workingType);//触发价格类型 最新价或标记价
			params.put("timeInForce", "GTE_GTC");
		} else if(type == Type.TAKE_PROFIT_MARKET) {//市价止盈
			params.put("symbol", symbol);
			params.put("side", side);
			params.put("positionSide", ps.value());
			params.put("type", type.value());
			if(StringUtil.isNotEmpty(newClientOrderId)) {
				params.put("newClientOrderId", newClientOrderId);
			}
			//params.put("quantity", quantity);//委托数量
			params.put("triggerPrice", stopPrice.toString());//触发价
			params.put("closePosition", closePosition);//市价止损是否全部平仓
			params.put("workingType", workingType);
			params.put("timeInForce", "GTE_GTC");
		} else if(type == Type.TRAILING_STOP_MARKET) {//追踪委托止损
			params.put("symbol", symbol);
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
		
		
		params.put("timestamp", getTime());
		
		MethodDataUtil.generateSignature(params, binanceSecretKey);
		
		method.put("params", params);
		
		logger.debug(method);
		
		websocketApi.sendMessage(method);
		
		JSONObject result = websocketApi.read(method.getString("id"));
		
		if(result.getInt("status") == 200 && result.has("result")) {
			
			logger.debug(result);
			
			JSONObject o = result.getJSONObject("result");
			
			if(o.has("avgPrice")) {
				order.setAvgPrice(o.getString("avgPrice"));
			}
			
			if(o.has("clientOrderId")) {
				order.setClientOrderId(o.getString("clientOrderId"));
			} else if(o.has("clientAlgoId")) {
				order.setClientOrderId(o.getString("clientAlgoId"));
			}
			
			if(o.has("cumQuote")) {
				order.setCumQuote(o.getString("cumQuote"));
			}
			
			if(o.has("executedQty")) {
				order.setExecutedQty(o.getString("executedQty"));
			}
			
			if(o.has("orderId")) {
				order.setOrderId(o.getLong("orderId"));
			} else if(o.has("algoId")) {
				order.setOrderId(o.getLong("algoId"));
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
			String title = "下单" + symbol + ps.getMemo() + type.getMemo() + "出现异常";
			String message = type.value() + "_" + side + " \r\n " + method.toString() + "\r\n" + result.toString();
			throw new OrderPlaceException(title, message);
		}
		order.setRequestData(method.toString());
		order.setResponseData(result.toString());
		return order;
	}

	@Override
	public String availableBalance(String apiKey, String secretKey, String asset) {
		List<Balance> list = balance_v2(apiKey, secretKey);
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
			PositionSide ps, BigDecimal quantity, BigDecimal stopLoss, BigDecimal takeProfit, CallbackRateEnabled callbackRateEnabled, 
			BigDecimal activationPrice, BigDecimal callbackRate, ProfitOrderEnabled profitOrderEnabled) {
		List<BinanceOrderInfo> orders = new ArrayList<BinanceOrderInfo>();
		
		if(ps == PositionSide.LONG) {//做多
			BinanceOrderInfo order = order_place(binanceApiKey, binanceSecretKey,
			        symbol, Side.BUY, PositionSide.LONG, Type.MARKET, 
			        null, quantity, null, 
			        null, null, null, null, null);
			
			BinanceOrderInfo slOrder = order_place(binanceApiKey, binanceSecretKey,
			        symbol, Side.SELL, PositionSide.LONG, Type.STOP_MARKET, 
			        null, new BigDecimal(order.getOrigQty()), null, 
			        stopLoss, true, WorkingType.CONTRACT_PRICE, null, null);
			
			orders.add(order);
			orders.add(slOrder);
			
			if(profitOrderEnabled == ProfitOrderEnabled.OPEN) {
				BinanceOrderInfo tpOrder = order_place(binanceApiKey, binanceSecretKey,
				        symbol, Side.SELL, PositionSide.LONG, Type.TAKE_PROFIT_MARKET, 
				        null, new BigDecimal(order.getOrigQty()), null, 
				        takeProfit, true, WorkingType.CONTRACT_PRICE, null, null);
				orders.add(tpOrder);
			}

			if(callbackRateEnabled == CallbackRateEnabled.OPEN) {
				BinanceOrderInfo cbOrder = order_place(binanceApiKey, binanceSecretKey,
				        symbol, Side.SELL, PositionSide.LONG, Type.TRAILING_STOP_MARKET, 
				        null, new BigDecimal(order.getOrigQty()), null, 
				        takeProfit, true, WorkingType.CONTRACT_PRICE, activationPrice, callbackRate);
				orders.add(cbOrder);
			}
			
		} else {//做空
			BinanceOrderInfo order = order_place(binanceApiKey, binanceSecretKey,
			        symbol, Side.SELL, PositionSide.SHORT, Type.MARKET, 
			        null, quantity, null, 
			        null, null, null, null, null);
			
			BinanceOrderInfo slOrder = order_place(binanceApiKey, binanceSecretKey,
			        symbol, Side.BUY, PositionSide.SHORT, Type.STOP_MARKET, 
			        null, new BigDecimal(order.getOrigQty()), null, 
			        stopLoss, true, WorkingType.CONTRACT_PRICE, null, null);
			
			orders.add(order);
			orders.add(slOrder);
			
			if(profitOrderEnabled == ProfitOrderEnabled.OPEN) {
				BinanceOrderInfo tpOrder = order_place(binanceApiKey, binanceSecretKey,
				        symbol, Side.BUY, PositionSide.SHORT, Type.TAKE_PROFIT_MARKET, 
				        null, new BigDecimal(order.getOrigQty()), null, 
				        takeProfit, true, WorkingType.CONTRACT_PRICE, null, null);
				orders.add(tpOrder);
				
			}
			
			if(callbackRateEnabled == CallbackRateEnabled.OPEN) {
				BinanceOrderInfo cbOrder = order_place(binanceApiKey, binanceSecretKey,
				        symbol, Side.BUY, PositionSide.SHORT, Type.TRAILING_STOP_MARKET, 
				        null, new BigDecimal(order.getOrigQty()), null, 
				        takeProfit, true, WorkingType.CONTRACT_PRICE, activationPrice, callbackRate);
				orders.add(cbOrder);
			}
		}
		
		return orders;
	}

	@Override
	public String getMarketMinQuantity(String symbol) {
		PriceInfo price = getPrice(symbol);
		SymbolExchangeInfo info = AppConfig.SYMBOL_EXCHANGE_INFO.get(symbol);
		if(info == null) {
			return "0";
		}
		return info.getMarketMinQuantity(Double.valueOf(price.getPrice()));
	}

	public long getTime() {
		long t = binanceRestTradeService.getTime();
		if(t <= 0) {
			t = new Date().getTime();
		}
		return t;
	}
	
	@Override
	public OrderCancelResult orderCancel(String binanceApiKey, String binanceSecretKey, String symbol, long orderId) {
		
		JSONObject method = MethodDataUtil.getMethodJsonObjec(Method.ORDER_CANCEL);
		
		JSONObject params = new JSONObject();
		
		params.put("apiKey", binanceApiKey);
		
		params.put("orderId", orderId);
		
		params.put("symbol", symbol);
		
		params.put("timestamp", getTime());
		
		MethodDataUtil.generateSignature(params, binanceSecretKey);
		
		method.put("params", params);
		
		logger.info(method);
		
		websocketApi.sendMessage(method);
		
		JSONObject result = websocketApi.read(method.getString("id"));
		
		logger.info(result);
		
		if(result.getInt("status") == 200 && result.has("result")) {
			
			JSONObject o = result.getJSONObject("result");
			
			OrderCancelInfo info = new OrderCancelInfo();
			if(o.has("clientOrderId")) {
				info.setClientOrderId(o.getString("clientOrderId"));
			}
			
			if(o.has("cumQty")) {
				info.setClientOrderId(o.getString("cumQty"));
			}
			
			if(o.has("cumQuote")) {
				info.setCumQuote(o.getString("cumQuote"));
			}
			
			if(o.has("executedQty")) {
				info.setExecutedQty(o.getString("executedQty"));
			}

			if(o.has("orderId")) {
				info.setOrderId(o.getLong("orderId"));
			}
			
			if(o.has("origQty")) {
				info.setOrigQty(o.getString("origQty"));
			}
			
			if(o.has("origType")) {
				info.setOrigType(o.getString("origType"));
			}
			
			if(o.has("price")) {
				info.setPrice(o.getString("price"));
			}

			if(o.has("reduceOnly")) {
				info.setReduceOnly(o.getBoolean("reduceOnly"));
			}
			
			if(o.has("side")) {
				info.setSide(o.getString("side"));
			}
			
			if(o.has("positionSide")) {
				info.setPositionSide(o.getString("positionSide"));
			}
			
			if(o.has("status")) {
				info.setStatus(o.getString("status"));
			}

			if(o.has("stopPrice")) {
				info.setStopPrice(o.getString("stopPrice"));
			}
			
			if(o.has("closePosition")) {
				info.setClosePosition(o.getBoolean("closePosition"));
			}
			
			if(o.has("symbol")) {
				info.setSymbol(o.getString("symbol"));
			}
			
			if(o.has("timeInForce")) {
				info.setTimeInForce(o.getString("timeInForce"));
			}
			
			if(o.has("type")) {
				info.setType(o.getString("type"));
			}

			if(o.has("activatePrice")) {
				info.setActivatePrice(o.getString("activatePrice"));
			}

			if(o.has("priceRate")) {
				info.setPriceRate(o.getString("priceRate"));
			}
			
			if(o.has("updateTime")) {
				info.setUpdateTime(o.getLong("updateTime"));
			}
			
			if(o.has("workingType")) {
				info.setWorkingType(o.getString("workingType"));
			}
			
			if(o.has("priceProtect")) {
				info.setPriceProtect(o.getBoolean("priceProtect"));
			}
			
			if(o.has("priceMatch")) {
				info.setPriceMatch(o.getString("priceMatch"));
			}
			
			if(o.has("selfTradePreventionMode")) {
				info.setSelfTradePreventionMode(o.getString("selfTradePreventionMode"));
			}
			
			if(o.has("goodTillDate")) {
				info.setGoodTillDate(o.getLong("goodTillDate"));
			}
			
			return new OrderCancelResult(result.getString("id"), result.getInt("status"), info);
		} else {
			throw new OrderCancelException("撤销" + symbol + "永续合约订单出现异常", result.toString());
		}
	}
}
