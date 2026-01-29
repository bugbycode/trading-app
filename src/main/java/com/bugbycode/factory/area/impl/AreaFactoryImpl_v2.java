package com.bugbycode.factory.area.impl;

import java.util.ArrayList;
import java.util.List;

import org.springframework.util.CollectionUtils;

import com.bugbycode.factory.area.AreaFactory;
import com.bugbycode.module.FibCode;
import com.bugbycode.module.Klines;
import com.bugbycode.module.MarketSentiment;
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
		
		Klines current = null;
		Klines parent = null;
		
		for(int index = list.size() - 1; index > 0; index--) {
			current = list.get(index);
			parent = list.get(index - 1);
			
			if(PriceUtil.isRise_v4(current, parent)) {
				ps = PositionSide.LONG;
				break;
			} else if(PriceUtil.isFall_v4(current, parent)) {
				ps = PositionSide.SHORT;
				break;
			}
			
			current = null;
			parent = null;
		}
		
		if(current == null || parent == null) {
			return;
		}
		
		FibCode openCode = FibCode.FIB618;
		List<Klines> data = new ArrayList<Klines>();
		data.add(current);
		data.add(parent);
		
		MarketSentiment ms = new MarketSentiment(data);
		
		double closePrice = current.getClosePriceDoubleValue();
		double parentBodyHighPrice = parent.getBodyHighPriceDoubleValue();
		double parentBodyLowPrice = parent.getBodyLowPriceDoubleValue();
		double bodyLowPrice = ms.getMinBodyLowPrice();
		double bodyHighPrice = ms.getMaxBodyHighPrice();
		double lowPrice = ms.getLowPrice();
		double highPrice = ms.getHighPrice();
		
		if(ps == PositionSide.LONG) {//做多
			
			addPrices(new OpenPriceDetails(openCode, closePrice, parentBodyHighPrice, highPrice, highPrice));//收盘价
			addPrices(new OpenPriceDetails(openCode, parentBodyHighPrice, bodyLowPrice, bodyHighPrice, highPrice));//前一根k线最高实体价
			addPrices(new OpenPriceDetails(openCode, bodyLowPrice, lowPrice, bodyHighPrice, highPrice));//最低实体价
		
			if(current.getBodyHighPriceDoubleValue() > parent.getHighPriceDoubleValue()) {//当前K线实体价格高于前一根K线最高价情况
				addPrices(new OpenPriceDetails(openCode, parent.getHighPriceDoubleValue(), parentBodyHighPrice, bodyHighPrice, highPrice));
			}
			
			this.openPrices.sort(new PriceComparator(SortType.DESC));
			
		} else {//做空
			
			addPrices(new OpenPriceDetails(openCode, closePrice, parentBodyLowPrice, lowPrice, lowPrice));//收盘价
			addPrices(new OpenPriceDetails(openCode, parentBodyLowPrice, bodyHighPrice, bodyLowPrice, lowPrice));//前一根K线最低实体价
			addPrices(new OpenPriceDetails(openCode, bodyHighPrice, highPrice, bodyLowPrice, lowPrice)); //最高实体价
			
			if(current.getBodyLowPriceDoubleValue() < parent.getLowPriceDoubleValue()) {//当前k线实体价格低于前一根k线最低价情况
				addPrices(new OpenPriceDetails(openCode, parent.getLowPriceDoubleValue(), parentBodyLowPrice, bodyLowPrice, lowPrice));
			}
			
			this.openPrices.sort(new PriceComparator(SortType.ASC));
			
		}
		
		Klines fibAfterFlag = PriceUtil.getAfterKlines(current, this.list_15m);
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
