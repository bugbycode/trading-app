package com.bugbycode.factory.ema;

import java.util.List;

import com.bugbycode.module.price.OpenPrice;

/**
 * 指数均线交易指标接口信息
 */
public interface EmaTradingFactory {

	/**
	 * 获取开仓点位
	 * @return
	 */
	public List<OpenPrice> getOpenPrices();
	
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
