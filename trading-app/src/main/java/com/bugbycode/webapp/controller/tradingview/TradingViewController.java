package com.bugbycode.webapp.controller.tradingview;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.bugbycode.module.config.TradingViewConfig;
import com.bugbycode.module.user.User;
import com.bugbycode.repository.tradingview.TradingViewConfigRepository;
import com.bugbycode.webapp.controller.base.BaseController;

@RestController
@RequestMapping("/tradingview")
public class TradingViewController extends BaseController{

	@Autowired
	private TradingViewConfigRepository tradingViewConfigRepository;
	
	@GetMapping("/getConfig")
	public TradingViewConfig getConfig() {
		User user = getUserInfo();
		TradingViewConfig config = tradingViewConfigRepository.queryByOwner(user.getUsername());
		if(config == null) {
			config = new TradingViewConfig();
			config.setOwner(user.getUsername());
			config.setInerval("15");
			config.setSymbol("BTCUSDT");
		}
		return config;
	}
	
	@PostMapping("/save")
	public TradingViewConfig save(@RequestBody TradingViewConfig cfg) {
		cfg.setOwner(getUserInfo().getUsername());
		return tradingViewConfigRepository.save(cfg);
	}
}
