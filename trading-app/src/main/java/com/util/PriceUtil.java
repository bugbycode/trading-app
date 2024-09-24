package com.util;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;

import com.bugbycode.module.EMAType;
import com.bugbycode.module.FibCode;
import com.bugbycode.module.FibInfo;
import com.bugbycode.module.FibKlinesData;
import com.bugbycode.module.HighOrLowHitPrice;
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
		double lowPrice = kl.getLowPrice();
		double hightPrice = kl.getHighPrice();
		
		while(offset >= 1) {
			Klines current = klinesList.get(offset--);
			Klines parent = klinesList.get(offset);
			if(isFall) {
				if(isRise(current, parent)) {
					if(hightPrice < current.getHighPrice()) {
						hightPrice = current.getHighPrice();
					}
					break;
				}
			} else {
				if(isFall(current, parent)) {
					if(lowPrice > current.getLowPrice()) {
						lowPrice = current.getLowPrice();
					}
					break;
				}
			}
			if(lowPrice > current.getLowPrice()) {
				lowPrice = current.getLowPrice();
			}
			if(hightPrice < current.getHighPrice()) {
				hightPrice = current.getHighPrice();
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
			if(codeOffset > 0 && afterLowKlines.getLowPrice() <= fibInfo.getFibValue(codes[codeOffset - 1])) {
				result = true;
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
			if(codeOffset > 0 && afterHighKlines.getHighPrice() >= fibInfo.getFibValue(codes[codeOffset - 1])) {
				result = true;
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
		return (current.getOpenPrice() >= hitPrice && current.getClosePrice() >= hitPrice && current.getLowPrice() <= hitPrice)
				//2. 开盘价小于等于条件价 收盘价大于等于条件价
				|| (current.getOpenPrice() <= hitPrice && current.getClosePrice() >= hitPrice);
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
			
			if(isBreachShort(current, hitPrice)) {
				isShort = true;
			}
		}
		
		return isShort;
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
		return (current.getOpenPrice() <= hitPrice && current.getClosePrice() <= hitPrice && current.getHighPrice() >= hitPrice) ||
			   //开盘价大于等于条件价、收盘价小于等于条件价
				(current.getOpenPrice() >= hitPrice && current.getClosePrice() <= hitPrice);
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
				
				if(tmp.getLowPrice() < result.getLowPrice()) {
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
				
				if(tmp.getHighPrice() > result.getHighPrice()) {
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
        
        // 计算初始的SMA作为第一个EMA的起始点
        for (int i = 0; i < period; i++) {
        	initialSMA += klinesList.get(i).getClosePrice();
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
	        	klinesList.get(i).setEma7(calculateEMA(klinesList.get(i - 1).getEma7(), klinesList.get(i).getClosePrice(), period));
				break;
			case EMA25:
	        	klinesList.get(i).setEma25(calculateEMA(klinesList.get(i - 1).getEma25(), klinesList.get(i).getClosePrice(), period));
				break;
			default:
	        	klinesList.get(i).setEma99(calculateEMA(klinesList.get(i - 1).getEma99(), klinesList.get(i).getClosePrice(), period));
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
			double closePrice = klines.getClosePrice();
			double hightPrice = klines.getHighPrice();
			
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

			double closePrice = klines.getClosePrice();
			double lowPrice = klines.getLowPrice();
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
    				highPrice = tmp.getHighPrice();
        			lowPrice = tmp.getLowPrice();
    			}
    			
    			if(tmp.getHighPrice() > highPrice) {
    				highPrice = tmp.getHighPrice();
    			}
    			
    			if(tmp.getLowPrice() < lowPrice) {
    				lowPrice = tmp.getLowPrice();
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
    	if((klines.getOpenPrice() <= highPrice && klines.getOpenPrice() >= lowPrice && 
    			klines.getClosePrice() <= highPrice && klines.getClosePrice() >= lowPrice)
    			|| (klines.getHighPrice() >= highPrice && klines.getLowPrice() <= lowPrice)) {
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
    		if(tmp.getHighPrice() > klines.getHighPrice()) {
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
    		if(tmp.getLowPrice() < klines.getLowPrice()) {
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
    		double highPrice = k.getHighPrice();
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
    		double lowPrice = k.getLowPrice();
    		if((isLong(lowPrice, klinesList_hit) || isShort(lowPrice, klinesList_hit)) 
    				&& !checkLowerKlines(k, subList(k, list))) {
    			result = k;
    			break;
    		}
    	}
    	return result;
    }
    
    public static HighOrLowHitPrice getMax(List<HighOrLowHitPrice> list) {
    	HighOrLowHitPrice result = null;
    	if(!CollectionUtils.isEmpty(list)) {
    		for(HighOrLowHitPrice price : list) {
    			if(result == null || result.getPrice() < price.getPrice()) {
    				result = price;
    			}
    		}
    	}
    	return result;
    }
    
    public static HighOrLowHitPrice getMin(List<HighOrLowHitPrice> list) {
    	HighOrLowHitPrice result = null;
    	if(!CollectionUtils.isEmpty(list)) {
    		for(HighOrLowHitPrice price : list) {
    			if(result == null || result.getPrice() > price.getPrice()) {
    				result = price;
    			}
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
    	return current.getClosePrice() >= parent.getOpenPrice();
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
}
