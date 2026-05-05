package com.bugbycode.factory.area.impl;

import java.util.ArrayList;
import java.util.List;

import org.springframework.util.CollectionUtils;

import com.bugbycode.factory.area.AreaFactory;
import com.bugbycode.module.FibCode;
import com.bugbycode.module.FibInfo;
import com.bugbycode.module.FibLevel;
import com.bugbycode.module.Klines;
import com.bugbycode.module.SortType;
import com.bugbycode.module.binance.AutoTradeType;
import com.bugbycode.module.price.OpenPrice;
import com.bugbycode.module.price.impl.OpenPriceDetails;
import com.bugbycode.module.trading.PositionSide;
import com.util.KlinesComparator;
import com.util.PriceUtil;

/**
 * 盘整区交易
 */
public class AreaFactoryImpl implements AreaFactory {

	private List<Klines> list;
	
	private List<Klines> list_15m;
	
	private List<Klines> fibAfterKlines;
	
	private PositionSide ps;
	
	private List<OpenPrice> openPrices;
	
	public AreaFactoryImpl(List<Klines> list, List<Klines> list_15m) {
		this.ps = PositionSide.DEFAULT;
		this.list = new ArrayList<Klines>();
		this.list_15m = new ArrayList<Klines>();
		this.fibAfterKlines = new ArrayList<Klines>();
		this.openPrices = new ArrayList<OpenPrice>();
		if(!CollectionUtils.isEmpty(list)) {
			this.list.addAll(list);
		}
		if(!CollectionUtils.isEmpty(list_15m)) {
			this.list_15m.addAll(list_15m);
		}
		
		this.init();
	}
	
	private void init() {
		
		if(CollectionUtils.isEmpty(list) || CollectionUtils.isEmpty(this.list_15m)) {
			return;
		}

		this.ps = PositionSide.DEFAULT;
		
		this.list.sort(new KlinesComparator(SortType.ASC));
		this.list_15m.sort(new KlinesComparator(SortType.ASC));
		
		Klines last = PriceUtil.getLastKlines(list);
		double o = last.getOpenPriceDoubleValue();
		double c = last.getClosePriceDoubleValue();
		double l = last.getLowPriceDoubleValue();
		double h = last.getHighPriceDoubleValue();
		
		double startPrice = c;
		double endPrice = o; 
		
		if(last.isRise()) {
			endPrice = l;
		} else {
			endPrice = h;
		}

		int decimalPoint = last.getDecimalNum();
		
		FibInfo fibInfo = new FibInfo(startPrice, endPrice, decimalPoint, FibLevel.LEVEL_0);
		
		Klines last_after = PriceUtil.getAfterKlines(last, list_15m);
		if(last_after == null) {
			return;
		}
		
		Klines hit_k = null;
		for(int index = list_15m.size() - 1; index >= 0; index--) {
			Klines current = list_15m.get(index);
			if(PriceUtil.isBreachLong(current, c)) {
				this.ps = PositionSide.LONG;
				hit_k = current;
				break;
			} else if(PriceUtil.isBreachShort(current, c)) {
				this.ps = PositionSide.SHORT;
				hit_k = current;
				break;
			}
			if(current.lte(last_after)) {
				break;
			}
		}
		
		if(this.ps == PositionSide.DEFAULT || hit_k == null) {
			hit_k = last_after;
			if(hit_k.isRise()) {
				this.ps = PositionSide.LONG;
			} else {
				this.ps = PositionSide.SHORT;
			}
		}
		
		double firstTakeProfit = fibInfo.getFibValue(FibCode.FIB1_618); 
		double secondTakeProfit = fibInfo.getFibValue(FibCode.FIB2);
		
		double stopLoss = isLong() ? hit_k.getLowPriceDoubleValue() : hit_k.getHighPriceDoubleValue(); 
		double op = isLong() ? hit_k.getBodyHighPriceDoubleValue() : hit_k.getBodyLowPriceDoubleValue();
		
		if((isLong() && fibInfo.isLong()) || (isShort() && fibInfo.isShort())) {
			firstTakeProfit = fibInfo.getFibValue(FibCode.FIB618);
			secondTakeProfit = fibInfo.getFibValue(FibCode.FIB5);
		}
		
		FibInfo childFibInfo = new FibInfo(secondTakeProfit, c, decimalPoint);
		firstTakeProfit = childFibInfo.getFibValue(FibCode.FIB618);
		secondTakeProfit = childFibInfo.getFibValue(FibCode.FIB786);
		
		if(firstTakeProfit <= 0 || secondTakeProfit <= 0) {
			return;
		}
		
		addPrices(new OpenPriceDetails(FibCode.FIB1, op, stopLoss, firstTakeProfit, secondTakeProfit, AutoTradeType.AREA_INDEX));
		
		Klines fibAfter = PriceUtil.getAfterKlines(hit_k, list_15m);
		if(fibAfter != null) {
			this.fibAfterKlines = PriceUtil.subList(fibAfter, list_15m);
		}
		
	}
	
	private void addPrices(OpenPrice price) {
		if(!PriceUtil.contains(openPrices, price) && price.getCode().gte(FibCode.FIB236)) {
			openPrices.add(price);
		}
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