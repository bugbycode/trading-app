package com.util;

import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.util.CollectionUtils;

import com.bugbycode.module.FibCode;
import com.bugbycode.module.FibInfo;
import com.bugbycode.module.FibLevel;
import com.bugbycode.module.Klines;
import com.bugbycode.module.QuotationMode;
import com.bugbycode.module.SortType;
import com.bugbycode.module.trading.PositionSide;

/**
 * 盘整区斐波那契回撤工具类
 */
public class ConsolidationAreaFibUtil {

	private final Logger logger = LogManager.getLogger(ConsolidationAreaFibUtil.class);
	
	private List<Klines> list;
	
	private List<Klines> fibAfterKlines;

	private FibInfo fibInfo;
	
	private List<Double> openPrices;
	
	public ConsolidationAreaFibUtil(List<Klines> list) {
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
		
		fibAfterKlines = PriceUtil.subList(afterFlag, list);
		
		if(CollectionUtils.isEmpty(firstSubList)) {
			return;
		}
		
		Klines start = null;
		Klines end = null;
		if(ps == PositionSide.LONG) {
			start = PriceUtil.getMinPriceKLine(firstSubList);
			end = PriceUtil.getMaxPriceKLine(firstSubList);
			fibInfo = new FibInfo(start, end, start.getDecimalNum(), FibLevel.LEVEL_1);
		} else if(ps == PositionSide.SHORT) {
			start = PriceUtil.getMaxPriceKLine(firstSubList);
			end = PriceUtil.getMinPriceKLine(firstSubList);
			fibInfo = new FibInfo(end, start, start.getDecimalNum(), FibLevel.LEVEL_1);
		}
		
		if(fibInfo == null) {
			return;
		}
		
		QuotationMode mode = fibInfo.getQuotationMode();
		addOpenPrice(fibInfo.getFibValue(FibCode.FIB1));
		if(mode == QuotationMode.SHORT) {
			//顶部压力区
			Klines bodyHighPriceKLine = PriceUtil.getMaxBodyHighPriceKLine(firstSubList);
			Klines fallMaxBodyLowPriceKLine = PriceUtil.getFallMaxBodyLowPriceKLine(firstSubList);
			if(bodyHighPriceKLine != null) {
				addOpenPrice(bodyHighPriceKLine.getBodyHighPriceDoubleValue());
			}
			if(fallMaxBodyLowPriceKLine != null) {
				addOpenPrice(fallMaxBodyLowPriceKLine.getBodyLowPriceDoubleValue());
			}
			openPrices.sort(new PriceComparator(SortType.ASC));
		} else {
			//底部压力区
			Klines bodyLowPriceKline = PriceUtil.getMinBodyLowPriceKLine(firstSubList);
			Klines riseMinBodyHighPriceKline = PriceUtil.getRiseMinBodyHighPriceKLine(firstSubList);
			if(bodyLowPriceKline != null) {
				addOpenPrice(bodyLowPriceKline.getBodyLowPriceDoubleValue());
			}
			if(riseMinBodyHighPriceKline != null) {
				addOpenPrice(riseMinBodyHighPriceKline.getBodyHighPriceDoubleValue());
			}
			openPrices.sort(new PriceComparator(SortType.DESC));
		}
	}
	
	public boolean verifyOpen(List<Klines> list) {
		boolean result = false;
		if(!(CollectionUtils.isEmpty(list) || fibInfo == null)) {
			Klines last = PriceUtil.getLastKlines(list);
			double closePrice = last.getClosePriceDoubleValue();
			double fib618Price = fibInfo.getFibValue(FibCode.FIB618);
			QuotationMode mode = fibInfo.getQuotationMode();
			Klines afterHitFlag = null;
			if(!CollectionUtils.isEmpty(fibAfterKlines)) {
				if(mode == QuotationMode.LONG) {
					afterHitFlag = PriceUtil.getMinPriceKLine(fibAfterKlines);
				} else {
					afterHitFlag = PriceUtil.getMaxPriceKLine(fibAfterKlines);
				}
			}
			for(int index = 0;index < openPrices.size();index++) {
				double price = openPrices.get(index);
				if(mode == QuotationMode.LONG && closePrice < fib618Price 
						&& PriceUtil.isLong_v2(price, list) && !PriceUtil.isObsoleteLong(afterHitFlag, openPrices, index)) {
					result = true;
					break;
				} else if(mode == QuotationMode.SHORT && closePrice > fib618Price
						&& PriceUtil.isShort_v2(price, list) && !PriceUtil.isObsoleteShort(afterHitFlag, openPrices, index)) {
					result = true;
					break;
				}
			}
		}
		return result;
	}
	
	private boolean verifyLow(Klines current, Klines parent) {
		return parent.isFall() && current.isRise() && parent.getBbPercentB() <= 0;
	}
	
	private boolean verifyHigh(Klines current, Klines parent) {
		return parent.isRise() && current.isFall() && parent.getBbPercentB() >= 1;
	}
	
	private void addOpenPrice(double price) {
		if(!PriceUtil.contains(openPrices, price)) {
			openPrices.add(price);
		}
	}

	public FibInfo getFibInfo() {
		return fibInfo;
	}

	public List<Double> getOpenPrices() {
		return openPrices;
	}

	public List<Klines> getFibAfterKlines() {
		return fibAfterKlines;
	}
}
