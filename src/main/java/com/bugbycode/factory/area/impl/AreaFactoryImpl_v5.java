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
import com.bugbycode.module.price.OpenPrice;
import com.bugbycode.module.price.impl.OpenPriceDetails;
import com.bugbycode.module.trading.PositionSide;
import com.util.KlinesComparator;
import com.util.PriceUtil;

/**
 * 盘整区网格交易
 */
public class AreaFactoryImpl_v5 implements AreaFactory {

	private List<Klines> list;
	
	private List<Klines> list_hit;
	
	private List<Klines> list_15m;
	
	private List<Klines> fibAfterKlines;
	
	private PositionSide ps;
	
	private List<OpenPrice> openPrices;
	
	public AreaFactoryImpl_v5(List<Klines> list, List<Klines> list_hit, List<Klines> list_15m) {
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
		
		if(list.size() < 1 || CollectionUtils.isEmpty(this.list_hit) || CollectionUtils.isEmpty(this.list_15m)) {
			return;
		}
		
		KlinesComparator kc = new KlinesComparator(SortType.ASC);
		this.list.sort(kc);
		this.list_hit.sort(kc);
		this.list_15m.sort(kc);

		Klines list_last = PriceUtil.getLastKlines(list);
		Klines list_last_after = PriceUtil.getAfterKlines(list_last, list_hit);
		
		double h = list_last.getHighPriceDoubleValue();
		double l = list_last.getLowPriceDoubleValue();
		int decimalPoint = list_last.getDecimalNum();
		
		FibInfo fibInfo = null;
		Klines last = null;
		double priceValue = 0;
		FibCode openCode = FibCode.FIB1;
		FibCode firstTakeProfitCode = FibCode.FIB618;
		FibCode secondTakeProfitCode = FibCode.FIB5;
		
		for(int index = list_hit.size() - 1; index >= 0; index--) {
			Klines current = list_hit.get(index);
			if(current.lt(list_last_after)) {
				break;
			}
			if(PriceUtil.isBreachLong(current, h)) {
				fibInfo = new FibInfo(h, l, decimalPoint, FibLevel.LEVEL_0);
				last = current;
				this.ps = PositionSide.LONG;
				priceValue = last.getBodyHighPriceDoubleValue();
				openCode = fibInfo.getFibCode(priceValue);
				firstTakeProfitCode = fibInfo.getPriceActionTakeProfit_nextCode(openCode);
				secondTakeProfitCode = fibInfo.getPriceActionTakeProfit_v1(openCode);
				break;
			} else if(PriceUtil.isBreachShort(current, h)) {
				fibInfo = new FibInfo(h, l, decimalPoint, FibLevel.LEVEL_0);
				last = current;
				this.ps = PositionSide.SHORT;
				priceValue = last.getBodyLowPriceDoubleValue();
				break;
			} else if(PriceUtil.isBreachLong(current, l)) {
				fibInfo = new FibInfo(l, h, decimalPoint, FibLevel.LEVEL_0);
				last = current;
				this.ps = PositionSide.LONG;
				priceValue = last.getBodyHighPriceDoubleValue();
				break;
			} else if(PriceUtil.isBreachShort(current, l)) {
				fibInfo = new FibInfo(l, h, decimalPoint, FibLevel.LEVEL_0);
				last = current;
				this.ps = PositionSide.SHORT;
				priceValue = last.getBodyLowPriceDoubleValue();
				openCode = fibInfo.getFibCode(priceValue);
				firstTakeProfitCode = fibInfo.getPriceActionTakeProfit_nextCode(openCode);
				secondTakeProfitCode = fibInfo.getPriceActionTakeProfit_v1(openCode);
				break;
			}
		}
		
		if(last == null || ps == PositionSide.DEFAULT) {
			return;
		}
		
		double stopLoss = isLong() ? last.getLowPriceDoubleValue() : last.getHighPriceDoubleValue();
		
		addPrices(new OpenPriceDetails(openCode, priceValue, stopLoss, fibInfo.getFibValue(firstTakeProfitCode), fibInfo.getFibValue(secondTakeProfitCode)));

		Klines fibAfterFlag = PriceUtil.getAfterKlines(last, this.list_15m);
		if(fibAfterFlag != null) {
			this.fibAfterKlines.addAll(PriceUtil.subList(fibAfterFlag, this.list_15m));
		}
	}
	
	private void addPrices(OpenPrice price) {
		if(!PriceUtil.contains(openPrices, price)) {
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