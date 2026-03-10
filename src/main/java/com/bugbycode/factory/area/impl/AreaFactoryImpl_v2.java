package com.bugbycode.factory.area.impl;

import java.util.ArrayList;
import java.util.List;

import org.springframework.util.CollectionUtils;

import com.bugbycode.factory.area.AreaFactory;
import com.bugbycode.module.FibCode;
import com.bugbycode.module.Klines;
import com.bugbycode.module.PriceActionInfo_v2;
import com.bugbycode.module.SortType;
import com.bugbycode.module.price.OpenPrice;
import com.bugbycode.module.price.impl.OpenPriceDetails;
import com.bugbycode.module.trading.PositionSide;
import com.util.KlinesComparator;
import com.util.PriceComparator;
import com.util.PriceUtil;

public class AreaFactoryImpl_v2 implements AreaFactory {

	private List<Klines> list;
	
	private List<Klines> list_15m;
	
	private List<Klines> fibAfterKlines;
	
	private PositionSide ps;
	
	private List<OpenPrice> openPrices;
	
	public AreaFactoryImpl_v2(List<Klines> list, List<Klines> list_15m) {
		this.ps = PositionSide.DEFAULT;
		this.list = new ArrayList<Klines>();
		this.list_15m = new ArrayList<Klines>();
		this.fibAfterKlines = new ArrayList<Klines>();
		this.openPrices = new ArrayList<OpenPrice>();
		if(!CollectionUtils.isEmpty(list)) {
			this.list.addAll(list);
		}
		if(!CollectionUtils.isEmpty(list_15m)) {
			this.list_15m.addAll(list_15m);
		}
		this.init();
	}
	
	private void init() {
		
		if(this.list.size() < 10 || CollectionUtils.isEmpty(this.list_15m)) {
			return;
		}
		
		KlinesComparator kc = new KlinesComparator(SortType.ASC);
		this.list.sort(kc);
		this.list_15m.sort(kc);
		
		ps = PositionSide.DEFAULT;
		
		PriceActionInfo_v2 info = null;
		
		for(int index = list.size() - 1; index > 0; index--) {
			Klines current = list.get(index);
			Klines parent = list.get(index - 1);
			
			if(PriceUtil.verifyPowerful_v27(current, parent)) {
				ps = PositionSide.LONG;
				info = new PriceActionInfo_v2(current, parent, null);
				break;
			} else if(PriceUtil.verifyDecliningPrice_v27(current, parent)) {
				ps = PositionSide.SHORT;
				info = new PriceActionInfo_v2(current, parent, null);
				break;
			}
			
		}
		
		if(info == null) {
			return;
		}
		
		Klines last_15m = PriceUtil.getLastKlines(list_15m);
		double stopLoss = isLong() ? last_15m.getLowPriceDoubleValue() : last_15m.getHighPriceDoubleValue();
		
		FibCode openCode = FibCode.FIB618;
		double firstTakeProfit;
		double secondTakeProfit;
		if(ps == PositionSide.LONG) {//做多
			
			firstTakeProfit = info.getCurrent().getBodyHighPriceDoubleValue();
			secondTakeProfit = info.getCurrent().getHighPriceDoubleValue();
			
			addPrices(new OpenPriceDetails(openCode, info.getParent().getHighPriceDoubleValue(), stopLoss, firstTakeProfit, secondTakeProfit));
			addPrices(new OpenPriceDetails(openCode, info.getParent().getBodyHighPriceDoubleValue(), stopLoss, firstTakeProfit, secondTakeProfit));
			addPrices(new OpenPriceDetails(openCode, info.getParent().getBodyLowPriceDoubleValue(), stopLoss, firstTakeProfit, secondTakeProfit));
			addPrices(new OpenPriceDetails(openCode, info.getLow().getLowPriceDoubleValue(), stopLoss, firstTakeProfit, secondTakeProfit));
			
			this.openPrices.sort(new PriceComparator(SortType.DESC));
			
		} else {//做空
			
			firstTakeProfit = info.getCurrent().getBodyLowPriceDoubleValue();
			secondTakeProfit = info.getCurrent().getLowPriceDoubleValue();
			
			addPrices(new OpenPriceDetails(openCode, info.getParent().getLowPriceDoubleValue(), stopLoss, firstTakeProfit, secondTakeProfit));
			addPrices(new OpenPriceDetails(openCode, info.getParent().getBodyLowPriceDoubleValue(), stopLoss, firstTakeProfit, secondTakeProfit));
			addPrices(new OpenPriceDetails(openCode, info.getParent().getBodyHighPriceDoubleValue(), stopLoss, firstTakeProfit, secondTakeProfit));
			addPrices(new OpenPriceDetails(openCode, info.getHigh().getHighPriceDoubleValue(), stopLoss, firstTakeProfit, secondTakeProfit));
			
			this.openPrices.sort(new PriceComparator(SortType.ASC));
			
		}
		
		Klines fibAfterFlag = PriceUtil.getAfterKlines(info.getCurrent(), this.list_15m);
		if(fibAfterFlag != null) {
			this.fibAfterKlines.addAll(PriceUtil.subList(fibAfterFlag, this.list_15m));
		}
	}

	private void addPrices(OpenPrice price) {
		if(!PriceUtil.contains(openPrices, price) && price.getCode().gte(FibCode.FIB236)) {
			openPrices.add(price);
		}
	}
	
	@Override
	public List<OpenPrice> getOpenPrices() {
		return this.openPrices;
	}

	@Override
	public List<Klines> getFibAfterKlines() {
		return this.fibAfterKlines;
	}

	@Override
	public boolean isLong() {
		return this.ps == PositionSide.LONG;
	}

	@Override
	public boolean isShort() {
		return this.ps == PositionSide.SHORT;
	}

}
