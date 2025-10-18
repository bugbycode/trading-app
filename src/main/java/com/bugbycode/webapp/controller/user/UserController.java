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
import com.bugbycode.module.BreakthroughTradeStatus;
import com.bugbycode.module.CountertrendTradingStatus;
import com.bugbycode.module.PolicyType;
import com.bugbycode.module.RecvCrossUnPnlStatus;
import com.bugbycode.module.RecvTradeStatus;
import com.bugbycode.module.Regex;
import com.bugbycode.module.ResultCode;
import com.bugbycode.module.TradeStepBackStatus;
import com.bugbycode.module.TradeStyle;
import com.bugbycode.module.binance.AutoTrade;
import com.bugbycode.module.binance.AutoTradeType;
import com.bugbycode.module.binance.Balance;
import com.bugbycode.module.binance.CallbackRateEnabled;
import com.bugbycode.module.binance.DrawTrade;
import com.bugbycode.module.user.User;
import com.bugbycode.repository.user.UserRepository;
import com.bugbycode.webapp.controller.base.BaseController;
import com.util.MD5Util;
import com.util.RegexUtil;
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
		
		userRepository.updateUserSubscribeInfo(user.getUsername(), data);
		
		user.copyAIInfo(data);
		
		JSONObject json = new JSONObject();
		
		json.put("code", code.getCode());
		json.put("message", "修改成功");
		
		return json.toString();
	}
	
	@GetMapping("/getBalance")
	public List<Balance> getBalance(){
		User user = getUserInfo();
		return binanceWebsocketTradeService.balance_v2(user.getBinanceApiKey(), user.getBinanceSecretKey());
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
		TradeStyle tradeStyle = TradeStyle.valueOf(data.getTradeStyle());
		CountertrendTradingStatus countertrendTradingStatus = CountertrendTradingStatus.valueOf(data.getCountertrendTrading());
		
		BreakthroughTradeStatus breakthroughTradeStatus = BreakthroughTradeStatus.valueOf(data.getBreakthroughTrade());
		
		CallbackRateEnabled callbackRateEnabled = CallbackRateEnabled.valueOf(data.getCallbackRateEnabled());
		
		PolicyType policyType = PolicyType.valueOf(data.getTradePolicyType());
		
		if(data.getCutLoss() == 0) {
			data.setCutLoss(3);
		}
		
		if(data.getProfit() == 0) {
			data.setProfit(3);
		}
		
		if(data.getCallbackRate() == 0) {
			data.setCallbackRate(3);
		}
		
		if(data.getActivationPriceRatio() == 0) {
			data.setActivationPriceRatio(3);
		}
		
		User user = getUserInfo();
		
		User dbUser = userRepository.queryByUsername(user.getUsername());
		if(dbUser == null) {
			throw new AccessDeniedException("无权访问");
		}
		List<Balance> balanceList = new ArrayList<Balance>();
		if(!(StringUtil.isEmpty(data.getBinanceApiKey()) || StringUtil.isEmpty(data.getBinanceSecretKey()))){
			balanceList = binanceWebsocketTradeService.balance_v2(data.getBinanceApiKey(), data.getBinanceSecretKey());
		}
		
		if(StringUtil.isEmpty(data.getPassword()) || !dbUser.getPassword().equals(MD5Util.md5(data.getPassword()))) {
			json.put("message", "密码不正确");
			code = ResultCode.ERROR;
		} else if(((StringUtil.isEmpty(data.getBinanceApiKey()) || StringUtil.isEmpty(data.getBinanceSecretKey())) && autoTrade == AutoTrade.CLOSE) 
				|| !CollectionUtils.isEmpty(balanceList)) {
			
			userRepository.updateBinanceApiSecurity(user.getUsername(), data.getBinanceApiKey(), data.getBinanceSecretKey(), autoTrade.value(),
					data.getBaseStepSize(),data.getLeverage(),data.getPositionValue(), data.getCutLoss(), data.getProfit(), autoTradeType.value(),
					drawTrade.getValue(), recvTradeStatus.getValue(), recvCrossUnPnlStatus.getValue(), data.getRecvCrossUnPnlPercent(), 
					tradeStepBackStatus.getValue(), tradeStyle.getValue(), data.getProfitLimit(), countertrendTradingStatus.getValue(), 
					data.getFibLevelType(), data.getTradeNumber(), breakthroughTradeStatus.getValue(), data.getCallbackRate(),
					data.getActivationPriceRatio() , callbackRateEnabled.getValue(), data.getTradePairPolicySelected(), policyType.getValue());
			
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
			user.setTradeStyle(tradeStyle.getValue());
			user.setProfitLimit(data.getProfitLimit());
			user.setCountertrendTrading(countertrendTradingStatus.getValue());
			user.setFibLevel(data.getFibLevelType().getValue());
			user.setTradeNumber(data.getTradeNumber());
			user.setBreakthroughTrade(data.getBreakthroughTrade());
			user.setCallbackRate(data.getCallbackRate());
			user.setCallbackRateEnabled(callbackRateEnabled.getValue());
			user.setActivationPriceRatio(data.getActivationPriceRatio());
			user.setTradePolicyType(policyType.getValue());
			user.setTradePairPolicySelected(data.getTradePairPolicySelected());
			
			json.put("message", "修改成功");
		} else {
			json.put("message", "ApiKey或SecretKey信息错误");
			code = ResultCode.ERROR;
		}
		
		json.put("code", code.getCode());
		
		return json.toString();
	}
	
	@PostMapping("/saveSmtpSetting")
	public String saveSmtpSetting(@RequestBody String jsonStr) {
		ResultCode code = ResultCode.SUCCESS;
		JSONObject json = new JSONObject();
		
		User user = getUserInfo();
		
		User dbUser = userRepository.queryByUsername(user.getUsername());
		if(dbUser == null) {
			throw new AccessDeniedException("无权访问");
		}
		
		try {
			JSONObject data = new JSONObject(jsonStr);
			String smtpUser = data.getString("smtpUser");
			String smtpPwd = data.getString("smtpPwd");
			String smtpUser2 = data.getString("smtpUser2");
			String smtpPwd2 = data.getString("smtpPwd2");
			String smtpUser3 = data.getString("smtpUser3");
			String smtpPwd3 = data.getString("smtpPwd3");
			String smtpHost = data.getString("smtpHost");
			String smtpPort = data.getString("smtpPort");
			if(!RegexUtil.test(smtpUser, Regex.EMAIL)) {
				throw new RuntimeException("请输入邮箱格式的SMTP账号");
			} else if(StringUtil.isEmpty(smtpPwd)) {
				throw new RuntimeException("请输入SMTP密码");
			}else if(!RegexUtil.test(smtpUser2, Regex.EMAIL)) {
				throw new RuntimeException("请输入邮箱格式的SMTP账号");
			} else if(StringUtil.isEmpty(smtpPwd2)) {
				throw new RuntimeException("请输入SMTP密码");
			}else if(!RegexUtil.test(smtpUser3, Regex.EMAIL)) {
				throw new RuntimeException("请输入邮箱格式的SMTP账号");
			} else if(StringUtil.isEmpty(smtpPwd3)) {
				throw new RuntimeException("请输入SMTP密码");
			} else if(!RegexUtil.test(smtpHost, Regex.DOMAIN)) {
				throw new RuntimeException("请输入域名格式的SMTP服务地址");
			} else if(!RegexUtil.test(smtpPort, Regex.PORT)) {
				throw new RuntimeException("请输入由0~65535数字组成的端口号");
			}
			
			userRepository.updateSmtpSetting(user.getUsername(), 
					smtpUser, smtpPwd, 
					smtpUser2, smtpPwd2, 
					smtpUser3, smtpPwd3, 
					smtpHost, Integer.valueOf(smtpPort));
			
			user.setSmtpHost(smtpHost);
			user.setSmtpPort(Integer.valueOf(smtpPort));
			user.setSmtpUser(smtpUser);
			user.setSmtpPwd(smtpPwd);

			user.setSmtpUser2(smtpUser2);
			user.setSmtpPwd2(smtpPwd2);

			user.setSmtpUser3(smtpUser3);
			user.setSmtpPwd3(smtpPwd3);
			
			json.put("message", "修改成功");
		} catch (Exception e) {
			code = ResultCode.ERROR;
			json.put("message", e.getMessage());
		}
		json.put("code", code.getCode());
		return json.toString();
	}
}
