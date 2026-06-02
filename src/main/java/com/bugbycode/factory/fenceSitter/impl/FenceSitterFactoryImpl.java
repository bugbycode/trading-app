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

public class FenceSitterFactoryImpl implements FenceSitterFactory{

	private OpenPrice openPrice;
	
	private List<Klines> list;
	
	private PositionSide ps = PositionSide.DEFAULT;
	
	public FenceSitterFactoryImpl(List<Klines> list) {
		this.list = new ArrayList<Klines>();
		if(!CollectionUtils.isEmpty(list)) {
			this.list.addAll(list);
		}
		this.init();
	}
	
	private void init() {
		if(CollectionUtils.isEmpty(list) || list.size() < 2) {
			return;
		}
		
		KlinesComparator kc = new KlinesComparator(SortType.ASC);
		this.list.sort(kc);
		
		PriceUtil.calculateAllBBPercentB(list);
		
		Klines current = null;
		Klines parent = null;
		
		for(int index = list.size() - 1; index >= 0; index--) {
			current = list.get(index);
			parent = list.get(index - 1);
			if(PriceUtil.verifyPowerful_v28(current, parent)) {
				this.ps = PositionSide.LONG;
				break;
			} else if(PriceUtil.verifyDeclining_v28(current, parent)) {
				this.ps = PositionSide.SHORT;
				break;
			}
		}
		
		if(this.ps == PositionSide.DEFAULT) {
			return;
		}
		
		double priceValue = current.getClosePriceDoubleValue();
		double bodyLen = current.getBodyHighPriceDoubleValue() - current.getBodyLowPriceDoubleValue();
		double takeProfitPrice = PriceUtil.formatDoubleDecimalValue(this.ps == PositionSide.LONG ? priceValue + bodyLen : priceValue - bodyLen, current.getDecimalNum());
		Klines stopLossKlines = current;
		if(this.ps == PositionSide.LONG && current.isFall()) {
			stopLossKlines = parent;
		} else if(this.ps == PositionSide.SHORT && current.isRise()) {
			stopLossKlines = parent;
		}
		double stopLossLimit = this.ps == PositionSide.LONG ? stopLossKlines.getLowPriceDoubleValue() : stopLossKlines.getHighPriceDoubleValue();
		/*
		FibInfo stopLossFibInfo = new FibInfo(priceValue, takeProfitPrice, current.getDecimalNum());
		double sf_limit = stopLossFibInfo.getFibValue(FibCode.FIB1_618);
		if(this.ps == PositionSide.LONG) {
			stopLossLimit = PriceUtil.getMaxPrice(stopLossLimit, sf_limit);
		} else if(this.ps == PositionSide.SHORT) {
			stopLossLimit = PriceUtil.getMinPrice(stopLossLimit, sf_limit);
		}
		*/
		this.openPrice = new OpenPriceDetails(FibCode.FIB618, priceValue, stopLossLimit, takeProfitPrice, takeProfitPrice, AutoTradeType.FENCE_SITTER);
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

}
