package com.bugbycode.factory.priceAction.impl;

import java.util.ArrayList;
import java.util.List;

import org.springframework.util.CollectionUtils;

import com.bugbycode.factory.priceAction.PriceActionFactory;
import com.bugbycode.module.FibCode;
import com.bugbycode.module.FibInfo;
import com.bugbycode.module.FibLevel;
import com.bugbycode.module.Klines;
import com.bugbycode.module.QuotationMode;
import com.bugbycode.module.SortType;
import com.bugbycode.module.price.OpenPrice;
import com.bugbycode.module.price.impl.OpenPriceDetails;
import com.bugbycode.module.trading.PositionSide;
import com.util.KlinesComparator;
import com.util.PriceComparator;
import com.util.PriceUtil;

/**
 * 价格行为指标接口实现类
 */
public class PriceActionFactoryImpl implements PriceActionFactory{
	
	private List<Klines> list;
	
	private List<Klines> fibAfterKlines;
	
	private FibInfo fibInfo;
	
	private List<Klines> list_15m;//十五分钟级别k线 用于补充回撤之后的k线信息
	
	private Klines start = null;
	
	private Klines end = null;
	
	private List<OpenPrice> openPrices;
	
	public PriceActionFactoryImpl(List<Klines> list, List<Klines> list_15m) {
		this.list = new ArrayList<Klines>();
		this.list_15m = new ArrayList<Klines>();
		this.openPrices = new ArrayList<OpenPrice>();
		this.fibAfterKlines = new ArrayList<Klines>();
		if(!CollectionUtils.isEmpty(list_15m)) {
			this.list_15m.addAll(list_15m);
		}
		if(!CollectionUtils.isEmpty(list)) {
			this.list.addAll(list);
			this.init();
		}
	}
	
	@Override
	public FibInfo getFibInfo() {
		return fibInfo;
	}

	@Override
	public List<Klines> getFibAfterKlines() {
		return fibAfterKlines;
	}

	@Override
	public List<OpenPrice> getOpenPrices() {
		return openPrices;
	}

	@Override
	public boolean isLong() {
		return fibInfo != null && fibInfo.getQuotationMode() == QuotationMode.SHORT;
	}

	@Override
	public boolean isShort() {
		return fibInfo != null && fibInfo.getQuotationMode() == QuotationMode.LONG;
	}

