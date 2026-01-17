package com.bugbycode.module.price;

import com.bugbycode.module.FibCode;
import com.bugbycode.module.QuotationMode;

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
	
	/**
	 * 获取止盈点 (盘整区指标)
	 * @param price 当前价格
	 * @param openPrice 开仓价
	 * @param profit 用户盈利预期
	 * @param profitLimit 用户止盈百分比限制
	 * @param mode LONG/SHORT
	 * @return
	 */
	public double getAreaTakeProfit(double price, OpenPrice openPrice, double profit, double profitLimit, QuotationMode mode);
}
