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

public class FibInfoFactory_v6 {

	private List<Klines> list;
	
	private List<Klines> fibAfterKlines;
	
	private FibInfo fibInfo;
	
	public FibInfoFactory_v6(List<Klines> list) {
		this.list = new ArrayList<Klines>();
		if(!CollectionUtils.isEmpty(list)) {
			this.list.addAll(list);
			this.init();
		}
	}
	
	private void init() {
		if(CollectionUtils.isEmpty(list) || list.size() < 99) {
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
		for(int index = list.size() - 1; index > 0; index--) {
			Klines current = list.get(index);
			//Klines parent = list.get(index - 1);
			if(ps == PositionSide.DEFAULT) {
				ps = getPositionSide(current);
			} else if(ps == PositionSide.LONG) { // high - low - high
				if(third == null) {
					if(verifyHigh(current)) {
						third = current;
					}
				} else if(second == null) {
					if(verifyLow(current)) {
						second = current;
					}
				} else if(first == null) {
					if(verifyHigh(current)) {
						first = current;
						break;
					}
				}
			} else if(ps == PositionSide.SHORT) { // low - high - low
				if(third == null) {
					if(verifyLow(current)) {
						third = current;
					}
				} else if(second == null) {
					if(verifyHigh(current)) {
						second = current;
					}
				} else if(first == null) {
					if(verifyLow(current)) {
						first = current;
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
		Klines startAfterFlag = null;
		Klines end = null;
		if(ps == PositionSide.LONG) {
			start = PriceUtil.getMinPriceKLine(firstSubList);
			startAfterFlag = PriceUtil.getAfterKlines(start, firstSubList);
			if(startAfterFlag != null) {
				secondSubList = PriceUtil.subList(startAfterFlag, third, list);
				end = PriceUtil.getMaxPriceKLine(secondSubList);
				fibInfo = new FibInfo(start.getLowPriceDoubleValue(), end.getHighPriceDoubleValue(), start.getDecimalNum(), FibLevel.LEVEL_5);
			}
		} else if(ps == PositionSide.SHORT) {
			start = PriceUtil.getMaxPriceKLine(firstSubList);
			startAfterFlag = PriceUtil.getAfterKlines(start, firstSubList);
			if(startAfterFlag != null) {
				secondSubList = PriceUtil.subList(startAfterFlag, third, list);
				end = PriceUtil.getMinPriceKLine(secondSubList);
				fibInfo = new FibInfo(start.getHighPriceDoubleValue(), end.getLowPriceDoubleValue(), start.getDecimalNum(), FibLevel.LEVEL_5);
			}
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
	
	private PositionSide getPositionSide(Klines current) {
		PositionSide ps = PositionSide.DEFAULT;
		if(verifyLong(current)) {
			ps = PositionSide.LONG;
		} else if(verifyShort(current)) {
			ps = PositionSide.SHORT;
		}
		return ps;
	}
	
	private boolean verifyLow(Klines current) {
		return current.getEma7() < current.getEma25() && current.getEma25() < current.getEma99() && current.getEma99() > 0;
	}
	
	private boolean verifyHigh(Klines current) {
		return current.getEma7() > current.getEma25() && current.getEma25() > current.getEma99() && current.getEma99() > 0;
	}
	
	private boolean verifyLong(Klines current) {
		return current.getEma25() < current.getEma99();
	}
	
	private boolean verifyShort(Klines current) {
		return current.getEma25() > current.getEma99();
	}
	
	public List<Klines> getFibAfterKlines() {
		return fibAfterKlines;
	}

	public FibInfo getFibInfo() {
		return fibInfo;
	}
}
