package com.bugbycode.factory.area.impl;

import java.util.ArrayList;
import java.util.List;

import org.springframework.util.CollectionUtils;

import com.bugbycode.factory.area.AreaFactory;
import com.bugbycode.module.FibCode;
import com.bugbycode.module.Klines;
import com.bugbycode.module.MarketSentiment;
import com.bugbycode.module.PriceActionInfo;
import com.bugbycode.module.PriceActionType;
import com.bugbycode.module.SortType;
import com.bugbycode.module.price.OpenPrice;
import com.bugbycode.module.price.impl.OpenPriceDetails;
import com.bugbycode.module.trading.PositionSide;
import com.util.KlinesComparator;
import com.util.PriceComparator;
import com.util.PriceUtil;

public class AreaFactoryImpl implements AreaFactory {

	private List<Klines> list;
	
	private List<Klines> list_15m;
	
	private List<Klines> fibAfterKlines;
	
	private PositionSide ps;
	
	private List<OpenPrice> openPrices;
	
	public AreaFactoryImpl(List<Klines> list, List<Klines> list_15m) {
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
		
		PriceActionInfo info = null;
		
		for(int index = list.size() - 1; index > 0; index--) {
			Klines current = list.get(index);
			Klines parent = list.get(index - 1);
			Klines next = list.get(index - 2);
			if(PriceUtil.isPutInto_v2(current, parent, next) || PriceUtil.isPutInto_v3(current, parent, next)) {
				PriceActionType type = PriceActionType.DEFAULT;
				if(current.getBodyLowPriceDoubleValue() < parent.getLowPriceDoubleValue()) {
					type = PriceActionType.BACK;
				}
				info = new PriceActionInfo(current, parent, next, type);
				ps = PositionSide.SHORT;
			} else if(PriceUtil.verifyDecliningPrice_v22(current, parent, next)) {
				info = new PriceActionInfo(current, parent, next, PriceActionType.DECL_POWER);
				ps = PositionSide.SHORT;
			} else if(PriceUtil.isBullishSwallowing_v2(current, parent, next) || PriceUtil.isBullishSwallowing_v3(current, parent, next)) {
				PriceActionType type = PriceActionType.DEFAULT;
				if(current.getBodyHighPriceDoubleValue() > parent.getHighPriceDoubleValue()) {
					type = PriceActionType.BACK;
				}
				info = new PriceActionInfo(current, parent, next, type);
				ps = PositionSide.LONG;
			} else if(PriceUtil.verifyPowerful_v22(current, parent, next)) {
				info = new PriceActionInfo(current, parent, next, PriceActionType.DECL_POWER);
				ps = PositionSide.LONG;
			}
			if(info != null) {
				break;
			}
		}
		
		if(info == null) {
			return;
		}
		
		MarketSentiment ms = null;
		PriceActionType type = PriceActionType.DEFAULT;
		List<Klines> data = new ArrayList<Klines>();
		FibCode openCode = FibCode.FIB618;
		if(isLong()) {
			type = info.getType();
			data.add(info.getCurrent());
			data.add(info.getParent());
			ms = new MarketSentiment(data);
			
			double stopLossLimit = ms.getLowPrice();
			double firstTakeProfit = ms.getBodyHighPrice();
			double secondTakeProfit = ms.getHighPrice();
			
			addPrices(new OpenPriceDetails(openCode, ms.getLowPrice(), stopLossLimit, firstTakeProfit, secondTakeProfit));
			addPrices(new OpenPriceDetails(openCode, ms.getMinBodyLowPrice(), stopLossLimit, firstTakeProfit, secondTakeProfit));
			
			if(type == PriceActionType.DEFAULT || type == PriceActionType.BACK) {
				addPrices(new OpenPriceDetails(openCode, info.getParent().getBodyHighPriceDoubleValue(), ms.getMinBodyLowPrice(), firstTakeProfit, secondTakeProfit));
			}
			
			if(type == PriceActionType.BACK) {
				addPrices(new OpenPriceDetails(openCode, info.getParent().getHighPriceDoubleValue(), info.getParent().getBodyHighPriceDoubleValue(), firstTakeProfit, secondTakeProfit));
			}
			
			if(type == PriceActionType.DECL_POWER) {
				addPrices(new OpenPriceDetails(openCode, info.getCurrent().getClosePriceDoubleValue(), ms.getMinBodyLowPrice(), firstTakeProfit, secondTakeProfit));
			}
			this.openPrices.sort(new PriceComparator(SortType.DESC));
		} else if(isShort()) {
			
			type = info.getType();
			data.add(info.getCurrent());
			data.add(info.getParent());
			ms = new MarketSentiment(data);
			
			double stopLossLimit = ms.getHighPrice();
			double firstTakeProfit = ms.getBodyLowPrice();
			double secondTakeProfit = ms.getLowPrice();
			
			addPrices(new OpenPriceDetails(openCode, ms.getHighPrice(), stopLossLimit, firstTakeProfit, secondTakeProfit));
			addPrices(new OpenPriceDetails(openCode, ms.getMaxBodyHighPrice(), stopLossLimit, firstTakeProfit, secondTakeProfit));
			
			if(type == PriceActionType.DEFAULT || type == PriceActionType.BACK) {
				addPrices(new OpenPriceDetails(openCode, info.getParent().getBodyLowPriceDoubleValue(), ms.getMaxBodyHighPrice(), firstTakeProfit, secondTakeProfit));
			}
			
			if(type == PriceActionType.BACK) {
				addPrices(new OpenPriceDetails(openCode, info.getParent().getLowPriceDoubleValue(), info.getParent().getBodyLowPriceDoubleValue(), firstTakeProfit, secondTakeProfit));
			}
			
			if(type == PriceActionType.DECL_POWER) {
				addPrices(new OpenPriceDetails(openCode, info.getCurrent().getClosePriceDoubleValue(), ms.getMaxBodyHighPrice(), firstTakeProfit, secondTakeProfit));
			}
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
