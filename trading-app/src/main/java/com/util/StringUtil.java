package com.util;

import java.util.Date;
import java.util.Set;

import com.bugbycode.module.EmailInfo;
import com.bugbycode.module.FibCode;
import com.bugbycode.module.FibInfo;

public class StringUtil {
	
	public static boolean isEmpty(String str) {
		return str == null || "".equals(str);
	}
	
	public static boolean isNotEmpty(String str) {
		return !isEmpty(str);
	}
	
	/**
	 * 空头格式化消息
	 * @param pair 交易对
	 * @param currentPrice 开仓价格
	 * @param minPrice 空头止盈价
	 * @param maxPrice 空头止损价
	 * @param decimalPoint 保留小数点数量
	 * 
	 * @return
	 */
	public static String formatShortMessage(String pair,double currentPrice,
			double minPrice,double maxPrice,int decimalPoint) {
		//空头回报率 (开仓价 - 止盈价) / (止损价 - 开仓价) || 多头回报率 (止盈价 - 开仓价) / (开仓价 - 止损价)
		return String.format("%s卖出价：%s，止盈价：%s，止损价：%s，盈亏比：%s", pair,
				PriceUtil.formatDoubleDecimal(currentPrice,decimalPoint),
				PriceUtil.formatDoubleDecimal(minPrice,decimalPoint),
				PriceUtil.formatDoubleDecimal(maxPrice,decimalPoint),
						PriceUtil.formatDoubleDecimal((currentPrice - minPrice) / (maxPrice - currentPrice),2));
	}
	
	/**
	 * 多头格式化消息
	 * @param pair 交易对
	 * @param currentPrice 开仓价格
	 * @param minPrice 止损价
	 * @param maxPrice 止盈价
	 * @param decimalPoint 保留小数点数量
	 * 
	 * @return
	 */
	public static String formatLongMessage(String pair,double currentPrice,
			double minPrice,double maxPrice,int decimalPoint) {
		//空头回报率 (开仓价 - 止盈价) / (止损价 - 开仓价) || 多头回报率 (止盈价 - 开仓价) / (开仓价 - 止损价)
		return String.format("%s买入价：%s，止盈价：%s，止损价：%s，盈亏比：%s", pair,
				PriceUtil.formatDoubleDecimal(currentPrice,decimalPoint),
				PriceUtil.formatDoubleDecimal(maxPrice,decimalPoint),
				PriceUtil.formatDoubleDecimal(minPrice,decimalPoint), 
				PriceUtil.formatDoubleDecimal((maxPrice - currentPrice) / (currentPrice - minPrice),2));
	}
	
	/**
	 * 空头格式化消息
	 * @param pair 交易对
	 * @param currentPrice 开仓价格
	 * @param fibInfo 斐波那契信息
	 * @param maxPrice 空头止损价
	 * @param fibCode 斐波那契点位
	 * @return
	 */
	public static String formatShortMessage(String pair,double currentPrice,
			FibInfo fibInfo,double maxPrice,FibCode fibCode) {
		
		return StringUtil.formatShortMessage(pair, currentPrice, fibInfo.getFibValue(fibCode), 
				PriceUtil.rectificationCutLossShortPrice(maxPrice),fibInfo.getDecimalPoint());
	}
	
	/**
	 * 多头格式化消息
	 * @param pair 交易对
	 * @param currentPrice 开仓价格
	 * @param fibInfo 斐波那契信息
	 * @param minPrice 多头止损价
	 * @param fibCode 斐波那契点位
	 * @return
	 */
	public static String formatLongMessage(String pair,double currentPrice,
			FibInfo fibInfo,double minPrice,FibCode fibCode) {
		
		return StringUtil.formatLongMessage(pair, currentPrice, PriceUtil.rectificationCutLossLongPrice(minPrice), 
				fibInfo.getFibValue(fibCode), fibInfo.getDecimalPoint());
	}
	
	/**
	 * 合约购买提醒格式化消息
	 * @param pair 交易对
	 * @param buyPrice 购买价
	 * @param referencePrice 参考价
	 * @return
	 */
	public static EmailInfo formatBuyFuturesMessage(String pair,double buyPrice,double referencePrice) {
		String text = String.format("%s交易对跌破%s并收回，当前价格：%s", pair,referencePrice,buyPrice);
		String subject = pair + "永续合约交易对买入时机 " + DateFormatUtil.format(new Date());
		return new EmailInfo(subject,text);
	}
	
	/**
	 * 合约卖出提醒格式化消息
	 * @param pair 交易对
	 * @param buyPrice 卖出价
	 * @param referencePrice 参考价
	 * @return
	 */
	public static EmailInfo formatSellFuturesMessage(String pair,double sellPrice,double referencePrice) {
		String text = String.format("%s交易对突破%s并收回，当前价格：%s", pair,referencePrice,sellPrice);
		String subject = pair + "永续合约交易对卖出时机 " + DateFormatUtil.format(new Date());
		return new EmailInfo(subject,text);
	}
	
	public static String concat(double[] arr) {
		StringBuffer buff = new StringBuffer();
		for(int index = 0;index < arr.length;index++) {
			if(index > 0) {
				buff.append(',');
			}
			buff.append(arr[index]);
		}
		return buff.toString();
	}
	
	/**
	 * 检查set中是否包含pair
	 * @param set
	 * @param pair
	 * @return
	 */
	public static boolean contains(Set<String> set,String pair) {
		boolean result = false;
		for(String coin : set) {
			if(coin.equals(pair)) {
				result = true;
				break;
			}
		}
		return result;
	}
}
