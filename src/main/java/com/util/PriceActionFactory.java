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
 * 价格行为指标
 */
public class PriceActionFactory {
	
	private final Logger logger = LogManager.getLogger(PriceActionFactory.class);

	private List<Klines> list;
	
	private FibInfo fibInfo;
	
	private List<Klines> fibAfterKlines;
	
	private List<Double> openPrices;
	
	public PriceActionFactory(List<Klines> list) {
		this.list = new ArrayList<Klines>();
		this.openPrices = new ArrayList<Double>();
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
		
		PriceUtil.calculateAllBBPercentB(list);
		
		PositionSide ps = PositionSide.DEFAULT;
		
		Klines fourth = null;
		Klines third = null;
		Klines second = null;
		Klines first = null;
		
		for(int index = list.size() - 1; index > 0; index--) {
			Klines k = list.get(index);
			if(ps == PositionSide.DEFAULT) {
				if(verifyHigh(k)) {
					ps = PositionSide.SHORT;
				} else if(verifyLow(k)) {
					ps = PositionSide.LONG;
				}
			} else if(ps == PositionSide.LONG){
				if(fourth == null) {
					if(verifyHigh(k)) {
						fourth = k;
					}
				} else if(third == null) {
					if(k.getBbPercentB() < 0.5) {
						third = k;
					}
				} else if(second == null) {
					if(k.getBbPercentB() >= 0.5) {
						second = k;
					}
				} else if(first == null) {
					if(k.getBbPercentB() < 0.5) {
						first = list.get(index + 1);
						break;
					}
				}
			} else if(ps == PositionSide.SHORT) {
				if(fourth == null) {
					if(verifyLow(k)) {
						fourth = k;
					}
				} else if(third == null) {
					if(k.getBbPercentB() > 0.5) {
						third = k;
					}
				} else if(second == null) {
					if(k.getBbPercentB() <= 0.5) {
						second = k;
					}
				} else if(first == null) {
					if(k.getBbPercentB() > 0.5) {
						first = list.get(index + 1);
						break;
					}
				}
			}
		}
		
		if(first == null) {
			return;
		}
		
		List<Klines> areaList = PriceUtil.subList(first, third, list);
		List<Klines> fibList = PriceUtil.subList(third, list);
		if(CollectionUtils.isEmpty(fibList)) {
			return;
		}
		
		Klines start = null;
		Klines end = null;
		List<Klines> fibSub = null;
		if(ps == PositionSide.LONG) {
			start = PriceUtil.getMaxPriceKLine(fibList);
			fibSub = PriceUtil.subList(start, list);
			end = PriceUtil.getMinPriceKLine(fibSub);
			fibInfo = new FibInfo(start.getHighPriceDoubleValue(), end.getLowPriceDoubleValue(), start.getDecimalNum(), FibLevel.LEVEL_1);
		} else if(ps == PositionSide.SHORT) {
			start = PriceUtil.getMinPriceKLine(fibList);
			fibSub = PriceUtil.subList(start, list);
			end = PriceUtil.getMaxPriceKLine(fibSub);
			fibInfo = new FibInfo(start.getLowPriceDoubleValue(), end.getHighPriceDoubleValue(), start.getDecimalNum(), FibLevel.LEVEL_1);
		}
		
		if(fibInfo == null) {
			return;
		}

		logger.debug(areaList);
		logger.debug(start);
		logger.debug(end);
		
		Klines areaLow = PriceUtil.getMinPriceKLine(areaList);
		Klines areaHigh = PriceUtil.getMaxPriceKLine(areaList);
		addPrices(areaLow.getLowPriceDoubleValue());
		addPrices(areaHigh.getHighPriceDoubleValue());
		
		if(ps == PositionSide.LONG) {
			openPrices.sort(new PriceComparator(SortType.DESC));
		} else {
			openPrices.sort(new PriceComparator(SortType.ASC));
		}
		
		Klines fibAfterFlag = PriceUtil.getAfterKlines(end, list);
		if(fibAfterFlag != null) {
			fibAfterKlines = PriceUtil.subList(fibAfterFlag, list);
		}
	}
	
	public boolean verifyHigh(Klines k) {
		return k.getBbPercentB() >= 1;
	}
	
	public boolean verifyLow(Klines k) {
		return k.getBbPercentB() <= 0;
	}
	
	public boolean verifyOpen(List<Klines> list) {
		boolean result = false;
		if(!(CollectionUtils.isEmpty(list) || fibInfo == null)) {
			Klines last = PriceUtil.getLastKlines(list);
			double closePrice = last.getClosePriceDoubleValue();
			double fibPrice = fibInfo.getFibValue(FibCode.FIB5);
			QuotationMode mode = fibInfo.getQuotationMode();
			for(int index = 0; index < openPrices.size(); index++) {
				double price = openPrices.get(index);
				if(mode == QuotationMode.SHORT && PriceUtil.isLong_v2(price, list) && closePrice < fibPrice) {
					Klines afterLowKlines  = PriceUtil.getMinPriceKLine(fibAfterKlines);
					if(!PriceUtil.isObsoleteLong(afterLowKlines, openPrices, index)) {
						result = true;
					}
				} else if(mode == QuotationMode.LONG && PriceUtil.isShort_v2(price, list) && closePrice > fibPrice) {
					Klines afterHighKlines  = PriceUtil.getMaxPriceKLine(fibAfterKlines);
					if(!PriceUtil.isObsoleteShort(afterHighKlines, openPrices, index)) {
						result = true;
					}
				}
			}
		}
		return result;
	}
	
	public FibInfo getFibInfo() {
		return this.fibInfo;
	}
	
	public void addPrices(double price) {
		if(!PriceUtil.contains(openPrices, price)) {
			openPrices.add(price);
		}
	}

	public List<Double> getOpenPrices() {
		return openPrices;
	}

	public List<Klines> getFibAfterKlines() {
		return fibAfterKlines;
	}
}
