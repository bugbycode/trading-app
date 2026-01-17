package com.bugbycode.factory.area;

import java.util.List;

import com.bugbycode.module.Klines;
import com.bugbycode.module.QuotationMode;
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
	
	/**
	 * 获取止盈点
	 * @param price 当前价格
	 * @param openPrice 开仓价
	 * @param profit 用户盈利预期
	 * @param profitLimit 用户止盈百分比限制
	 * @param mode LONG/SHORT
	 * @return
	 */
	public double getTakeProfit(double price, OpenPrice openPrice, double profit, double profitLimit, QuotationMode mode);
	
}
