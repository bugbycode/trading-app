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
		if(CollectionUtils.isEmpty(list) || list.size() < 99) {
			return;
		}
		
		KlinesComparator kc = new KlinesComparator(SortType.ASC);
		this.list.sort(kc);
		
		PriceUtil.calculateEMA_7_25_99(list);
		
		PositionSide ps = PositionSide.DEFAULT;
		
		Klines third = null;
		Klines second = null;
		Klines first = null;
		
		for(int index = list.size() - 1; index > 0; index--) {
			Klines current = list.get(index);
			if(ps == PositionSide.DEFAULT) {
				if(verifyLong(current)) {
					ps = PositionSide.LONG;
				} else if(verifyShort(current)) {
					ps = PositionSide.SHORT;
				}
			} else if(ps == PositionSide.LONG) {// high - low - high
				if(third == null) { //high
					if(verifyHigh(current)) {
						third = current;
					}
				} else if(second == null) {// low
					if(verifyLow(current)) {
						second = current;
					}
				} else if(first == null) {// high
					if(verifyHigh(current)) {
						first = current;
						break;
					}
				}
			} else if(ps == PositionSide.SHORT) { // low - high - low
				if(third == null) { //low
					if(verifyLow(current)) {
						third = current;
					}
				} else if(second == null) {// high
					if(verifyHigh(current)) {
						second = current;
					}
				} else if(first == null) {// low
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
		Klines end = null;
		
		if(ps == PositionSide.LONG) {
			start = PriceUtil.getMinPriceKLine(firstSubList);
			secondSubList = PriceUtil.subList(start, third, list);
			end = PriceUtil.getMaxPriceKLine(secondSubList);
			fibInfo = new FibInfo(start.getLowPriceDoubleValue(), end.getHighPriceDoubleValue(), start.getDecimalNum(), FibLevel.LEVEL_3);
		} else if(ps == PositionSide.SHORT) {
			start = PriceUtil.getMaxPriceKLine(firstSubList);
			secondSubList = PriceUtil.subList(start, third, list);
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
	
	private boolean verifyLong(Klines k) {
		return k.getEma25() < k.getEma99();
	}
	
	private boolean verifyShort(Klines k) {
		return k.getEma25() > k.getEma99();
	}
	
	private boolean verifyHigh(Klines k) {
		return k.getEma7() > k.getEma25() && k.getEma25() > k.getEma99();
	}
	
	private boolean verifyLow(Klines k) {
		return k.getEma7() < k.getEma25() && k.getEma25() < k.getEma99();
	}

	public List<Klines> getFibAfterKlines() {
		return fibAfterKlines;
	}

	public FibInfo getFibInfo() {
		return fibInfo;
	}
}