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
	
	private PositionSide ps = PositionSide.DEFAULT;
	
	public FenceSitterFactoryImpl_v2(List<Klines> list) {
		this.list = new ArrayList<Klines>();
		
		if(!CollectionUtils.isEmpty(list)) {
			this.list.addAll(list);
		}
		this.init();
	}
	
	private void init() {
		if(CollectionUtils.isEmpty(list) || list.size() < 3) {
			return;
		}
		
		KlinesComparator kc = new KlinesComparator(SortType.ASC);
		this.list.sort(kc);
		
		Klines hit_current = null;
		Klines hit_parent = null;
		
		for(int index = list.size() - 1; index > 1; index--) {
			Klines current = list.get(index);
			Klines parent = list.get(index - 1);
			Klines next = list.get(index - 2);
			if(ps == PositionSide.DEFAULT) {
				if(PriceUtil.verifyPowerful_v28(current, parent)) {
					ps = PositionSide.LONG;
				} else if(PriceUtil.verifyDeclining_v28(current, parent)) {
					ps = PositionSide.SHORT;
				}
			}
			
			if((ps == PositionSide.LONG && PriceUtil.verifyDeclining_v28(parent, next))
					|| (ps == PositionSide.SHORT && PriceUtil.verifyPowerful_v28(parent, next))) {
				hit_current = current;
				hit_parent = parent;
				break;
			}
		}
		
		if(hit_current == null || hit_parent == null) {
			return;
		}
		
		Klines stopLossKlines = hit_current;
		if((ps == PositionSide.LONG && hit_current.isFall())
				|| (ps == PositionSide.SHORT && hit_current.isRise())) {
			stopLossKlines = hit_parent;
		}
		
		int decimalNum = hit_current.getDecimalNum();
		double hitPrice = hit_current.getClosePriceDoubleValue();
		double stopLossLimit = ps == PositionSide.LONG ? stopLossKlines.getLowPriceDoubleValue() : stopLossKlines.getHighPriceDoubleValue();
		double bodyLen = PriceUtil.getMaxPrice(getLen(hit_current), getLen(hit_parent));
		double takeProfitPrice = this.ps == PositionSide.LONG ? hitPrice + bodyLen : hitPrice - bodyLen;
		takeProfitPrice = PriceUtil.formatDoubleDecimalValue(takeProfitPrice, decimalNum);
		
		double openPriceValue = hit_current.getClosePriceDoubleValue();
		this.openPrice = new OpenPriceDetails(FibCode.FIB1, openPriceValue, stopLossLimit, takeProfitPrice, takeProfitPrice, AutoTradeType.FENCE_SITTER);
		this.openPrice.setResetStopLoss(false);
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

	private double getLen(Klines k) {
		return k.isRise() ? k.getHighPriceDoubleValue() - k.getOpenPriceDoubleValue() : k.getOpenPriceDoubleValue() - k.getLowPriceDoubleValue();
	}
}
