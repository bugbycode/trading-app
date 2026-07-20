package com.bugbycode.factory.fenceSitter.impl;

import java.util.ArrayList;
import java.util.List;

import org.springframework.util.CollectionUtils;

import com.bugbycode.factory.fenceSitter.FenceSitterFactory;
import com.bugbycode.module.FibCode;
import com.bugbycode.module.Klines;
import com.bugbycode.module.SortType;
import com.bugbycode.module.binance.AutoTradeType;
import com.bugbycode.module.price.OpenPrice;
import com.bugbycode.module.price.impl.OpenPriceDetails;
import com.bugbycode.module.trading.PositionSide;
import com.util.KlinesComparator;
import com.util.PriceUtil;

public class FenceSitterFactoryImpl_v2 implements FenceSitterFactory{

	private OpenPrice openPrice;
	
	private List<Klines> list;
	
	private List<Klines> list_15m;
	
	private PositionSide ps = PositionSide.DEFAULT;
	
	public FenceSitterFactoryImpl_v2(List<Klines> list, List<Klines> list_15m) {
		this.list = new ArrayList<Klines>();
		this.list_15m = new ArrayList<Klines>();
		if(!CollectionUtils.isEmpty(list)) {
			this.list.addAll(list);
		}
		if(!CollectionUtils.isEmpty(list_15m)) {
			this.list_15m.addAll(list_15m);
		}
		this.init();
	}
	
	private void init() {
		if(CollectionUtils.isEmpty(list) || CollectionUtils.isEmpty(list_15m)) {
			return;
		}
		
		KlinesComparator kc = new KlinesComparator(SortType.ASC);
		this.list.sort(kc);
		this.list_15m.sort(kc);
		
		PriceUtil.calculateMACD(list);
		
		Klines last = PriceUtil.getLastKlines(list);
		Klines last_15m = PriceUtil.getLastKlines(list_15m);
		
		double hitPrice = last.getClosePriceDoubleValue();
		double last_15m_close = last_15m.getClosePriceDoubleValue();
		
		if(last.getDea() >= 0 && last_15m_close >= hitPrice) {
			this.ps = PositionSide.LONG;
		} else if(last.getDea() >= 0 && last_15m_close < hitPrice) {
			this.ps = PositionSide.SHORT;
		} else if(last.getDea() < 0 && last_15m_close <= hitPrice) {
			this.ps = PositionSide.SHORT;
		} else if(last.getDea() < 0 && last_15m_close > hitPrice) {
			this.ps = PositionSide.LONG;
		}
		
		double cutLoss = 10;
		double stopLossLimit = this.ps == PositionSide.LONG ? PriceUtil.rectificationCutLossLongPrice_v3(hitPrice, cutLoss)
				: PriceUtil.rectificationCutLossShortPrice_v3(hitPrice, cutLoss);
		
		stopLossLimit = PriceUtil.formatDoubleDecimalValue(stopLossLimit, last.getDecimalNum());
		
		this.openPrice = new OpenPriceDetails(FibCode.FIB1, hitPrice, stopLossLimit, AutoTradeType.FENCE_SITTER);
	}

	@Override
	public OpenPrice getOpenPrice() {
		return this.openPrice;
	}

	@Override
	public boolean isLong() {
		return this.ps == PositionSide.LONG && this.openPrice != null;
	}

	@Override
	public boolean isShort() {
		return this.ps == PositionSide.SHORT && this.openPrice != null;
	}

	@Override
	public boolean isClosePosition() {
		boolean result = false;
		return result;
	}

}
