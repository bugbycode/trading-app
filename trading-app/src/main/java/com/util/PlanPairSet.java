package com.util;

import java.util.HashSet;
import java.util.List;

import org.springframework.util.CollectionUtils;

import com.bugbycode.module.TradingPlan;

public class PlanPairSet extends HashSet<String> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	public void addPair(List<TradingPlan> list) {
		if(!CollectionUtils.isEmpty(list)) {
			for(TradingPlan plan : list) {
				super.add(plan.getPair());
			}
		}
	}
	
	@Override
	public void clear() {
		super.clear();
	}
}
