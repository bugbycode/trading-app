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
		PriceUtil.calculateAllBBPercentB(list);
		
		Klines last = PriceUtil.getLastKlines(list);
		
		double ema25 = last.getEma25();
		double ema99 = last.getEma99();

		List<Klines> fibList = new ArrayList<Klines>();
		
		if(ema25 > ema99) { // 做多
			
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
				fibInfo = new FibInfo(startKlines.getHighPriceDoubleValue(), endKlines.getLowPriceDoubleValue(), startKlines.getDecimalNum(), FibLevel.LEVEL_1);
			}
			
		} else if(ema25 < ema99) { //做空
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
				fibInfo = new FibInfo(startKlines.getLowPriceDoubleValue(), endKlines.getHighPriceDoubleValue(), startKlines.getDecimalNum(), FibLevel.LEVEL_1);
			}
		}
		return fibInfo;
	}
	
	public boolean verify() {
		
		boolean result = false;
		
		Klines last = PriceUtil.getLastKlines(list);
		
		double ema25 = last.getEma25();
		double ema99 = last.getEma99();
		
		if(ema25 > ema99 && PriceUtil.isOversold(list)) {//做多
			result = true;
		} else if(ema25 < ema99 && PriceUtil.isOverbuying(list)) {//做空
			result = true;
		}
		
		return result;
	}
	
}
