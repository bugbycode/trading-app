package com.util;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;

import com.bugbycode.module.FibKlinesData;
import com.bugbycode.module.Klines;

public class PriceUtil {
	
	private static final Logger logger = LogManager.getLogger(PriceUtil.class);
	
	public static double getMaxPrice(double[] arr) {
		double result = arr[0];
		for(int index = 1;index < arr.length;index++) {
			if(result < arr[index]) {
				result = arr[index];
			}
		}
		return result;
	}
	
	public static double getMinPrice(double[] arr) {
		double result = arr[0];
		for(int index = 1;index < arr.length;index++) {
			if(result > arr[index]) {
				result = arr[index];
			}
		}
		return result;
	}
	
	public static Klines getMaxPriceKLine(List<Klines> klinesList) {
		Klines result = klinesList.get(0);
		for(int index = 1;index < klinesList.size();index++) {
			if(result.getHighPrice() < klinesList.get(index).getHighPrice()) {
				result = klinesList.get(index);
			}
		}
		return result;
	}
	
	public static Klines getMinPriceKLine(List<Klines> klinesList) {
		Klines result = klinesList.get(0);
		for(int index = 1;index < klinesList.size();index++) {
			if(result.getLowPrice() > klinesList.get(index).getLowPrice()) {
				result = klinesList.get(index);
			}
		}
		return result;
	}
	
	public static Klines getMaxPriceKLine(List<Klines> klinesList,int startIndex,int endIndex) {
		return getMaxPriceKLine(klinesList.subList(startIndex, endIndex));
	}
	
	public static Klines getMinPriceKLine(List<Klines> klinesList,int startIndex,int endIndex) {
		return getMinPriceKLine(klinesList.subList(startIndex, endIndex));
	}
	
	public static String formatDoubleDecimal(double number,int decimalPoint) {
		String pattern = "0.";
		
		for(int index = 0;index < decimalPoint;index++) {
			pattern += "0";
		}
		
		DecimalFormat decimalFormat = new DecimalFormat(pattern);
		
		BigDecimal bigDecimal = new BigDecimal(decimalFormat.format(number));
		
		return bigDecimal.toPlainString();
	}
	
	public static double getEma7(List<Klines> klinesList) {
		//周期 N
		int n = klinesList.size() - 1;
		//平滑常数
		double a = 2 / (6 + 1);
		//SMA(9)
		double sma_7 = (/*klinesList.get(0).getClosePrice() + klinesList.get(1).getClosePrice() +*/ klinesList.get(2).getClosePrice() + 
				klinesList.get(3).getClosePrice() + klinesList.get(4).getClosePrice() + klinesList.get(5).getClosePrice()
				+ klinesList.get(6).getClosePrice() + klinesList.get(7).getClosePrice() + klinesList.get(8).getClosePrice()) / 7;
		//p7
		double p_7 = klinesList.get(9).getClosePrice();
		//EMA7 
		double ema_7 = a * p_7 + (1 - a) * sma_7;
		
		return ema_7;
	}
	
	public static double getEma7_parent(List<Klines> klinesList) {
		//周期 N
		int n = klinesList.size() - 1;
		//平滑常数
		double a = 2 / (6 + 1);
		//SMA(9)
		double sma_7 = (/*klinesList.get(0).getClosePrice() +*/ klinesList.get(1).getClosePrice() + klinesList.get(2).getClosePrice() + 
				klinesList.get(3).getClosePrice() + klinesList.get(4).getClosePrice() + klinesList.get(5).getClosePrice()
				+ klinesList.get(6).getClosePrice() + klinesList.get(7).getClosePrice()) / 7;
		//p7
		double p_7 = klinesList.get(8).getClosePrice();
		//EMA7 
		double ema_7 = a * p_7 + (1 - a) * sma_7;
		
		return ema_7;
	}
	
