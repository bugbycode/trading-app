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
	
	private PositionSide ps = PositionSide.DEFAULT;
	
	public FenceSitterFactoryImpl_v2(List<Klines> list) {
		this.list = new ArrayList<Klines>();
		if(!CollectionUtils.isEmpty(list)) {
			this.list.addAll(list);
		}
		this.init();
	}
	
	private void init() {
		if(CollectionUtils.isEmpty(list)) {
			return;
		}
		
		KlinesComparator kc = new KlinesComparator(SortType.ASC);
		this.list.sort(kc);
		
		PriceUtil.calculateEMA_7_25_99(list);
		
		Klines c = null;
		Klines p = null;
		//Klines n = null;
		for(int index = list.size() - 1; index > 0; index--) {
			Klines current = list.get(index);
			Klines parent = list.get(index - 1);
			//Klines next = list.get(index - 2);
			
			if(this.ps == PositionSide.DEFAULT) {
				/*if(PriceUtil.verifyPowerful_v28(current, parent)) {
					this.ps = PositionSide.LONG;
				} else if(PriceUtil.verifyDeclining_v28(current, parent)) {
					this.ps = PositionSide.SHORT;
				}*/
				if(current.getClosePriceDoubleValue() < current.getEma7()) {
					this.ps = PositionSide.SHORT;
				} else if(current.getClosePriceDoubleValue() > current.getEma7()) {
					this.ps = PositionSide.LONG;
				}
			}
			/*
			if((this.ps == PositionSide.LONG && PriceUtil.verifyDeclining_v28(parent, next))
					|| (this.ps == PositionSide.SHORT && PriceUtil.verifyPowerful_v28(parent, next))) {
				c = current;
				p = parent;
				n = next;
				break;
			}*/
			
			if((this.ps == PositionSide.LONG && PriceUtil.verifyPowerful_v29(current, parent))
					|| (this.ps == PositionSide.SHORT && PriceUtil.verifyDeclining_v29(current, parent))) {
				c = current;
				p = parent;
				break;
			}
		}
		/*
		if(c == null || p == null || n == null || this.ps == PositionSide.DEFAULT) {
			return;
		}*/
		
		if(c == null || p == null || this.ps == PositionSide.DEFAULT) {
			return;
		}
		
		List<Klines> data = new ArrayList<Klines>();
		data.add(c);
		data.add(p);
		//data.add(n);
		
		Klines stopLossKlines = this.ps == PositionSide.LONG ? PriceUtil.getMinPriceKLine(data) : PriceUtil.getMaxPriceKLine(data);
		double stopLossLimit = this.ps == PositionSide.LONG ? stopLossKlines.getLowPriceDoubleValue() : stopLossKlines.getHighPriceDoubleValue();
		
		data = PriceUtil.subList(c, list);
		
		Klines openKlines = this.ps == PositionSide.LONG ? PriceUtil.getMinClosePriceKLine(data) : PriceUtil.getMaxClosePriceKLine(data);
		
		double openPriceValue = openKlines.getClosePriceDoubleValue();
		
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
		/*if(isLong() || isShort()) {
			int index = list.size() - 1;
			Klines current = list.get(index);
			Klines parent = list.get(index - 1);
			double closePrice = current.getClosePriceDoubleValue();
			if(closePrice != this.openPrice.getPrice()) {
				if((isLong() && PriceUtil.verifyDeclining_v33(current, parent)) 
						|| (isShort() && PriceUtil.verifyPowerful_v33(current, parent))) {
					result = true;
				}
			}
		}*/
		return result;
	}

}
