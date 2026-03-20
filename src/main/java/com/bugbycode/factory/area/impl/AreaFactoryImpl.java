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

	private List<Klines> list_trend;
	
	private List<Klines> list;
	
	private List<Klines> list_15m;
	
	private List<Klines> fibAfterKlines;
	
	private PositionSide ps;
	
	private List<OpenPrice> openPrices;
	
	public AreaFactoryImpl(List<Klines> list, List<Klines> list_trend, List<Klines> list_15m) {
		this.ps = PositionSide.DEFAULT;
		this.list_trend = new ArrayList<Klines>();
		this.list = new ArrayList<Klines>();
		this.list_15m = new ArrayList<Klines>();
		this.fibAfterKlines = new ArrayList<Klines>();
		this.openPrices = new ArrayList<OpenPrice>();
		if(!CollectionUtils.isEmpty(list_trend)) {
			this.list_trend.addAll(list_trend);
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
		
		if(list_trend.size() < 2 || list.size() < 50 || CollectionUtils.isEmpty(this.list_15m)) {
			return;
		}
		
		this.list.sort(new KlinesComparator(SortType.ASC));
		this.list_15m.sort(new KlinesComparator(SortType.ASC));
		this.list_trend.sort(new KlinesComparator(SortType.ASC));
		
		this.ps = getPositionSide();
		
		if(this.ps == PositionSide.DEFAULT) {
			return;
		}
		
		Klines last_trend = PriceUtil.getLastKlines(list_trend);
		
		double hitPrice = last_trend.getClosePriceDoubleValue();
		
		Klines afterKline = PriceUtil.getAfterKlines(last_trend, list);
		
		if(afterKline == null) {
			return;
		}
		
		Klines last = null;

		QuotationMode mode = (ps == PositionSide.LONG) ? QuotationMode.LONG : QuotationMode.SHORT;
		
		for(int index = list.size() - 1; index > 0; index--) {
			Klines current = list.get(index);
			if((mode == QuotationMode.LONG && PriceUtil.isBreachLong(current, hitPrice))
					|| (mode == QuotationMode.SHORT && PriceUtil.isBreachShort(current, hitPrice))) {
				last = current;
				break;
			}
			if(current.lte(afterKline)) {
				break;
			}
		}
		
		if(last == null) {
			return;
		}
		
		double h = last_trend.getHighPriceDoubleValue();
		double l = last_trend.getLowPriceDoubleValue();
		//double c = last.getClosePriceDoubleValue();
		double bh = last.getBodyHighPriceDoubleValue();
		double bl = last.getBodyLowPriceDoubleValue();
		
		double take = h - l;
		
		double stopLoss = mode == QuotationMode.LONG ? last.getLowPriceDoubleValue() : last.getHighPriceDoubleValue();
		
		double firstTakeProfit = 0; 
		double secondTakeProfit = 0;
		
		if(isLong()) {
			firstTakeProfit =Double.valueOf( PriceUtil.formatDoubleDecimal(bh + (take / 2), last.getDecimalNum()) );
			secondTakeProfit = Double.valueOf( PriceUtil.formatDoubleDecimal(bh + take, last.getDecimalNum()) );
			addPrices(new OpenPriceDetails(FibCode.FIB618, bh, stopLoss, firstTakeProfit, secondTakeProfit));
		} else if(isShort()) {
			firstTakeProfit =Double.valueOf( PriceUtil.formatDoubleDecimal(bl - (take / 2), last.getDecimalNum()) );
			secondTakeProfit = Double.valueOf( PriceUtil.formatDoubleDecimal(bl - take, last.getDecimalNum()) );
			addPrices(new OpenPriceDetails(FibCode.FIB618, bl, stopLoss, firstTakeProfit, secondTakeProfit));
		}
		
		Klines fibAfterFlag = PriceUtil.getAfterKlines(last, this.list_15m);
		if(fibAfterFlag != null) {
			this.fibAfterKlines.addAll(PriceUtil.subList(fibAfterFlag, this.list_15m));
		}
	}
	
	private PositionSide getPositionSide() {
		PositionSide ps = PositionSide.DEFAULT;
		int index = list_trend.size() - 1;
		Klines current = list_trend.get(index);
		Klines parent = list_trend.get(index - 1);
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