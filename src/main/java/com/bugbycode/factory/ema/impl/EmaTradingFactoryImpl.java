package com.bugbycode.factory.ema.impl;

import java.util.ArrayList;
import java.util.List;

import org.springframework.util.CollectionUtils;

import com.bugbycode.factory.ema.EmaTradingFactory;
import com.bugbycode.module.FibCode;
import com.bugbycode.module.Klines;
import com.bugbycode.module.SortType;
import com.bugbycode.module.price.OpenPrice;
import com.bugbycode.module.price.impl.OpenPriceDetails;
import com.bugbycode.module.trading.PositionSide;
import com.util.PriceComparator;
import com.util.PriceUtil;

/**
 * 指数均线交易指标接口实现类
 */
public class EmaTradingFactoryImpl implements EmaTradingFactory {

	private List<Klines> list;
	
	private List<OpenPrice> openPrices;
	
	private PositionSide ps = PositionSide.DEFAULT;
	
	public EmaTradingFactoryImpl(List<Klines> list) {
		this.list = new ArrayList<Klines>();
		this.openPrices = new ArrayList<OpenPrice>();
		if(!CollectionUtils.isEmpty(list)) {
			this.list.addAll(list);
			this.init();
		}
	}

	@Override
	public List<OpenPrice> getOpenPrices() {
		return openPrices;
	}

	@Override
	public boolean isLong() {
		return ps == PositionSide.LONG;
	}

	@Override
	public boolean isShort() {
		return ps == PositionSide.SHORT;
	}
	
	private void init() {
		if(CollectionUtils.isEmpty(list) || list.size() < 30) {
			return;
		}
		
		PriceUtil.calculateEMA_7_25_99(list);
		
		int index = list.size() - 1;
		Klines current = list.get(index);
		//Klines parent = list.get(index - 1);
		
		if(verifyLong(current)) {
			ps = PositionSide.LONG;
		} else if(verifyShort(current)) {
			ps = PositionSide.SHORT;
		}
		
		if(ps == PositionSide.DEFAULT) {
			return;
		}
		
		//处理开仓点位
		if(ps == PositionSide.LONG) {
			/*
			if(PriceUtil.isBreachLong(current, current.getEma7()) && PriceUtil.isRise_v3(current, parent)) {
				addPrices(new OpenPriceDetails(FibCode.FIB0, current.getClosePriceDoubleValue()));
			}*/
			
			addPrices(new OpenPriceDetails(FibCode.FIB0, current.getEma25()));
			
			this.openPrices.sort(new PriceComparator(SortType.DESC));
		} else {
			
			/*if(PriceUtil.isBreachShort(current, current.getEma7()) && PriceUtil.isFall_v3(current, parent)) {
				addPrices(new OpenPriceDetails(FibCode.FIB0, current.getClosePriceDoubleValue()));
			}*/
			
			addPrices(new OpenPriceDetails(FibCode.FIB0, current.getEma25()));
			
			this.openPrices.sort(new PriceComparator(SortType.ASC));
		}
		
		//回撤信息
		
	}
	
	private boolean verifyLong(Klines k) {
		return k.getEma7() > k.getEma25();
	}
	
	private boolean verifyShort(Klines k) {
		return k.getEma7() < k.getEma25();
	}
	
	private void addPrices(OpenPrice price) {
		if(!PriceUtil.contains(openPrices, price)) {
			openPrices.add(price);
		}
	}

}
