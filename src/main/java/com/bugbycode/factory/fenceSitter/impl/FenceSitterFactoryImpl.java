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
	
	private List<Klines> list_hit;
	
	private List<Klines> list_15m;
	
	private PositionSide ps = PositionSide.DEFAULT;
	
	public FenceSitterFactoryImpl(List<Klines> list, List<Klines> list_hit, List<Klines> list_15m) {
		this.list = new ArrayList<Klines>();
		this.list_hit = new ArrayList<Klines>();
		this.list_15m = new ArrayList<Klines>();
		if(!CollectionUtils.isEmpty(list)) {
			this.list.addAll(list);
		}
		if(!CollectionUtils.isEmpty(list_hit)) {
			this.list_hit.addAll(list_hit);
		}
		if(!CollectionUtils.isEmpty(list_15m)) {
			this.list_15m.addAll(list_15m);
		}
		this.init();
	}
	
	private void init() {
		if(CollectionUtils.isEmpty(list) || CollectionUtils.isEmpty(list_hit) || CollectionUtils.isEmpty(list_15m)) {
			return;
		}
		
		KlinesComparator kc = new KlinesComparator(SortType.ASC);
		this.list.sort(kc);
		this.list_hit.sort(kc);
		this.list_15m.sort(kc);
		
		int list_index = list.size() - 1;
		Klines list_parent = list.get(list_index - 1);
		Klines last = list.get(list_index);
		Klines last_after_hit = PriceUtil.getAfterKlines(last, list_hit);
		
		if(last_after_hit == null) {
			return;
		}
		
		double hitPrice = last.getClosePriceDoubleValue();
		
		Klines hitK = null;
		
		for(int index = list_hit.size() - 1; index >= 0; index--) {
			Klines current = list_hit.get(index);
			if(PriceUtil.isBreachLong(current, hitPrice)) {
				this.ps = PositionSide.LONG;
				hitK = current;
				break;
			} else if(PriceUtil.isBreachShort(current, hitPrice)) {
				this.ps = PositionSide.SHORT;
				hitK = current;
				break;
			}
			if(current.lte(last_after_hit)) {
				break;
			}
		}
		
		if(hitK == null || this.ps == PositionSide.DEFAULT) {
			hitK = last_after_hit;
			if(hitK.isRise()) {
				this.ps = PositionSide.LONG;
			} else {
				this.ps = PositionSide.SHORT;
			}
		}
		
		int decimalNum = last.getDecimalNum();
		double bodyLen = PriceUtil.getMaxPrice(getLen(last), getLen(list_parent));
		double takeProfitPrice = this.ps == PositionSide.LONG ? hitPrice + bodyLen : hitPrice - bodyLen;
			   takeProfitPrice = PriceUtil.formatDoubleDecimalValue(takeProfitPrice, decimalNum);
			   
	    //double stopLossLimit = this.ps == PositionSide.LONG ? hitK.getLowPriceDoubleValue() : hitK.getHighPriceDoubleValue();
	    FibInfo stopLossFibInfo = new FibInfo(hitPrice, takeProfitPrice, decimalNum);
	    double sf_limit = stopLossFibInfo.getFibValue(FibCode.FIB1_272);
	    /*if(this.ps == PositionSide.LONG) {
	    	stopLossLimit = PriceUtil.getMaxPrice(stopLossLimit, sf_limit);
	    } else {
	    	stopLossLimit = PriceUtil.getMinPrice(stopLossLimit, sf_limit);
	    }
	    
	    double openPriceValue = this.ps == PositionSide.LONG ? hitK.getBodyHighPriceDoubleValue() : hitK.getBodyLowPriceDoubleValue();
	    */
	    
	    double openPriceValue = hitK.getClosePriceDoubleValue();
	    
	    this.openPrice = new OpenPriceDetails(FibCode.FIB1, openPriceValue, sf_limit, takeProfitPrice, takeProfitPrice, AutoTradeType.FENCE_SITTER);
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
		return (k.isRise() ? k.getHighPriceDoubleValue() - k.getOpenPriceDoubleValue() : k.getOpenPriceDoubleValue() - k.getLowPriceDoubleValue()) * 0.786;
	}

	@Override
	public boolean isClosePosition() {
		return false;
	}
}
