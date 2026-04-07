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

/**
 * 高频交易
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
		
		if(list.size() < 50 || CollectionUtils.isEmpty(this.list_15m)) {
			return;
		}

		this.ps = PositionSide.DEFAULT;
		
		this.list.sort(new KlinesComparator(SortType.ASC));
		this.list_15m.sort(new KlinesComparator(SortType.ASC));
		
		PriceUtil.calculateMACD(list);
		
		
		Klines current = null;
		Klines parent = null;
		Klines next = null;
		
		for(int index = list.size() - 1; index > 3; index--) {
			current = list.get(index);
			parent = list.get(index - 1);
			next = list.get(index - 2);
			if(PriceUtil.verifyPowerful_v10(current, parent, next)) {
				this.ps = PositionSide.LONG;
				break;
			} else if(PriceUtil.verifyDeclining_v10(current, parent, next)) {
				this.ps = PositionSide.SHORT;
				break;
			}
			current = null;
			parent = null;
			next = null;
		}
		
		if(ps == PositionSide.DEFAULT) {
			return;
		}
		
		QuotationMode mode = ps == PositionSide.LONG ? QuotationMode.LONG : QuotationMode.SHORT;
		
		double h = current.getHighPriceDoubleValue();
		double l = current.getLowPriceDoubleValue();
		double c = current.getClosePriceDoubleValue();
		
		double lt_bh = current.getBodyHighPriceDoubleValue();
		double lt_bl = current.getBodyLowPriceDoubleValue();
		
		double take = mode == QuotationMode.LONG ? (h - lt_bl) : (lt_bh - l);
		take = take * 0.886;
		
		double firstTakeProfit = 0; 
		double secondTakeProfit = 0;
		double stopLoss = 0; 
		
		if(isLong()) {
			firstTakeProfit =Double.valueOf( PriceUtil.formatDoubleDecimal(c + (take / 2), current.getDecimalNum()) );
			secondTakeProfit = Double.valueOf( PriceUtil.formatDoubleDecimal(c + take, current.getDecimalNum()) );
			stopLoss = current.isRise() ? current.getLowPriceDoubleValue() : PriceUtil.getMinPrice(current.getLowPriceDoubleValue(), parent.getLowPriceDoubleValue());
		} else if(isShort()) {
			firstTakeProfit =Double.valueOf( PriceUtil.formatDoubleDecimal(c - (take / 2), current.getDecimalNum()) );
			secondTakeProfit = Double.valueOf( PriceUtil.formatDoubleDecimal(c - take, current.getDecimalNum()) );
			stopLoss = current.isFall() ? current.getHighPriceDoubleValue() : PriceUtil.getMaxPrice(current.getHighPriceDoubleValue(), parent.getHighPriceDoubleValue());
			
			if(firstTakeProfit <= 0) {
				firstTakeProfit = l;
			}
			
			if(secondTakeProfit <= 0) {
				secondTakeProfit = l;
			}
		}
		
		addPrices(new OpenPriceDetails(FibCode.FIB618, c, stopLoss, firstTakeProfit, secondTakeProfit));
		/*
		Klines fibAfterFlag = PriceUtil.getAfterKlines(current, this.list_15m);
		if(fibAfterFlag != null) {
			this.fibAfterKlines.addAll(PriceUtil.subList(fibAfterFlag, this.list_15m));
		}*/
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