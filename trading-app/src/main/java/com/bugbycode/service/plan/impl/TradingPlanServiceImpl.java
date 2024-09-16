package com.bugbycode.service.plan.impl;

import java.io.File;
import java.util.List;

import org.springframework.stereotype.Service;

import com.bugbycode.config.AppConfig;
import com.bugbycode.module.LongOrShortType;
import com.bugbycode.module.TradingPlan;
import com.bugbycode.service.plan.TradingPlanService;
import com.util.StringUtil;
import com.util.TradingPlanFilenameFilter;

@Service("tradingPlanService")
public class TradingPlanServiceImpl implements TradingPlanService {

	@Override
	public List<TradingPlan> getAllTradingPlan() {
		
		File plan_dir = new File(AppConfig.CACHE_PATH);
		
		String[] filenames = plan_dir.list(new TradingPlanFilenameFilter());
		
		return StringUtil.parse(filenames);
	}

	@Override
	public void removeTradingPlan(String pair, LongOrShortType type, double hitPrice) {
		String filename = String.format("%s_%s_%s", pair, type.getLabel(), hitPrice).toLowerCase();
		File file = new File(AppConfig.CACHE_PATH + "/" + filename);
		if(file.exists()) {
			file.delete();
		}
	}

}