	private void init() {
		if(CollectionUtils.isEmpty(list) || list.size() < 50 || CollectionUtils.isEmpty(list_15m)) {
			return;
		}
		
		KlinesComparator kc = new KlinesComparator(SortType.ASC);
		this.list.sort(kc);
		this.list_15m.sort(kc);
		
		//PriceUtil.calculateBollingerBands(list);
		//PriceUtil.calculateEMA_7_25_99(list);
		PriceUtil.calculateMACD(list);
		//PriceUtil.calculateDeltaAndCvd(list);
		
		this.openPrices.clear();
		this.fibAfterKlines.clear();
		
		PositionSide ps = getPositionSide();
		if(ps == PositionSide.DEFAULT) {
			return;
		}
		
		Klines third = null;
		Klines second = null;
		Klines first = null;
		
		for(int index = list.size() - 1; index > 2; index--) {
			Klines current = list.get(index);
			Klines parent = list.get(index - 1);
			Klines next = list.get(index - 2);
			if(ps == PositionSide.LONG) {// low - high - low
				if(third == null) {
					if(verifyLow(current, parent, next)) {
						third = current;
					}
				} else if(second == null) {
					if(verifyHigh(current, parent, next)) {
						second = current;
					}
				} else if(first == null) {
					if(verifyLow(current, parent, next)) {
						first = current;
						break;
					}
				}
			} else if(ps == PositionSide.SHORT) { // high - low - high
				if(third == null) {
					if(verifyHigh(current, parent, next)) {
						third = current;
					}
				} else if(second == null) {
					if(verifyLow(current, parent, next)) {
						second = current;
					}
				} else if(first == null) {
					if(verifyHigh(current, parent, next)) {
						first = current;
						break;
					}
				}
			}
		}
		
		if(first == null || second == null || third == null) {
			return;
		}
		
		List<Klines> firstSubList = PriceUtil.subList(first, second, list);
		List<Klines> secondSubList = null;
		Klines startAfter = null;
		if(ps == PositionSide.LONG) {
			start = PriceUtil.getMaxPriceKLine(firstSubList);
			startAfter = PriceUtil.getAfterKlines(start, list);
			if(startAfter != null) {
				secondSubList = PriceUtil.subList(startAfter, third, list);
				end = PriceUtil.getMinPriceKLine(secondSubList);
				if(end != null) {
					fibInfo = new FibInfo(start.getHighPriceDoubleValue(), end.getLowPriceDoubleValue(), start.getDecimalNum(), FibLevel.LEVEL_1);
				}
			}
		} else if(ps == PositionSide.SHORT) {
			start = PriceUtil.getMinPriceKLine(firstSubList);
			startAfter = PriceUtil.getAfterKlines(start, list);
			if(startAfter != null) {
				secondSubList = PriceUtil.subList(startAfter, third, list);
				end = PriceUtil.getMaxPriceKLine(secondSubList);
				if(end != null) {
					fibInfo = new FibInfo(start.getLowPriceDoubleValue(), end.getHighPriceDoubleValue(), start.getDecimalNum(), FibLevel.LEVEL_1);
				}
			}
		}
		
		if(fibInfo == null) {
			return;
		}

		QuotationMode mode = this.fibInfo.getQuotationMode();
		
		Klines fibEnd = end;
		
		Klines last_15m = PriceUtil.getLastKlines(list_15m);
		
		double stopLoss = mode == QuotationMode.LONG ? last_15m.getHighPriceDoubleValue() : last_15m.getLowPriceDoubleValue();
		
		for(int index = list.size() - 1; index > 2;index--) {
			Klines current = list.get(index);
			Klines parent = list.get(index - 1);
			Klines next = list.get(index - 2);
			if((mode == QuotationMode.LONG && verifyHigh(current, parent, next)) 
					|| (mode == QuotationMode.SHORT && verifyLow(current, parent, next))) {
				fibEnd = current;
				addPrices(new OpenPriceDetails(fibInfo.getFibCode(current.getClosePriceDoubleValue()), current.getClosePriceDoubleValue(), stopLoss));
				break;
			}
		}
		
		if(fibEnd.lte(end)) {
			fibEnd = end;
		}
		
		Klines fibAfterFlag = PriceUtil.getAfterKlines(fibEnd, this.list_15m);
		if(fibAfterFlag != null) {
			this.fibAfterKlines.addAll(PriceUtil.subList(fibAfterFlag, this.list_15m));
			this.fibInfo.setFibAfterKlines(fibAfterKlines);
		}
		
		if(mode == QuotationMode.LONG) {
			this.openPrices.sort(new PriceComparator(SortType.ASC));
		} else {
			this.openPrices.sort(new PriceComparator(SortType.DESC));
		}
		
	}

	private PositionSide getPositionSide() {
		PositionSide ps = PositionSide.DEFAULT;
		int index = list.size() - 1;
		Klines current = list.get(index);
		Klines parent = list.get(index - 1);
		if(verifyLong(current, parent)) {
			ps = PositionSide.LONG;
		} else if(verifyShort(current, parent)) {
			ps = PositionSide.SHORT;
		}
		return ps;
	}
	
	private boolean verifyLong(Klines current, Klines parent) {
		return PriceUtil.verifyPowerful_v28(current, parent);
	}
	
	private boolean verifyShort(Klines current, Klines parent) {
		return PriceUtil.verifyDecliningPrice_v28(current, parent);
	}
	
	private boolean verifyHigh(Klines current, Klines parent, Klines next) {
		return PriceUtil.verifyDecliningPrice_v28(current, parent) && PriceUtil.verifyPowerful_v28(parent, next);
	}
	
	private boolean verifyLow(Klines current, Klines parent, Klines next) {
		return PriceUtil.verifyPowerful_v28(current, parent) && PriceUtil.verifyDecliningPrice_v28(parent, next);
	}
	
	private void addPrices(OpenPrice price) {
		if(fibInfo != null && FibCode.FIB4_618.gt(fibInfo.getFibCode(price.getPrice()))) {
			if(!PriceUtil.contains(openPrices, price)) {
				openPrices.add(price);
			}
		}
	}
}