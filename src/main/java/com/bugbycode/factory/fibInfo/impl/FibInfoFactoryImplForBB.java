package com.bugbycode.factory.fibInfo.impl;

import java.util.ArrayList;
import java.util.List;

import org.springframework.util.CollectionUtils;

import com.bugbycode.factory.fibInfo.FibInfoFactory;
import com.bugbycode.module.FibCode;
import com.bugbycode.module.FibInfo;
import com.bugbycode.module.FibLevel;
import com.bugbycode.module.Klines;
import com.bugbycode.module.MarketSentiment;
import com.bugbycode.module.QuotationMode;
import com.bugbycode.module.SortType;
import com.bugbycode.module.trading.PositionSide;
import com.util.KlinesComparator;
import com.util.PriceUtil;

/**
 * 斐波那契回撤指标实现类 以BB %B值强弱获取开仓点位
 */
public class FibInfoFactoryImplForBB implements FibInfoFactory {

	private List<Klines> list_1d;
	
	private List<Klines> list;
	
	private List<Klines> fibAfterKlines;
	
	private FibInfo fibInfo;
	
	private List<Klines> list_15m;//十五分钟级别k线 用于补充回撤之后的k线信息
	
	private Klines start = null;
	
	private Klines end = null;
	
	private List<Double> openPrices;
	
	public FibInfoFactoryImplForBB(List<Klines> list_1d, List<Klines> list, List<Klines> list_15m) {
		this.list_1d = new ArrayList<Klines>();
		this.list = new ArrayList<Klines>();
		this.list_15m = new ArrayList<Klines>();
		this.openPrices = new ArrayList<Double>();
		this.fibAfterKlines = new ArrayList<Klines>();
		if(!CollectionUtils.isEmpty(list_1d)) {
			this.list_1d.addAll(list_1d);
		}
		if(!CollectionUtils.isEmpty(list_15m)) {
			this.list_15m.addAll(list_15m);
		}
		if(!CollectionUtils.isEmpty(list)) {
			this.list.addAll(list);
			this.init();
		}
	}
	
	@Override
	public boolean isLong() {
		boolean result = false;
		if(fibInfo != null && fibInfo.getQuotationMode() == QuotationMode.LONG) {
			result = true;
		}
		return result;
	}
	
	@Override
	public boolean isShort() {
		boolean result = false;
		if(fibInfo != null && fibInfo.getQuotationMode() == QuotationMode.SHORT) {
			result = true;
		}
		return result;
	}
	
	@Override
	public FibInfo getFibInfo() {
		return this.fibInfo;
	}

	@Override
	public List<Klines> getFibAfterKlines() {
		return fibAfterKlines;
	}

	@Override
	public List<Double> getOpenPrices() {
		return openPrices;
	}

