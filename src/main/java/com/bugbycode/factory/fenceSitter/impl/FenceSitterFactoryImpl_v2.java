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
	
	public FenceSitterFactoryImpl_v2(List<Klines> list) {
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
		if(CollectionUtils.isEmpty(list)) {
			return;
		}
		
		KlinesComparator kc = new KlinesComparator(SortType.ASC);
		this.list.sort(kc);
		this.list_15m.sort(kc);
		
		for(int index = list.size() - 1; index > 0; index--) {
			Klines current = list.get(index);
			Klines parent = list.get(index - 1);
			if(this.ps == PositionSide.DEFAULT) {
				if(current.isRise()) {
					this.ps = PositionSide.LONG;
				} else {
					this.ps = PositionSide.SHORT;
				}
			}
			
			if((this.ps == PositionSide.LONG && parent.isFall()) 
					|| (this.ps == PositionSide.SHORT && parent.isRise())) {
				double openPriceValue = current.getClosePriceDoubleValue();
				Klines stopLossKlines = this.ps == PositionSide.LONG ? 
						PriceUtil.getMinPriceKlines(current, parent) : PriceUtil.getMaxPriceKlines(current, parent);
				double stopLossLimit = this.ps == PositionSide.LONG ? 
						stopLossKlines.getLowPriceDoubleValue() : stopLossKlines.getHighPriceDoubleValue();
				
				this.openPrice = new OpenPriceDetails(FibCode.FIB1, openPriceValue, stopLossLimit, AutoTradeType.FENCE_SITTER);
				break;
			}
		}
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
