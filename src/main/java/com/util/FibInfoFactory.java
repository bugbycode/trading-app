package com.util;

import java.util.ArrayList;
import java.util.List;

import org.springframework.util.CollectionUtils;

import com.bugbycode.module.FibInfo;
import com.bugbycode.module.FibLevel;
import com.bugbycode.module.Klines;
import com.bugbycode.module.SortType;
import com.bugbycode.module.trading.PositionSide;

/**
 * 斐波那契回撤工厂类 V3 <br/>
 * 该版本使用指数均线判断市场走势
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
		
		if(CollectionUtils.isEmpty(this.list)) {
			return;
		}
		
		KlinesComparator kc = new KlinesComparator(SortType.ASC);
		this.list.sort(kc);
		
		PriceUtil.calculateEMA_7_25_99(list);
		
		Klines last = PriceUtil.getLastKlines(list);
		
		PositionSide ps = PositionSide.DEFAULT;
		
		if(verifyLong(last)) {
			ps = PositionSide.LONG;
		} else if(verifyShort(last)) {
			ps = PositionSide.SHORT;
		} else {
			return;
		}
		
		Klines second = null;
		Klines first = null;
		Klines searchFlag = null;
		
		for(int index = this.list.size() - 2; index > 0; index--) {
			Klines current = this.list.get(index);
			
			if(ps == PositionSide.LONG) {
				if(searchFlag == null && verifyHigh(current)) {
					searchFlag = current;
				} 
				if(searchFlag == null) {
					continue;
				} else if(second == null) {
					if(verifyLow(current)) {
						second = this.list.get(index + 1);
					}
				} else if(first == null) {
					if(verifyHigh(current)) {
						first = current;
						break;
					}
				}
			} else if(ps == PositionSide.SHORT) {
				if(searchFlag == null && verifyLow(current)) {
					searchFlag = current;
				}
				if(searchFlag == null) {
					continue;
				} else if(second == null) {
					if(verifyHigh(current)) {
						second = this.list.get(index + 1);
					}
				} else if(first == null) {
					if(verifyLow(current)) {
						first = current;
						break;
					}
				}
			}
		}
		
		if(first == null || second == null) {
			return;
		}
		
		List<Klines> firstSubList = PriceUtil.subList(first, second, list);
		List<Klines> secondSubList = null;
		Klines start = null;
		Klines end = null;
		Klines after = null;
		if(ps == PositionSide.LONG) {//
			start = PriceUtil.getMinPriceKLine(firstSubList);
			after = PriceUtil.getAfterKlines(start, list);
			if(after == null) {
				secondSubList = PriceUtil.subList(start, list);
			} else {
				secondSubList = PriceUtil.subList(after, list);
			}
			end = PriceUtil.getMaxPriceKLine(secondSubList);
			fibInfo = new FibInfo(start.getLowPriceDoubleValue(), end.getHighPriceDoubleValue(), start.getDecimalNum(), FibLevel.LEVEL_0);
		} else if(ps == PositionSide.SHORT) {//
			start = PriceUtil.getMaxPriceKLine(firstSubList);
			after = PriceUtil.getAfterKlines(start, list);
			if(after == null) {
				secondSubList = PriceUtil.subList(start, list);
			} else {
				secondSubList = PriceUtil.subList(after, list);
			}
			end = PriceUtil.getMinPriceKLine(secondSubList);
			fibInfo = new FibInfo(start.getHighPriceDoubleValue(), end.getLowPriceDoubleValue(), start.getDecimalNum(), FibLevel.LEVEL_0);
		}
		
		if(fibInfo == null) {
			return;
		}
		
		Klines fibAfter = PriceUtil.getAfterKlines(end, list);
		if(fibAfter == null) {
			return;
		}
		
		fibAfterKlines = PriceUtil.subList(fibAfter, list); 
		
		fibInfo.setFibAfterKlines(fibAfterKlines);
	}
	
	private boolean verifyHigh(Klines k) {
		return k.getEma7() > k.getEma25();
	}
	
	private boolean verifyLow(Klines k) {
		return k.getEma7() < k.getEma25();
	}
	
	private boolean verifyLong(Klines k) {
		return k.getEma25() > k.getEma99();
	}
	
	private boolean verifyShort(Klines k) {
		return k.getEma25() < k.getEma99();
	}

	public List<Klines> getFibAfterKlines() {
		return fibAfterKlines;
	}

	public FibInfo getFibInfo() {
		return fibInfo;
	}
}
