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
	
	private List<FibInfo> fibInfoList;
	
	public AreaFactoryImpl(List<Klines> list, List<Klines> list_hit, List<Klines> list_15m) {
		this.ps = PositionSide.DEFAULT;
		this.list = new ArrayList<Klines>();
		this.list_hit = new ArrayList<Klines>();
		this.list_15m = new ArrayList<Klines>();
		this.fibAfterKlines = new ArrayList<Klines>();
		this.openPrices = new ArrayList<OpenPrice>();
		this.fibInfoList = new ArrayList<FibInfo>();
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

		this.list.sort(new KlinesComparator(SortType.ASC));
		this.list_hit.sort(new KlinesComparator(SortType.ASC));
		this.list_15m.sort(new KlinesComparator(SortType.ASC));
		
		Klines last = PriceUtil.getLastKlines(list);
		
		if(last == null) {
			return;
		}
		
		double c = last.getClosePriceDoubleValue();
		double l = last.getLowPriceDoubleValue();
		double h = last.getHighPriceDoubleValue();
		double endPriceValue = last.isRise() ? l : h;
		
		Klines last_after_hit = PriceUtil.getAfterKlines(last, list_hit);
		
		if(last_after_hit == null) {
			return;
		}
		
		Klines hit_k = null;
		for(int index = list_hit.size() - 1; index >= 0; index--) {
			Klines current = list_hit.get(index);
			
			if(PriceUtil.isBreachLong(current, c)) {
				hit_k = current;
				this.ps = PositionSide.LONG;
				break;
			} else if(PriceUtil.isBreachShort(current, c)) {
				this.ps = PositionSide.SHORT;
				hit_k = current;
				break;
			}
			
			if(current.lte(last_after_hit)) {
				break;
			}
		}
		
		if(this.ps == PositionSide.DEFAULT || hit_k == null) {
			hit_k = last_after_hit;
			this.ps = hit_k.isRise() ? PositionSide.LONG : PositionSide.SHORT;
		}
		
		int decimalNum = hit_k.getDecimalNum();
		double openPriceValue = isLong() ? hit_k.getBodyHighPriceDoubleValue() : hit_k.getBodyLowPriceDoubleValue();
		FibInfo fibInfo = new FibInfo(openPriceValue, endPriceValue, decimalNum);
		
		FibCode takeProfitCode = FibCode.FIB1_618;
		if((isLong() && fibInfo.isLong()) || (isShort() && fibInfo.isShort())) {
			takeProfitCode = FibCode.FIB5;
		}
		
		double takeProfitCodeValue = fibInfo.getFibValue(takeProfitCode);
		
		double stopLossLimit = isLong() ? hit_k.getLowPriceDoubleValue() : hit_k.getHighPriceDoubleValue();
		//计算最佳止损点
		FibInfo stopLossFibInfo = new FibInfo(openPriceValue, takeProfitCodeValue, decimalNum);
		if(isLong()) {
			stopLossLimit = PriceUtil.getMaxPrice(stopLossLimit, stopLossFibInfo.getFibValue(FibCode.FIB1_272));
		} else {
			stopLossLimit = PriceUtil.getMinPrice(stopLossLimit, stopLossFibInfo.getFibValue(FibCode.FIB1_272));
		}
		
		addPrices(new OpenPriceDetails(FibCode.FIB1, openPriceValue, stopLossLimit, takeProfitCodeValue, takeProfitCodeValue, AutoTradeType.AREA_INDEX, fibInfo));
		
		Klines hitAfter = PriceUtil.getAfterKlines(hit_k, list_15m);
		if(hitAfter != null) {
			this.fibAfterKlines = PriceUtil.subList(hitAfter, list_15m);
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
	
	@Override
	public List<FibInfo> getFibInfoList() {
		return this.fibInfoList;
	}

}