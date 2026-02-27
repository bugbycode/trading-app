package com.bugbycode.factory.area.impl;

import java.util.ArrayList;
import java.util.List;

import org.springframework.util.CollectionUtils;

import com.bugbycode.factory.area.AreaFactory;
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
import com.util.PriceUtil;

public class AreaFactoryImpl implements AreaFactory {

	private List<Klines> list_1d;
	
	private List<Klines> list;
	
	private List<Klines> list_15m;
	
	private List<Klines> fibAfterKlines;
	
	private PositionSide ps;
	
	private List<OpenPrice> openPrices;
	
	private FibInfo fibInfo;
	
	private Klines start = null;
	
	private Klines end = null;
	
	public AreaFactoryImpl(List<Klines> list_1d, List<Klines> list, List<Klines> list_15m) {
		this.ps = PositionSide.DEFAULT;
		this.list_1d = new ArrayList<Klines>();
		this.list = new ArrayList<Klines>();
		this.list_15m = new ArrayList<Klines>();
		this.fibAfterKlines = new ArrayList<Klines>();
		this.openPrices = new ArrayList<OpenPrice>();
		if(!CollectionUtils.isEmpty(list_1d)) {
			this.list_1d.addAll(list_1d);
		}
		if(!CollectionUtils.isEmpty(list)) {
			this.list.addAll(list);
		}
		if(!CollectionUtils.isEmpty(list_15m)) {
			this.list_15m.addAll(list_15m);
		}
		
		this.init();
	}
	
