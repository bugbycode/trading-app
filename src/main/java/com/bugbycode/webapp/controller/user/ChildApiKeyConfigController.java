package com.bugbycode.webapp.controller.user;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.bugbycode.binance.trade.websocket.BinanceWebsocketTradeService;
import com.bugbycode.config.AppConfig;
import com.bugbycode.module.Regex;
import com.bugbycode.module.ResultCode;
import com.bugbycode.module.binance.Balance;
import com.bugbycode.module.user.ChildApiKeyConfig;
import com.bugbycode.module.user.User;
import com.bugbycode.repository.user.UserRepository;
import com.bugbycode.service.user.ChildApiKeyConfigService;
import com.bugbycode.webapp.controller.base.BaseController;
import com.util.RegexUtil;
import com.util.StringUtil;
import com.util.page.SearchResult;

import jakarta.annotation.Resource;

@RestController
@RequestMapping("/childApiKey")
public class ChildApiKeyConfigController extends BaseController{

	private final Logger logger = LogManager.getLogger(ChildApiKeyConfigController.class);
	
	@Resource
	private ChildApiKeyConfigService childApiKeyConfigService;
	
	@Resource
	private UserRepository userRepository;
	
	@Autowired
	private BinanceWebsocketTradeService binanceWebsocketTradeService;
	
	@GetMapping("/query")
	public SearchResult<ChildApiKeyConfig> query(
			@RequestParam(defaultValue = "",name = "keyword")
			String keyword,
			@RequestParam(defaultValue = "0",name = "startIndex") 
			long skip,
			@RequestParam(defaultValue = "10",name = "limit")
			int limit){
		User user = getUserInfo();
		User dbUser = userRepository.queryByUsername(user.getUsername());
		if(dbUser == null) {
			throw new AccessDeniedException("无权访问");
		}
		
		return childApiKeyConfigService.query(user.getUsername(), keyword, skip, limit);
	}
	
	@PostMapping("/saveChildCfg")
	public String saveChildCfg(@RequestBody ChildApiKeyConfig cfg) {
		long now = new Date().getTime();
		JSONObject json = new JSONObject();
		ResultCode code = ResultCode.ERROR;
		String message = "添加成功";
		
		User user = getUserInfo();
		User dbUser = userRepository.queryByUsername(user.getUsername());
		if(dbUser == null) {
			throw new AccessDeniedException("无权访问");
		}
		
		cfg.setUsername(user.getUsername());
		
		List<Balance> balanceList = new ArrayList<Balance>();
		if(!AppConfig.DEBUG && !(StringUtil.isEmpty(cfg.getBinanceApiKey()) || StringUtil.isEmpty(cfg.getBinanceSecretKey()))){
			try {
				balanceList = binanceWebsocketTradeService.balance_v2(cfg.getBinanceApiKey(), cfg.getBinanceSecretKey());
			} catch (Exception e) {
				logger.error(e.getMessage(), e);
			}
		}
		
		if(!RegexUtil.test(cfg.getEmail(), Regex.EMAIL)) {
			message = "请输入正确的邮箱账号";
		} else if(StringUtil.isEmpty(cfg.getBinanceApiKey())) {
			message = "请输入API key";
		} else if(StringUtil.isEmpty(cfg.getBinanceSecretKey())) {
			message = "请输入Secret Key";
		} else if(!AppConfig.DEBUG && CollectionUtils.isEmpty(balanceList)) {
			message = "请输入正确的API key和Secret Key";
		} else if(StringUtil.isNotEmpty(cfg.getId())) {
			cfg.setUpdateTime(now);
			message = "修改成功";
			childApiKeyConfigService.updateById(cfg);
			code = ResultCode.SUCCESS;
		} else {
			cfg.setId(null);
			cfg.setCreateTime(now);
			childApiKeyConfigService.insert(cfg);
			code = ResultCode.SUCCESS;
		}
		
		json.put("message", message);
		json.put("code", code.getCode());
		
		return json.toString();
	}
	
	@PostMapping("/removeChildCfg/{id}")
	public String removeChildCfg(@PathVariable("id") String id) {
		User user = getUserInfo();
		User dbUser = userRepository.queryByUsername(user.getUsername());
		if(dbUser == null) {
			throw new AccessDeniedException("无权访问");
		}
		
		JSONObject json = new JSONObject();
		ResultCode code = ResultCode.SUCCESS;
		String message = "删除成功";
		
		if(StringUtil.isEmpty(id)) {
			throw new AccessDeniedException("无权访问");
		}
		
		ChildApiKeyConfig tmp = childApiKeyConfigService.findById(id);
		if(tmp == null || !user.getUsername().equals(tmp.getUsername())) {
			throw new AccessDeniedException("无权访问");
		}
		
		childApiKeyConfigService.deleteById(id);
		
		json.put("message", message);
		json.put("code", code.getCode());
		return json.toString();
	}
}
