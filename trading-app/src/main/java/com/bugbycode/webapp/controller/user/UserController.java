package com.bugbycode.webapp.controller.user;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.bugbycode.binance.trade.websocket.BinanceWebsocketTradeService;
import com.bugbycode.module.RecvCrossUnPnlStatus;
import com.bugbycode.module.RecvTradeStatus;
import com.bugbycode.module.ResultCode;
import com.bugbycode.module.TradeStepBackStatus;
import com.bugbycode.module.binance.AutoTrade;
import com.bugbycode.module.binance.AutoTradeType;
import com.bugbycode.module.binance.Balance;
import com.bugbycode.module.binance.DrawTrade;
import com.bugbycode.module.user.User;
import com.bugbycode.repository.user.UserRepository;
import com.bugbycode.webapp.controller.base.BaseController;
import com.util.MD5Util;
import com.util.StringUtil;

@RestController
@RequestMapping("/user")
public class UserController extends BaseController{

	@Autowired
	private UserRepository userRepository;
	
	@Autowired
	private BinanceWebsocketTradeService binanceWebsocketTradeService;
	
	@GetMapping("/userInfo")
	public User userInfo() {
		return getUserInfo();
	}
	
	@PostMapping("/changPwd")
	public String changPwd(String oldPwd,String newPwd) {
		
		ResultCode code = ResultCode.ERROR;
		
		JSONObject json = new JSONObject();
		User user = getUserInfo();
		User dbUser = userRepository.queryByUsername(user.getUsername());
		if(dbUser == null) {
			throw new AccessDeniedException("无权访问");
		}
		
		if(StringUtil.isEmpty(oldPwd)) {
			json.put("message", "请输入旧密码");
		} else if(StringUtil.isEmpty(newPwd)) {
			json.put("message", "请输入新密码");
		} else if(!MD5Util.md5(oldPwd).equals(dbUser.getPassword())) {
			json.put("message", "旧密码错误");
		} else {
			userRepository.updatePassword(user.getUsername(), newPwd);
			code = ResultCode.SUCCESS;
			json.put("message", "修改密码成功");
		}
		
		json.put("code", code.getCode());
		
		return json.toString();
	}
	
	@PostMapping("/changeSubscribeAi")
	public String changeSubscribeAi(@RequestBody User data) {
		ResultCode code = ResultCode.SUCCESS;
		User user = getUserInfo();
		
		User dbUser = userRepository.queryByUsername(user.getUsername());
		if(dbUser == null) {
			throw new AccessDeniedException("无权访问");
		}
		
		user.copyAIInfo(data);
		
		userRepository.updateUserSubscribeInfo(user.getUsername(), data);
		
		JSONObject json = new JSONObject();
		
		json.put("code", code.getCode());
		json.put("message", "修改成功");
		
		return json.toString();
	}
	
	@GetMapping("/getBalance")
	public List<Balance> getBalance(){
		User user = getUserInfo();
		return binanceWebsocketTradeService.balance(user.getBinanceApiKey(), user.getBinanceSecretKey());
	}
	
	@PostMapping("/changeHmac")
	public String changeHmac(@RequestBody User data) {
		ResultCode code = ResultCode.SUCCESS;
		JSONObject json = new JSONObject();
		
		AutoTrade autoTrade = AutoTrade.valueOf(data.getAutoTrade());
		AutoTradeType autoTradeType = AutoTradeType.valueOf(data.getAutoTradeType());
		DrawTrade drawTrade = DrawTrade.valueOf(data.getDrawTrade());
		RecvTradeStatus recvTradeStatus = RecvTradeStatus.valueOf(data.getRecvTrade());
		RecvCrossUnPnlStatus recvCrossUnPnlStatus = RecvCrossUnPnlStatus.valueOf(data.getRecvCrossUnPnl());
		TradeStepBackStatus tradeStepBackStatus = TradeStepBackStatus.valueOf(data.getTradeStepBack());
		
		if(data.getCutLoss() == 0) {
			data.setCutLoss(3);
		}
		
		if(data.getProfit() == 0) {
			data.setProfit(3);
		}
		
		User user = getUserInfo();
		
		User dbUser = userRepository.queryByUsername(user.getUsername());
		if(dbUser == null) {
			throw new AccessDeniedException("无权访问");
		}
		List<Balance> balanceList = new ArrayList<Balance>();
		if(!(StringUtil.isEmpty(data.getBinanceApiKey()) || StringUtil.isEmpty(data.getBinanceSecretKey()))){
			balanceList = binanceWebsocketTradeService.balance(data.getBinanceApiKey(), data.getBinanceSecretKey());
		}
		
		if(StringUtil.isEmpty(data.getPassword()) || !dbUser.getPassword().equals(MD5Util.md5(data.getPassword()))) {
			json.put("message", "密码不正确");
			code = ResultCode.ERROR;
		} else if(((StringUtil.isEmpty(data.getBinanceApiKey()) || StringUtil.isEmpty(data.getBinanceSecretKey())) && autoTrade == AutoTrade.CLOSE) 
				|| !CollectionUtils.isEmpty(balanceList)) {
			
			userRepository.updateBinanceApiSecurity(user.getUsername(), data.getBinanceApiKey(), data.getBinanceSecretKey(), autoTrade.value(),
					data.getBaseStepSize(),data.getLeverage(),data.getPositionValue(), data.getCutLoss(), data.getProfit(), autoTradeType.value(),
					drawTrade.getValue(), recvTradeStatus.getValue(), recvCrossUnPnlStatus.getValue(), data.getRecvCrossUnPnlPercent(), tradeStepBackStatus.getValue());
			
			user.setBinanceApiKey(data.getBinanceApiKey());
			user.setBinanceSecretKey(data.getBinanceSecretKey());
			user.setAutoTrade(autoTrade.value());
			user.setBaseStepSize(data.getBaseStepSize());
			user.setLeverage(data.getLeverage());
			user.setPositionValue(data.getPositionValue());
			user.setCutLoss(data.getCutLoss());
			user.setProfit(data.getProfit());
			user.setAutoTradeType(autoTradeType.value());
			user.setDrawTrade(drawTrade.getValue());
			user.setRecvTrade(recvTradeStatus.getValue());
			user.setRecvCrossUnPnl(recvCrossUnPnlStatus.getValue());
			user.setRecvCrossUnPnlPercent(data.getRecvCrossUnPnlPercent());
			user.setTradeStepBack(tradeStepBackStatus.getValue());
			
			json.put("message", "修改成功");
		} else {
			json.put("message", "ApiKey或SecretKey信息错误");
			code = ResultCode.ERROR;
		}
		
		json.put("code", code.getCode());
		
		return json.toString();
	}
}
