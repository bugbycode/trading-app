package com.bugbycode.factory.eoption;

import java.util.List;

import com.bugbycode.module.Klines;
import com.bugbycode.module.price.OpenPrice;

public interface EoptionFactory {

	/**
	 * 获取开仓价
	 * @return
	 */
	public List<OpenPrice> getOpenPrices();
	
	public List<Klines> getFibAfterKlines();
	
	public boolean isLong();
	
	public boolean isShort();
}
