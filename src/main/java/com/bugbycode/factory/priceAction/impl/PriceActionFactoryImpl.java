package com.bugbycode.factory.priceAction.impl;

import java.util.ArrayList;
import java.util.List;

import org.springframework.util.CollectionUtils;

import com.bugbycode.factory.priceAction.PriceActionFactory;
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
 * 价格行为指标接口实现类
 */
public class PriceActionFactoryImpl implements PriceActionFactory{
	
	private List<Klines> list;
	
	private List<Klines> list_15m;//十五分钟级别k线 用于补充回撤之后的k线信息
	
	private OpenPrice openPrice;
	
	private PositionSide ps = PositionSide.DEFAULT;
	
	public PriceActionFactoryImpl(List<Klines> list, List<Klines> list_15m) {
		this.list = new ArrayList<Klines>();
		this.list_15m = new ArrayList<Klines>();
		if(!CollectionUtils.isEmpty(list_15m)) {
			this.list_15m.addAll(list_15m);
		}
		if(!CollectionUtils.isEmpty(list)) {
			this.list.addAll(list);
			this.init();
		}
	}
	
	private void init() {
		if(list.size() < 3 || CollectionUtils.isEmpty(list_15m)) {
			return;
		}
		
		KlinesComparator kc = new KlinesComparator(SortType.ASC);
		this.list.sort(kc);
		this.list_15m.sort(kc);
		
		ps = getPositionSide();
		
		if(ps == PositionSide.DEFAULT) {
			return;
		}
		
		Klines current = null;
		Klines parent = null;
		
		for(int index = list.size() - 1; index > 0;index--) {
			current = list.get(index);
			parent = list.get(index - 1);
			if((ps == PositionSide.LONG && PriceUtil.verifyDeclining_v28(current, parent)) 
					|| (ps == PositionSide.SHORT && PriceUtil.verifyPowerful_v28(current, parent))) {
				break;
			}
		}
		
		if(current == null || parent == null) {
			return;
		}
		
		Klines start = PriceUtil.getAfterKlines(current, list);
		if(start == null) {
			return;
		}
		
		current = null;
		parent = null;
		
		List<Klines> data = PriceUtil.subList(start, list);
		Klines hitKlines = ps == PositionSide.LONG ? PriceUtil.getMinClosePriceKLine(data) : PriceUtil.getMaxClosePriceKLine(data);
		if(hitKlines == null) {
			return;
		}
		
		for(int index = list.size() - 1; index > 0; index--) {
			current = list.get(index);
			parent = list.get(index - 1);
			if(current.isEquals(hitKlines)) {
				break;
			}
		}
		
		if(current == null || parent == null) {
			return;
		}
		
		Klines stopLossKlines = current;
		if((ps == PositionSide.LONG && stopLossKlines.isFall())
				|| (ps == PositionSide.SHORT && stopLossKlines.isRise())) {
			stopLossKlines = parent;
		}
		
		double openPriceValue = current.getClosePriceDoubleValue();
		double stopLossLimit = ps == PositionSide.LONG ? stopLossKlines.getLowPriceDoubleValue() : stopLossKlines.getHighPriceDoubleValue();
		
		this.openPrice = new OpenPriceDetails(FibCode.FIB1, openPriceValue, stopLossLimit, AutoTradeType.PRICE_ACTION);
	}
	
	private PositionSide getPositionSide() {
		PositionSide ps = PositionSide.DEFAULT;
		for(int index = list.size() - 1; index > 0; index--) {
			Klines current = list.get(index);
			Klines parent = list.get(index - 1);
			if(PriceUtil.verifyPowerful_v28(current, parent)) {
				ps = PositionSide.LONG;
				break;
			} else if(PriceUtil.verifyDeclining_v28(current, parent)) {
				ps = PositionSide.SHORT;
				break;
			}
		}
		
		return ps;
	}
	
	@Override
	public boolean isLong() {
		boolean result = false;
		if(this.ps == PositionSide.LONG && this.openPrice != null) {
			result = true;
		}
		return result;
	}
	
	@Override
	public boolean isShort() {
		boolean result = false;
		if(this.ps == PositionSide.SHORT && this.openPrice != null) {
			result = true;
		}
		return result;
	}

	@Override
	public OpenPrice getOpenPrice() {
		return this.openPrice;
	}
	
}