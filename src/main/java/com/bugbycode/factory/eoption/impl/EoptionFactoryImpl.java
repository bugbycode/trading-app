package com.bugbycode.factory.eoption.impl;

import java.util.ArrayList;
import java.util.List;

import org.springframework.util.CollectionUtils;

import com.bugbycode.factory.eoption.EoptionFactory;
import com.bugbycode.module.FibCode;
import com.bugbycode.module.Klines;
import com.bugbycode.module.SortType;
import com.bugbycode.module.price.OpenPrice;
import com.bugbycode.module.price.impl.OpenPriceDetails;
import com.bugbycode.module.trading.PositionSide;
import com.util.KlinesComparator;
import com.util.PriceUtil;

public class EoptionFactoryImpl implements EoptionFactory {

	private List<Klines> list;
	
	private List<Klines> list_15m;
	
	private List<Klines> fibAfterKlines;
	
	private PositionSide ps;
	
	private List<OpenPrice> openPrices;
	
	public EoptionFactoryImpl(List<Klines> list, List<Klines> list_15m) {
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
		
		PriceUtil.calculateEMA_7_25_99(list);
		Klines current = null;
		for(int index = list.size() - 1; index >= 0; index--) {
			current = list.get(index);
			double ema25 = current.getEma25();
			if(PriceUtil.isBreachLong(current, ema25)) {
				this.ps = PositionSide.LONG;
				break;
			} else if(PriceUtil.isBreachShort(current, ema25)) {
				this.ps = PositionSide.SHORT;
				break;
			}
		}
		
		if(this.ps == PositionSide.DEFAULT || current == null) {
			return;
		}
		
		addPrices(new OpenPriceDetails(FibCode.FIB618, current.getClosePriceDoubleValue()));
		
		Klines fibAfterFlag = PriceUtil.getAfterKlines(current, this.list_15m);
		if(fibAfterFlag != null) {
			this.fibAfterKlines.addAll(PriceUtil.subList(fibAfterFlag, this.list_15m));
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

	private void addPrices(OpenPrice price) {
		if(!PriceUtil.contains(openPrices, price)) {
			openPrices.add(price);
		}
	}
}
