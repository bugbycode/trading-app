package com.bugbycode.factory.area.impl;

import java.util.ArrayList;
import java.util.List;

import org.springframework.util.CollectionUtils;

import com.bugbycode.factory.area.AreaFactory;
import com.bugbycode.factory.fibInfo.FibInfoFactory;
import com.bugbycode.factory.fibInfo.impl.FibInfoFactoryImpl;
import com.bugbycode.module.FibCode;
import com.bugbycode.module.FibInfo;
import com.bugbycode.module.Klines;
import com.bugbycode.module.QuotationMode;
import com.bugbycode.module.SortType;
import com.bugbycode.module.StepPriceInfo;
import com.bugbycode.module.price.OpenPrice;
import com.bugbycode.module.price.impl.OpenPriceDetails;
import com.bugbycode.module.trading.PositionSide;
import com.util.PriceComparator;
import com.util.PriceUtil;

public class AreaFactoryImpl_v3 implements AreaFactory {

	private List<Klines> list;
	
	private List<Klines> list_15m;
	
	private List<Klines> fibAfterKlines;
	
	private PositionSide ps;
	
	private List<OpenPrice> openPrices;
	
	public AreaFactoryImpl_v3(List<Klines> list, List<Klines> list_15m) {
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
		
		if(this.list.size() < 50 || CollectionUtils.isEmpty(this.list_15m)) {
			return;
		}
		
		FibInfoFactory factory = new FibInfoFactoryImpl(list, list, list_15m);
		if(!(factory.isLong() || factory.isShort())) {
			return;
		}
		
		FibInfo fibInfo = factory.getFibInfo();
		
		this.fibAfterKlines.addAll(fibInfo.getFibAfterKlines());
		
		double fibEndPrice = fibInfo.getFibValue(FibCode.FIB0);
		
		StepPriceInfo info = PriceUtil.calculateStepPrice(String.valueOf(fibEndPrice), fibInfo.getDecimalPoint());
		
		double startPrice = info.getHitPriceDoubleValue();
		double stepPrice = info.getStepPriceDoubleValue();
		
		settingOpenPrice(fibInfo, startPrice, stepPrice, fibInfo.getDecimalPoint());
		
		QuotationMode mode = fibInfo.getQuotationMode();
		if(mode == QuotationMode.LONG) {
			ps = PositionSide.LONG;
			this.openPrices.sort(new PriceComparator(SortType.DESC));
		} else {
			ps = PositionSide.SHORT;
			this.openPrices.sort(new PriceComparator(SortType.ASC));
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

	private void settingOpenPrice(FibInfo fibInfo, double startPrice, double stepPrice, int decimalPoint) {
		QuotationMode mode = fibInfo.getQuotationMode();
		FibCode lastCode = FibCode.FIB1_618;
		FibCode openCode = fibInfo.getFibCode(startPrice);
		double lastPrice = fibInfo.getFibValue(lastCode);
		if((mode == QuotationMode.LONG && startPrice <= lastPrice) 
				|| (mode == QuotationMode.SHORT && startPrice >= lastPrice)) {
			return;
		}
		
		OpenPrice p = null;
		if(mode == QuotationMode.LONG) {
			p = new OpenPriceDetails(openCode, formatPrice(startPrice, decimalPoint), formatPrice(startPrice - stepPrice, decimalPoint),
					formatPrice(startPrice + stepPrice * 2, decimalPoint), formatPrice(startPrice + stepPrice, decimalPoint));
		} else {
			p = new OpenPriceDetails(openCode, formatPrice(startPrice, decimalPoint), formatPrice(startPrice + stepPrice, decimalPoint), 
					formatPrice(startPrice - stepPrice * 2, decimalPoint), formatPrice(startPrice - stepPrice, decimalPoint));
		}
		
		addPrices(p);
		
		settingOpenPrice(fibInfo, p.getStopLossLimit(), stepPrice, decimalPoint);
	}
	
	private double formatPrice(double price, int decimalPoint) {
		return Double.valueOf(PriceUtil.formatDoubleDecimal(price,decimalPoint));
	}
}
