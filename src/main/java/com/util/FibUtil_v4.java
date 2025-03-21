package com.util;

import java.util.ArrayList;
import java.util.List;

import org.springframework.util.CollectionUtils;

import com.bugbycode.module.FibInfo;
import com.bugbycode.module.FibLevel;
import com.bugbycode.module.Klines;
import com.bugbycode.module.SortType;

/**
 * 指数均线斐波那契回撤工具类
 */
public class FibUtil_v4 {

	private List<Klines> list;
	
	public FibUtil_v4(List<Klines> list) {
		this.list = new ArrayList<Klines>();
		this.list.addAll(list);
	}
	
	public FibInfo getFibInfo() {
		
		FibInfo fibInfo = null;
		
		KlinesComparator kc = new KlinesComparator(SortType.ASC);
		this.list.sort(kc);
		
		PriceUtil.calculateEMA_7_25_99(list);
		
		Klines last = PriceUtil.getLastKlines(list);
		
		double ema25 = last.getEma25();
		double ema99 = last.getEma99();

		List<Klines> fibList = new ArrayList<Klines>();
		
		if(ema25 > ema99 && PriceUtil.isBreachLong(last, ema99)) { // 做多
			
			for(int i = list.size() - 1; i >= 0; i--) {
				Klines k = list.get(i);
				if(k.getEma25() < k.getEma99()) {
					fibList = PriceUtil.subList(k, list);
					break;
				}
			}
			
			if(!CollectionUtils.isEmpty(fibList)) {
				Klines startKlines = PriceUtil.getMaxPriceKLine(fibList);
				List<Klines> afterList = PriceUtil.subList(startKlines, fibList);
				Klines endKlines = PriceUtil.getMinPriceKLine(afterList);
				fibInfo = new FibInfo(startKlines.getHighPriceDoubleValue(), endKlines.getBodyLowPriceDoubleValue(), startKlines.getDecimalNum(), FibLevel.LEVEL_1);
			}
			
		} else if(ema25 < ema99 && PriceUtil.isBreachShort(last, ema99)) { //做空
			for(int i = list.size() - 1; i >= 0; i--) {
				Klines k = list.get(i);
				if(k.getEma25() > k.getEma99()) {
					fibList = PriceUtil.subList(k, list);
					break;
				}
			}
			
			if(!CollectionUtils.isEmpty(fibList)) {
				Klines startKlines = PriceUtil.getMinPriceKLine(fibList);
				List<Klines> afterList = PriceUtil.subList(startKlines, fibList);
				Klines endKlines = PriceUtil.getMaxPriceKLine(afterList);
				fibInfo = new FibInfo(startKlines.getBodyLowPriceDoubleValue(), endKlines.getHighPriceDoubleValue(), startKlines.getDecimalNum(), FibLevel.LEVEL_1);
			}
		}
		return fibInfo;
	}
	
	public boolean verify() {
		
		boolean result = false;
		
		int index = list.size() - 1;
		Klines last = list.get(index);
		Klines parent = list.get(index - 1);
		
		double ema25 = last.getEma25();
		double ema99 = last.getEma99();
		
		if(ema25 > ema99 && isLong(last, parent)) {//做多
			result = true;
		} else if(ema25 < ema99 && isShort(last, parent)) {//做空
			result = true;
		}
		
		return result;
	}
	
	private boolean isLong(Klines last, Klines parent) {
		
		double ema99 = last.getEma99();
		
		double parent_ema99 = parent.getEma99();
		
		return ( PriceUtil.isBreachLong(last, ema99) && last.isRise() ) || 
				( PriceUtil.isBreachLong(parent, parent_ema99) && parent.isFall() && last.isRise() );
	}
	
	private boolean isShort(Klines last, Klines parent) {
		double ema99 = last.getEma99();
		
		double parent_ema99 = parent.getEma99();
		
		return ( PriceUtil.isBreachShort(last, ema99) && last.isFall() ) || 
				( PriceUtil.isBreachShort(parent, parent_ema99) && parent.isRise() && last.isFall() );
	}
}
