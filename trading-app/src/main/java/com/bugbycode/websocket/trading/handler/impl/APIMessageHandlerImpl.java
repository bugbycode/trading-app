package com.bugbycode.websocket.trading.handler.impl;

import java.util.Date;

import org.json.JSONObject;

import com.bugbycode.module.Method;
import com.bugbycode.module.trading.PositionSide;
import com.bugbycode.module.trading.Side;
import com.bugbycode.module.trading.Type;
import com.bugbycode.websocket.trading.handler.APIMessageHandler;
import com.util.MethodDataUtil;
import com.util.RandomUtil;

public class APIMessageHandlerImpl implements APIMessageHandler{

	@Override
	public void buyOrSell(String symbol, Side side, PositionSide positionSide, double quantity) {
		//开仓
		JSONObject buyOrSellData = new JSONObject();
		//仓位信息
		buyOrSellData.put("id", RandomUtil.GetGuid32());
		buyOrSellData.put("method", "order.place");

		JSONObject buyOrSellParam = new JSONObject();
		buyOrSellData.put("params", buyOrSellParam);
		//参数
		//buyOrSellParam.put("apiKey", this.client.getApiKey());
		buyOrSellParam.put("symbol", symbol);
		buyOrSellParam.put("side", side);
		buyOrSellParam.put("positionSide", positionSide);
		buyOrSellParam.put("quantity", quantity);
		buyOrSellParam.put("timestamp", new Date().getTime());
		buyOrSellParam.put("type", Type.MARKET);
		
		//MethodDataUtil.generateSignature(buyOrSellParam,this.client.getApiKey());
		
		//logger.info(buyOrSellData.toString());
		
		//super.sendMessage(buyOrSellData);
		
	}

	@Override
	public void takeProfit(String symbol, Side side,PositionSide positionSide, double quantity, double stopPrice) {
		//开仓
		JSONObject buyOrSellData = new JSONObject();
		//仓位信息
		buyOrSellData.put("id", RandomUtil.GetGuid32());
		buyOrSellData.put("method", "order.place");

		JSONObject buyOrSellParam = new JSONObject();
		buyOrSellData.put("params", buyOrSellParam);
		//参数
		//buyOrSellParam.put("apiKey", this.client.getApiKey());
		buyOrSellParam.put("symbol", symbol);
		buyOrSellParam.put("side", side);
		buyOrSellParam.put("positionSide", positionSide);
		buyOrSellParam.put("quantity", quantity);
		buyOrSellParam.put("stopPrice", String.format("%.2f", stopPrice));
		buyOrSellParam.put("timestamp", new Date().getTime());
		buyOrSellParam.put("type", Type.TAKE_PROFIT_MARKET);
		
		//MethodDataUtil.generateSignature(buyOrSellParam,this.client.getApiKey());
		
		//logger.info(buyOrSellData.toString());
		
		//super.sendMessage(buyOrSellData);
		
	}

	@Override
	public void stopMarket(String symbol, Side side, PositionSide positionSide, double quantity, double stopPrice) {
		//开仓
		JSONObject buyOrSellData = new JSONObject();
		//仓位信息
		buyOrSellData.put("id", RandomUtil.GetGuid32());
		buyOrSellData.put("method", "order.place");

		JSONObject buyOrSellParam = new JSONObject();
		buyOrSellData.put("params", buyOrSellParam);
		//参数
		//buyOrSellParam.put("apiKey", this.client.getApiKey());
		buyOrSellParam.put("symbol", symbol);
		buyOrSellParam.put("side", side);
		buyOrSellParam.put("positionSide", positionSide);
		buyOrSellParam.put("quantity", quantity);
		buyOrSellParam.put("stopPrice", String.format("%.2f", stopPrice));
		buyOrSellParam.put("timestamp", new Date().getTime());
		buyOrSellParam.put("type", Type.STOP_MARKET);
		
		//MethodDataUtil.generateSignature(buyOrSellParam,this.client.getApiKey());
		
		//logger.info(buyOrSellData.toString());
		
		//super.sendMessage(buyOrSellData);
		
	}

	//@Override
	public void balance() {
		JSONObject method = MethodDataUtil.getMethodJsonObjec(Method.BALANCE);
		JSONObject params = new JSONObject();
		//params.put("apiKey", apiKey);
		params.put("timestamp", new Date().getTime());
		
		//MethodDataUtil.generateSignature(params, secretKey);
		
		method.put("params", params);
		
		//super.sendMessage(method);
	}

	//@Override
	public void balance_v2() {
		JSONObject method = MethodDataUtil.getMethodJsonObjec(Method.BALANCE_V2);
		JSONObject params = new JSONObject();
		//params.put("apiKey", apiKey);
		params.put("timestamp", new Date().getTime());
		
		//MethodDataUtil.generateSignature(params, secretKey);
		
		method.put("params", params);
		
		//super.sendMessage(method);
	}

}
