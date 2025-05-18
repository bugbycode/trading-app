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
		
		PositionSide ps = PositionSide.DEFAULT;
		
		Klines second = null;
		Klines first = null;
		
		Klines afterFlag = null;
		
		for(int index = this.list.size() - 1; index > 1; index--) {
			Klines current = this.list.get(index);
			Klines parent = this.list.get(index - 1);
			if(ps == PositionSide.DEFAULT) {
				if(verifyHigh(current, parent)) {
					ps = PositionSide.SHORT;
				} else if(verifyLow(current, parent)) {
					ps = PositionSide.LONG;
				}
			} else if(ps == PositionSide.LONG) {
				if(second == null) {
					if(verifyHigh(current, parent)) {
						second = current;
					}
				} else if(first == null) {
					if(verifyLow(current, parent)) {
						first = parent;
						break;
					}
				}
			} else if(ps == PositionSide.SHORT) {
				if(second == null) {
					if(verifyLow(current, parent)) {
						second = current;
					}
				} else if(first == null) {
					if(verifyHigh(current, parent)) {
						first = parent;
						break;
					}
				}
			}
		}
		
		if(first == null || second == null) {
			return;
		}
		
		List<Klines> secondSubList = PriceUtil.subList(second, list);
		if(!CollectionUtils.isEmpty(secondSubList) || secondSubList.size() > 2) {
			for(int index = 1; index < secondSubList.size(); index++) {
				Klines current = secondSubList.get(index);
				Klines parent = secondSubList.get(index - 1);
				if(ps == PositionSide.SHORT && verifyHigh(current, parent)) {
					afterFlag = parent;
					break;
				} else if(ps == PositionSide.LONG && verifyLow(current, parent)) {
					afterFlag = parent;
					break;
				}
			}
			if(afterFlag != null) {
				second = afterFlag;
			}
		}
		
		
		List<Klines> firstSubList = PriceUtil.subList(first, second, list);
		
		if(CollectionUtils.isEmpty(firstSubList)) {
			return;
		}
		
		Klines start = null;
		Klines end = null;
		if(ps == PositionSide.LONG) {
			start = PriceUtil.getMinPriceKLine(firstSubList);
			end = PriceUtil.getMaxPriceKLine(firstSubList);
			fibInfo = new FibInfo(start, end, start.getDecimalNum(), FibLevel.LEVEL_3);
		} else if(ps == PositionSide.SHORT) {
			start = PriceUtil.getMaxPriceKLine(firstSubList);
			end = PriceUtil.getMinPriceKLine(firstSubList);
			fibInfo = new FibInfo(end, start, start.getDecimalNum(), FibLevel.LEVEL_3);
		}
		
		if(fibInfo == null) {
			return;
		}
		
		List<Klines> start_end = new ArrayList<Klines>();
		start_end.add(start);
		start_end.add(end);
		start_end.sort(kc);
		
		Klines fibAfter = PriceUtil.getAfterKlines(start_end.get(start_end.size() - 1), list);
		if(fibAfter == null) {
			return;
		}
		
		fibAfterKlines = PriceUtil.subList(fibAfter, list); 
		
		fibInfo.setFibAfterKlines(fibAfterKlines);
	}
	
	private boolean verifyLow(Klines current, Klines parent) {
		return parent.isFall() && current.isRise() && parent.getBbPercentB() <= 0;
	}
	
	private boolean verifyHigh(Klines current, Klines parent) {
		return parent.isRise() && current.isFall() && parent.getBbPercentB() >= 1;
	}

	public List<Klines> getFibAfterKlines() {
		return fibAfterKlines;
	}

	public FibInfo getFibInfo() {
		return fibInfo;
	}
}
