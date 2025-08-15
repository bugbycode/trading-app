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
import com.util.PriceComparator;
import com.util.PriceUtil;

public class FibInfoFactoryImplForEma implements FibInfoFactory {
	
	private List<Klines> list;
	
	private List<Klines> fibAfterKlines;
	
	private FibInfo fibInfo;
	
	private List<Klines> list_15m;//十五分钟级别k线 用于补充回撤之后的k线信息
	
	private Klines start = null;
	
	private Klines end = null;
	
	private List<Double> openPrices;
	
	public FibInfoFactoryImplForEma(List<Klines> list, List<Klines> list_15m) {
		this.list = new ArrayList<Klines>();
		this.list_15m = new ArrayList<Klines>();
		this.openPrices = new ArrayList<Double>();
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
	public List<Double> getOpenPrices() {
		return openPrices;
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

	
	private void init() {
		if(CollectionUtils.isEmpty(list) || list.size() < 99 || CollectionUtils.isEmpty(list_15m)) {
			return;
		}
		
		KlinesComparator kc = new KlinesComparator(SortType.ASC);
		this.list.sort(kc);
		
		PriceUtil.calculateEMA_7_25_99(list);
		PriceUtil.calculateMACD(list);
		
		this.openPrices.clear();
		
		PositionSide ps = getPositionSide();
		
		Klines fourth = null;
		Klines third = null;
		Klines second = null;
		Klines first = null;
		
		for(int index = list.size() - 1; index > 0; index--) {
			Klines current = list.get(index);
			if(ps == PositionSide.SHORT) {//high - low - high - low
				if(fourth == null) {
					if(verifyHigh(current)) {
						fourth = current;
					}
				} else if(third == null) {
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
			} else if(ps == PositionSide.LONG) { // low - high - low - high
				if(fourth == null) {
					if(verifyLow(current)) {
						fourth = current;
					}
				} else if(third == null) {
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
		
		if(first == null || second == null || third == null || fourth == null) {
			return;
		}
		
		List<Klines> firstSubList = PriceUtil.subList(first, third, list);
		List<Klines> secondSubList = PriceUtil.subList(second, fourth, list);
		
		if(ps == PositionSide.LONG) {
			start = PriceUtil.getMinPriceKLine(firstSubList);
			end = PriceUtil.getMaxPriceKLine(secondSubList);
			if(!(start == null || end == null)) {
				this.fibInfo = new FibInfo(start.getLowPriceDoubleValue(), end.getHighPriceDoubleValue(), start.getDecimalNum(), FibLevel.LEVEL_1);
			}
		} else if(ps == PositionSide.SHORT) {
			start = PriceUtil.getMaxPriceKLine(firstSubList);
			end = PriceUtil.getMinPriceKLine(secondSubList);
			if(!(start == null || end == null)) {
				this.fibInfo = new FibInfo(start.getHighPriceDoubleValue(), end.getLowPriceDoubleValue(), start.getDecimalNum(), FibLevel.LEVEL_1);
			}
		}
		
		if(this.fibInfo == null) {
			return;
		}
		
		this.resetFibLevel();

		QuotationMode mode = this.fibInfo.getQuotationMode();

		//Klines fibAfterFlag = PriceUtil.getAfterKlines(fourth, this.list);
		//if(fibAfterFlag != null) {
			this.fibAfterKlines.addAll(PriceUtil.subList(fourth, this.list));
		//}
		
		Klines fibEnd = null;
		
		if(!CollectionUtils.isEmpty(fibAfterKlines)) {
			List<MarketSentiment> msList = new ArrayList<MarketSentiment>();
			for(int index = 0; index < fibAfterKlines.size() - 1; index++) {
				Klines parent = fibAfterKlines.get(index);
				Klines current = fibAfterKlines.get(index + 1);
				if((mode == QuotationMode.LONG && PriceUtil.verifyPowerful_v11(current, parent))
						|| (mode == QuotationMode.SHORT && PriceUtil.verifyDecliningPrice_v11(current, parent))) {
					/*addPrices(current.getBodyHighPriceDoubleValue());
					addPrices(current.getBodyLowPriceDoubleValue());
					fibEnd = current;
					break;*/
					msList.add(new MarketSentiment(current));
				}
			}
			//开始处理开仓点位
			if(!CollectionUtils.isEmpty(msList)) {
				MarketSentiment high = PriceUtil.getMaxMarketSentiment(msList);
				MarketSentiment low = PriceUtil.getMinMarketSentiment(msList);
				if(mode == QuotationMode.LONG) {
					fibEnd = low.getLow();
				} else {
					fibEnd = high.getHigh();
				}
				addPrices(fibEnd.getBodyHighPriceDoubleValue());
				addPrices(fibEnd.getBodyLowPriceDoubleValue());
			}
		}
		
		this.fibAfterKlines.clear();
		
		if(fibEnd != null) {
			Klines fibAfterFlag = PriceUtil.getAfterKlines(fibEnd, this.list_15m);
			if(fibAfterFlag != null) {
				this.fibAfterKlines.addAll(PriceUtil.subList(fibAfterFlag, this.list_15m));
				this.fibInfo.setFibAfterKlines(fibAfterKlines);
			}
		}
		
		/*
		Klines fibAfterFlag = PriceUtil.getAfterKlines(end, this.list_15m);
		if(fibAfterFlag != null) {
			this.fibAfterKlines = PriceUtil.subList(fibAfterFlag, this.list_15m);
			this.fibInfo.setFibAfterKlines(fibAfterKlines);
		}*/
		
		if(mode == QuotationMode.LONG) {
			this.openPrices.sort(new PriceComparator(SortType.DESC));
		} else {
			this.openPrices.sort(new PriceComparator(SortType.ASC));
		}
	}
	
	private PositionSide getPositionSide() {
		PositionSide ps = PositionSide.DEFAULT;
		Klines last = PriceUtil.getLastKlines(list);
		if(verifyLong(last)) {
			ps = PositionSide.LONG;
		} else if(verifyShort(last)) {
			ps = PositionSide.SHORT;
		}
		return ps;
	}
	
	private boolean verifyLong(Klines current) {
		return current.getEma7() > current.getEma25() && current.getEma25() > 0;
	}
	
	private boolean verifyShort(Klines current) {
		return current.getEma7() < current.getEma25() && current.getEma25() > 0;
	}
	
	private boolean verifyHigh(Klines k) {
		return k.getEma7() > k.getEma25() && k.getEma25() > 0 && k.getMacd() > 0;
	}
	
	private boolean verifyLow(Klines k) {
		return k.getEma7() < k.getEma25() && k.getEma25() > 0 && k.getMacd() < 0;
	}
	
	private FibCode getFibCode() {
		FibCode result = FibCode.FIB1;
		if(this.fibInfo != null) {
			Klines last = PriceUtil.getLastKlines(list);
			result = this.fibInfo.getFibCode(last.getEma99());
		}
		return result;
	}
	
	private void resetFibLevel() {
		if(this.fibInfo != null) {
			FibCode levelFibCode = getFibCode();
			FibLevel level = FibLevel.valueOf(levelFibCode);
			this.fibInfo = new FibInfo(this.fibInfo.getFibValue(FibCode.FIB1), this.fibInfo.getFibValue(FibCode.FIB0), this.fibInfo.getDecimalPoint(), level);
			
		}
	}
	
	public void addPrices(double price) {
		if(!PriceUtil.contains(openPrices, price)) {
			openPrices.add(price);
		}
	}
}
