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
import com.bugbycode.module.binance.AutoTradeType;
import com.bugbycode.module.price.OpenPrice;
import com.bugbycode.module.price.impl.OpenPriceDetails;
import com.bugbycode.module.trading.PositionSide;
import com.util.KlinesComparator;
import com.util.PriceUtil;

/**
 * 期权交易
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
		
		if(list.size() < 3 || CollectionUtils.isEmpty(this.list_15m)) {
			return;
		}

		this.ps = PositionSide.DEFAULT;
		
		this.list.sort(new KlinesComparator(SortType.ASC));
		this.list_15m.sort(new KlinesComparator(SortType.ASC));
		
		double startPrice = 0;
		double endPrice = 0;
		int decimalPoint = 2;
		Klines current = null;
		Klines parent = null;
		Klines next = null;
		
		for(int index = list.size() - 1; index > 1; index--) {
			current = list.get(index);
			parent = list.get(index - 1);
			next = list.get(index - 2);
			decimalPoint = current.getDecimalNum();
			if(verifyLong(current, parent, next)) {
				this.ps = PositionSide.LONG;
				startPrice = current.getClosePriceDoubleValue();
				if(current.isFall()) {
					endPrice = parent.getLowPriceDoubleValue();
				} else {
					endPrice = current.getLowPriceDoubleValue();
				}
				break;
			} else if(verifyShort(current, parent, next)) {
				this.ps = PositionSide.SHORT;
				startPrice = current.getClosePriceDoubleValue();
				if(current.isRise()) {
					endPrice = parent.getHighPriceDoubleValue();
				} else {
					endPrice = current.getHighPriceDoubleValue();
				}
				break;
			}
		}
		
		if(ps == PositionSide.DEFAULT) {
			return;
		}
		
		FibInfo fibInfo = new FibInfo(startPrice, endPrice, decimalPoint, FibLevel.LEVEL_0);
		
		double firstTakeProfit = fibInfo.getFibValue(FibCode.FIB1_618); 
		double secondTakeProfit = fibInfo.getFibValue(FibCode.FIB2);
		double stopLoss = fibInfo.getFibValue(FibCode.FIB5); 
		double c = fibInfo.getFibValue(FibCode.FIB1);
		
		addPrices(new OpenPriceDetails(FibCode.FIB618, c, stopLoss, firstTakeProfit, secondTakeProfit, AutoTradeType.AREA_INDEX));
		
		Klines fibAfter = PriceUtil.getAfterKlines(current, list_15m);
		if(fibAfter != null) {
			this.fibAfterKlines = PriceUtil.subList(fibAfter, list_15m);
		}
		
	}
	
	private void addPrices(OpenPrice price) {
		if(!PriceUtil.contains(openPrices, price) && price.getCode().gte(FibCode.FIB236)) {
			openPrices.add(price);
		}
	}
	
	private boolean verifyLong(Klines current, Klines parent, Klines next) {
		return PriceUtil.verifyDeclining_v28(parent, next) && PriceUtil.verifyPowerful_v28(current, parent);
	}
	
	private boolean verifyShort(Klines current, Klines parent, Klines next) {
		return PriceUtil.verifyPowerful_v28(parent, next) && PriceUtil.verifyDeclining_v28(current, parent);
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