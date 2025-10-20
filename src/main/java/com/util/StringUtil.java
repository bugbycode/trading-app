package com.util;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.Set;

import com.bugbycode.module.FibCode;
import com.bugbycode.module.FibInfo;
import com.bugbycode.module.Inerval;
import com.bugbycode.module.Klines;

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
	 * 空头格式化消息
	 * @param pair 交易对
	 * @param currentPrice 开仓价格
	 * @param minPrice 空头止盈价
	 * @param maxPrice 空头止损价
	 * @param decimalPoint 保留小数点数量
	 * @param pnl 盈利百分比
	 * @return
	 */
	public static String formatShortMessage_v2(String pair,double currentPrice,
			double minPrice,double maxPrice,int decimalPoint, String pnl) {
		//空头回报率 (开仓价 - 止盈价) / (止损价 - 开仓价) || 多头回报率 (止盈价 - 开仓价) / (开仓价 - 止损价)
		return String.format("%s卖出价：%s，止盈价：%s，止损价：%s，盈亏比：%s，PNL：%s%%", pair,
				PriceUtil.formatDoubleDecimal(currentPrice,decimalPoint),
				PriceUtil.formatDoubleDecimal(minPrice,decimalPoint),
				PriceUtil.formatDoubleDecimal(maxPrice,decimalPoint),
				PriceUtil.formatDoubleDecimal((currentPrice - minPrice) / (maxPrice - currentPrice),2),
				pnl);
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
	 * 多头格式化消息
	 * @param pair 交易对
	 * @param currentPrice 开仓价格
	 * @param minPrice 止损价
	 * @param maxPrice 止盈价
	 * @param decimalPoint 保留小数点数量
	 * @param pnl 盈利百分比
	 * @return
	 */
	public static String formatLongMessage_v2(String pair,double currentPrice,
			double minPrice,double maxPrice,int decimalPoint, String pnl) {
		//空头回报率 (开仓价 - 止盈价) / (止损价 - 开仓价) || 多头回报率 (止盈价 - 开仓价) / (开仓价 - 止损价)
		return String.format("%s买入价：%s，止盈价：%s，止损价：%s，盈亏比：%s，PNL：%s%%", pair,
				PriceUtil.formatDoubleDecimal(currentPrice,decimalPoint),
				PriceUtil.formatDoubleDecimal(maxPrice,decimalPoint),
				PriceUtil.formatDoubleDecimal(minPrice,decimalPoint), 
				PriceUtil.formatDoubleDecimal((maxPrice - currentPrice) / (currentPrice - minPrice),2),
				pnl);
	}
	
	/**
	 * 空头格式化消息
	 * @param pair 交易对
	 * @param currentPrice 开仓价格
	 * @param fibInfo 斐波那契信息
	 * @param maxPrice 空头止损价
	 * @param fibCode 斐波那契点位（止损参考价）
	 * @return
	 */
	public static String formatShortMessage(String pair,double currentPrice,
			FibInfo fibInfo,double maxPrice,FibCode fibCode) {
		
		return StringUtil.formatShortMessage(pair, currentPrice, fibInfo.getFibValue(fibCode), 
				maxPrice,fibInfo.getDecimalPoint());
	}
	
	/**
	 * 多头格式化消息
	 * @param pair 交易对
	 * @param currentPrice 开仓价格
	 * @param fibInfo 斐波那契信息
	 * @param minPrice 多头止损价
	 * @param fibCode 斐波那契点位（止盈参考价）
	 * @return
	 */
	public static String formatLongMessage(String pair,double currentPrice,
			FibInfo fibInfo,double minPrice,FibCode fibCode) {
		
		return StringUtil.formatLongMessage(pair, currentPrice, minPrice, 
				fibInfo.getFibValue(fibCode), fibInfo.getDecimalPoint());
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
	
	public static String formatCollectionName(String pair,Inerval interval) {
		return pair + "_" + interval.getDescption();
	}
	
	public static String formatCollectionName(Klines k) {
		return formatCollectionName(k.getPair(), k.getInervalType());
	}
	
	public static String urlEncoder(String str) {
		String result = null;
		try {
			result = URLEncoder.encode(str, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
			result = str;
		}
		return result;
	}
	
	public static String urlDecoder(String str) {
		String result = null;
		try {
			result = URLDecoder.decode(str, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
			result = str;
		}
		return result;
	}
}
