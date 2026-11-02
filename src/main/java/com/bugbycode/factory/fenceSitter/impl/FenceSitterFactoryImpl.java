package com.bugbycode.factory.fenceSitter.impl;

import java.util.ArrayList;
import java.util.List;

import org.springframework.util.CollectionUtils;

import com.bugbycode.factory.fenceSitter.FenceSitterFactory;
import com.bugbycode.module.AutoClosePosition;
import com.bugbycode.module.DualSidePositionStatus;
import com.bugbycode.module.FibCode;
import com.bugbycode.module.FibInfo;
import com.bugbycode.module.Klines;
import com.bugbycode.module.PriceType;
import com.bugbycode.module.SortType;
import com.bugbycode.module.binance.AutoTrade;
import com.bugbycode.module.binance.AutoTradeType;
import com.bugbycode.module.price.OpenPrice;
import com.bugbycode.module.price.impl.OpenPriceDetails;
import com.bugbycode.module.trading.PositionSide;
import com.util.KlinesComparator;
import com.util.PriceUtil;

public class FenceSitterFactoryImpl implements FenceSitterFactory{

	private List<Klines> list;
	
	private List<Klines> list_hit;
	
	private List<Klines> fibAfterKlines;
	
	private FibInfo fibInfo;
	
	private FibInfo childFibInfo;
	
	private OpenPrice openPrice;
	
	private AutoTrade autoTrade = AutoTrade.OPEN;
	
	private AutoClosePosition autoClosePosition = AutoClosePosition.OPEN;
	
	private DualSidePositionStatus dualSidePositionStatus = DualSidePositionStatus.CLOSE;
	
	private boolean resetStopLoss = false;
	
	private PriceType priceType = PriceType.HIGH;
	
	private PositionSide ps_mode = PositionSide.DEFAULT;
	
	public FenceSitterFactoryImpl(List<Klines> list, List<Klines> list_hit, PriceType priceType, PositionSide ps_mode) {
		this.list = new ArrayList<Klines>();
		this.list_hit = new ArrayList<Klines>();
		this.fibAfterKlines = new ArrayList<Klines>();
		if(priceType != null) {
			this.priceType = priceType;
		}
		if(ps_mode != null) {
			this.ps_mode = ps_mode; 
		}
		if(!CollectionUtils.isEmpty(list_hit)) {
			this.list_hit.addAll(list_hit);
		}
		if(!CollectionUtils.isEmpty(list)) {
			this.list.addAll(list);
			this.init();
		}
	}
	
	private void init() {
		if(CollectionUtils.isEmpty(list) || CollectionUtils.isEmpty(list_hit) || this.ps_mode == PositionSide.DEFAULT) {
			return;
		}
		
		KlinesComparator kc = new KlinesComparator(SortType.ASC);
		this.list.sort(kc);
		this.list_hit.sort(kc);
		
		Klines last = PriceUtil.getLastKlines(list);
		Klines hit_last = PriceUtil.getLastKlines(list_hit);
		double stopLossLen = hit_last.getHighPriceDoubleValue() - hit_last.getLowPriceDoubleValue();
		
		double h = last.getHighPriceDoubleValue();
		double l = last.getLowPriceDoubleValue();
		
		double openPriceValue = 0;
		int decimalNum = last.getDecimalNum();
		
		if(priceType == PriceType.HIGH) {
			openPriceValue = h;
			this.fibInfo = new FibInfo(h, l, decimalNum);
		} else {
			openPriceValue = l;
			this.fibInfo = new FibInfo(l, h, decimalNum);
		}
		
		double stopLossLimit = ps_mode == PositionSide.LONG ? openPriceValue - stopLossLen : openPriceValue + stopLossLen;
		stopLossLimit = PriceUtil.formatDoubleDecimalValue(stopLossLimit, decimalNum);
		
		FibCode openCode = this.fibInfo.getFibCode(openPriceValue);
		
		addPrices(new OpenPriceDetails(openCode, openPriceValue, stopLossLimit, AutoTradeType.FENCE_SITTER));
	}
	
	private void addPrices(OpenPrice price) {
		if(price.getCode().gte(FibCode.FIB236)) {
			price.setResetStopLoss(resetStopLoss);
			price.setAutoTrade(autoTrade);
			price.setAutoClosePosition(autoClosePosition);
			price.setChildFibInfo(childFibInfo);
			price.setDualSidePositionStatus(dualSidePositionStatus);
			openPrice = price;
		}
	}
	
	@Override
	public FibInfo getFibInfo() {
		return fibInfo;
	}

	@Override
	public List<Klines> getFibAfterKlines() {
		return fibAfterKlines;
	}

	@Override
	public OpenPrice getOpenPrice() {
		return openPrice;
	}

	@Override
	public boolean isLong() {
		boolean result = false;
		if(openPrice != null && ps_mode == PositionSide.LONG) {
			result = true;
		}
		return result;
	}
	
	@Override
	public boolean isShort() {
		boolean result = false;
		if(openPrice != null && ps_mode == PositionSide.SHORT) {
			result = true;
		}
		return result;
	}
	
}
