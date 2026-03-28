package com.bugbycode.factory.area.impl;

import java.util.ArrayList;
import java.util.List;

import org.springframework.util.CollectionUtils;

import com.bugbycode.factory.area.AreaFactory;
import com.bugbycode.module.FibCode;
import com.bugbycode.module.Inerval;
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
		
		if(list.size() < 3 || CollectionUtils.isEmpty(this.list_15m)) {
			return;
		}

		this.ps = PositionSide.DEFAULT;
		
		this.list.sort(new KlinesComparator(SortType.ASC));
		this.list_15m.sort(new KlinesComparator(SortType.ASC));
		
		Klines last = null;
		
		Klines stopLossKlines = PriceUtil.getLastKlines(list_15m);
		
		for(int index = list.size() - 1; index > 2; index--) {
			Klines current = list.get(index);
			Klines parent = list.get(index - 1);
			Klines next = list.get(index - 2);
			if(verifyLong(current, parent, next)) {
				if(current.isFall()) {
					stopLossKlines = parent;
				} else {
					stopLossKlines = current;
				}
				last = current;
				this.ps = PositionSide.LONG;
				break;
			} else if(verifyShort(current, parent, next)) {
				if(current.isRise()) {
					stopLossKlines = parent;
				} else {
					stopLossKlines = current;
				}
				last = current;
				this.ps = PositionSide.SHORT;
				break;
			}
		}
		
		if(this.ps == PositionSide.DEFAULT || last == null) {
			return;
		}
		
		QuotationMode mode = (ps == PositionSide.LONG) ? QuotationMode.LONG : QuotationMode.SHORT;
		
		double h = last.getHighPriceDoubleValue();
		double l = last.getLowPriceDoubleValue();
		//double c = last.getClosePriceDoubleValue();
		double bh = last.getBodyHighPriceDoubleValue();
		double bl = last.getBodyLowPriceDoubleValue();
		
		double take = h - l;
		
		double stopLoss = mode == QuotationMode.LONG ? stopLossKlines.getLowPriceDoubleValue() : stopLossKlines.getHighPriceDoubleValue();
		
		double firstTakeProfit = 0; 
		double secondTakeProfit = 0;
		
		Klines list_last = PriceUtil.getLastKlines(list);
		Inerval list_inInerval = list_last.getInervalType();
		
		double price = 0;
		if(isLong()) {
			firstTakeProfit =Double.valueOf( PriceUtil.formatDoubleDecimal(bh + (take / 2), last.getDecimalNum()) );
			secondTakeProfit = Double.valueOf( PriceUtil.formatDoubleDecimal(bh + take, last.getDecimalNum()) );
			if(list_inInerval == Inerval.INERVAL_15M) {
				price = bl;
			} else {
				price = bh;
			}
		} else if(isShort()) {
			firstTakeProfit =Double.valueOf( PriceUtil.formatDoubleDecimal(bl - (take / 2), last.getDecimalNum()) );
			secondTakeProfit = Double.valueOf( PriceUtil.formatDoubleDecimal(bl - take, last.getDecimalNum()) );
			if(list_inInerval == Inerval.INERVAL_15M) {
				price = bh;
			} else {
				price = bl;
			}
		}
		
		if(price > 0) {
			addPrices(new OpenPriceDetails(FibCode.FIB618, price, stopLoss, firstTakeProfit, secondTakeProfit));
		}
		
		Klines fibAfterFlag = PriceUtil.getAfterKlines(last, this.list_15m);
		if(fibAfterFlag != null) {
			this.fibAfterKlines.addAll(PriceUtil.subList(fibAfterFlag, this.list_15m));
		}
	}
	
	private boolean verifyLong(Klines current, Klines parent, Klines next) {
		return PriceUtil.verifyDecliningPrice_v28(parent, next) && PriceUtil.verifyPowerful_v28(current, parent);
	}
	
	private boolean verifyShort(Klines current, Klines parent, Klines next) {
		return PriceUtil.verifyPowerful_v28(parent, next) && PriceUtil.verifyDecliningPrice_v28(current, parent);
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