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
public class FibInfoFactory_v2 {

	private List<Klines> list;
	
	private List<Klines> fibAfterKlines;
	
	private FibInfo fibInfo;
	
	public FibInfoFactory_v2(List<Klines> list) {
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
		
		//PriceUtil.calculateAllBBPercentB(list);
		PriceUtil.calculateEMA_7_25_99(list);
		PositionSide ps = PositionSide.DEFAULT;
		
		Klines first = null;
		Klines second = null;
		Klines third = null;
		for(int index = list.size() - 1; index > 1; index--) {
			Klines k = list.get(index);
			if(ps == PositionSide.DEFAULT) {
				if(verifyLong(k)) {
					ps = PositionSide.LONG;
				} else if(verifyShort(k)) {
					ps = PositionSide.SHORT;
				}
			} else if(ps == PositionSide.LONG) {// high - low - high
				if(third == null) {
					if(verifyHigh(k)) {
						third = k;
					}
				} else if(second == null) {
					if(verifyLow(k)) {
						second = k;
					}
				} else if(first == null) {
					if(verifyHigh(k)) {
						first = k;
						break;
					}
				}
			} else if(ps == PositionSide.SHORT) { // low - high - low
				if(third == null) {
					if(verifyLow(k)) {
						third = k;
					}
				} else if(second == null) {
					if(verifyHigh(k)) {
						second = k;
					}
				} else if(first == null) {
					if(verifyLow(k)) {
						first = k;
						break;
					}
				}
			}
		}
		
		if(first == null || second == null || third == null) {
			return;
		}
		
		List<Klines> firstSubList = PriceUtil.subList(first, third, list);
		Klines start = null;
		Klines end = null;
		List<Klines> secondSubList = null;
		if(ps == PositionSide.LONG) {
			start = PriceUtil.getMinPriceKLine(firstSubList);
			secondSubList = PriceUtil.subList(start, third, firstSubList);
			end = PriceUtil.getMaxPriceKLine(secondSubList);
			fibInfo = new FibInfo(start.getLowPriceDoubleValue(), end.getHighPriceDoubleValue(), start.getDecimalNum(), FibLevel.LEVEL_3);
		} else if(ps == PositionSide.SHORT) {
			start = PriceUtil.getMaxPriceKLine(firstSubList);
			secondSubList = PriceUtil.subList(start, third, firstSubList);
			end = PriceUtil.getMinPriceKLine(secondSubList);
			fibInfo = new FibInfo(start.getHighPriceDoubleValue(), end.getLowPriceDoubleValue(), start.getDecimalNum(), FibLevel.LEVEL_3);
		}
		
		if(fibInfo == null) {
			return;
		}
		
		Klines fibAfterFlag = PriceUtil.getAfterKlines(end, list);
		if(fibAfterFlag != null) {
			fibAfterKlines = PriceUtil.subList(fibAfterFlag, list);
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
	
	private boolean verifyLong(Klines k) {
		return k.getEma25() < k.getEma99() && k.getEma99() > 0;
	}
	
	private boolean verifyShort(Klines k) {
		return k.getEma25() > k.getEma99() && k.getEma99() > 0;
	}
	
	private boolean verifyHigh(Klines k) {
		return k.getEma7() > k.getEma25() && k.getEma25() > k.getEma99() && k.getEma99() > 0;
	}
	
	private boolean verifyLow(Klines k) {
		return k.getEma7() < k.getEma25() && k.getEma25() < k.getEma99() && k.getEma99() > 0;
	}

	public List<Klines> getFibAfterKlines() {
		return fibAfterKlines;
	}

	public FibInfo getFibInfo() {
		return fibInfo;
	}
}