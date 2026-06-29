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

/**
 * 高频交易
 */
public class FenceSitterFactoryImpl_v2 implements FenceSitterFactory{

	private OpenPrice openPrice;
	
	private List<Klines> list;
	
	private List<Klines> list_15m;
	
	private PositionSide ps = PositionSide.DEFAULT;
	
	public FenceSitterFactoryImpl_v2(List<Klines> list, List<Klines> list_15m) {
		this.list = new ArrayList<Klines>();
		this.list_15m = new ArrayList<Klines>();
		if(!CollectionUtils.isEmpty(list_15m)) {
			this.list_15m.addAll(list_15m);
		}
		if(!CollectionUtils.isEmpty(list)) {
			this.list.addAll(list);
		}
		this.init();
	}
	
	private void init() {
		
		if(CollectionUtils.isEmpty(list) || list.size() < 50 || CollectionUtils.isEmpty(list_15m)) {
			return;
		}
		
		KlinesComparator kc = new KlinesComparator(SortType.ASC);
		this.list.sort(kc);
		
		PriceUtil.calculateAllBBPercentB(list);
		
		Klines hit_c = null;
		Klines hit_p = null;
		for(int index = list.size() - 1; index > 1; index--) {
			Klines current = list.get(index);
			Klines parent = list.get(index - 1);
			Klines next = list.get(index - 2);
			if(this.ps == PositionSide.DEFAULT) {
				if(verifyLong(current, parent)) {
					this.ps = PositionSide.LONG;
				} else if(verifyShort(current, parent)) {
					this.ps = PositionSide.SHORT;
				}
			}
			
			if((this.ps == PositionSide.LONG && PriceUtil.verifyPowerful_v34(current, parent, next)) 
					|| (this.ps == PositionSide.SHORT && PriceUtil.verifyDeclining_v34(current, parent, next))) {
				hit_c = current;
				hit_p = parent;
				break;
			}
		}
		
		if(hit_c == null || hit_p == null) {
			return;
		}
		
		Klines stopLossKlines = this.ps == PositionSide.LONG ? PriceUtil.getMinPriceKlines(hit_c, hit_p) : PriceUtil.getMaxPriceKlines(hit_c, hit_p);
		double stopLossLimit = this.ps == PositionSide.LONG ? stopLossKlines.getLowPriceDoubleValue() : stopLossKlines.getHighPriceDoubleValue();
		
		double openPriceValue = hit_c.getClosePriceDoubleValue();
		
		this.openPrice = new OpenPriceDetails(FibCode.FIB1, openPriceValue, stopLossLimit, AutoTradeType.FENCE_SITTER);
	}
	
	private boolean verifyLong(Klines current, Klines parent) {
		return current.getBbPercentB() > parent.getBbPercentB();
	}
	
	private boolean verifyShort(Klines current, Klines parent) {
		return current.getBbPercentB() < parent.getBbPercentB();
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

}
