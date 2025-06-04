package com.util;

import java.util.ArrayList;
import java.util.List;

import org.springframework.util.CollectionUtils;

import com.bugbycode.module.FibInfo;
import com.bugbycode.module.FibLevel;
import com.bugbycode.module.Klines;
import com.bugbycode.module.QuotationMode;
import com.bugbycode.module.SortType;
import com.bugbycode.module.trading.PositionSide;

/**
 * 斐波那契回撤工厂类 <br/>
 */
public class FibInfoFactory {

	private List<Klines> list;
	
	private List<Klines> fibAfterKlines;
	
	private FibInfo fibInfo;
	
	public FibInfoFactory(List<Klines> list) {
		this.list = new ArrayList<Klines>();
		if(!CollectionUtils.isEmpty(list)) {
			this.list.addAll(list);
			this.init();
		}
	}
	
	private void init() {
		if(CollectionUtils.isEmpty(list) || list.size() < 20) {
			return;
		}
		
		KlinesComparator kc = new KlinesComparator(SortType.ASC);
		this.list.sort(kc);
		
		PriceUtil.calculateAllBBPercentB(list);
		PriceUtil.calculateEMA_7_25_99(list);
		PositionSide ps = PositionSide.DEFAULT;
		
		Klines first = null;
		Klines second = null;
		Klines third = null;
		for(int index = list.size() - 1; index > 1; index--) {
			Klines current = list.get(index);
			Klines parent = list.get(index - 1);
			if(ps == PositionSide.DEFAULT) {
				if(verifyLow(current, parent)) {//做多
					ps = PositionSide.LONG;
				} else if(verifyHigh(current, parent)) {//做空
					ps = PositionSide.SHORT;
				}
			} else if(ps == PositionSide.LONG) {// high - low - high
				if(third == null) {
					if(verifyHigh(current, parent)) {
						third = current;
					}
				} else if(second == null) {
					if(verifyLow(current, parent)) {
						second = current;
					}
				} else if(first == null) {
					if(verifyHigh(current, parent)) {
						first = parent;
						break;
					}
				}
			} else if(ps == PositionSide.SHORT) { // low - high - low
				if(third == null) {
					if(verifyLow(current, parent)) {
						third = current;
					}
				} else if(second == null) {
					if(verifyHigh(current, parent)) {
						second = current;
					}
				} else if(first == null) {
					if(verifyLow(current, parent)) {
						first = parent;
						break;
					}
				}
			}
		}
		
		if(first == null || second == null || third == null) {
			return;
		}
		
		List<Klines> firstSubList = PriceUtil.subList(first, third, list);
		List<Klines> secondSubList = null;
		Klines start = null;
		Klines end = null;
		if(ps == PositionSide.LONG) {
			start = PriceUtil.getMinPriceKLine(firstSubList);
			secondSubList = PriceUtil.subList(start, list);
			end = PriceUtil.getMaxPriceKLine(secondSubList);
			fibInfo = new FibInfo(start.getLowPriceDoubleValue(), end.getHighPriceDoubleValue(), start.getDecimalNum(), FibLevel.LEVEL_3);
		} else if(ps == PositionSide.SHORT) {
			start = PriceUtil.getMaxPriceKLine(firstSubList);
			secondSubList = PriceUtil.subList(start, list);
			end = PriceUtil.getMinPriceKLine(secondSubList);
			fibInfo = new FibInfo(start.getHighPriceDoubleValue(), end.getLowPriceDoubleValue(), start.getDecimalNum(), FibLevel.LEVEL_3);
		}
		
		if(fibInfo == null) {
			return;
		}
		
		Klines fibAfterFlag = PriceUtil.getAfterKlines(end, list);
		if(fibAfterFlag != null) {
			fibAfterKlines = PriceUtil.subList(fibAfterFlag, list);
			fibInfo.setFibAfterKlines(fibAfterKlines);
		}
	}
	
	public boolean isLong() {
		boolean result = false;
		if(fibInfo != null && fibInfo.getQuotationMode() == QuotationMode.LONG) {
			result = true;
		}
		return result;
	}
	
	public boolean isShort() {
		boolean result = false;
		if(fibInfo != null && fibInfo.getQuotationMode() == QuotationMode.SHORT) {
			result = true;
		}
		return result;
	}
	
	private boolean verifyLow(Klines current) {
		return current.getBbPercentB() <= 0;
	}
	
	private boolean verifyHigh(Klines current) {
		return current.getBbPercentB() >= 1;
	}
	
	private boolean verifyLow(Klines current, Klines parent) {
		return parent.isFall() && current.isRise() && (verifyLow(current) || verifyLow(parent));
	}
	
	private boolean verifyHigh(Klines current, Klines parent) {
		return parent.isRise() && current.isFall() && (verifyHigh(current) || verifyHigh(parent));
	}

	public List<Klines> getFibAfterKlines() {
		return fibAfterKlines;
	}

	public FibInfo getFibInfo() {
		return fibInfo;
	}
}