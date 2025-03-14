package com.util;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;

import com.bugbycode.module.EMAType;
import com.bugbycode.module.EmaTrade;
import com.bugbycode.module.FibCode;
import com.bugbycode.module.FibInfo;
import com.bugbycode.module.FibLevel;
import com.bugbycode.module.Inerval;
import com.bugbycode.module.Klines;
import com.bugbycode.module.QuotationMode;
import com.bugbycode.module.SortType;
import com.bugbycode.module.binance.PriceInfo;

public class PriceUtil {
	
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
	
	public static double getMaxPrice(double price0,double price1) {
		if(price0 > price1) {
			return price0;
		} else {
			return price1;
		}
	}
	
	public static double getMinPrice(double price0,double price1) {
		if(price0 < price1) {
			return price0;
		} else {
			return price1;
		}
	}
	
	public static Klines getLastKlines(List<Klines> klinesList) {
		Klines result = null;
		
		if(!CollectionUtils.isEmpty(klinesList)) {
			result = klinesList.get(klinesList.size() - 1);
		}
		
		return result;
	}
	
	public static List<Klines> getTodayKlines(List<Klines> klinesList) {
		
		int size = klinesList.size();
		int startIndex = 0;
		int endIndex = size;
		Date now = new Date();
		Date todayStart = DateFormatUtil.getTodayStartTime(now);
		if(!CollectionUtils.isEmpty(klinesList)) {
			for(int index = 0; index < size; index++) {
				Klines tmp = klinesList.get(index);
				if(tmp.getStartTime() >= todayStart.getTime()) {
					startIndex = index;
					break;
				}
			}
		}
		return klinesList.subList(startIndex, endIndex);
	}
	
	public static List<Klines> getLastDayKlines(List<Klines> klinesList){
		int size = klinesList.size();
		int startIndex = 0;
		int endIndex = 0;
		Date now = new Date();
		Date todayStart = DateFormatUtil.getTodayStartTime(now);
		Date lastDayStart = DateFormatUtil.getStartTimeBySetDay(todayStart, -1);
		
		if(!CollectionUtils.isEmpty(klinesList)) {
			for(int index = 0; index < size; index++) {
				Klines tmp = klinesList.get(index);
				if(tmp.getStartTime() == todayStart.getTime()) {
					endIndex = index;
				}
				
				if(tmp.getStartTime() == lastDayStart.getTime()) {
					startIndex = index;
				}
			}
		}
		return klinesList.subList(startIndex, endIndex);
	}
	
	public static Klines getMaxPriceKLine(List<Klines> klinesList) {
		Klines result = null;
		if(!CollectionUtils.isEmpty(klinesList)) {
			result = klinesList.get(0);
			for(int index = 1;index < klinesList.size();index++) {
				if(Double.valueOf(result.getHighPrice()) < Double.valueOf(klinesList.get(index).getHighPrice())) {
					result = klinesList.get(index);
				}
			}
		}
		return result;
	}
	
