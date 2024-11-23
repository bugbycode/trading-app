package com.bugbycode.repository.trading;

import java.util.List;

import com.bugbycode.module.trading.TradingOrder;

/**
 * 订单管理接口
 */
public interface OrderRepository {

	/**
	 * 新建订单
	 * @param order 订单信息
	 * @return
	 */
	public String insert(TradingOrder order);
	
	/**
	 * 更新订单信息
	 * @param id 数据库唯一标识
	 * @param closePrice 平仓价
	 * @param closeTime 平仓时间
	 * @param pnl 盈亏金额
	 */
	public void updateById(String id,double closePrice,long closeTime,double pnl);
	
	/**
	 * 自定义交易对查询条件查询所有关联的订单信息
	 * @param pair 交易对信息 为空则查询所有
	 * @param closeStatus 订单状态 0: 未平仓 1： 已平仓 -1: 查询所有
	 * @param skip 起始记录数 小于0则查询所有
	 * @param limit 分页条数
	 * @return
	 */
	public List<TradingOrder> findAll(String pair,int closeStatus,long skip,int limit);
	
	/**
	 * 自定义交易对查询条件统计所有关联的订单信息数量
	 * @param pair 交易对信息 为空则查询所有
	 * @param closeStatus 订单状态 0: 未平仓 1： 已平仓 -1: 查询所有
	 * @return
	 */
	public long countAll(String pair,int closeStatus);
	
	/**
	 * 根据开仓条件统计当前已持仓的订单数
	 * @param pair 交易对
	 * @param openPrice 开仓价
	 * @param takeProfit 止盈价
	 * @param stopLoss 止损价
	 * @param longOrShort //仓位方向 多：1 空：0
	 * @return
	 */
	public long countOpening(String pair,double openPrice,double takeProfit,double stopLoss,int longOrShort);
}