	private void init() {
		
		if(CollectionUtils.isEmpty(list_1d) || list.size() < 99 || CollectionUtils.isEmpty(this.list_15m)) {
			return;
		}

		this.list.sort(new KlinesComparator(SortType.ASC));
		this.list_15m.sort(new KlinesComparator(SortType.ASC));
		this.list_1d.sort(new KlinesComparator(SortType.ASC));
		
		PriceUtil.calculateMACD(list);
		
		Klines last_1d = PriceUtil.getLastKlines(list_1d);
		double price_1d = last_1d.getClosePriceDoubleValue();
		Klines last_15m = PriceUtil.getLastKlines(list_15m);
		
		Klines fibAfterKline = PriceUtil.getAfterKlines(last_1d, this.list_15m);
		if(fibAfterKline != null) {
			this.fibAfterKlines = PriceUtil.subList(fibAfterKline, this.list_15m);
		}
		
		this.list_15m = new ArrayList<Klines>();
		
		if(!CollectionUtils.isEmpty(this.fibAfterKlines)) {
			this.list_15m.addAll(this.fibAfterKlines);
		}
		
		if(CollectionUtils.isEmpty(this.list_15m)) {
			return;
		}

		ps = getPositionSide();
		
		PositionSide fib_ps = getFibPositionSide();
		
		Klines third = null;
		Klines second = null;
		Klines first = null;
		
		for(int index = list.size() - 1; index > 0; index--) {
			Klines current = list.get(index);
			if(fib_ps == PositionSide.SHORT) {//low - high - low
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
			} else if(fib_ps == PositionSide.LONG) { // high - low - high
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
		if(fib_ps == PositionSide.SHORT) {
			start = PriceUtil.getMaxPriceKLine(firstSubList);
			startAfterFlag = PriceUtil.getAfterKlines(start, firstSubList);
			if(startAfterFlag == null) {
				startAfterFlag = start;
			}
			secondSubList = PriceUtil.subList(startAfterFlag, third, list);
			end = PriceUtil.getMinPriceKLine(secondSubList);
			this.fibInfo = new FibInfo(start.getHighPriceDoubleValue(), end.getLowPriceDoubleValue(), start.getDecimalNum(), FibLevel.LEVEL_0);
		} else if(fib_ps == PositionSide.LONG) {
			start = PriceUtil.getMinPriceKLine(firstSubList);
			startAfterFlag = PriceUtil.getAfterKlines(start, firstSubList);
			if(startAfterFlag == null) {
				startAfterFlag = start;
			}
			secondSubList = PriceUtil.subList(startAfterFlag, third, list);
			end = PriceUtil.getMaxPriceKLine(secondSubList);
			this.fibInfo = new FibInfo(start.getLowPriceDoubleValue(), end.getHighPriceDoubleValue(), start.getDecimalNum(), FibLevel.LEVEL_0);
		}
		
		if(this.fibInfo == null) {
			return;
		}
		
		double stopLossLimit = 0;
		//QuotationMode mode = this.fibInfo.getQuotationMode();
		//QuotationMode ps_mode = isLong() ? QuotationMode.LONG : QuotationMode.SHORT;
		if(isLong()) {
			stopLossLimit = last_15m.getLowPriceDoubleValue();
		} else {
			stopLossLimit = last_15m.getHighPriceDoubleValue();
		}
		
		FibCode openCode = this.fibInfo.getFibCode_v2(price_1d);
		addPrices(openCode, price_1d, stopLossLimit);
	}
	
	private PositionSide getPositionSide() {
		Klines last_1d = PriceUtil.getLastKlines(list_1d);
		Klines last_15m = PriceUtil.getLastKlines(list_15m);
		double price_15m = last_15m.getClosePriceDoubleValue();
		double price_1d = last_1d.getClosePriceDoubleValue();
		
		PositionSide ps = PositionSide.DEFAULT;
		
		if(PriceUtil.isBreachLong(last_15m, price_1d) || price_15m > price_1d) {
			ps = PositionSide.LONG;
		} else if(PriceUtil.isBreachShort(last_15m, price_1d) || price_15m < price_1d) {
			ps = PositionSide.SHORT;
		}
		
		return ps;
	}
	
	private PositionSide getFibPositionSide() {
		PositionSide ps = PositionSide.DEFAULT;
		Klines last = PriceUtil.getLastKlines(list);
		if(verifyShort(last)) {
			ps = PositionSide.SHORT;
		} else if(verifyLong(last)) {
			ps = PositionSide.LONG;
		}
		return ps;
	}
	
	private boolean verifyLong(Klines current) {
		return current.getDea() < 0;
	}
	
	private boolean verifyShort(Klines current) {
		return current.getDea() > 0;
	}
	
	private boolean verifyHigh(Klines k) {
		return k.getMacd() > 0 && k.getDea() > 0;
	}
	
	private boolean verifyLow(Klines k) {
		return k.getMacd() < 0 && k.getDea() < 0;
	}

	private void addPrices(OpenPrice price) {
		if(!PriceUtil.contains(openPrices, price) && price.getCode().gte(FibCode.FIB236)) {
			openPrices.add(price);
		}
	}
	
	private void addPrices(FibCode openCode,double price, double stopLoss) {
		FibCode firstCode = getFirstCode(openCode);
		FibCode seconCode = getSecondCode(openCode);
		addPrices(new OpenPriceDetails(openCode, price, stopLoss, fibInfo.getFibValue(firstCode), fibInfo.getFibValue(seconCode)));
	}
	
	private FibCode getFirstCode(FibCode openCode) {
		
		QuotationMode mode = this.fibInfo.getQuotationMode();
		int index = 0;
		FibCode[] codes = FibCode.values();
		for(int offset = 0; offset < codes.length; offset++) {
			if(openCode == codes[offset]) {
				if((mode == QuotationMode.LONG && isLong()) || (mode == QuotationMode.SHORT && isShort())) {
					index = offset + 1;
					if(openCode == FibCode.FIB786) {
						index = offset + 2;
					}
				} else if((mode == QuotationMode.LONG && isShort()) || (mode == QuotationMode.SHORT && isLong())) {
					index = offset - 2;
					if(openCode == FibCode.FIB618) {
						index = offset - 3;
					}
				}
				break;
			}
		}
		
		if(index < 0) {
			index = 0;
		} else if(index >= codes.length) {
			index = codes.length - 1;
		}
		
		return codes[index];
	}
	
	private FibCode getSecondCode(FibCode openCode) {
		
		QuotationMode mode = this.fibInfo.getQuotationMode();
		int index = 0;
		FibCode[] codes = FibCode.values();
		for(int offset = 0; offset < codes.length; offset++) {
			if(openCode == codes[offset]) {
				if((mode == QuotationMode.LONG && isLong()) || (mode == QuotationMode.SHORT && isShort())) {
					index = offset + 2;
					if(openCode == FibCode.FIB786) {
						index = offset + 3;
					}
				} else if((mode == QuotationMode.LONG && isShort()) || (mode == QuotationMode.SHORT && isLong())) {
					index = offset - 3;
					if(openCode == FibCode.FIB618) {
						index = offset - 4;
					}
				}
				break;
			}
		}

		if(index < 0) {
			index = 0;
		} else if(index >= codes.length) {
			index = codes.length - 1;
		}
		
		return codes[index];
	}
	
	@Override
	public List<OpenPrice> getOpenPrices() {
		return this.openPrices;
	}

	@Override
	public List<Klines> getFibAfterKlines() {
		return this.fibAfterKlines;
	}

	@Override
	public boolean isLong() {
		return this.ps == PositionSide.LONG;
	}

	@Override
	public boolean isShort() {
		return this.ps == PositionSide.SHORT;
	}

}