	/**
	 * 获取涨跌幅百分比
	 * @param klinesList
	 * @param index
	 * @return
	 */
	public static double getPriceFluctuationPercentage(List<Klines> klinesList,int index) {
		
		int offset = index - 1;
		
		Klines kl = klinesList.get(index);
		
		boolean isFall = kl.isFall();
		double lowPrice = kl.getLowPrice();
		double hightPrice = kl.getHighPrice();
		
		while(offset >= 0) {
			Klines tmp = klinesList.get(offset--);
			if(isFall) {
				if(tmp.isRise()) {
					break;
				}
			} else {
				if(tmp.isFall()) {
					break;
				}
			}
			if(lowPrice > tmp.getLowPrice()) {
				lowPrice = tmp.getLowPrice();
			}
			if(hightPrice < tmp.getHighPrice()) {
				hightPrice = tmp.getHighPrice();
			}
		}
		if(kl.isFall()) {
			return ((hightPrice - lowPrice) / hightPrice) * 100;
		} else {
			return ((hightPrice - lowPrice) / lowPrice) * 100;
		}
	}
	
	/**
	 * 获取日线级别斐波那契回高点K线信息
	 * 
	 * @param klinesList 所有标志性高点k线信息
	 * @param lastDayKlines 昨日k线信息
	 * @return Klines
	 */
	public static Klines getFibHightKlines(List<Klines> klinesList,Klines lastDayKlines) {
		
		int size = klinesList.size();
		Klines current = null;
		for(int index = 0;index < klinesList.size(); index++) {
			Klines next = null;//前一根k线
			Klines tmp = klinesList.get(index);//当前k线
			
			if(lastDayKlines.getHighPrice() <= tmp.getHighPrice()) {
				current = tmp;
				
				if(index < size - 1) {
					next = klinesList.get(index + 1);//下一根k
				} else {
					next = current;
				}
				
				if(current.getHighPrice() > next.getHighPrice()) {
					break;
				}
			}
		}
		
		if(ObjectUtils.isEmpty(current)) {
			current = lastDayKlines;
		}
		
		return current;
	}
	
	/**
	 * 修正斐波那契高点信息
	 * 
	 * @param lconicHighPriceList 标志性高点k线信息
	 * @param fibLowKlines 已知低点
	 * @param fibHightKlines 已知高点
	 * @return
	 */
	public static Klines rectificationFibHightKlines(List<Klines> lconicHighPriceList,Klines fibLowKlines,Klines fibHightKlines) {
		if(!CollectionUtils.isEmpty(lconicHighPriceList)) {
			long startTime = 0;
			long endTime = 0;
			if(fibLowKlines.getStarTime() < fibHightKlines.getStarTime()) {
				startTime = fibLowKlines.getStarTime();
				endTime = fibHightKlines.getStarTime();
			} else {
				startTime = fibHightKlines.getStarTime();
				endTime = fibLowKlines.getStarTime();
			}
			
			Klines tmp = null;
			for(Klines k : lconicHighPriceList) {
				if(k.getStarTime() >  startTime && k.getStarTime() < endTime) {
					if(k.getHighPrice() > fibHightKlines.getHighPrice()) {
						if(ObjectUtils.isEmpty(tmp) || k.getHighPrice() > tmp.getHighPrice()) {
							tmp = k;
						}
					}
				}
			}
			
			if(!ObjectUtils.isEmpty(tmp)) {
				return tmp;
			}
		}
		return fibHightKlines;
	}
	
	/**
	 * 获取日线级别斐波那契回低点K线信息
	 * 
	 * @param klinesList 所有标志性低点k线信息
	 * @param lastDayKlines 昨日k线信息
	 * @return Klines
	 */
	public static Klines getFibLowKlines(List<Klines> klinesList,Klines lastDayKlines) {
		int size = klinesList.size();
		Klines current = null;
		for(int index = 0;index < size; index++) {
			
			Klines next = null;//前一根k线
			Klines tmp = klinesList.get(index);//当前k线
			
			if(tmp.getLowPrice() <= lastDayKlines.getLowPrice()) {
				current = tmp;
				
				if(index < size - 1) {
					next = klinesList.get(index + 1);//下一根k
				} else {
					next = current;
				}
				
				if(current.getLowPrice() < next.getLowPrice()) {
					break;
				}
			}
		}
		
		if(ObjectUtils.isEmpty(current)) {
			current = lastDayKlines;
		}
		
		return current;
	}
	
