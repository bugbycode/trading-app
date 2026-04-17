package com.bugbycode.factory.area.impl;

import java.util.ArrayList;
import java.util.List;

import org.springframework.util.CollectionUtils;

import com.bugbycode.factory.area.AreaFactory;
import com.bugbycode.factory.fibInfo.FibInfoFactory;
import com.bugbycode.factory.fibInfo.impl.FibInfoFactoryImpl_v3;
import com.bugbycode.module.FibCode;
import com.bugbycode.module.FibInfo;
import com.bugbycode.module.Klines;
import com.bugbycode.module.price.OpenPrice;
import com.bugbycode.module.price.impl.OpenPriceDetails;
import com.bugbycode.module.trading.PositionSide;
import com.util.PriceUtil;

/**
 * 盘整区高低点交易
 */
public class AreaFactoryImpl_v3 implements AreaFactory {

	private List<Klines> list;
	
	private List<Klines> list_15m;
	
	private List<Klines> fibAfterKlines;
	
	private PositionSide ps;
	
	private List<OpenPrice> openPrices;
	
	public AreaFactoryImpl_v3(List<Klines> list, List<Klines> list_15m, PositionSide ps_mode) {
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
		
		this.init(ps_mode);
	}
	
	private void init(PositionSide ps_mode) {
		
		if(list.size() < 99 || CollectionUtils.isEmpty(this.list_15m)) {
			return;
		}

		FibInfoFactory factory = new FibInfoFactoryImpl_v3(list, list_15m, ps_mode);
		
		if(!(factory.isLong() || factory.isShort())) {
			return;
		}
		
		Klines end = factory.getEnd();
		FibInfo fibInfo = factory.getFibInfo();
		
		FibCode openCode = FibCode.FIB1;
		double hitPrice = fibInfo.getFibValue(openCode);
		Klines last = null;
		
		for(int index = list.size() - 1; index > 0; index--) {
			Klines current = list.get(index);
			if(current.lte(end)) {
				break;
			}
			if(PriceUtil.isBreachLong(current, hitPrice)) {
				this.ps = PositionSide.LONG;
				last = current;
				break;
			} else if(PriceUtil.isBreachShort(current, hitPrice)) {
				this.ps = PositionSide.SHORT;
				last = current;
				break;
			}
		}
		
		if(this.ps == PositionSide.DEFAULT || last == null) {
			return;
		}
		
		FibCode firstTakeProfitCode = FibCode.FIB618;
		FibCode secondTakeProfitCode = FibCode.FIB5;
		
		double stopLoss = isLong() ? last.getLowPriceDoubleValue() : last.getHighPriceDoubleValue();
		double priceValue = last.getClosePriceDoubleValue();
		if(isLong()) {
			priceValue = last.getBodyHighPriceDoubleValue();
		} else if(isShort()) {
			priceValue = last.getBodyLowPriceDoubleValue();
		}
		
		if(!((factory.isLong() && this.isLong()) || (factory.isShort() && this.isShort()))) {
			openCode = fibInfo.getFibCode(priceValue);
			firstTakeProfitCode = fibInfo.getPriceActionTakeProfit_nextCode(openCode);
			secondTakeProfitCode = fibInfo.getPriceActionTakeProfit_v1(openCode);
		}
		
		addPrices(new OpenPriceDetails(openCode, priceValue, stopLoss, fibInfo.getFibValue(firstTakeProfitCode), fibInfo.getFibValue(secondTakeProfitCode)));
		
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