	public static Klines getMinPriceKLine(List<Klines> klinesList) {
		Klines result = null;
		if(!CollectionUtils.isEmpty(klinesList)) {
			result = klinesList.get(0);
			for(int index = 1;index < klinesList.size();index++) {
				if(Double.valueOf(result.getLowPrice()) > Double.valueOf(klinesList.get(index).getLowPrice())) {
					result = klinesList.get(index);
				}
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
	
	public static Klines getMaxPriceKLine(List<Klines> klinesList,long startTime,long endTime) {
		int startIndex = 0;
		int endIndex = 0;
		for(int index = 0;index < klinesList.size();index++) {
			Klines tmp = klinesList.get(index);
			if(tmp.getStartTime() == startTime) {
				startIndex = index;
			} 
			if(tmp.getEndTime() == endTime) {
				endIndex = index + 1;
			}
		}
		return getMaxPriceKLine(klinesList,startIndex,endIndex);
	}
	
	public static Klines getMinPriceKLine(List<Klines> klinesList,long startTime,long endTime) {
		int startIndex = 0;
		int endIndex = 0;
		for(int index = 0;index < klinesList.size();index++) {
			Klines tmp = klinesList.get(index);
			if(tmp.getStartTime() == startTime) {
				startIndex = index;
			}
			if(tmp.getEndTime() == endTime) {
				endIndex = index + 1;
			}
		}
		return getMinPriceKLine(klinesList,startIndex,endIndex);
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
	
	/**
	 * 获取涨跌幅百分比
	 * @param klinesList
	 * @return
	 */
	public static double getPriceFluctuationPercentage(List<Klines> klinesList) {
		
		int index = klinesList.size() - 1;
		int offset = index - 1;
		
		Klines kl = klinesList.get(index);
		
		boolean isFall = isFall(klinesList);
		double lowPrice = Double.valueOf(kl.getLowPrice());
		double hightPrice = Double.valueOf(kl.getHighPrice());
		
		while(offset >= 1) {
			Klines current = klinesList.get(offset--);
			Klines parent = klinesList.get(offset);
			if(isFall) {
				if(isRise_v3(current, parent)) {
					if(hightPrice < Double.valueOf(current.getHighPrice())) {
						hightPrice = Double.valueOf(current.getHighPrice());
					}
					break;
				}
			} else {
				if(isFall_v3(current, parent)) {
					if(lowPrice > Double.valueOf(current.getLowPrice())) {
						lowPrice = Double.valueOf(current.getLowPrice());
					}
					break;
				}
			}
			if(lowPrice > Double.valueOf(current.getLowPrice())) {
				lowPrice = Double.valueOf(current.getLowPrice());
			}
			if(hightPrice < Double.valueOf(current.getHighPrice())) {
				hightPrice = Double.valueOf(current.getHighPrice());
			}
		}
		if(isFall) {
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
			
			if(Double.valueOf(lastDayKlines.getHighPrice()) <= Double.valueOf(tmp.getHighPrice())) {
				current = tmp;
				
				if(index < size - 1) {
					next = klinesList.get(index + 1);//下一根k
				} else {
					next = current;
				}
				
				if(Double.valueOf(current.getHighPrice()) > Double.valueOf(next.getHighPrice())) {
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
			if(fibLowKlines.getStartTime() < fibHightKlines.getStartTime()) {
				startTime = fibLowKlines.getStartTime();
				endTime = fibHightKlines.getStartTime();
			} else {
				startTime = fibHightKlines.getStartTime();
				endTime = fibLowKlines.getStartTime();
			}
			
			Klines tmp = null;
			for(Klines k : lconicHighPriceList) {
				if(k.getStartTime() >  startTime && k.getStartTime() < endTime) {
					if(Double.valueOf(k.getHighPrice()) > Double.valueOf(fibHightKlines.getHighPrice())) {
						if(tmp == null || Double.valueOf(k.getHighPrice()) > Double.valueOf(tmp.getHighPrice())) {
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
			
			if(Double.valueOf(tmp.getLowPrice()) <= Double.valueOf(lastDayKlines.getLowPrice())) {
				current = tmp;
				
				if(index < size - 1) {
					next = klinesList.get(index + 1);//下一根k
				} else {
					next = current;
				}
				
				if(Double.valueOf(current.getLowPrice()) < Double.valueOf(next.getLowPrice())) {
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
			if(fibLowKlines.getStartTime() < fibHightKlines.getStartTime()) {
				startTime = fibLowKlines.getStartTime();
				endTime = fibHightKlines.getStartTime();
			} else {
				startTime = fibHightKlines.getStartTime();
				endTime = fibLowKlines.getStartTime();
			}
			
			Klines tmp = null;
			for(Klines k : lconicLowPriceList) {
				if(k.getStartTime() >  startTime && k.getStartTime() < endTime) {
					if(Double.valueOf(k.getLowPrice()) < Double.valueOf(fibLowKlines.getLowPrice())) {
						if(tmp == null || Double.valueOf(k.getLowPrice()) < Double.valueOf(tmp.getLowPrice())) {
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
			
			if(isBreachLong(current, hitPrice)) {
				isLong = true;
			}
		}
		
		return isLong;
	}
	
	/**
	 * 判断以往的价格是否低于当前回撤点位
	 * @param fibInfo 回撤价格信息
	 * @param afterLowKlines 最低价
	 * @param codes 回撤点位
	 * @param codeOffset 
	 * @return
	 */
	public static boolean isObsoleteLong(FibInfo fibInfo,Klines afterLowKlines,FibCode[] codes,int codeOffset) {
		boolean result = false;

		if(!ObjectUtils.isEmpty(afterLowKlines)) {
			if(codeOffset > 0) {
				FibCode current = codes[codeOffset];
				FibCode next = codes[codeOffset - 1];
				if(current == FibCode.FIB618) {
					next = FibCode.FIB786;
				}
				if(afterLowKlines.getLowPriceDoubleValue() <= fibInfo.getFibValue(next)) {
					result = true;
				}
			}
		}
		
		return result;
	}
	
	/**
	 * 判断以往的价格是否高于当前回撤点位
	 * @param fibInfo 回撤价格信息
	 * @param afterHighKlines 最高价
	 * @param codes 回撤点位
	 * @param codeOffset
	 * @return
	 */
	public static boolean isObsoleteShort(FibInfo fibInfo,Klines afterHighKlines,FibCode[] codes,int codeOffset) {
		boolean result = false;

		if(!ObjectUtils.isEmpty(afterHighKlines)) {
			if(codeOffset > 0) {
				FibCode current = codes[codeOffset];
				FibCode next = codes[codeOffset - 1];
				if(current == FibCode.FIB618) {
					next = FibCode.FIB786;
				}
				if(afterHighKlines.getHighPriceDoubleValue() >= fibInfo.getFibValue(next)) {
					result = true;
				}
			}
		}
		
		return result;
	}
	
	/**
	 * 是否出现突破条件价的价格行为</br></br>
	 * 1. 开盘价、收盘价大于等于条件价且最低价小于等于条件价</br> 
	 * 2. 开盘价小于等于条件价 收盘价大于等于条件价</br>
	 * @param current 当前k线
	 * @param hitPrice 条件价
	 * @return
	 */
	public static boolean isBreachLong(Klines current,double hitPrice) {
		       //1. 开盘价、收盘价大于等于条件价且最低价小于等于条件价
		return (Double.valueOf(current.getOpenPrice()) >= hitPrice && Double.valueOf(current.getClosePrice()) >= hitPrice && Double.valueOf(current.getLowPrice()) <= hitPrice)
				//2. 开盘价小于等于条件价 收盘价大于等于条件价
				|| (Double.valueOf(current.getOpenPrice()) <= hitPrice && Double.valueOf(current.getClosePrice()) >= hitPrice);
	}
	

	/**
	 * 是否出现跌破条件价的价格行为</br></br>
	 * 1、开盘价收盘价小于等于条件价且最高价大于等于条件价</br>
	 * 2、开盘价大于等于条件价、收盘价小于等于条件价</br>
	 * 
	 * @param current 当前k线
	 * @param hitPrice 条件价
	 * @return
	 */
	public static boolean isBreachShort(Klines current,double hitPrice) {
				//开盘价收盘价小于等于条件价且最高价大于等于条件价
		return (Double.valueOf(current.getOpenPrice()) <= hitPrice && Double.valueOf(current.getClosePrice()) <= hitPrice && Double.valueOf(current.getHighPrice()) >= hitPrice) ||
			   //开盘价大于等于条件价、收盘价小于等于条件价
				(Double.valueOf(current.getOpenPrice()) >= hitPrice && Double.valueOf(current.getClosePrice()) <= hitPrice);
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
				if(Double.valueOf(klines.getClosePrice()) <= hitPrice) {
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
				if(Double.valueOf(klines.getClosePrice()) >= hitPrice) {
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
			
			if(isBreachShort(current, hitPrice)) {
				isShort = true;
			}
		}
		
		return isShort;
	}
	
	/**
	 * 修正多头止损价
	 * @param price
	 * @return
	 */
	public static double rectificationCutLossLongPrice(double price) {
		return price - (price * 0.03);
	}
	
	/**
	 * 修正空头止损价
	 * @param price
	 * @return
	 */
	public static double rectificationCutLossShortPrice(double price) {
		return price + (price * 0.03);
	}
	
	/**
	 * 修正多头止损价 V2
	 * @param price
	 * @return
	 */
	public static double rectificationCutLossLongPrice_v2(double price) {
		return price - (price * 0.06);
	}
	
	/**
	 * 修正空头止损价 V2
	 * @param price
	 * @return
	 */
	public static double rectificationCutLossShortPrice_v2(double price) {
		return price + (price * 0.06);
	}
	
	/**
	 * 修正多头止损价 V3
	 * @param price 参考价
	 * @param cutLoss 止损百分比值
	 * @return
	 */
	public static double rectificationCutLossLongPrice_v3(double price,double cutLoss) {
		return price - (price * (cutLoss * 0.01));
	}
	
	/**
	 * 修正空头止损价 V3
	 * @param price 参考价
	 * @param cutLoss 止损百分比值
	 * @return
	 */
	public static double rectificationCutLossShortPrice_v3(double price,double cutLoss) {
		return price + (price * (cutLoss * 0.01));
	}
	
	
	/**
	 * 获取起始k线之后的最低价k线信息
	 * @param klinesList 所有k线信息 
	 * @param startKlines 起始k线信息
	 * @return 最低价k线信息
	 */
	public static Klines getLowKlinesByStartKlines(List<Klines> klinesList,Klines startKlines) {
		Klines result = null;
		if(!CollectionUtils.isEmpty(klinesList)) {
			for(Klines tmp : klinesList) {
				if(tmp.getStartTime() <= startKlines.getStartTime()) {
					continue;
				}
				
				if(result == null) {
					result = tmp;
					continue;
				}
				
				if(Double.valueOf(tmp.getLowPrice()) < Double.valueOf(result.getLowPrice())) {
					result = tmp;
				}
			}
		}
		return result;
	}

	/**
	 * 获取起始k线之后的最高价k线信息
	 * @param klinesList 所有k线信息 
	 * @param startKlines 起始k线信息
	 * @return 最高价k线信息
	 */
	public static Klines getHightKlinesByStartKlines(List<Klines> klinesList,Klines startKlines) {
		Klines result = null;
		if(!CollectionUtils.isEmpty(klinesList)) {
			for(Klines tmp : klinesList) {
				if(tmp.getStartTime() <= startKlines.getStartTime()) {
					continue;
				}
				
				if(result == null) {
					result = tmp;
					continue;
				}
				
				if(Double.valueOf(tmp.getHighPrice()) > Double.valueOf(result.getHighPrice())) {
					result = tmp;
				}
			}
		}
		return result;
	}
	
	/**
     * 计算单个周期的EMA值
     *
     * @param previousEMA 上一个周期的EMA值
     * @param price 当前周期的价格（通常为收盘价）
     * @param period EMA的周期长度，例如99表示99周期的EMA
     * @return 计算出的当前周期的EMA值
     */
    public static double calculateEMA(double previousEMA, double price, int period) {
        // 计算平滑因子
        double smoothingFactor = 2.0 / (period + 1);
        // 使用公式计算EMA值
        return (price - previousEMA) * smoothingFactor + previousEMA;
    }

    /**
     * 计算给定价格数组的EMA
     *
     * @param prices 包含时间序列的价格数组，从旧到新排序
     * @param type EMA的周期长度，例如99表示99周期的EMA
     * @return 计算出的EMA值数组，其中每个元素对应于prices数组的同一索引处的EMA值
     */
    public static void calculateEMAArray(List<Klines> klinesList, EMAType type) {
        
        double initialSMA = 0.0;

        int period = type.getValue(); 
        int size = size(klinesList);
        if(period > size) {
        	period = size;
        }
        // 计算初始的SMA作为第一个EMA的起始点
        for (int i = 0; i < period; i++) {
        	initialSMA += Double.valueOf(klinesList.get(i).getClosePrice());
        }
        initialSMA /= period;

        // 第一个有效的EMA值是初始SMA
        switch (type) {
		case EMA7:
	        klinesList.get(period - 1).setEma7(initialSMA);
			break;
		case EMA25:
	        klinesList.get(period - 1).setEma25(initialSMA);
			break;

		default:
	        klinesList.get(period - 1).setEma99(initialSMA);
			break;
		}

        // 从第period个价格开始计算EMA
        for (int i = period; i < klinesList.size(); i++) {
        	switch (type) {
			case EMA7:
	        	klinesList.get(i).setEma7(calculateEMA(klinesList.get(i - 1).getEma7(), Double.valueOf(klinesList.get(i).getClosePrice()), period));
				break;
			case EMA25:
	        	klinesList.get(i).setEma25(calculateEMA(klinesList.get(i - 1).getEma25(), Double.valueOf(klinesList.get(i).getClosePrice()), period));
				break;
			default:
	        	klinesList.get(i).setEma99(calculateEMA(klinesList.get(i - 1).getEma99(), Double.valueOf(klinesList.get(i).getClosePrice()), period));
				break;
			}
        }
    }
    
    /**
     * 根据EMA判断是否可做空
     * @param klinesList
     * @return
     */
    public static boolean isOpenShortEMA(List<Klines> klinesList) {
    	
    	boolean isOpenShort = false;
    	if(!CollectionUtils.isEmpty(klinesList)){

			Klines klines = getLastKlines(klinesList);
    	
			double ema7 = klines.getEma7();
			double ema25 = klines.getEma25();
			double ema99 = klines.getEma99();
			double closePrice = Double.valueOf(klines.getClosePrice());
			double hightPrice = Double.valueOf(klines.getHighPrice());
			
			//ema7、ema25小于ema99 收盘价小于ema99 最高价大于ema99
			if(ema7 <= ema99 && ema25 <= ema99 && closePrice <= ema99 && hightPrice >= ema99){
				isOpenShort = true;
			}
		}
    	return isOpenShort;
    }
    
    /**
     * 根据EMA判断是否可做多
     * @param klinesList
     * @return
     */
    public static boolean isOpenLongEMA(List<Klines> klinesList) {
    	
    	boolean isOpenLong = false;
		if(!CollectionUtils.isEmpty(klinesList)){
			Klines klines = getLastKlines(klinesList);
			
			double ema7 = klines.getEma7();
			double ema25 = klines.getEma25();
			double ema99 = klines.getEma99();

			double closePrice = Double.valueOf(klines.getClosePrice());
			double lowPrice = Double.valueOf(klines.getLowPrice());
			//ema7、ema25 大于ema99 收盘价大于ema99 最低价小于ema99
			if(ema7 >= ema99 && ema25 >= ema99 && closePrice >= ema99 && lowPrice <= ema99){
				isOpenLong = true;
			}
		}

    	return isOpenLong;
    }
    
    /**
     * 获取盘整区第一根k线
     * @param list
     * @return
     */
    public static Klines getConsolidationAreaFirstKlines(List<Klines> list) {
    	
    	int len = list.size();
    	;
    	
    	Klines first = null;
    	double highPrice = 0;
    	double lowPrice = 0;
    	
    	for(int offset = len - 1; offset > 0;offset--) {
    		Klines tmp = list.get(offset);
    		//开盘价收盘价均在lowPrice~highPrice之间 或者最高价最低价包含lowPrice~highPrice
    		if(first == null || isConsolidationArea(tmp,highPrice,lowPrice)) {
    			
    			if(first == null) {
    				highPrice = Double.valueOf(tmp.getHighPrice());
        			lowPrice = Double.valueOf(tmp.getLowPrice());
    			}
    			
    			if(Double.valueOf(tmp.getHighPrice()) > highPrice) {
    				highPrice = Double.valueOf(tmp.getHighPrice());
    			}
    			
    			if(Double.valueOf(tmp.getLowPrice()) < lowPrice) {
    				lowPrice = Double.valueOf(tmp.getLowPrice());
    			}
    			
    			first = tmp;
    		}
    		
    		if(!isConsolidationArea(tmp,highPrice,lowPrice)) {
    			break;
    		}
    	}
    	
    	return first;
    }
    
    /**
     * 判断k线是否在某个盘整区
     * @param klines k线
     * @param highPrice 盘整区最高价
     * @param lowPrice 盘整区最低价
     * @return
     */
    public static boolean isConsolidationArea(Klines klines,double highPrice,double lowPrice) {
    	boolean flag = false;
    	if((Double.valueOf(klines.getOpenPrice()) <= highPrice && Double.valueOf(klines.getOpenPrice()) >= lowPrice && 
    			Double.valueOf(klines.getClosePrice()) <= highPrice && Double.valueOf(klines.getClosePrice()) >= lowPrice)
    			|| (Double.valueOf(klines.getHighPrice()) >= highPrice && Double.valueOf(klines.getLowPrice()) <= lowPrice)) {
    		flag = true;
    	}
    	return flag;
    }
    
    /**
     * 截取某段K线数据
     * @param startKlines 起始K线
     * @param endKlines 结束K线
     * @param list 要截取的K线集合
     * @return 从startKlines~endKlines的所有K线
     */
    public static List<Klines> subList(Klines startKlines,Klines endKlines,List<Klines> list){
    	int startIndex = -1;
    	int endIndex = -1;
    	for(int index = 0;index < list.size(); index++) {
    		Klines tmp = list.get(index);
    		if(startKlines.getStartTime() == tmp.getStartTime()) {
    			startIndex = index;
    		}
    		if(endKlines.getStartTime() == tmp.getStartTime()) {
    			endIndex = index + 1;
    		}
    		
    		if(!(startIndex == -1 || endIndex == -1)) {
    			break;
    		}
    	}
    	
    	if(startIndex == -1 || endIndex == -1) {
    		startIndex = 0;
    		endIndex = 0;
    	}
    	return list.subList(startIndex, endIndex);
    }
    
    /**
     * 截取某段K线数据
     * @param startKlines 起始K线
     * @param list 要截取的K线集合
     * @return 从startKlines开始的所有K线
     */
    public static List<Klines> subList(Klines startKlines,List<Klines> list){
    	
    	return subList(startKlines, getLastKlines(list), list);
    	
    }
    
    /**
     * 检查是否还有更高的k线
     * @param klines 条件k线
     * @param list 匹配的集合
     * @return true为真 false为假
     */
    public static boolean checkHigherKlines(Klines klines,List<Klines> list) {
    	
    	boolean result = false;
    	
    	for(Klines tmp : list) {
    		if(tmp.getStartTime() == klines.getStartTime()) {
    			continue;
    		}
    		if(Double.valueOf(tmp.getHighPrice()) > Double.valueOf(klines.getHighPrice())) {
    			result = true;
    			break;
    		}
    	}
    	return result;
    }
    
    /**
     * 检查是否还有更低的k线
     * @param klines 条件k线
     * @param list 匹配的集合
     * @return true为真 false为假
     */
    public static boolean checkLowerKlines(Klines klines,List<Klines> list) {
    	
    	boolean result = false;
    	
    	for(Klines tmp : list) {
    		if(tmp.getStartTime() == klines.getStartTime()) {
    			continue;
    		}
    		if(Double.valueOf(tmp.getLowPrice()) < Double.valueOf(klines.getLowPrice())) {
    			result = true;
    			break;
    		}
    	}
    	return result;
    }
    
    /**
     * 获取命中的高点k线信息
     * @param list 标志性高点K线信息
     * @param hitKlines
     * @return
     */
    public static Klines getPositionHighKlines(List<Klines> list,List<Klines> klinesList_hit) {
    	Klines result = null;
    	for(Klines k : list) {
    		double highPrice = Double.valueOf(k.getHighPrice());
    		if((isLong(highPrice, klinesList_hit) || isShort(highPrice, klinesList_hit)) 
    				&& !checkHigherKlines(k, subList(k, list))) {
    			result = k;
    			break;
    		}
    	}
    	return result;
    }
    
    /**
     * 获取命中的低点k线信息
     * @param list 标志性低点K线信息
     * @param hitKlines 
     * @return
     */
    public static Klines getPositionLowKlines(List<Klines> list,List<Klines> klinesList_hit) {
    	Klines result = null;
    	for(Klines k : list) {
    		double lowPrice = Double.valueOf(k.getLowPrice());
    		if((isLong(lowPrice, klinesList_hit) || isShort(lowPrice, klinesList_hit)) 
    				&& !checkLowerKlines(k, subList(k, list))) {
    			result = k;
    			break;
    		}
    	}
    	return result;
    }
    
    /**
     * 判断是否为上涨
     * @param list
     * @return
     */
    public static boolean isRise(List<Klines> list) {
    	if(!CollectionUtils.isEmpty(list)) {
    		int size = list.size();
    		if(size == 1) {
    			return list.get(0).isRise();
    		} else {
    			KlinesUtil ku = new KlinesUtil(list);
    			return isRise(ku.removeLast(), ku.removeLast());
    		}
    	}
    	return true;
    }
    
    /**
     * 判断是否下跌
     * @param list
     * @return
     */
    public static boolean isFall(List<Klines> list) {
    	return !isRise(list);
    }
    
    /**
     * 判断是否在上涨
     * @param current 当前k线
     * @param parent 前一根k线
     * @return
     */
    public static boolean isRise(Klines current,Klines parent) {
    	return Double.valueOf(current.getClosePrice()) >= Double.valueOf(parent.getOpenPrice());
    }
    
    /**
     * 判断是否在下跌
     * @param current 当前k线
     * @param parent 前一根k线
     * @return
     */
    public static boolean isFall(Klines current,Klines parent){
    	return !isRise(current, parent);
    }
    
    /**
     * 获取大小
     * 
     * @param list
     * @return
     */
    public static int size(List<Klines> list) {
    	return list == null ? 0 : list.size();
    }
    
    /**
     * 
     * 15分钟k线合成1小时
     * 
     * @param list_15m
     * @return
     */
    public static List<Klines> to1HFor15MKlines(List<Klines> list_15m){
    	
    	KlinesComparator kc = new KlinesComparator(SortType.ASC);
    	List<Klines> list_1h = new ArrayList<Klines>();
    	
    	if(!CollectionUtils.isEmpty(list_15m)) {
    		
			list_15m.sort(kc);
			
    		Map<String,List<Klines>> klinesMap = new HashMap<String, List<Klines>>();
    		
    		for(Klines kl_15m : list_15m) {
    			if(kl_15m.getInervalType() != Inerval.INERVAL_15M) {
    				throw new RuntimeException("将15分钟级别k线合成1小时级别k线时出现错误，错误信息：传入的k线不是15分钟级别");
    			}
    			
    			String key = DateFormatUtil.format_yyyy_mm_dd_HH_00_00(new Date(kl_15m.getStartTime()));
    			List<Klines> kl_temp = klinesMap.get(key);
    			if(kl_temp == null) {
    	    		kl_temp = new ArrayList<Klines>();
    	    		klinesMap.put(key, kl_temp);
    			}
    			
				kl_temp.add(kl_15m);
    			
    		}
    		
    		Set<String> keySet = klinesMap.keySet();
    		for(String key : keySet) {
    			List<Klines> kl_temp = klinesMap.get(key);
    			kl_temp.sort(kc);
    			list_1h.add(parse15To1h(kl_temp));
    		}
    	}
		list_1h.sort(kc);
    	return list_1h;
    }

	private static Klines parse15To1h(List<Klines> list_15m){
		KlinesUtil ku = new KlinesUtil(list_15m);
		Klines highKlines = ku.getMax();
		Klines lowKlines = ku.getMin();
		Klines firstKlines = ku.getFirst();
		Klines lastKlines = ku.getLast();
		String openPrice = firstKlines.getOpenPrice();
		String closePrice = lastKlines.getClosePrice();
		String highPrice = highKlines.getHighPrice();
		String lowPrice = lowKlines.getLowPrice();
		long startTime = firstKlines.getStartTime();
		long endTime = lastKlines.getEndTime();
		String pair = firstKlines.getPair();
		int decimalNum = firstKlines.getDecimalNum();
		String inerval = Inerval.INERVAL_1H.getDescption();
		
		String v = String.valueOf(sum_volume(list_15m));
		String iv = String.valueOf(sum_i_volume(list_15m));
		long n = sum_n(list_15m);
		String q = String.valueOf(sum_q(list_15m));
		String iq = String.valueOf(sum_iq(list_15m));
		
		return new Klines(pair, startTime, openPrice, highPrice, lowPrice, closePrice, endTime, inerval, decimalNum, v, n, q, iv, iq);
	}
	
	public static double sum_volume(List<Klines> list) {
		double v = 0;
		for(Klines k : list) {
			v += k.getVDoubleValue();
		}
		return v;
	}
	
	public static double sum_i_volume(List<Klines> list) {
		double iv = 0;
		for(Klines k : list) {
			iv += k.getIvDoubleValue();
		}
		return iv;
	}
	
	public static long sum_n(List<Klines> list) {
		long n = 0;
		for(Klines k : list) {
			n += k.getN();
		}
		return n;
	}
	
	public static double sum_q(List<Klines> list) {
		double q = 0;
		for(Klines k : list) {
			q += k.getQDoubleValue();
		}
		return q;
	}
	
	public static double sum_iq(List<Klines> list) {
		double iq = 0;
		for(Klines k : list) {
			iq += k.getIqDoubleValue();
		}
		return iq;
	}
	
	/**
	 * 检查是否命中斐波那契回撤价格
	 * @param list
	 * @param hitPrice
	 * @return
	 */
	public static boolean checkFibHitPrice(List<Klines> list,double hitPrice) {
		KlinesUtil ku = new KlinesUtil(list);
		Klines last = ku.removeLast();
		Klines next = ku.removeLast();
		return checkHitPrice(last,hitPrice) || checkHitPrice(next,hitPrice);
	}
	
	/**
	 * 检查k线是否命中目标价格
	 * @param k
	 * @param hitPrice
	 * @return
	 */
	public static boolean checkHitPrice(Klines k,double hitPrice) {
		return k == null ? false : (Double.valueOf(k.getLowPrice()) <= hitPrice && Double.valueOf(k.getHighPrice()) >= hitPrice);
	}
	
	/**
	 * 计算价格上涨幅度
	 * @param price1 
	 * @param price2 
	 * @return
	 */
	public static double getRiseFluctuationPercentage(double price1,double price2) {
		if(price1 > price2) {
			return (price1 - price2) / price2;
		} else {
			return (price2 - price1) / price1;
		}
	}
	
	/**
	 * 计算价格下跌幅度
	 * @param price1 
	 * @param price2 
	 * @return
	 */
	public static double getFallFluctuationPercentage(double price1,double price2) {
		if(price1 > price2) {
			return (price1 - price2) / price1;
		} else {
			return (price2 - price1) / price2;
		}
	}
	
	/**
	 * 根据波动幅度获取上涨之后价格
	 * @param price 参考价格
	 * @param percent 波动幅度
	 * @return
	 */
	public static double getLongTakeProfitForPercent(double price,double percent) {
		return price + (price * percent);
	}
	
	/**
	 * 根据波动幅度获取下跌之后价格
	 * @param price 参考价格
	 * @param percent 波动幅度
	 * @return
	 */
	public static double getShortTakeProfitForPercent(double price,double percent) {
		return price - (price * percent);
	}
	
	/**
	 * 根据EMA指标分析计算斐波那契回撤信息
	 * @param list
	 * @return
	 */
	public static FibInfo getFibInfoForEma(List<Klines> klinesList) {
		
		List<Klines> list = new ArrayList<Klines>();
		list.addAll(klinesList);
		
		calculateEMAArray(list, EMAType.EMA7);
		calculateEMAArray(list, EMAType.EMA25);
		calculateEMAArray(list, EMAType.EMA99);
		
		EmaTrade et = getTradingBehavior(list);
		
		int lastIndex = list.size() - 1;
		
		List<Klines> fibKlines = new ArrayList<Klines>();
		
		KlinesComparator kc_asc = new KlinesComparator(SortType.ASC);
		
		fibKlines.sort(kc_asc);
		
		for(int index = lastIndex; index >= 0; index--) {
			Klines current = list.get(index);
			fibKlines.add(current);
			if(et == EmaTrade.LONG && verifyLtEma99(current)) {
				break;
			} else if(et == EmaTrade.SHORT && verifyGtEma99(current)) {
				break;
			}
		}
		
		Klines fib1Klines = null;
		Klines fib0Klines = null;
		
		FibInfo fibInfo = null;
		
		if(et == EmaTrade.LONG) {
			fib1Klines = getMaxPriceKLine(fibKlines);
			fibKlines = subList(fib1Klines, list);
			fib0Klines = getMinPriceKLine(fibKlines);
			fibInfo = new FibInfo(Double.valueOf(fib1Klines.getHighPrice()), Double.valueOf(fib0Klines.getLowPrice()), fib0Klines.getDecimalNum(), FibLevel.LEVEL_1);
		} else if(et == EmaTrade.SHORT) {
			fib1Klines = getMinPriceKLine(fibKlines);
			fibKlines = subList(fib1Klines, list);
			fib0Klines = getMaxPriceKLine(fibKlines);
			fibInfo = new FibInfo(Double.valueOf(fib1Klines.getLowPrice()), Double.valueOf(fib0Klines.getHighPrice()), fib0Klines.getDecimalNum(), FibLevel.LEVEL_1);
		}
		
		return fibInfo;
	}
	
	/**
	 * 判断交易行为 LONG/SHORT
	 * @param list
	 * @return
	 */
	public static EmaTrade getTradingBehavior(List<Klines> list) {
		calculateEMAArray(list, EMAType.EMA7);
		calculateEMAArray(list, EMAType.EMA25);
		calculateEMAArray(list, EMAType.EMA99);
		
		EmaTrade result = EmaTrade.NOTHING;
		
		KlinesUtil ku = new KlinesUtil(list);
		Klines last = ku.getLast();
		
		if(verifyGtEma99(last)) {
			result = EmaTrade.LONG;
		} else if(verifyLtEma99(last)) {
			result = EmaTrade.SHORT;
		}
		
		return result;
	}
	
	/**
	 * 判断k线EMA7 EMA25是否在EMA99之上
	 * @param k
	 * @return
	 */
	public static boolean verifyGtEma99(Klines k) {
		return k.getEma7() >= k.getEma99() && k.getEma25() >= k.getEma99();
	}
	
	/**
	 * 判断k线EMA7 EMA25是否在EMA99之下
	 * @param k
	 * @return
	 */
	public static boolean verifyLtEma99(Klines k) {
		return k.getEma7() <= k.getEma99() && k.getEma25() <= k.getEma99();
	}
	
	/**
	 * 判断list中是否包含klines
	 * @param list
	 * @param klines
	 * @return
	 */
	public static boolean contains(List<Klines> list, Klines klines) {
		if(!CollectionUtils.isEmpty(list)) {
			for(Klines k : list) {
				if(k.isEquals(klines)) {
					return true;
				}
			}
		}
		return false;
	}
	
	/**
	 * 校验当前k线是否为最后一天k线
	 * @param klines
	 * @return
	 */
	public static boolean verifyLastDay(Klines klines) {
		boolean flag = false;
		Date now = new Date();
		Date todayStart = DateFormatUtil.getTodayStartTime(now);
		Date lastDayStart = DateFormatUtil.getStartTimeBySetDay(todayStart, -1);
		if(klines.getStartTime() == lastDayStart.getTime()) {
			flag = true;
		}
		return flag;
	}
	
	/**
	 * 修正持仓数量
	 * 
	 * @param quantity 预计持仓数量 持仓数量 = 最小持仓数量 x 名义价值倍数
	 * @param minQuantity 最小持仓数量
	 * @param baseStepSize 名义价值倍数
	 * @param positionValue 持仓成本限制
	 * @param priceInfo 当前价格
	 * @return
	 */
	public static BigDecimal rectificationQuantity(BigDecimal quantity,BigDecimal minQuantity,int baseStepSize, double positionValue,PriceInfo priceInfo) {
		BigDecimal result = quantity;
		//持仓成本限制
		BigDecimal positionVal = new BigDecimal(positionValue);
		//持仓价值
		BigDecimal quantityValue = quantity.multiply(priceInfo.getPriceBigDecimalValue());
		//预计持仓价值大于持仓成本限制
		if(quantityValue.compareTo(positionVal) == 1) {
			if(baseStepSize > 1) {
				//重新计算名义价值倍数
				//新名义价值倍数 = 持仓成本限制金额 / (最小持仓数量 x 当前价格) 
				BigDecimal newBaseStepSizeVal = positionVal.divide(minQuantity.multiply(priceInfo.getPriceBigDecimalValue()), 10, RoundingMode.HALF_UP);
				int newBaseStepSize = newBaseStepSizeVal.intValue();
				//新计算倍数小于1时为1
				if(newBaseStepSize < 1) {
					newBaseStepSize = 1;
				}
				newBaseStepSizeVal = new BigDecimal(newBaseStepSize);
				result = minQuantity.multiply(newBaseStepSizeVal);
			}
		}
		return result;
	}
	
	/**
	 * 校验价格是否出现颓势 </br>
	 * 
	 * 1、前4根k线连续上涨且当前k线收盘价小于等于前两根k线实体部分最低价格
	 * 
	 * @param list
	 * @return
	 */
	public static boolean verifyDecliningPrice(List<Klines> list) {
		
		int lastIndex = list.size() - 1;
		Klines k0 = list.get(lastIndex);
		Klines k1 = list.get(lastIndex - 1);
		Klines k2 = list.get(lastIndex - 2);
		Klines k3 = list.get(lastIndex - 2);
		Klines k4 = list.get(lastIndex - 2);
		
		return (isRise(k3, k4) && isRise(k2, k3) && isRise(k1, k2)) 
				&& k0.getClosePriceDoubleValue() <= k1.getBodyLowPriceDoubleValue()
				&& k0.getClosePriceDoubleValue() <= k2.getBodyLowPriceDoubleValue();
	}
	
	/**
	 * 校验价格是否出现强势</br>
	 * 
	 * 1、前4根k线连续下跌且当前k线收盘价大于等于前两根k线实体部分最高价格
	 * 
	 * @param list
	 * @return
	 */
	public static boolean verifyPowerful(List<Klines> list) {
		int lastIndex = list.size() - 1;
		Klines k0 = list.get(lastIndex);
		Klines k1 = list.get(lastIndex - 1);
		Klines k2 = list.get(lastIndex - 2);
		Klines k3 = list.get(lastIndex - 2);
		Klines k4 = list.get(lastIndex - 2);
		
		return (isFall(k3, k4) && isFall(k2, k3) && isFall(k1, k2)) 
				&& k0.getClosePriceDoubleValue() >= k1.getBodyLowPriceDoubleValue()
				&& k0.getClosePriceDoubleValue() >= k2.getBodyLowPriceDoubleValue();
	}
	
	/**
	 * 判断是否上涨
	 * @param current 当前k线
	 * @param parrent 前一根k线
	 * @return
	 */
	public static boolean isRise_V2(Klines current,Klines parrent) {
		boolean flag = false;
		if(current.isRise() && parrent.isRise()) {//都为阳线则上涨
			flag = true;
		} else if(parrent.isRise() && current.isFall() && 
				current.getBodyLowPriceDoubleValue() > parrent.getBodyLowPriceDoubleValue()) {//前一根为阳线 当前为阴线且收盘价大于前一根开盘价
			flag = true;
		} else if(parrent.isFall() && current.isRise() && 
				current.getBodyHighPriceDoubleValue() >= parrent.getHighPriceDoubleValue()) { //前一根k线为阴线 当前为阳线且收盘价大于等于前一根k线最高价
			flag = true;
		}
		return flag;
	}
	
	/**
	 * 判断是否下跌
	 * @param current 当前k线
	 * @param parrent 前一根k线
	 * @return
	 */
	public static boolean isFall_V2(Klines current,Klines parrent) {
		boolean flag = false;
		if(current.isFall() && current.isFall()) { //皆为阴线则为下跌
			flag = true;
		} else if(parrent.isFall() && current.isRise() 
				&& current.getBodyHighPriceDoubleValue() < parrent.getBodyHighPriceDoubleValue()) { //前一根k为阴线 当前为阳线且收盘价小于前一根开盘价
			flag = true;
		} else if(parrent.isRise() && current.isFall() 
				&& current.getBodyLowPriceDoubleValue() <= parrent.getLowPriceDoubleValue()) { //前一根k线为阳线 当前k线为阴线且收盘价小于等于前一根k线最低价
			flag = true;
		}
		return flag;
	}
	
	/**
	 * 判断是否出现颓势
	 * @param current 当前k线
	 * @param parent 前一根k线
	 * @return
	 */
	public static boolean verifyDecliningPrice(Klines current,Klines parent) {
		boolean flag = false;
		if(isFall_V2(current,parent)) {// 下跌
			flag = true;
		} else if(parent.isRise() && current.isFall() 
				&& current.getBodyLowPriceDoubleValue() <= parent.getBodyLowPriceDoubleValue()) {//看跌吞没
			flag = true;
		} else if(parent.isFall() && current.isRise() 
				&& current.getBodyHighPriceDoubleValue() < parent.getHighPriceDoubleValue()) {
			flag = true;
		}
		return flag;
	}
	
	/**
	 * 判断是否出现强势
	 * @param current
	 * @param parent
	 * @return
	 */
	public static boolean verifyPowerful(Klines current,Klines parent) {
		boolean flag = false;
		if(isRise_V2(current,parent)) {// 上涨
			flag = true;
		} else if(parent.isFall() && current.isRise() 
				&& current.getBodyLowPriceDoubleValue() >= parent.getBodyLowPriceDoubleValue()) {//看涨吞没
			flag = true;
		} else if(parent.isRise() && current.isFall() 
				&& current.getBodyLowPriceDoubleValue() > parent.getLowPriceDoubleValue()) {
			flag = true;
		}
		return flag;
	}
	
	/**
	 * 判断是否出现颓势 V3
	 * @param list
	 * @return
	 */
	public static boolean verifyDecliningPrice_v3(List<Klines> list) {
		
		int lastIndex = list.size() - 1;
		Klines k0 = list.get(lastIndex);
		Klines k1 = list.get(lastIndex - 1);
		Klines k2 = list.get(lastIndex - 2);
		
		return isRise_v3(k1, k2) && isFall_v3(k0, k1);
	}
	
	/**
	 * 判断是否出现强势 V3
	 * @param list
	 * @return
	 */
	public static boolean verifyPowerful_v3(List<Klines> list) {
		
		int lastIndex = list.size() - 1;
		Klines k0 = list.get(lastIndex);
		Klines k1 = list.get(lastIndex - 1);
		Klines k2 = list.get(lastIndex - 2);
		
		return isFall_v3(k1, k2) && isRise_v3(k0, k1);
	}
	
	/**
	 * 判断是否出现强势 V4
	 * @param fibInfo 回撤信息
	 * @param list 价格参考的k线信息
	 * @return
	 */
	public static boolean verifyPowerful_v4(FibInfo fibInfo, List<Klines> list) {
		boolean result = false;
		if(!(fibInfo == null || CollectionUtils.isEmpty(list))) {
			Klines current = getLastKlines(list);
			double closePrice = current.getClosePriceDoubleValue();
			double openPrice = current.getOpenPriceDoubleValue();
			double fib236Price = fibInfo.getFibValue(FibCode.FIB236);
			double fib382Price = fibInfo.getFibValue(FibCode.FIB382);
			QuotationMode qm = fibInfo.getQuotationMode();
			if(qm == QuotationMode.SHORT) {
				result = (isBullishSwallowing(list) || verifyPowerful(list)) && openPrice <= fib236Price && closePrice < fib382Price;
			}
		}
		
		return result;
	}
	
	/**
	 * 判断是否出现颓势 V4
	 * @param fibInfo 回撤信息
	 * @param list 价格参考的k线信息
	 * @return
	 */
	public static boolean verifyDecliningPrice_v4(FibInfo fibInfo, List<Klines> list) {
		boolean result = false;
		if(!(fibInfo == null || CollectionUtils.isEmpty(list))) {
			Klines current = getLastKlines(list);
			double closePrice = current.getClosePriceDoubleValue();
			double openPrice = current.getOpenPriceDoubleValue();
			double fib236Price = fibInfo.getFibValue(FibCode.FIB236);
			double fib382Price = fibInfo.getFibValue(FibCode.FIB382);
			QuotationMode qm = fibInfo.getQuotationMode();
			if(qm == QuotationMode.LONG) {
				result = (isPutInto(list) || verifyDecliningPrice(list)) && openPrice >= fib236Price && closePrice > fib382Price;
			}
		}
		return result;
	}
	
	/**
	 * 判断两根k线是否为上涨 V3 
	 * </br>
	 * 
	 * 如果两根k线是上涨 那么则应满足 实体部分最低价在不断抬高
	 * 
	 * @param current 当前k线
	 * @param parrent 前一根k线
	 * @return
	 */
	public static boolean isRise_v3(Klines current, Klines parrent) {
		boolean flag = false;
		if(current.isRise() && parrent.isRise()) { //两根k线均为阳线则为上涨
			flag = true;
		} else if(parrent.isRise() && current.isFall() 
				&& current.getClosePriceDoubleValue() > parrent.getOpenPriceDoubleValue()) { //前一根k线为阳线 当前k线为阴线 且当前k线收盘价大于前一根k线开盘价 则为上涨
			flag = true;
		} else if(parrent.isFall() && current.isRise() 
				&& current.getClosePriceDoubleValue() >= parrent.getOpenPriceDoubleValue()) { //前一根k线为阴线 当前为阳线 且当前k线收盘价大于等于前一根k线开盘价（看涨吞没）
			flag = true;
		}
		return flag;
	}
	
	/**
	 * 判断两根k线是否为下跌 V3 
	 * </br>
	 * 
	 * 如果两根k线是下跌 那么则应满足 实体部分最高价在不断降低
	 * 
	 * @param current 当前k线
	 * @param parrent 前一根k线
	 * @return
	 */
	public static boolean isFall_v3(Klines current, Klines parrent) {
		boolean flag = false;
		if(current.isFall() && parrent.isFall()) { //两根k线均为阴线则为下跌
			flag = true;
		} else if(parrent.isFall() && current.isRise() 
				&& current.getClosePriceDoubleValue() < parrent.getOpenPriceDoubleValue()) { //一阴一阳 当前k线收盘价小于前一根k线开盘价
			flag = true;
		} else if(parrent.isRise() && current.isFall()
				&& current.getClosePriceDoubleValue() <= parrent.getOpenPriceDoubleValue()) { //一阳一阴 当前k线收盘价小于等于前一根k线开盘价
			flag = true;
		}
		return flag;
	}
	
	/**
	 * 判断EMA指标是否可做多
	 * @param list
	 * @return
	 */
	public static boolean isLongForEma(List<Klines> list) {
		int index = list.size() - 1;
		Klines k0 = list.get(index);
		Klines k1 = list.get(index -1);
		return isOpenForEma(list) && isRise_v3(k0, k1);
	}
	
	/**
	 * 判断EMA指标是否可做空
	 * @param list
	 * @return
	 */
	public static boolean isShortForEma(List<Klines> list) {
		int index = list.size() - 1;
		Klines k0 = list.get(index);
		Klines k1 = list.get(index -1);
		return isOpenForEma(list) && isFall_v3(k0, k1);
	}
	
	/**
	 * 判断EMA指标是否可开仓
	 * @param list k线信息
	 * @return
	 */
	public static boolean isOpenForEma(List<Klines> list) {
		
		int index = list.size() - 1;
		Klines k0 = list.get(index);
		Klines k1 = list.get(index -1);
		Klines k2 = list.get(index -1);
		//判断回踩ema99 情况
	 	
		return isHitEma99(k0) || isHitEma99(k1) || isHitEma99(k2);
	}
	
	/**
	 * 判断k线是否回踩ema99均线
	 * @param current
	 * @return
	 */
	public static boolean isHitEma99(Klines current) {
		double ema99 = current.getEma99();
		return isHitPrice(current, ema99);
	}
	
	/**
	 * 判断是否到达某个价格
	 * @param current 当前k线
	 * @param price 价格
	 * @return
	 */
	public static boolean isHitPrice(Klines current,double price) {
		
		double lowPrice = current.getLowPriceDoubleValue();
	 	double highPrice = current.getHighPriceDoubleValue();
	 	
	 	return lowPrice <= price && highPrice >= price;
	}
	
	/**
	 * 判断是否处于上涨状态
	 * @param list
	 * @return
	 */
	public static boolean isRise_v3(List<Klines> list) {
		boolean flag = false;
		int index = list.size() - 1;
		Klines k0 = list.get(index);
		Klines k1 = list.get(index -1);
		if(isRise_v3(k0, k1)) {
			flag = true;
		}
		return flag;
	}
	
	/**
	 * 判断是否处于下跌状态
	 * @param list
	 * @return
	 */
	public static boolean isFall_v3(List<Klines> list) {
		boolean flag = false;
		int index = list.size() - 1;
		Klines k0 = list.get(index);
		Klines k1 = list.get(index -1);
		if(isFall_v3(k0, k1)) {
			flag = true;
		}
		return flag;
	}
	
	/**
	 * 判断是否可做多
	 * @param price 价格
	 * @param list k线
	 * @return
	 */
	public static boolean isLong_v3(double price, List<Klines> list) {
		
		int index = list.size() - 1;
		Klines k0 = list.get(index);
		Klines k1 = list.get(index -1);
		
		double c_0 = k0.getClosePriceDoubleValue();
		
		return (isBreachLong(k0, price) && k0.isRise()) || (isBreachLong(k1, price) && k1.isFall() && c_0 >= price);
	}
	
	/**
	 * 判断是否可做空
	 * @param price 价格
	 * @param list k线
	 * @return
	 */
	public static boolean isShort_v3(double price, List<Klines> list) {
		
		int index = list.size() - 1;
		Klines k0 = list.get(index);
		Klines k1 = list.get(index -1);

		double c_0 = k0.getClosePriceDoubleValue();
		
		return (isBreachShort(k0, price) && k0.isFall()) || (isBreachShort(k1, price) && k1.isRise() && c_0 <= price);
	}
	
	/**
	 * 判断是否可做多
	 * @param price 价格
	 * @param list k线
	 * @return
	 */
	public static boolean isLong_v2(double price, List<Klines> list) {
		int index = list.size() - 1;
		Klines k0 = list.get(index);
		Klines k1 = list.get(index -1);
		Klines k2 = list.get(index -2);
		
		double closePrice = k0.getClosePriceDoubleValue();
		
		return closePrice >= price && (isHitPrice(k0, price) || isHitPrice(k1, price) || isHitPrice(k2, price)) && isRise_v3(k0, k1);
	}
	
	/**
	 * 判断是否可做空
	 * @param price 价格
	 * @param list k线
	 * @return
	 */
	public static boolean isShort_v2(double price, List<Klines> list) {
		int index = list.size() - 1;
		Klines k0 = list.get(index);
		Klines k1 = list.get(index -1);
		Klines k2 = list.get(index -2);
		
		double closePrice = k0.getClosePriceDoubleValue();
		
		return closePrice <= price && (isHitPrice(k0, price) || isHitPrice(k1, price) || isHitPrice(k2, price)) && isFall_v3(k0, k1);
	}
	
	/**
	 * 获取当前k线之后的第一根k线
	 * @param current
	 * @param list
	 * @return
	 */
	public static Klines getAfterKlines(Klines current,List<Klines> list) {
		Klines result = null;
		if(!(current == null || CollectionUtils.isEmpty(list))) {
			for(int index = 0; index < list.size(); index++) {
				Klines tmp = list.get(index);
				if(tmp.getStartTime() == current.getStartTime() && index != list.size() - 1) {
					result = list.get(index + 1);
					break;
				}
			}
		}
		return result;
	}
	
	/**
	 * 获取当前k线之前的第一根k线
	 * @param current
	 * @param list
	 * @return
	 */
	public static Klines getBeforeKlines(Klines current,List<Klines> list) {
		Klines result = null;
		if(!(current == null || CollectionUtils.isEmpty(list))) {
			for(int index = 0; index < list.size(); index++) {
				Klines tmp = list.get(index);
				if(tmp.getStartTime() == current.getStartTime() && index != 0) {
					result = list.get(index - 1);
					break;
				}
			}
		}
		return result;
	}
	
	/**
	 * 获取最低价K线
	 * @param k0
	 * @param k1
	 * @return
	 */
	public static Klines getMinPriceKlines(Klines k0, Klines k1) {
		if(!(k0 == null || k1 == null)) {
			if(k0.getLowPriceDoubleValue() < k1.getLowPriceDoubleValue()) {
				return k0;
			} else {
				return k1;
			}
		} else if(k0 != null && k1 == null) {
			return k0;
		} else if(k0 == null && k1 != null) {
			return k1;
		} else {
			return null;
		}
	}
	
	/**
	 * 获取最高价k线
	 * @param k0
	 * @param k1
	 * @return
	 */
	public static Klines getMaxPriceKlines(Klines k0, Klines k1) {
		if(!(k0 == null || k1 == null)) {
			if(k0.getHighPriceDoubleValue() > k1.getHighPriceDoubleValue()) {
				return k0;
			} else {
				return k1;
			}
		} else if(k0 != null && k1 == null) {
			return k0;
		} else if(k0 == null && k1 != null) {
			return k1;
		} else {
			return null;
		}
	}
	
	/**
	 * 验证多头盈利百分比
	 * @param currentPrice 当前价格
	 * @param takeProfit 止盈价
	 * @param profit 预期获利百分比
	 * @return
	 */
	public static boolean checkLongProfit(double currentPrice,BigDecimal takeProfit,double profit) {
		double profitPercent = PriceUtil.getRiseFluctuationPercentage(currentPrice,takeProfit.doubleValue());
		return (profitPercent * 100) >= profit;
	}
	
	/**
	 * 验证空头盈利百分比
	 * @param currentPrice 当前价格
	 * @param takeProfit 止盈价
	 * @param profit 预期获利百分比
	 * @return
	 */
	public static boolean checkShortProfit(double currentPrice,BigDecimal takeProfit,double profit) {
		double profitPercent = PriceUtil.getFallFluctuationPercentage(currentPrice,takeProfit.doubleValue());
		return (profitPercent * 100) >= profit;
	}
	
	/**
	 * 获取时间在某一天结束之后的所有k线信息
	 * @param last 某一天k线信息
	 * @param list k线信息
	 * @return
	 */
	public static List<Klines> getLastDayAfterKline(Klines last,List<Klines> list) {
		if(!(last == null || CollectionUtils.isEmpty(list))) {
			int startIndex = -1;
			for(int index = 0;index < list.size();index++) {
				Klines tmp = list.get(index);
				if(tmp.getStartTime() > last.getEndTime()) {
					startIndex = index;
					break;
				}
			}
			if(startIndex != -1) {
				Klines startKlines = list.get(startIndex);
				return subList(startKlines, list);
			}
		}
		return new ArrayList<Klines>();
	}
	
	/**
	 * 是否出现放量
	 * @param list
	 * @return
	 */
	public static boolean isRelease(List<Klines> list) {
		int size = list.size();
		int index = size - 1;
		
		Klines k0 = list.get(index);
		Klines k1 = list.get(index - 1);
		
		double v_0 = k0.getVDoubleValue();
		double v_1 = k1.getVDoubleValue();
		
		if(v_0 == 0 || v_1 == 0) {
			return false;
		}
		
		return v_0 > (v_1 * 5) && k0.getPriceFluctuationPercentage() > 3;
	}
	
	/**
	 * 是否出现增量
	 * @param k0
	 * @param k1
	 * @param k2
	 * @return
	 */
	public static boolean isIncrement(Klines k0, Klines k1, Klines k2) {
		double v_0 = k0.getVDoubleValue();
		double v_1 = k1.getVDoubleValue();
		double v_2 = k2.getVDoubleValue();
		return v_0 >= v_1 && v_1 >= v_2;
	}
	
	/**
	 * 是否出现增量
	 * @param list
	 * @return
	 */
	public static boolean isIncrement(List<Klines> list) {
		int size = list.size();
		int index = size - 1;
		
		Klines k0 = list.get(index);
		Klines k1 = list.get(index - 1);
		Klines k2 = list.get(index - 2);
		
		return isIncrement(k0, k1, k2);
	}
	
	/**
	 * 是否出现缩量
	 * @param k0
	 * @param k1
	 * @param k2
	 * @return
	 */
	public static boolean isReduced(Klines k0, Klines k1, Klines k2) {
		double v_0 = k0.getVDoubleValue();
		double v_1 = k1.getVDoubleValue();
		double v_2 = k2.getVDoubleValue();
		return v_0 <= v_1 && v_1 <= v_2;
	}
	
	/**
	 * 是否出现缩量
	 * @param list
	 * @return
	 */
	public static boolean isReduced(List<Klines> list) {
		int size = list.size();
		int index = size - 1;
		
		Klines k0 = list.get(index);
		Klines k1 = list.get(index - 1);
		Klines k2 = list.get(index - 2);
		
		return isReduced(k0, k1, k2);
	}
	
	/**
	 * 是否出现买盘衰竭 量增价涨且最后一根k线成交量比前一根k线成交量小
	 * @param list
	 * @return
	 */
	public static boolean isBuyingExhaustion(List<Klines> list) {
		
		int size = list.size();
		int index = size - 1;
		
		Klines k0 = list.get(index);
		Klines k1 = list.get(index - 1);
		Klines k2 = list.get(index - 2);
		Klines k3 = list.get(index - 3);
		
		return (isRise_v3(k1, k2) && isRise_v3(k2, k3)) && isIncrement(k1, k2, k3) && k0.getVDoubleValue() < k1.getVDoubleValue();
	}
	
	/**
	 * 是否出现买盘 缩量下跌且最后一根k线成交量比前一根k线成交量大
	 * @param list
	 * @return
	 */
	public static boolean isBuying(List<Klines> list) {
		int size = list.size();
		int index = size - 1;
		
		Klines k0 = list.get(index);
		Klines k1 = list.get(index - 1);
		Klines k2 = list.get(index - 2);
		Klines k3 = list.get(index - 3);
		
		return (isFall_v3(k1, k2) && isFall_v3(k2, k3)) && isReduced(k1, k2, k3) && k0.getVDoubleValue() > k1.getVDoubleValue();
	}
	
	/**
	 * 是否出现卖盘衰竭 量增价跌且最后一根k线成交量比前一根k线成交量小
	 * @param list
	 */
	public static boolean isSellingExhaustion(List<Klines> list) {
		
		int size = list.size();
		int index = size - 1;
		
		Klines k0 = list.get(index);
		Klines k1 = list.get(index - 1);
		Klines k2 = list.get(index - 2);
		Klines k3 = list.get(index - 3);
		
		return (isFall_v3(k1, k2) && isFall_v3(k2, k3)) && isIncrement(k1, k2, k3) && k0.getVDoubleValue() < k1.getVDoubleValue();
	}
	
	/**
	 * 是否出现卖盘 缩量上涨且最后一根k线成交量比前一根k线成交量大
	 * @param list
	 * @return
	 */
	public static boolean isSelling(List<Klines> list) {
		
		int size = list.size();
		int index = size - 1;
		
		Klines k0 = list.get(index);
		Klines k1 = list.get(index - 1);
		Klines k2 = list.get(index - 2);
		Klines k3 = list.get(index - 3);
		
		return (isRise_v3(k1, k2) && isRise_v3(k2, k3)) && isReduced(k1, k2, k3) && k0.getVDoubleValue() > k1.getVDoubleValue();
	}
	
	/**
	 * 是否出现看涨吞没 <br/>
	 * 
	 * 1、出现买盘信号或卖盘衰竭信号且最后一根k线收盘价大于等于前一根k线开盘价
	 * 
	 * @param list
	 * @return
	 */
	public static boolean isBullishSwallowing(List<Klines> list) {
		int size = list.size();
		int index = size - 1;
		
		Klines k0 = list.get(index);
		Klines k1 = list.get(index - 1);
		
		return (isBuying(list) || isSellingExhaustion(list)) && k0.getClosePriceDoubleValue() >= k1.getOpenPriceDoubleValue();
	}
	
	/**
	 * 是否出现看跌吞没 <br/>
	 * 
	 * 1、出现卖盘信号或买盘衰竭信号且最后一根k线收盘价小于等于前一根k线开盘价
	 * 
	 * @param list
	 * @return
	 */
	public static boolean isPutInto(List<Klines> list) {
		int size = list.size();
		int index = size - 1;
		
		Klines k0 = list.get(index);
		Klines k1 = list.get(index - 1);
		
		return (isSelling(list) || isBuyingExhaustion(list)) && k0.getClosePriceDoubleValue() <= k1.getOpenPriceDoubleValue();
	}
	
	/**
	 * 计算EMA信息
	 * @param list
	 */
	public static void calculateEMA_7_25_99(List<Klines> list) {
		calculateEMAArray(list, EMAType.EMA7);
		calculateEMAArray(list, EMAType.EMA25);
		calculateEMAArray(list, EMAType.EMA99);
	}
	
	/**
	 * 计算涨跌幅度
	 * @param price 当前价格
	 * @param code 止盈点位
	 * @param fibInfo 回撤信息
	 * @return
	 */
	public static double getPercent(double price,FibCode code, FibInfo fibInfo) {
		/*double pricePercent = 0;
		if(fibInfo.getQuotationMode() == QuotationMode.LONG) {
			pricePercent = PriceUtil.getRiseFluctuationPercentage(price, fibInfo.getFibValue(code)) * 100;
		} else {
			pricePercent = PriceUtil.getFallFluctuationPercentage(price, fibInfo.getFibValue(code)) * 100;
		}
		return pricePercent;*/
		return getPercent(price, fibInfo.getFibValue(code), fibInfo.getQuotationMode());
	}
	
	/**
	 * 计算涨跌幅度
	 * @param price 当前价格
	 * @param takeProfitPrice 止盈价
	 * @param qm LONG/SHORT LONG：上涨 SHORT：下跌
	 * @return
	 */
	public static double getPercent(double price, double takeProfitPrice, QuotationMode qm) {
		double pricePercent = 0;
		if(qm == QuotationMode.LONG) {
			pricePercent = PriceUtil.getRiseFluctuationPercentage(price, takeProfitPrice) * 100;
		} else {
			pricePercent = PriceUtil.getFallFluctuationPercentage(price, takeProfitPrice) * 100;
		}
		return pricePercent;
	}
	
	/**
	 * 校验波动幅度是否满足用户设定的阈值
	 * @param pricePercent 价格波动幅度
	 * @param nextPricePercent 下一个点位价格波动幅度
	 * @param profit 盈利过滤
	 * @param profitLimit 盈利限制
	 * @return
	 */
	public static boolean checkPercent(double pricePercent, double nextPricePercent, double profit, double profitLimit) {
		return pricePercent > profitLimit && nextPricePercent >= profit;
	}
}
