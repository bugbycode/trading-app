package com.util;

import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.util.CollectionUtils;

import com.bugbycode.module.FibCode;
import com.bugbycode.module.FibInfo;
import com.bugbycode.module.FibLevel;
import com.bugbycode.module.Klines;
import com.bugbycode.module.QuotationMode;
import com.bugbycode.module.SortType;
import com.bugbycode.module.trading.PositionSide;

/**
 * 价格行为斐波那契回撤工具类 V2
 */
public class DeclineAndStrengthFibUtil_v2 {
	
	private final Logger logger = LogManager.getLogger(DeclineAndStrengthFibUtil_v2.class);
	
	private List<Klines> list;
	
	private FibInfo fibInfo;
	
	private List<Double> openPriceList = new ArrayList<Double>();
	
	private Klines fibEnd;
	
	public DeclineAndStrengthFibUtil_v2(List<Klines> list) {
		this.list = new ArrayList<Klines>();
		if(!CollectionUtils.isEmpty(list)) {
			this.list.addAll(list);
			this.init();
		}
	}
	
	public void init() {
		
		if(CollectionUtils.isEmpty(list)) {
			return;
		}
		
		this.list.sort(new KlinesComparator(SortType.ASC));
		
		PriceUtil.calculateAllBBPercentB(list);
		
		Klines first = null;
		Klines second = null;
		Klines third = null;
		
		PositionSide ps = PositionSide.DEFAULT;
		
		for(int index = list.size() - 1; index > 0; index--) {
			Klines current = list.get(index);
			Klines next = list.get(index - 1);
			double c_high = current.getHighPriceDoubleValue();
			double n_high = next.getHighPriceDoubleValue();
			double c_low = current.getLowPriceDoubleValue();
			double n_low = next.getLowPriceDoubleValue();
			double c_body_high = current.getBodyHighPriceDoubleValue();
			double n_body_high = next.getBodyHighPriceDoubleValue();
			double c_body_low = current.getBodyLowPriceDoubleValue();
			double n_body_low = next.getBodyLowPriceDoubleValue();
			if(first == null) {//第一个标志性高低点
				if(PriceUtil.isHigh(current, next)) {//高点
					ps = PositionSide.LONG;
					first = current;
					
					openPriceList.add(PriceUtil.getMaxPrice(c_high, n_high));//最高点
					openPriceList.add(PriceUtil.getMaxPrice(c_body_high, n_body_high));//实体部分高点最高价
					openPriceList.add(PriceUtil.getMaxPrice(c_body_low, n_body_low));//实体部分低点最高价
					openPriceList.sort(new PriceComparator(SortType.ASC));
					
				} else if(PriceUtil.isLow(current, next)) { //低点
					ps = PositionSide.SHORT;
					first = current;
					
					openPriceList.add(PriceUtil.getMinPrice(c_low, n_low));//最低点
					openPriceList.add(PriceUtil.getMinPrice(c_body_high, n_body_high));//实体部分高点最低价
					openPriceList.add(PriceUtil.getMinPrice(c_body_low, n_body_low));//实体部分低点最低价
					openPriceList.sort(new PriceComparator(SortType.DESC));
				}
			} else if(second == null) {//第二个标志性高低点
				if(ps == PositionSide.LONG && PriceUtil.isLow(current, next)) {//寻找低点
					second = current;
				} else if(ps == PositionSide.SHORT && PriceUtil.isHigh(current, next)) {//寻找高点
					second = current;
				}
			} else if(third == null) {//第三个标志性高低点
				if(ps == PositionSide.LONG && PriceUtil.isHigh(current, next)) {//寻找高点
					third = current;
				} else if(ps == PositionSide.SHORT && PriceUtil.isLow(current, next)) {//寻找低点
					third = current;
				}
			} 
			
			if(!(first == null || second == null || third == null)) {
				break;
			}
		}
		
		if(first == null || second == null || third == null) {
			return;
		}
		
		//重新确定高低点
		//寻找起始点
		Klines start = null;
		Klines end = null;
		List<Klines> fisrtSub = PriceUtil.subList(third, first, list);
		if(ps == PositionSide.LONG) {//低点
			start = PriceUtil.getMinPriceKLine(fisrtSub);
		} else if(ps == PositionSide.SHORT) {//高点
			start = PriceUtil.getMaxPriceKLine(fisrtSub);
		}
		if(start == null) {
			return;
		}
		
		//寻找终点
		List<Klines> secondSub = PriceUtil.subList(start, list);
		if(ps == PositionSide.LONG) {//高点
			end = PriceUtil.getMaxPriceKLine(secondSub);
		} else if(ps == PositionSide.SHORT) {//低点
			end = PriceUtil.getMinPriceKLine(secondSub);
		}
		
		if(end == null) {
			return;
		}
		
		fibEnd = end;
		
		if(ps == PositionSide.LONG) {
			fibInfo = new FibInfo(start.getLowPriceDoubleValue(), end.getHighPriceDoubleValue(), start.getDecimalNum(), FibLevel.LEVEL_1);
		} else if(ps == PositionSide.SHORT) {
			fibInfo = new FibInfo(start.getHighPriceDoubleValue(), end.getLowPriceDoubleValue(), start.getDecimalNum(), FibLevel.LEVEL_1);
		}
		
		logger.debug(openPriceList);
		logger.debug(fibInfo);
		
	}
	
	public FibInfo getFibInfo() {
		return this.fibInfo;
	}
	
	public List<Klines> getFibAfterKlines() {
		List<Klines> afterList = new ArrayList<Klines>();
		if(this.fibEnd != null) {
			Klines last = PriceUtil.getAfterKlines(fibEnd, list);
			if(last != null) {
				afterList = PriceUtil.subList(last, list);
			}
		}
		return afterList;
	}

	public boolean verifyOpen(List<Klines> hitList) {
		boolean result = false;
		if(fibInfo != null) {
			QuotationMode qm = fibInfo.getQuotationMode();
			double fib382Price = fibInfo.getFibValue(FibCode.FIB382);
			List<Klines> afterList = getFibAfterKlines();
			Klines afterLowKlines = PriceUtil.getMinPriceKLine(afterList);
			Klines afterHighKlines = PriceUtil.getMaxPriceKLine(afterList);
			Klines last = PriceUtil.getLastKlines(hitList);
			double closePrice = last.getClosePriceDoubleValue();
			for(int index = 0; index < openPriceList.size(); index++) {
				double price = openPriceList.get(index);
				if(qm == QuotationMode.SHORT && PriceUtil.isBreachLong(last, price) 
						&& !PriceUtil.isObsoleteLong(afterLowKlines, openPriceList, index)
						&& closePrice < fib382Price) {
					result = true;
				} else if(qm == QuotationMode.LONG && PriceUtil.isBreachShort(last, price) 
						&& !PriceUtil.isObsoleteShort(afterHighKlines, openPriceList, index)
						&& closePrice > fib382Price) {
					result = true;
				}
			}
		}
		return result;
	}

	public List<Double> getOpenPriceList() {
		return this.openPriceList;
	}
}
