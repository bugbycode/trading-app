package com.util;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.util.CollectionUtils;

import com.bugbycode.binance.module.leverage.LeverageBracketInfo;
import com.bugbycode.module.QuotationMode;
import com.bugbycode.module.binance.PriceInfo;

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
	
	public static int resetLeverage(int leverage, PriceInfo priceInfo, BigDecimal stopLoss) {
		return resetLeverage(leverage, priceInfo, stopLoss.doubleValue());
	}
	
	public static int resetLeverage(int leverage, PriceInfo priceInfo, double stopLoss) {
		int leverageReset = 0;
		
		double percent = PriceUtil.getPercent(priceInfo.getPriceDoubleValue(), stopLoss, 
				priceInfo.getPriceDoubleValue() > stopLoss ? QuotationMode.SHORT : QuotationMode.LONG);
		
		for(int index = leverage; index > 0; index--) {
			if(index * percent <= 30) {
				leverageReset = index;
				break;
			}
		}
		
		return leverageReset < 0 ? 0 : leverageReset;
	}
	
}
