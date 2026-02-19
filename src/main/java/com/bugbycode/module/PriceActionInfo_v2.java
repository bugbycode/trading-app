package com.bugbycode.module;

import java.util.ArrayList;
import java.util.List;

import com.util.PriceUtil;

public class PriceActionInfo_v2 {

	private final  Klines current;
	
	private final  Klines parent;
	
	private final Klines low;
	
	private final Klines high;
	
	private final PriceActionType_v2 type;
	
	public PriceActionInfo_v2(Klines current, Klines parent, PriceActionType_v2 type) {
		this.current = current;
		this.parent = parent;
		this.type = type;
		
		List<Klines> data = new ArrayList<Klines>();
		data.add(this.current);
		data.add(this.parent);
		
		this.low = PriceUtil.getMinPriceKLine(data);
		this.high = PriceUtil.getMaxPriceKLine(data);
	}
	
	public Klines getLow() {
		return low;
	}

	public Klines getHigh() {
		return high;
	}

	public PriceActionType_v2 getType() {
		return type;
	}

	public Klines getCurrent() {
		return current;
	}

	public Klines getParent() {
		return parent;
	}
}
