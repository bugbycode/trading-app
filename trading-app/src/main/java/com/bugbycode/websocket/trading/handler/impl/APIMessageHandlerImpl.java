package com.bugbycode.websocket.trading.handler.impl;

import java.util.Date;

import org.json.JSONObject;

import com.bugbycode.module.trading.PositionSide;
import com.bugbycode.module.trading.Side;
import com.bugbycode.module.trading.Type;
import com.bugbycode.websocket.trading.endpoint.TradingWebSocketClientEndpoint;
import com.bugbycode.websocket.trading.handler.APIMessageHandler;
import com.bugbycode.websocket.trading.handler.base.BaseAPIMessageHandler;
import com.util.RandomUtil;

public class APIMessageHandlerImpl extends BaseAPIMessageHandler implements APIMessageHandler {

	@Override
	public void handleMessage(String message) {
		logger.info(message);
	}

	@Override
	public void setClient(TradingWebSocketClientEndpoint client) {
		super.setClient(client);
	}

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
		buyOrSellParam.put("apiKey", this.client.getApiKey());
		buyOrSellParam.put("symbol", symbol);
		buyOrSellParam.put("side", side);
		buyOrSellParam.put("positionSide", positionSide);
		buyOrSellParam.put("quantity", quantity);
		buyOrSellParam.put("timestamp", new Date().getTime());
		buyOrSellParam.put("type", Type.MARKET);
		
		generateSignature(buyOrSellParam);
		
		logger.info(buyOrSellData.toString());
		
		super.sendMessage(buyOrSellData.toString());
		
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
		buyOrSellParam.put("apiKey", this.client.getApiKey());
		buyOrSellParam.put("symbol", symbol);
		buyOrSellParam.put("side", side);
		buyOrSellParam.put("positionSide", positionSide);
		buyOrSellParam.put("quantity", quantity);
		buyOrSellParam.put("stopPrice", String.format("%.2f", stopPrice));
		buyOrSellParam.put("timestamp", new Date().getTime());
		buyOrSellParam.put("type", Type.TAKE_PROFIT_MARKET);
		
		generateSignature(buyOrSellParam);
		
		logger.info(buyOrSellData.toString());
		
		super.sendMessage(buyOrSellData.toString());
		
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
		buyOrSellParam.put("apiKey", this.client.getApiKey());
		buyOrSellParam.put("symbol", symbol);
		buyOrSellParam.put("side", side);
		buyOrSellParam.put("positionSide", positionSide);
		buyOrSellParam.put("quantity", quantity);
		buyOrSellParam.put("stopPrice", String.format("%.2f", stopPrice));
		buyOrSellParam.put("timestamp", new Date().getTime());
		buyOrSellParam.put("type", Type.STOP_MARKET);
		
		generateSignature(buyOrSellParam);
		
		logger.info(buyOrSellData.toString());
		
		super.sendMessage(buyOrSellData.toString());
		
	}

}
