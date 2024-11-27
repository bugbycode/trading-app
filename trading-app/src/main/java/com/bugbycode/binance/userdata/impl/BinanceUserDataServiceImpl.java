package com.bugbycode.binance.userdata.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.bugbycode.binance.userdata.BinanceUserDataService;
import com.bugbycode.module.Method;
import com.bugbycode.module.binance.Balance;
import com.bugbycode.websocket.trading.endpoint.TradingWebSocketClientEndpoint;
import com.util.MethodDataUtil;

@Service("binanceUserDataService")
public class BinanceUserDataServiceImpl implements BinanceUserDataService{

	private final Logger logger = LogManager.getLogger(BinanceUserDataServiceImpl.class);
	
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
				balance.setBalance(item.getDouble("balance"));
				balance.setCrossWalletBalance(item.getDouble("crossWalletBalance"));
				balance.setCrossUnPnl(item.getDouble("crossUnPnl"));
				balance.setAvailableBalance(item.getDouble("availableBalance"));
				balance.setMaxWithdrawAmount(item.getDouble("maxWithdrawAmount"));
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
				balance.setBalance(item.getDouble("balance"));
				balance.setCrossWalletBalance(item.getDouble("crossWalletBalance"));
				balance.setCrossUnPnl(item.getDouble("crossUnPnl"));
				balance.setAvailableBalance(item.getDouble("availableBalance"));
				balance.setMaxWithdrawAmount(item.getDouble("maxWithdrawAmount"));
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
	

}
