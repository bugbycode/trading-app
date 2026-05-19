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
	
	private List<Klines> list_hit;
	
	private List<Klines> list_15m;
	
	private List<Klines> fibAfterKlines;
	
	private PositionSide ps;
	
	private List<OpenPrice> openPrices;
	
	public AreaFactoryImpl(List<Klines> list, List<Klines> list_hit, List<Klines> list_15m) {
		this.ps = PositionSide.DEFAULT;
		this.list = new ArrayList<Klines>();
		this.list_hit = new ArrayList<Klines>();
		this.list_15m = new ArrayList<Klines>();
		this.fibAfterKlines = new ArrayList<Klines>();
		this.openPrices = new ArrayList<OpenPrice>();
		if(!CollectionUtils.isEmpty(list)) {
			this.list.addAll(list);
		}
		if(!CollectionUtils.isEmpty(list_hit)) {
			this.list_hit.addAll(list_hit);
		}
		if(!CollectionUtils.isEmpty(list_15m)) {
			this.list_15m.addAll(list_15m);
		}
		
		this.init();
	}
	
	private void init() {
		
		if(CollectionUtils.isEmpty(list) || CollectionUtils.isEmpty(this.list_hit) || CollectionUtils.isEmpty(this.list_15m)) {
			return;
		}

		this.ps = PositionSide.DEFAULT;
		
		this.list.sort(new KlinesComparator(SortType.ASC));
		this.list_hit.sort(new KlinesComparator(SortType.ASC));
		this.list_15m.sort(new KlinesComparator(SortType.ASC));
		
		Klines last = PriceUtil.getLastKlines(list);
		double c = last.getClosePriceDoubleValue();
		double l = last.getLowPriceDoubleValue();
		double h = last.getHighPriceDoubleValue();
		
		Klines last_after = PriceUtil.getAfterKlines(last, list_hit);
		if(last_after == null) {
			return;
		}
		
		FibInfo fibInfo = null;
		int decimalPoint = last_after.getDecimalNum();
		FibCode openCode = FibCode.FIB1;
		
		Klines hit = null;
		
		for(int index = list_hit.size() - 1; index >= 0; index--) {
			Klines current = list_hit.get(index);
			if(PriceUtil.isBreachLong(current, c)) {//做多
				hit = current;
				this.ps = PositionSide.LONG;
				break;
			} else if(PriceUtil.isBreachShort(current, c)) {//做空
				hit = current;
				this.ps = PositionSide.SHORT;
				break;
			}
			if(current.lte(last_after)) {
				break;
			}
		}
		
		if(ps == PositionSide.DEFAULT || hit == null) {
			hit = last_after;
			if(last_after.isRise()) {
				this.ps = PositionSide.LONG;
			} else {
				this.ps = PositionSide.SHORT;
			}
		}
		
		if(last.isRise()) {
			fibInfo = new FibInfo(c, l, decimalPoint);
		} else {
			fibInfo = new FibInfo(c, h, decimalPoint);
		}
		
		double hitPrice = isLong() ? hit.getBodyHighPriceDoubleValue() : hit.getBodyLowPriceDoubleValue();
		
		FibCode takeProfitCode = FibCode.FIB1;
		if((isLong() && fibInfo.isLong()) || (isShort() && fibInfo.isShort())) {
			takeProfitCode = FibCode.FIB5;
		} else {
			takeProfitCode = FibCode.FIB1_618;
		}
		
		double takeProfitCodeValue = fibInfo.getFibValue(takeProfitCode);
		double fib1Value = fibInfo.getFibValue(FibCode.FIB1);
		
		FibInfo stopLossFibInfo = new FibInfo(fib1Value, takeProfitCodeValue, decimalPoint);
		double stopLossLimit = isLong() ? hit.getLowPriceDoubleValue() : hit.getHighPriceDoubleValue();
		if(isLong()) {
			stopLossLimit = PriceUtil.getMaxPrice(stopLossLimit, stopLossFibInfo.getFibValue(FibCode.FIB1_272));
		} else {
			stopLossLimit = PriceUtil.getMinPrice(stopLossLimit, stopLossFibInfo.getFibValue(FibCode.FIB1_272));
		}
		
		FibInfo takeProfitFibInfo = new FibInfo(takeProfitCodeValue, fib1Value, decimalPoint);
		double takeProfitValue = takeProfitFibInfo.getFibValue(FibCode.FIB786);
		
		addPrices(new OpenPriceDetails(openCode, hitPrice, stopLossLimit, takeProfitValue, takeProfitValue, AutoTradeType.AREA_INDEX, fibInfo));
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