	/**
	 * 修正斐波那契低点信息
	 * 
	 * @param lconicHighPriceList 标志性低点k线信息
	 * @param fibLowKlines 已知低点
	 * @param fibHightKlines 已知高点
	 * @return
	 */
	public static Klines rectificationFibLowKlines(List<Klines> lconicLowPriceList,Klines fibLowKlines,Klines fibHightKlines) {
		if(!CollectionUtils.isEmpty(lconicLowPriceList)) {
			long startTime = 0;
			long endTime = 0;
			if(fibLowKlines.getStarTime() < fibHightKlines.getStarTime()) {
				startTime = fibLowKlines.getStarTime();
				endTime = fibHightKlines.getStarTime();
			} else {
				startTime = fibHightKlines.getStarTime();
				endTime = fibLowKlines.getStarTime();
			}
			
			Klines tmp = null;
			for(Klines k : lconicLowPriceList) {
				if(k.getStarTime() >  startTime && k.getStarTime() < endTime) {
					if(k.getLowPrice() < fibLowKlines.getLowPrice()) {
						if(ObjectUtils.isEmpty(tmp) || k.getLowPrice() < tmp.getLowPrice()) {
							tmp = k;
						}
					}
				}
			}
			
			if(!ObjectUtils.isEmpty(tmp)) {
				return tmp;
			}
		}
		return fibLowKlines;
	}
	
	/**
	 * 判断做多条件
	 * @param hitPrice 开仓条件的价格
	 * @param list 最近一段时间K线信息
	 * @return
	 */
	public static boolean isLong(double hitPrice,List<Klines> list) {
		
		boolean isLong = false;
		
		if(!CollectionUtils.isEmpty(list)) {
			int size = list.size();
			Klines current = list.get(size - 1); //倒数第1根k线
			Klines parent = list.get(size - 2);//倒数第2根k线
			
			if(isBreachLong(current, parent, hitPrice)) {
				isLong = true;
			}
		}
		
		return isLong;
	}
	
	/**
	 * 是否出现突破条件价的价格行为</br></br>
	 * 1. 开盘价、收盘价大于等于条件价且最低价小于等于条件价</br> 
	 * 2. 开盘价小于等于条件价 收盘价大于等于条件价 且没有上引线</br>
	 * 3. 开盘价、收盘价大于等于条件价 且前一根k线开盘价小于等于条件价 收盘价大于等于条件价</br>
	 * @param current 当前k线
	 * @param parent 前一根k线
	 * @param hitPrice 条件价
	 * @return
	 */
	public static boolean isBreachLong(Klines current,Klines parent,double hitPrice) {
		       //1. 开盘价、收盘价大于等于条件价且最低价小于等于条件价
		return (current.getOpenPrice() >= hitPrice && current.getClosePrice() >= hitPrice && current.getLowPrice() <= hitPrice)
				//2. 开盘价小于等于条件价 收盘价大于等于条件价 且没有上引线
				|| (current.getOpenPrice() <= hitPrice && current.getClosePrice() >= hitPrice && !current.isUplead()) ||
				//3. 开盘价、收盘价大于等于条件价 且前一根k线开盘价小于等于条件价 收盘价大于等于条件价
				(current.getOpenPrice() >= hitPrice && current.getClosePrice() >= hitPrice && 
				parent.getOpenPrice() <= hitPrice && parent.getClosePrice() >= hitPrice && current.isUplead());
	}
	
	/**
	 * 判断是否为首次突破
	 * @param list
	 * @return
	 */
	public static boolean isFirstBreakthrough(double hitPrice,List<Klines> list) {
		
		boolean isFirstBreakthrough = false;
		if(!CollectionUtils.isEmpty(list)) {
			double hit = 0.0;
			for(Klines klines : list) {
				if(klines.getClosePrice() <= hitPrice) {
					hit++;
				}
			}
			
			if(hit / list.size() > 0.5) {
				isFirstBreakthrough = true;
			}
		}
		return isFirstBreakthrough;
	}
	
