package com.bugbycode.module;

import java.util.ArrayList;
import java.util.List;

import com.util.PriceUtil;

public class PriceActionInfo {

	private Klines current;
	
	private Klines parent;
	
	private Klines next;
	
	private List<Klines> data;
	
	private Klines low;
	
	private Klines high;
	
	private PriceActionType type;
	
	public PriceActionInfo(Klines current, Klines parent, Klines next, PriceActionType type) {
		this.current = current;
		this.parent = parent;
		this.next = next;
		this.type = type;
		this.data = new ArrayList<Klines>();
		this.data.add(this.current);
		this.data.add(this.parent);
		this.data.add(this.next);
		this.low = PriceUtil.getMinPriceKLine(data);
		this.high = PriceUtil.getMaxPriceKLine(data);
	}
	
	public Klines getLow() {
		return low;
	}

	public Klines getHigh() {
		return high;
	}

	public PriceActionType getType() {
		return type;
	}

	public Klines getCurrent() {
		return current;
	}

	public Klines getParent() {
		return parent;
	}
	
}
