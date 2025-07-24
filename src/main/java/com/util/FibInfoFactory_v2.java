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
 * 斐波那契回撤指标 V2 以日线级别作为参考做趋势交易
 */
public class FibInfoFactory_v2 {

	private List<Klines> list;
	
	private List<Klines> fibAfterKlines;
	
	private FibInfo fibInfo;
	
	private List<Klines> list_15m;//十五分钟级别k线 用于补充回撤之后的k线信息
	
	private List<Double> openPrices;
	
	public FibInfoFactory_v2(List<Klines> list, List<Klines> list_15m) {
		this.list = new ArrayList<Klines>();
		this.list_15m = new ArrayList<Klines>();
		this.openPrices = new ArrayList<Double>();
		this.fibAfterKlines = new ArrayList<Klines>();
		if(!CollectionUtils.isEmpty(list_15m)) {
			this.list_15m.addAll(list_15m);
		}
		if(!CollectionUtils.isEmpty(list)) {
			this.list.addAll(list);
			this.init(false);
		}
	}

	private void init(boolean loadParent) {
		if(CollectionUtils.isEmpty(list) || list.size() < 10 || CollectionUtils.isEmpty(list_15m)) {
			return;
		}
		
		KlinesComparator kc = new KlinesComparator(SortType.ASC);
		this.list.sort(kc);
		
		PriceUtil.calculateEMA_7_25_99(list);
		
		this.openPrices.clear();
		
		Klines last = PriceUtil.getLastKlines(list);
		
		PositionSide ps = getPositionSide(last);
		double lowPrice = last.getLowPriceDoubleValue();
		double highPrice = last.getHighPriceDoubleValue();
		double bodyLowPrice = last.getBodyLowPriceDoubleValue();
		double bodyHighPrice = last.getBodyHighPriceDoubleValue();
		int decimalNum = last.getDecimalNum();
		
		if(ps == PositionSide.LONG) {
			this.fibInfo = new FibInfo(lowPrice, highPrice, decimalNum, FibLevel.LEVEL_0);
			
			if(last.isRise()) {
				addPrices(bodyHighPrice);
				addPrices(bodyLowPrice);
				addPrices(lowPrice);
			} else if(last.isFall()) {
				addPrices(bodyLowPrice);
				addPrices(lowPrice);
			}
			
		} else if(ps == PositionSide.SHORT) {
			this.fibInfo = new FibInfo(highPrice, lowPrice, decimalNum, FibLevel.LEVEL_0);
			
			if(last.isRise()) {
				addPrices(highPrice);
				addPrices(bodyHighPrice);
			} else if(last.isFall()) {
				addPrices(highPrice);
				addPrices(bodyHighPrice);
				addPrices(bodyLowPrice);
			}
		}
		
		if(this.fibInfo == null) {
			return;
		}
		
		Klines fibAfter = PriceUtil.getAfterKlines(last, list_15m);
		if(fibAfter != null) {
			this.fibAfterKlines = PriceUtil.subList(fibAfter, list_15m);
			this.fibInfo.setFibAfterKlines(fibAfterKlines);
		}
	}
	
	private PositionSide getPositionSide(Klines last) {
		PositionSide ps = PositionSide.DEFAULT;
		
		if(verifyLong(last)) {
			ps = PositionSide.LONG;
		} else if(verifyShort(last)) {
			ps = PositionSide.SHORT;
		}
		return ps;
	}
	
	private boolean verifyLong(Klines k) {
		return k.getClosePriceDoubleValue() > k.getEma7();
	}
	
	private boolean verifyShort(Klines k) {
		return k.getClosePriceDoubleValue() < k.getEma7();
	}
	
	public FibInfo getFibInfo() {
		return this.fibInfo;
	}
	
	public List<Klines> getFibAfterKlines() {
		return fibAfterKlines;
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
	
	public void addPrices(double price) {
		if(!PriceUtil.contains(openPrices, price)) {
			openPrices.add(price);
		}
	}

	public List<Double> getOpenPrices() {
		return openPrices;
	}
}