	/**
	 * 判断是否为首次跌破
	 * @param hitPrice
	 * @param list
	 * @return
	 */
	public static boolean isFirstfallingBelow(double hitPrice,List<Klines> list) {
		boolean isFirstfallingBelow = true;
		if(!CollectionUtils.isEmpty(list)) {
			double hit = 0.0;
			for(Klines klines : list) {
				if(klines.getClosePrice() >= hitPrice) {
					hit++;
				}
			}
			
			if(hit / list.size() > 0.5) {
				isFirstfallingBelow = true;
			}
		}
		return isFirstfallingBelow;
	}
	
	/**
	 * 判断做空条件
	 * @param hitPrice 开仓条件的价格
	 * @param list 最近一段时间K线信息
	 * @return
	 */
	public static boolean isShort(double hitPrice,List<Klines> list) {
		
		boolean isShort = false;
		
		if(!CollectionUtils.isEmpty(list)) {
			int size = list.size();
			Klines current = list.get(size - 1); //当前k线
			Klines parent = list.get(size - 2);//上一根k线
			
			if(isBreachShort(current, parent, hitPrice)) {
				isShort = true;
			}
		}
		
		return isShort;
	}
	
	/**
	 * 是否出现跌破条件价的价格行为</br></br>
	 * 1、开盘价收盘价小于等于条件价且最高价大于等于条件价</br>
	 * 2、开盘价大于等于条件价、收盘价小于等于条件价且没有下引线</br>
	 * 3、开盘价收盘价小于等于条件价 且前一根k线开盘价大于等于条件价、收盘价小于等于条件价
	 * 
	 * @param current 当前k线
	 * @param parent 前一根k线
	 * @param hitPrice 条件价
	 * @return
	 */
	public static boolean isBreachShort(Klines current,Klines parent,double hitPrice) {
				//开盘价收盘价小于等于条件价且最高价大于等于条件价
		return (current.getOpenPrice() <= hitPrice && current.getClosePrice() <= hitPrice && current.getHighPrice() >= hitPrice) ||
			   //开盘价大于等于条件价、收盘价小于等于条件价且没有下引线
				(current.getOpenPrice() >= hitPrice && current.getClosePrice() <= hitPrice && !current.isDownlead()) || 
			   //开盘价收盘价小于等于条件价 且前一根k线开盘价大于等于条件价、收盘价小于等于条件价
				(current.getOpenPrice() <= hitPrice && current.getClosePrice() <= hitPrice && 
				parent.getOpenPrice() >= hitPrice && parent.getClosePrice() <= hitPrice && current.isDownlead());
	}
	
	public static FibKlinesData<List<Klines>,List<Klines>> getFibKlinesData(List<Klines> list){
		int klinesSize = list.size();
		
		//标志性高点K线信息
		List<Klines> lconicHighPriceList = new ArrayList<Klines>();
		//标志性低点K线信息
		List<Klines> lconicLowPriceList = new ArrayList<Klines>();
		//获取标志性高低点K线信息
		int index = klinesSize - 2;
		while(index >= 1) {
			Klines klines = list.get(index);
			Klines klines_parent = list.get(index - 1);
			Klines klines_after = list.get(index + 1);
			//判断是否为标志性高点
			if(klines.getHighPrice() >= klines_parent.getHighPrice() 
					&& klines.getHighPrice() >= klines_after.getHighPrice()){
				lconicHighPriceList.add(klines);
			}
			
			//判断是否为标志性低点
			if(klines.getLowPrice() <= klines_parent.getLowPrice() 
					&& klines.getLowPrice() <= klines_after.getLowPrice()){
				lconicLowPriceList.add(klines);
			}
			index--;
		}
		
		return new FibKlinesData<List<Klines>,List<Klines>>(lconicLowPriceList,lconicHighPriceList);
	}
	
	/**
	 * 修正多头止损价
	 * @param price
	 * @return
	 */
	public static double rectificationCutLossLongPrice(double price) {
		return price - (price * 0.01);
	}
	
	/**
	 * 修正空头止损价
	 * @param price
	 * @return
	 */
	public static double rectificationCutLossShortPrice(double price) {
		return price + (price * 0.01);
	}
}
