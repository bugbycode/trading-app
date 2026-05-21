package com.bugbycode.factory.area.impl;

import java.util.ArrayList;
import java.util.List;

import org.springframework.util.CollectionUtils;

import com.bugbycode.factory.area.AreaFactory;
import com.bugbycode.module.FibCode;
import com.bugbycode.module.FibInfo;
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
	
	public AreaFactoryImpl(List<Klines> list, List<Klines> list_15m, PositionSide ps) {
		this.ps = ps;
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

		this.list.sort(new KlinesComparator(SortType.ASC));
		this.list_15m.sort(new KlinesComparator(SortType.ASC));
		
		if(this.ps == PositionSide.DEFAULT) {
			return;
		}
		
		Klines last = null;
		
		for(int index = list.size() - 1; index >= 0; index--) {
			Klines current = list.get(index);
			if((ps == PositionSide.LONG && current.isFall())
					|| (ps == PositionSide.SHORT && current.isRise())) {
				last = current;
				break;
			}
		}
		
		if(last == null) {
			return;
		}
		
		int decimalPoint = last.getDecimalNum();
		double o = last.getOpenPriceDoubleValue();
		double h = last.getHighPriceDoubleValue();
		double l = last.getLowPriceDoubleValue();
		
		double startPrice = o;
		double endPrice = isLong() ? l : h;
		
		FibInfo fibInfo = new FibInfo(startPrice, endPrice, decimalPoint);

		double fib1Value = fibInfo.getFibValue(FibCode.FIB1);
		
		FibCode takeProfitCode = FibCode.FIB2;
		double takeProfitCodeValue = fibInfo.getFibValue(takeProfitCode);
		
		FibInfo takeProfitFibInfo = new FibInfo(takeProfitCodeValue, fib1Value, decimalPoint);
		
		FibCode firstTakeProfitCode = FibCode.FIB618;
		FibCode secondTakeProfitCode = FibCode.FIB786;
		
		double firstTakeProfit = takeProfitFibInfo.getFibValue(firstTakeProfitCode);
		double secondTakeProfit = takeProfitFibInfo.getFibValue(secondTakeProfitCode);
		
		if(firstTakeProfit <= 0 || secondTakeProfit <= 0) {
			takeProfitCode = FibCode.FIB1_272;
			takeProfitCodeValue = fibInfo.getFibValue(takeProfitCode);
			takeProfitFibInfo = new FibInfo(takeProfitCodeValue, fib1Value, decimalPoint);
			firstTakeProfit = takeProfitFibInfo.getFibValue(firstTakeProfitCode);
			secondTakeProfit = takeProfitFibInfo.getFibValue(secondTakeProfitCode);
		}
		
		if(firstTakeProfit <= 0 || secondTakeProfit <= 0) {
			return;
		}
		
		
		FibInfo stopLossFibInfo = new FibInfo(fib1Value, firstTakeProfit, decimalPoint);
		double stopLossLimit = stopLossFibInfo.getFibValue(FibCode.FIB1_272);
		
		addPrices(new OpenPriceDetails(FibCode.FIB1, fib1Value, stopLossLimit, firstTakeProfit, secondTakeProfit, AutoTradeType.AREA_INDEX, fibInfo));
		
		Klines fibAfter = PriceUtil.getAfterKlines(last, list_15m);
		if(fibAfter != null) {
			this.fibAfterKlines = PriceUtil.subList(fibAfter, list_15m);
			fibInfo.setFibAfterKlines(fibAfterKlines);
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