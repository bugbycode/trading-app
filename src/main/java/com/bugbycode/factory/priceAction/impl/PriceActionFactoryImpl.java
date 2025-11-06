package com.bugbycode.factory.priceAction.impl;

import java.util.ArrayList;
import java.util.List;

import org.springframework.util.CollectionUtils;

import com.bugbycode.factory.fibInfo.impl.FibInfoFactoryImpl;
import com.bugbycode.factory.priceAction.PriceActionFactory;
import com.bugbycode.module.FibCode;
import com.bugbycode.module.FibInfo;
import com.bugbycode.module.FibLevel;
import com.bugbycode.module.Klines;
import com.bugbycode.module.MarketSentiment;
import com.bugbycode.module.QuotationMode;
import com.bugbycode.module.SortType;
import com.bugbycode.module.price.OpenPrice;
import com.bugbycode.module.price.impl.OpenPriceDetails;
import com.util.KlinesComparator;
import com.util.PriceComparator;
import com.util.PriceUtil;

/**
 * 价格行为指标接口实现类
 */
public class PriceActionFactoryImpl implements PriceActionFactory{
	
	private List<Klines> list;
	
	private List<Klines> fibAfterKlines;
	
	private FibInfo fibInfo;
	
	private List<Klines> list_15m;//十五分钟级别k线 用于补充回撤之后的k线信息
	
	private List<OpenPrice> openPrices;
	
	public PriceActionFactoryImpl(List<Klines> list, List<Klines> list_15m) {
		this.list = new ArrayList<Klines>();
		this.list_15m = new ArrayList<Klines>();
		this.openPrices = new ArrayList<OpenPrice>();
		this.fibAfterKlines = new ArrayList<Klines>();
		if(!CollectionUtils.isEmpty(list_15m)) {
			this.list_15m.addAll(list_15m);
		}
		if(!CollectionUtils.isEmpty(list)) {
			this.list.addAll(list);
			this.init();
		}
	}
	
	@Override
	public FibInfo getFibInfo() {
		return fibInfo;
	}

	@Override
	public List<Klines> getFibAfterKlines() {
		return fibAfterKlines;
	}

	@Override
	public List<OpenPrice> getOpenPrices() {
		return openPrices;
	}

	@Override
	public boolean isLong() {
		return fibInfo != null && fibInfo.getQuotationMode() == QuotationMode.SHORT;
	}

	@Override
	public boolean isShort() {
		return fibInfo != null && fibInfo.getQuotationMode() == QuotationMode.LONG;
	}

	private void init() {
		if(CollectionUtils.isEmpty(list) || list.size() < 50 || CollectionUtils.isEmpty(list_15m)) {
			return;
		}
		
		KlinesComparator kc = new KlinesComparator(SortType.ASC);
		this.list.sort(kc);
		this.list_15m.sort(kc);
		
		PriceUtil.calculateBollingerBands(list);
		PriceUtil.calculateEMA_7_25_99(list);
		PriceUtil.calculateMACD(list);
		
		this.openPrices.clear();
		this.fibAfterKlines.clear();
		
		FibInfoFactoryImpl factory = new FibInfoFactoryImpl(list, list, list_15m);
		if(!(factory.isLong() || factory.isShort())) {
			return;
		}
		
		FibInfo parentFibInfo = factory.getFibInfo();
		List<Klines> parentAfterKlines = factory.getFibAfterKlines();
		if(CollectionUtils.isEmpty(parentAfterKlines)) {
			return;
		}
		
		QuotationMode parentMode = parentFibInfo.getQuotationMode();
		
		MarketSentiment ms = new MarketSentiment(parentAfterKlines);
		Klines fibEnd = null;
		if(parentMode == QuotationMode.LONG) {
			fibEnd = ms.getLow();
			this.fibInfo = new FibInfo(parentFibInfo.getFibValue(FibCode.FIB0), ms.getLowPrice(), parentFibInfo.getDecimalPoint(), FibLevel.LEVEL_0);
		} else {
			fibEnd = ms.getHigh();
			this.fibInfo = new FibInfo(parentFibInfo.getFibValue(FibCode.FIB0), ms.getHighPrice(), parentFibInfo.getDecimalPoint(), FibLevel.LEVEL_0);
		}
		
		QuotationMode mode = this.fibInfo.getQuotationMode();
		
		//Klines fibAfterFlag = PriceUtil.getAfterKlines(fibEnd, this.list_15m);
		//if(fibAfterFlag != null) {
			this.fibAfterKlines.addAll(PriceUtil.subList(fibEnd, this.list_15m));
			this.fibInfo.setFibAfterKlines(fibAfterKlines);
		//}
		
		List<OpenPrice> openPriceList = factory.getOpenPrices();
		for(OpenPrice p : openPriceList) {
			addPrices(new OpenPriceDetails(fibInfo.getFibCode(p.getPrice()), p.getPrice()));
		}
		
		if(mode == QuotationMode.LONG) {
			this.openPrices.sort(new PriceComparator(SortType.ASC));
		} else {
			this.openPrices.sort(new PriceComparator(SortType.DESC));
		}
		
	}
	
	private void addPrices(OpenPrice price) {
		if(fibInfo != null && FibCode.FIB4_618.gt(fibInfo.getFibCode(price.getPrice()))) {
			if(!PriceUtil.contains(openPrices, price)) {
				openPrices.add(price);
			}
		}
	}
}