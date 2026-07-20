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
		if(CollectionUtils.isEmpty(list) || list.size() < 25 || CollectionUtils.isEmpty(list_15m)) {
			return;
		}
		
		KlinesComparator kc = new KlinesComparator(SortType.ASC);
		this.list.sort(kc);
		this.list_15m.sort(kc);
		
		PriceUtil.calculateMACD(list);
		
		PositionSide ps_mode = PositionSide.DEFAULT;
		
		double openPriceValue = 0;
		
		for(int index = list.size() - 1; index > 1; index--) {
			Klines current = list.get(index);
			Klines parent = list.get(index - 1);
			Klines next = list.get(index - 2);
			
			if(ps_mode == PositionSide.DEFAULT) {
				if(PriceUtil.verifyPowerful_v14(current, parent)) {
					ps_mode = PositionSide.LONG;
				} else if(PriceUtil.verifyDeclining_v14(current, parent)) {
					ps_mode = PositionSide.SHORT;
				}
			}
			
			if((ps_mode == PositionSide.LONG && PriceUtil.verifyPowerful_v10(current, parent, next))
					|| (ps_mode == PositionSide.SHORT && PriceUtil.verifyDeclining_v10(current, parent, next))) {
				openPriceValue = current.getClosePriceDoubleValue();
				break;
			}
		}
		
		if(ps_mode == PositionSide.DEFAULT || openPriceValue == 0) {
			return;
		}
		
		Klines last_15m = PriceUtil.getLastKlines(list_15m);
		double last_15m_close = last_15m.getClosePriceDoubleValue();
		
		if(ps_mode == PositionSide.LONG && last_15m_close >= openPriceValue) {
			this.ps = PositionSide.LONG;
		} else if(ps_mode == PositionSide.LONG && last_15m_close < openPriceValue) {
			this.ps = PositionSide.SHORT;
		} else if(ps_mode == PositionSide.SHORT && last_15m_close <= openPriceValue) {
			this.ps = PositionSide.SHORT;
		} else if(ps_mode == PositionSide.SHORT && last_15m_close > openPriceValue) {
			this.ps = PositionSide.LONG;
		}
		
		double cutLoss = 10;
		double stopLossLimit = this.ps == PositionSide.LONG ? PriceUtil.rectificationCutLossLongPrice_v3(openPriceValue, cutLoss)
				: PriceUtil.rectificationCutLossShortPrice_v3(openPriceValue, cutLoss);
		
		stopLossLimit = PriceUtil.formatDoubleDecimalValue(stopLossLimit, last_15m.getDecimalNum());
		
		this.openPrice = new OpenPriceDetails(FibCode.FIB1, openPriceValue, stopLossLimit, AutoTradeType.FENCE_SITTER);
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
