package com.bugbycode.factory.area;

import java.util.List;

import com.bugbycode.module.Klines;
import com.bugbycode.module.price.OpenPrice;

/**
 * 盘整区指标接口
 */
public interface AreaFactory {

	/**
	 * 获取开仓价
	 * @return
	 */
	public List<OpenPrice> getOpenPrices();
	
	/**
	 * 获取回撤之后的K线信息
	 * @return
	 */
	public List<Klines> getFibAfterKlines();
	
	public boolean isLong();
	
	public boolean isShort();
	
}
