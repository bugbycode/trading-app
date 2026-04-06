package com.bugbycode.factory.area.impl;

import java.util.ArrayList;
import java.util.List;

import org.springframework.util.CollectionUtils;

import com.bugbycode.factory.area.AreaFactory;
import com.bugbycode.module.FibCode;
import com.bugbycode.module.Klines;
import com.bugbycode.module.QuotationMode;
import com.bugbycode.module.SortType;
import com.bugbycode.module.price.OpenPrice;
import com.bugbycode.module.price.impl.OpenPriceDetails;
import com.bugbycode.module.trading.PositionSide;
import com.util.KlinesComparator;
import com.util.PriceUtil;

public class AreaFactoryImpl implements AreaFactory {

	private List<Klines> list;
	
	private List<Klines> list_15m;
	
	private List<Klines> list_trend;
	
	private Klines list_trend_last;
	
	private List<Klines> fibAfterKlines;
	
	private PositionSide ps;
	
	private List<OpenPrice> openPrices;
	
	public AreaFactoryImpl(List<Klines> list_trend, List<Klines> list, List<Klines> list_15m) {
		this.ps = PositionSide.DEFAULT;
		this.list = new ArrayList<Klines>();
		this.list_15m = new ArrayList<Klines>();
		this.list_trend = new ArrayList<Klines>();
		this.fibAfterKlines = new ArrayList<Klines>();
		this.openPrices = new ArrayList<OpenPrice>();
		if(!CollectionUtils.isEmpty(list)) {
			this.list.addAll(list);
		}
		if(!CollectionUtils.isEmpty(list_trend)) {
			this.list_trend.addAll(list_trend);
		}
		if(!CollectionUtils.isEmpty(list_15m)) {
			this.list_15m.addAll(list_15m);
		}
		
		this.init();
	}
	
	public AreaFactoryImpl(Klines list_trend_last, List<Klines> list, List<Klines> list_15m) {
		this.ps = PositionSide.DEFAULT;
		this.list = new ArrayList<Klines>();
		this.list_15m = new ArrayList<Klines>();
		this.list_trend = new ArrayList<Klines>();
		this.fibAfterKlines = new ArrayList<Klines>();
		this.openPrices = new ArrayList<OpenPrice>();
		if(!CollectionUtils.isEmpty(list)) {
			this.list.addAll(list);
		}
		this.list_trend_last = list_trend_last;
		if(!CollectionUtils.isEmpty(list_15m)) {
			this.list_15m.addAll(list_15m);
		}
		
		this.init();
	}
	
	private void init() {
		
		if(list.size() < 3 || CollectionUtils.isEmpty(this.list_15m)) {
			return;
		}

		this.ps = PositionSide.DEFAULT;
		
		this.list.sort(new KlinesComparator(SortType.ASC));
		this.list_15m.sort(new KlinesComparator(SortType.ASC));
		
		if(list_trend_last == null) {
			if(CollectionUtils.isEmpty(list_trend)){
				return;
			}
			this.list_trend.sort(new KlinesComparator(SortType.ASC));
			list_trend_last = PriceUtil.getLastKlines(list_trend);
		}
		
		double hitPrice = list_trend_last.getClosePriceDoubleValue();
		
		Klines last = null;
		
		Klines list_trend_last_after = PriceUtil.getAfterKlines(list_trend_last, list);
		if(list_trend_last_after == null) {
			return;
		}
		
		for(int index = list.size() - 1; index > 2; index--) {
			Klines current = list.get(index);
			if(current.lt(list_trend_last_after)) {
				break;
			}
			if(PriceUtil.isBreachLong(current, hitPrice)) {
				last = current;
				this.ps = PositionSide.LONG;
				break;
			} else if(PriceUtil.isBreachShort(current, hitPrice)) {
				last = current;
				this.ps = PositionSide.SHORT;
				break;
			}
		}
		
		if(this.ps == PositionSide.DEFAULT || last == null) {
			this.ps = list_trend_last_after.isRise() ? PositionSide.LONG : PositionSide.SHORT;
			last = list_trend_last_after;
		}
		
		QuotationMode mode = (ps == PositionSide.LONG) ? QuotationMode.LONG : QuotationMode.SHORT;
		
		double h = list_trend_last.getHighPriceDoubleValue();
		double l = list_trend_last.getLowPriceDoubleValue();
		//double lt_bh = list_trend_last.getBodyHighPriceDoubleValue();
		//double lt_bl = list_trend_last.getBodyLowPriceDoubleValue();
		
		//double c = last.getClosePriceDoubleValue();
		double bh = last.getBodyHighPriceDoubleValue();
		double bl = last.getBodyLowPriceDoubleValue();
		
		double take = (h - l) * 0.786;
		
		double stopLoss = mode == QuotationMode.LONG ? last.getLowPriceDoubleValue() : last.getHighPriceDoubleValue();
		
		double firstTakeProfit = 0; 
		double secondTakeProfit = 0;
		
		double price = 0;
		if(isLong()) {
			firstTakeProfit =Double.valueOf( PriceUtil.formatDoubleDecimal(bh + (take / 2), last.getDecimalNum()) );
			secondTakeProfit = Double.valueOf( PriceUtil.formatDoubleDecimal(bh + take, last.getDecimalNum()) );
			price = bh;
		} else if(isShort()) {
			firstTakeProfit =Double.valueOf( PriceUtil.formatDoubleDecimal(bl - (take / 2), last.getDecimalNum()) );
			secondTakeProfit = Double.valueOf( PriceUtil.formatDoubleDecimal(bl - take, last.getDecimalNum()) );
			
			if(firstTakeProfit <= 0) {
				firstTakeProfit = l;
			}
			
			if(secondTakeProfit <= 0) {
				secondTakeProfit = l;
			}
			
			price = bl;
		}
		
		if(price > 0) {
			addPrices(new OpenPriceDetails(FibCode.FIB618, price, stopLoss, firstTakeProfit, secondTakeProfit));
		}
		
		Klines fibAfterFlag = PriceUtil.getAfterKlines(last, this.list_15m);
		if(fibAfterFlag != null) {
			this.fibAfterKlines.addAll(PriceUtil.subList(fibAfterFlag, this.list_15m));
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