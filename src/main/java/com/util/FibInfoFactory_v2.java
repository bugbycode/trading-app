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
import com.bugbycode.module.trading.PositionSide;

/**
 * 斐波那契回撤工厂类 <br/>
 */
public class FibInfoFactory_v2 {

	private List<Klines> list;
	
	private List<Klines> fibAfterKlines;
	
	private FibInfo fibInfo;
	
	private List<Double> openPrices;
	
	public FibInfoFactory_v2(List<Klines> list) {
		this.openPrices = new ArrayList<Double>();
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
		//PriceUtil.calculateEMA_7_25_99(list);
		PositionSide ps = PositionSide.DEFAULT;
		
		Klines first = null;
		Klines second = null;
		Klines third = null;
		for(int index = list.size() - 1; index > 1; index--) {
			Klines current = list.get(index);
			Klines parent = list.get(index - 1);
			if(ps == PositionSide.DEFAULT) {
				if(verifyHigh(current, parent)) {// high - low - high
					ps = PositionSide.LONG;
					third = current;
				} else if(verifyLow(current, parent)) {// low - high - low
					ps = PositionSide.SHORT;
					third = current;
				}
			} else if(second == null) {
				if(ps == PositionSide.LONG && verifyLow(current, parent)) {//low
					second = current;
				} else if(ps == PositionSide.SHORT && verifyHigh(current, parent)) {//high
					second = current;
				}
			} else if(first == null) {
				if(ps == PositionSide.LONG && verifyHigh(current, parent)) {//high
					first = current;
				} else if(ps == PositionSide.SHORT && verifyLow(current, parent)) {//low
					first = current;
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
			fibInfo = new FibInfo(start.getLowPriceDoubleValue(), end.getHighPriceDoubleValue(), start.getDecimalNum(), FibLevel.LEVEL_1);
		} else if(ps == PositionSide.SHORT) {
			start = PriceUtil.getMaxPriceKLine(firstSubList);
			secondSubList = PriceUtil.subList(start, list);
			end = PriceUtil.getMinPriceKLine(secondSubList);
			fibInfo = new FibInfo(start.getHighPriceDoubleValue(), end.getLowPriceDoubleValue(), start.getDecimalNum(), FibLevel.LEVEL_1);
		}
		
		if(fibInfo == null) {
			return;
		}
		
		Klines fibAfterFlag = PriceUtil.getAfterKlines(end, list);
		if(fibAfterFlag != null) {
			fibAfterKlines = PriceUtil.subList(fibAfterFlag, list);
		}
		
		Klines areaEnd = null;
		Klines areaFirst = null;
		Klines areaSecond = null;
		Klines areaThird = null;
		QuotationMode mode = fibInfo.getQuotationMode();
		List<Klines> fibSubList = PriceUtil.subList(start, end, list);
		for(int index = 0; index < fibSubList.size(); index++) {
			Klines current = fibSubList.get(index);
			if(mode == QuotationMode.LONG) {
				if(areaFirst == null) {
					if(current.getBbPercentB() > 0.5) {
						areaFirst = current;
						if(index == 0) {
							areaEnd = current;
						} else {
							areaEnd = fibSubList.get(index - 1);
						}
					}
				} else if(areaSecond == null) {
					if(current.getBbPercentB() < 0.5) {
						areaSecond = current;
					}
				} else if(areaThird == null) {
					if(current.getBbPercentB() > 0.5) {
						areaThird = fibSubList.get(index - 1);
					}
				}
			} else {
				if(areaFirst == null) {
					if(current.getBbPercentB() < 0.5) {
						areaFirst = current;
						if(index == 0) {
							areaEnd = current;
						} else {
							areaEnd = fibSubList.get(index - 1);
						}
					}
				} else if(areaSecond == null) {
					if(current.getBbPercentB() > 0.5) {
						areaSecond = current;
					}
				} else if(areaThird == null) {
					if(current.getBbPercentB() < 0.5) {
						areaThird = fibSubList.get(index - 1);
					}
				}
			}
		}
		
		Klines areaStart = null;
		if(areaThird == null) {
			areaStart = fibSubList.get(0);
		} else {
			List<Klines> areaFirstSub = PriceUtil.subList(areaFirst, areaSecond, fibSubList);
			if(mode == QuotationMode.LONG) {
				areaStart = PriceUtil.getMaxPriceKLine(areaFirstSub);
			} else {
				areaStart = PriceUtil.getMinPriceKLine(areaFirstSub);
			}
			areaEnd = areaThird;
		}
		
		List<Klines> areaList = PriceUtil.subList(areaStart, areaEnd, fibSubList);
		
		Klines high = PriceUtil.getMaxPriceKLine(areaList);
		Klines low = PriceUtil.getMinPriceKLine(areaList);
		addOpenPrice(high.getHighPriceDoubleValue());
		addOpenPrice(low.getLowPriceDoubleValue());
		addOpenPrice(fibInfo.getFibValue(FibCode.FIB1));
		
		if(mode == QuotationMode.LONG) {
			openPrices.sort(new PriceComparator(SortType.DESC));
		} else {
			openPrices.sort(new PriceComparator(SortType.ASC));
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
	

	public List<Double> getOpenPrices() {
		return openPrices;
	}
	
	private void addOpenPrice(double price) {
		if(!PriceUtil.contains(openPrices, price)) {
			openPrices.add(price);
		}
	}
}