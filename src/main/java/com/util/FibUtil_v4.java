package com.util;

import java.util.ArrayList;
import java.util.List;

import org.springframework.util.CollectionUtils;

import com.bugbycode.module.FibCode;
import com.bugbycode.module.FibInfo;
import com.bugbycode.module.FibLevel;
import com.bugbycode.module.Klines;
import com.bugbycode.module.QuotationMode;
import com.bugbycode.module.SortType;

/**
 * 指数均线斐波那契回撤工具类
 */
public class FibUtil_v4 {

	private List<Klines> list;
	
	private List<Klines> fibAfterKlines;
	
	private List<Double> openPriceList = new ArrayList<Double>();
	
	private FibInfo fibInfo;
	
	public FibUtil_v4(List<Klines> list) {
		this.list = new ArrayList<Klines>();
		if(!CollectionUtils.isEmpty(list)) {
			this.list.addAll(list);
			this.init();
		}
	}
	
	public void init() {
		
		if(CollectionUtils.isEmpty(this.list)) {
			return;
		}
		
		KlinesComparator kc = new KlinesComparator(SortType.ASC);
		this.list.sort(kc);
		
		PriceUtil.calculateEMA_7_25_99(list);
		PriceUtil.calculateAllBBPercentB(list);
		
		Klines last = PriceUtil.getLastKlines(list);
		
		double ema25 = last.getEma25();
		double ema99 = last.getEma99();

		List<Klines> fibList = new ArrayList<Klines>();
		
		Klines endKlines = null;
		
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
				endKlines = PriceUtil.getMinPriceKLine(afterList);
				this.fibInfo = new FibInfo(startKlines.getHighPriceDoubleValue(), endKlines.getLowPriceDoubleValue(), startKlines.getDecimalNum(), FibLevel.LEVEL_1);
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
				endKlines = PriceUtil.getMaxPriceKLine(afterList);
				this.fibInfo = new FibInfo(startKlines.getLowPriceDoubleValue(), endKlines.getHighPriceDoubleValue(), startKlines.getDecimalNum(), FibLevel.LEVEL_1);
			}
		}
		
		if(this.fibInfo == null) {
			return;
		}
		
		QuotationMode qm = this.fibInfo.getQuotationMode();
		
		for(int index = this.list.size() - 1; index > 0; index--) {
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
			
			if(qm == QuotationMode.SHORT && PriceUtil.isLow(current, next)) { //做多
				
				openPriceList.add(PriceUtil.getMinPrice(c_low, n_low));//最低点
				openPriceList.add(PriceUtil.getMinPrice(c_body_high, n_body_high));//实体部分高点最低价
				openPriceList.add(PriceUtil.getMinPrice(c_body_low, n_body_low));//实体部分低点最低价
				openPriceList.sort(new PriceComparator(SortType.DESC));
				
				if(current.gt(endKlines)) {
					endKlines = current;
				}
				
				break;
			} else if(qm == QuotationMode.LONG && PriceUtil.isHigh(current, next)){//做空
				
				openPriceList.add(PriceUtil.getMaxPrice(c_high, n_high));//最高点
				openPriceList.add(PriceUtil.getMaxPrice(c_body_high, n_body_high));//实体部分高点最高价
				openPriceList.add(PriceUtil.getMaxPrice(c_body_low, n_body_low));//实体部分低点最高价
				openPriceList.sort(new PriceComparator(SortType.ASC));
				
				if(current.gt(endKlines)) {
					endKlines = current;
				}
				
				break;
			}
		}
		
		Klines endKlineAfter = PriceUtil.getAfterKlines(endKlines, list);
		
		if(endKlineAfter != null) {
			this.fibAfterKlines = PriceUtil.subList(endKlineAfter, list);
		}
	}
	
	public FibInfo getFibInfo() {
		return this.fibInfo;
	}
	
	public List<Klines> getFibAfterKlines() {
		return fibAfterKlines;
	}

	public List<Double> getOpenPriceList() {
		return openPriceList;
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
				if(qm == QuotationMode.SHORT && (PriceUtil.isBreachLong(last, price) || PriceUtil.isLong_v2(price, hitList)) 
						&& !PriceUtil.isObsoleteLong(afterLowKlines, openPriceList, index)
						&& closePrice < fib382Price) {
					result = true;
				} else if(qm == QuotationMode.LONG && (PriceUtil.isBreachShort(last, price) || PriceUtil.isShort_v2(price, hitList)) 
						&& !PriceUtil.isObsoleteShort(afterHighKlines, openPriceList, index)
						&& closePrice > fib382Price) {
					result = true;
				}
			}
		}
		return result;
	}
	
}
