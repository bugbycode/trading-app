package com.bugbycode.factory.priceAction;

import java.util.List;

import com.bugbycode.module.FibInfo;
import com.bugbycode.module.Klines;
import com.bugbycode.module.price.OpenPrice;

/**
 * 价格行为指标接口
 */
public interface PriceActionFactory {

	/**
	 * 获取回撤信息
	 * @return
	 */
	public FibInfo getFibInfo();
	
	/**
	 * 获取回撤之后的K线信息
	 * @return
	 */
	public List<Klines> getFibAfterKlines();
	
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
