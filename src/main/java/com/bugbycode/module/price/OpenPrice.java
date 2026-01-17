package com.bugbycode.module.price;

import com.bugbycode.module.FibCode;

/**
 * 开仓价
 */
public interface OpenPrice {

	/**
	 * 获取开仓价格
	 * @return
	 */
	public double getPrice();
	
	/**
	 * 获取开仓价所处的点位
	 * @return
	 */
	public FibCode getCode();
	
	/**
	 * 获取最佳止损点
	 * @return
	 */
	public double getStopLossLimit();
	
	/**
	 * 获取第一止盈点
	 * @return
	 */
	public double getFirstTakeProfit();
	
	/**
	 * 获取第二止盈点
	 * @return
	 */
	public double getSecondTakeProfit();
}
