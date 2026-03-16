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
		
		if(list_trend.size() < 50 || list.size() < 50 || CollectionUtils.isEmpty(this.list_15m)) {
			return;
		}

		this.list.sort(new KlinesComparator(SortType.ASC));
		this.list_15m.sort(new KlinesComparator(SortType.ASC));
		this.list_trend.sort(new KlinesComparator(SortType.ASC));
		
		PriceUtil.calculateMACD(list);
		PriceUtil.calculateMACD(list_trend);
		
		this.ps = getPositionSide();
		
		if(this.ps == PositionSide.DEFAULT) {
			return;
		}
		
		//Klines last_15m = PriceUtil.getLastKlines(list_15m);
		
		Klines last = PriceUtil.getLastKlines(list);
		
		double h = last.getHighPriceDoubleValue();
		double l = last.getLowPriceDoubleValue();
		double c = last.getClosePriceDoubleValue();
		
		double take = h - l;
		
		QuotationMode mode = (ps == PositionSide.LONG) ? QuotationMode.LONG : QuotationMode.SHORT;
		double stopLoss = mode == QuotationMode.LONG ? last.getLowPriceDoubleValue() : last.getHighPriceDoubleValue();
		
		double firstTakeProfit = 0; 
		double secondTakeProfit = 0;
		
		if(isLong()) {
			firstTakeProfit =Double.valueOf( PriceUtil.formatDoubleDecimal(c + (take / 2), last.getDecimalNum()) );
			secondTakeProfit = Double.valueOf( PriceUtil.formatDoubleDecimal(c + take, last.getDecimalNum()) );
		} else if(isShort()) {
			firstTakeProfit =Double.valueOf( PriceUtil.formatDoubleDecimal(c - (take / 2), last.getDecimalNum()) );
			secondTakeProfit = Double.valueOf( PriceUtil.formatDoubleDecimal(c - take, last.getDecimalNum()) );
		}
		
		if(firstTakeProfit > 0 && secondTakeProfit > 0) {
			addPrices(new OpenPriceDetails(FibCode.FIB618, c, stopLoss, firstTakeProfit, secondTakeProfit));
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
		return PriceUtil.verifyPowerful_v14(current, parent);
	}
	
	private boolean verifyShort(Klines current, Klines parent) {
		return PriceUtil.verifyDecliningPrice_v14(current, parent);
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
