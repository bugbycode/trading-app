package com.bugbycode.factory.priceAction;

import com.bugbycode.module.price.OpenPrice;

/**
 * 价格行为指标接口
 */
public interface PriceActionFactory {

	/**
	 * 获取开仓点
	 * @return
	 */
	public OpenPrice getOpenPrice();
	

	/**
	 * 判断是否为多头行情
	 * @return
	 */
	public boolean isLong();
	
	/**
	 * 判断是否为空头行情
	 * @return
	 */
	public boolean isShort();
}
