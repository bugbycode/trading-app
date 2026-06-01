package com.bugbycode.factory.fenceSitter.impl;

import java.util.ArrayList;
import java.util.List;

import org.springframework.util.CollectionUtils;

import com.bugbycode.factory.fenceSitter.FenceSitterFactory;
import com.bugbycode.module.FibCode;
import com.bugbycode.module.FibInfo;
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
	
	private List<Klines> list_15m;
	
	private PositionSide ps = PositionSide.DEFAULT;
	
	public FenceSitterFactoryImpl(List<Klines> list, List<Klines> list_15m) {
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
		if(CollectionUtils.isEmpty(list) || list.size() < 1 || CollectionUtils.isEmpty(list_15m)) {
			return;
		}
		
		KlinesComparator kc = new KlinesComparator(SortType.ASC);
		this.list.sort(kc);
		this.list_15m.sort(kc);
		
		Klines last = PriceUtil.getLastKlines(list);
		double hitPrice = last.getClosePriceDoubleValue();
		
		Klines last_after_15m = PriceUtil.getAfterKlines(last, list_15m);
		if(last_after_15m == null) {
			return;
		}
		
		Klines hitK = null;
		for(int index = list_15m.size() - 1; index >= 0; index--) {
			Klines current = list_15m.get(index);
			if(PriceUtil.isBreachLong(current, hitPrice)) {
				hitK = current;
				this.ps = PositionSide.LONG;
				break;
			} else if(PriceUtil.isBreachShort(current, hitPrice)) {
				hitK = current;
				this.ps = PositionSide.SHORT;
				break;
			}
			if(current.lte(last_after_15m)) {
				break;
			}
		}
		
		if(hitK == null || this.ps == PositionSide.DEFAULT) {
			hitK = last_after_15m;
			if(hitK.isRise()) {
				this.ps = PositionSide.LONG;
			} else {
				this.ps = PositionSide.SHORT;
			}
		}
		
		if(this.ps == PositionSide.DEFAULT) {
			return;
		}
		
		int decimalNum = last.getDecimalNum();
		
		double bodyLen = last.getBodyHighPriceDoubleValue() - last.getBodyLowPriceDoubleValue();
		double takeProfitPrice = PriceUtil.formatDoubleDecimalValue(this.ps == PositionSide.LONG ? hitPrice + bodyLen : hitPrice - bodyLen, decimalNum);
		double openPriceValue = this.ps == PositionSide.LONG ? hitK.getBodyHighPriceDoubleValue() : hitK.getBodyLowPriceDoubleValue();
		double stopLossLimit = this.ps == PositionSide.LONG ? hitK.getLowPriceDoubleValue() : hitK.getHighPriceDoubleValue();
		
		FibInfo stopLossFibInfo = new FibInfo(hitPrice, takeProfitPrice, decimalNum);
		double sf_limit = stopLossFibInfo.getFibValue(FibCode.FIB1_272);
		
		if(this.ps == PositionSide.LONG) {
			stopLossLimit = PriceUtil.getMaxPrice(stopLossLimit, sf_limit);
		} else {
			stopLossLimit = PriceUtil.getMinPrice(stopLossLimit, sf_limit);
		}
		
		this.openPrice = new OpenPriceDetails(FibCode.FIB1, openPriceValue, stopLossLimit, takeProfitPrice, takeProfitPrice, AutoTradeType.FENCE_SITTER);
		
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
