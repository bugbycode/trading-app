package com.util;

import java.util.List;

import org.springframework.util.CollectionUtils;

import com.bugbycode.binance.module.leverage.LeverageBracketInfo;

public class LeverageBracketUtil {

	public static LeverageBracketInfo getMaxLeverageBracketInfo(List<LeverageBracketInfo> list) {
		LeverageBracketInfo result = null;
		if(!CollectionUtils.isEmpty(list)) {
			for(int index = 0; index < list.size(); index++) {
				if(result == null || result.getInitialLeverage() < list.get(index).getInitialLeverage()) {
					result = list.get(index);
				}
			}
		}
		return result;
	}
	
	public static LeverageBracketInfo getMinLeverageBracketInfo(List<LeverageBracketInfo> list) {
		LeverageBracketInfo result = null;
		if(!CollectionUtils.isEmpty(list)) {
			for(int index = 0; index < list.size(); index++) {
				if(result == null || result.getInitialLeverage() > list.get(index).getInitialLeverage()) {
					result = list.get(index);
				}
			}
		}
		return result;
	}
}
