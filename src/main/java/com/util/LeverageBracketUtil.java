package com.util;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.util.CollectionUtils;

import com.bugbycode.binance.module.leverage.LeverageBracketInfo;
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
	
	/**
	 * 修正杠杆倍数
	 * @param leverage 当前杠杆倍数
	 * @param price 当前价格
	 * @param stopLoss 止损价格
	 * @return 返回杠杆倍数
	 */
	public static int resetLeverage(int leverage, PriceInfo priceInfo, BigDecimal stopLoss) {
		return resetLeverage(leverage, priceInfo.getPriceDoubleValue(), stopLoss.doubleValue());
	}
	
	/**
	 * 修正杠杆倍数
	 * @param leverage 当前杠杆倍数
	 * @param price 当前价格
	 * @param stopLoss 止损价格
	 * @return 返回杠杆倍数
	 */
	public static int resetLeverage(int leverage, double price, double stopLoss) {
		
		int leverageReset = 0;
		
		if(stopLoss > 0) {
			//止损差价
			double stopLoss_sub = price > stopLoss ? price - stopLoss : stopLoss - price;
			
			for(int index = leverage; index > 0; index--) {
				//仓位保证金 = 价格 % 杠杆倍数
				double orderBalance = price / index;
				if((stopLoss_sub / orderBalance) <= 0.3) {
					leverageReset = index;
					break;
				}
			}
		}
		
		return leverageReset < 0 ? 0 : leverageReset;
	}
	
}
