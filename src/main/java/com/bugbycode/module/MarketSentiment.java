package com.bugbycode.module;

import java.util.ArrayList;
import java.util.List;

import org.springframework.util.CollectionUtils;

import com.util.PriceUtil;

public class MarketSentiment {

	private List<Klines> list;
	
	private Klines high;
	
	private Klines low;
	
	private Klines highBody;
	
	private Klines lowBody;
	
	private Klines maxDif;
	
	private Klines minDif;
	
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
		highBody = PriceUtil.getMaxBodyHighPriceKLine(list);
		lowBody = PriceUtil.getMinBodyLowPriceKLine(list);
		maxDif = PriceUtil.getMaxDifKLine(list);
		minDif = PriceUtil.getMinDifKLine(list);
	}
	
	public Klines getMaxDif() {
		return maxDif;
	}
	
	public Klines getMinDif() {
		return minDif;
	}

	public Klines getHigh() {
		return high;
	}

	public Klines getLow() {
		return low;
	}
	
	public Klines getHighBody() {
		return highBody;
	}

	public Klines getLowBody() {
		return lowBody;
	}
	
	public double getHighBodyPrice() {
		return highBody == null ? 0 : highBody.getBodyHighPriceDoubleValue();
	}

	public double getLowBodyPrice() {
		return lowBody == null ? 0 : lowBody.getBodyLowPriceDoubleValue();
	}

	public double getHighPrice() {
		return high == null ? 0 : high.getHighPriceDoubleValue();
	}
	
	public double getLowPrice() {
		return low == null ? 0 : low.getLowPriceDoubleValue();
	}
	
	public double getBodyHighPrice() {
		return high == null ? 0 : high.getBodyHighPriceDoubleValue();
	}
	
	public double getBodyLowPrice() {
		return low == null ? 0 : low.getBodyLowPriceDoubleValue();
	}
	
	public boolean isEmpty() {
		return this.list == null || this.list.isEmpty();
	}
}
