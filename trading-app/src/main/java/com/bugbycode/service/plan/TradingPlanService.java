package com.bugbycode.service.plan;

import java.util.List;

import com.bugbycode.module.LongOrShortType;
import com.bugbycode.module.TradingPlan;

public interface TradingPlanService {

	public List<TradingPlan> getAllTradingPlan();
	
	public void removeTradingPlan(String pair,LongOrShortType type, double hitPrice);
}