	private void init() {
		if(CollectionUtils.isEmpty(list) || list.size() < 99 || CollectionUtils.isEmpty(list_15m) || CollectionUtils.isEmpty(list_1d) || list_1d.size() < 35) {
			return;
		}
		
		KlinesComparator kc = new KlinesComparator(SortType.ASC);
		this.list_15m.sort(kc);
		this.list.sort(kc);
		this.list_1d.sort(kc);
		
		PriceUtil.calculateEMA_7_25_99(list);
		PriceUtil.calculateMACD(list);
		PriceUtil.calculateAllBBPercentB(list);
		PriceUtil.calculateMACD(list_1d);
		
		this.openPrices.clear();
		this.fibAfterKlines.clear();
		
		PositionSide ps = getPositionSide();
		
		Klines third = null;
		Klines second = null;
		Klines first = null;
		
		for(int index = list.size() - 1; index > 0; index--) {
			Klines current = list.get(index);
			if(ps == PositionSide.SHORT) {//low - high - low
				if(third == null) {
					if(verifyLow(current)) {
						third = current;
					}
				} else if(second == null) {
					if(verifyHigh(current)) {
						second = current;
					}
				} else if(first == null) {
					if(verifyLow(current)) {
						first = current;
						break;
					}
				}
			} else if(ps == PositionSide.LONG) { // high - low - high
				if(third == null) {
					if(verifyHigh(current)) {
						third = current;
					}
				} else if(second == null) {
					if(verifyLow(current)) {
						second = current;
					}
				} else if(first == null) {
					if(verifyHigh(current)) {
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
		
		Klines startAfterFlag = null;
		if(ps == PositionSide.SHORT) {
			start = PriceUtil.getMaxPriceKLine(firstSubList);
			startAfterFlag = PriceUtil.getAfterKlines(start, firstSubList);
			if(startAfterFlag == null) {
				startAfterFlag = start;
			}
			secondSubList = PriceUtil.subList(startAfterFlag, list);
			end = PriceUtil.getMinPriceKLine(secondSubList);
			this.fibInfo = new FibInfo(start.getHighPriceDoubleValue(), end.getLowPriceDoubleValue(), start.getDecimalNum(), FibLevel.LEVEL_1);
		} else if(ps == PositionSide.LONG) {
			start = PriceUtil.getMinPriceKLine(firstSubList);
			startAfterFlag = PriceUtil.getAfterKlines(start, firstSubList);
			if(startAfterFlag == null) {
				startAfterFlag = start;
			}
			secondSubList = PriceUtil.subList(startAfterFlag, list);
			end = PriceUtil.getMaxPriceKLine(secondSubList);
			this.fibInfo = new FibInfo(start.getLowPriceDoubleValue(), end.getHighPriceDoubleValue(), start.getDecimalNum(), FibLevel.LEVEL_1);
		}
		
		if(this.fibInfo == null) {
			return;
		}

		QuotationMode mode = this.fibInfo.getQuotationMode();
		
		Klines fibAfterFlag = PriceUtil.getAfterKlines(end, this.list);
		if(fibAfterFlag != null) {
			this.fibAfterKlines.addAll(PriceUtil.subList(fibAfterFlag, list));
		}

		//List<MarketSentiment> msList = new ArrayList<MarketSentiment>();
		MarketSentiment ms = null;
		Klines current = null;
		//Klines parent = null;
		//Klines next = null;
		List<Klines> sub_list = null;
		Klines fibEnd = null;
		FibCode openCode = null;
		for(int index = this.fibAfterKlines.size() - 1; index > 0; index--) {
			current = this.fibAfterKlines.get(index);
			if((mode == QuotationMode.LONG && current.getBbPercentB() <= 0) 
					|| (mode == QuotationMode.SHORT && current.getBbPercentB() >= 1)) {
				sub_list = PriceUtil.subList(fibAfterFlag, current, fibAfterKlines);
				ms = new MarketSentiment(sub_list);
				if(mode == QuotationMode.LONG) {
					fibEnd = ms.getLow();
					openCode = this.fibInfo.getFibCode(fibEnd.getLowPriceDoubleValue());
				} else {
					fibEnd = ms.getHigh();
					openCode = this.fibInfo.getFibCode(fibEnd.getHighPriceDoubleValue());
				}
				break;
			}
		}
		
		if(openCode != null) {
			addPrices(this.fibInfo.getFibValue(openCode));
		}
		
		this.fibAfterKlines.clear();
		
		//开始处理开仓点位
		
		if(fibEnd != null) {
			fibAfterFlag = PriceUtil.getAfterKlines(fibEnd, this.list_15m);
			
			if(fibAfterFlag != null) {
				
				this.fibAfterKlines.addAll(PriceUtil.subList(fibAfterFlag, this.list_15m));
				this.fibInfo.setFibAfterKlines(fibAfterKlines);
			}
		}
		
		this.resetFibLevel();
	}
	
	private PositionSide getPositionSide() {
		PositionSide ps = PositionSide.DEFAULT;
		/*Klines last = PriceUtil.getLastKlines(list);
		if(verifyShort(last)) {
			ps = PositionSide.SHORT;
		} else if(verifyLong(last)) {
			ps = PositionSide.LONG;
		}*/
		int size = list_1d.size();
		if(size > 1) {
			int index = size - 1;
			Klines current = list_1d.get(index);
			Klines parent = list_1d.get(index - 1);
			if(verifyShort(current, parent)) {
				ps = PositionSide.SHORT;
			} else if(verifyLong(current, parent)) {
				ps = PositionSide.LONG;
			}
		}
		return ps;
	}
	
	private boolean verifyLong(Klines current, Klines parent) {
		return current.getMacd() > parent.getMacd();
	}
	
	private boolean verifyShort(Klines current, Klines parent) {
		return current.getMacd() < parent.getMacd();
	}
	
	/*
	private boolean verifyLong(Klines current) {
		return current.getEma7() < current.getEma25();
	}
	
	private boolean verifyShort(Klines current) {
		return current.getEma7() > current.getEma25();
	}*/
	
	private boolean verifyHigh(Klines k) {
		return k.getEma7() > k.getEma25() && k.getEma25() > 0 && k.getMacd() > 0;
	}
	
	private boolean verifyLow(Klines k) {
		return k.getEma7() < k.getEma25() && k.getEma25() > 0 && k.getMacd() < 0;
	}
	
	private FibCode getFibCode() {
		FibCode result = FibCode.FIB1;
		if(this.fibInfo != null) {
			
			if(!CollectionUtils.isEmpty(openPrices)) {
				double price = openPrices.get(0);
				result = this.fibInfo.getFibCode(price);
			}
			
			if(result == FibCode.FIB66) {
				result = FibCode.FIB618;
			} else if(result == FibCode.FIB0) {
				result = FibCode.FIB236;
			}
		}
		return result;
	}
	
	private void resetFibLevel() {
		if(this.fibInfo != null) {
			FibCode levelFibCode = getFibCode();
			FibLevel level = FibLevel.valueOf(levelFibCode);
			this.fibInfo = new FibInfo(this.fibInfo.getFibValue(FibCode.FIB1), this.fibInfo.getFibValue(FibCode.FIB0), this.fibInfo.getDecimalPoint(), level);
			this.fibInfo.setFibAfterKlines(fibAfterKlines);
		}
	}
	
	private void addPrices(double price) {
		if(!PriceUtil.contains(openPrices, price)) {
			openPrices.add(price);
		}
	}
}