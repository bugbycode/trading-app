package com.bugbycode.module;

import java.util.ArrayList;
import java.util.List;

import org.springframework.util.CollectionUtils;

import com.util.PriceUtil;

public class MarketSentiment {

	private List<Klines> list;
	
	private Klines high;
	
	private Klines low;
	
	public MarketSentiment(List<Klines> list) {
		this.list = new ArrayList<Klines>();
		if(!CollectionUtils.isEmpty(list)) {
			this.list.addAll(list);
			this.init();
		}
	}
	
	public MarketSentiment(Klines k) {
		this.list = new ArrayList<Klines>();
		if(k != null) {
			List<Klines> list = new ArrayList<Klines>();
			list.add(k);
			this.list.addAll(list);
			this.init();
		}
	}
	
	private void init() {
		if(CollectionUtils.isEmpty(list)) {
			return;
		}
		
		high = PriceUtil.getMaxPriceKLine(list);
		low = PriceUtil.getMinPriceKLine(list);
	}

	public Klines getHigh() {
		return high;
	}

	public Klines getLow() {
		return low;
	}
	
	public double getHighPrice() {
		return high == null ? 0 : high.getHighPriceDoubleValue();
	}
	
	public double getLowPrice() {
		return low == null ? 0 : low.getLowPriceDoubleValue();
	}
}
