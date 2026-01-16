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
	public double getStopLoss();
}
