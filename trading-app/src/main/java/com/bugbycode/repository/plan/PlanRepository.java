package com.bugbycode.repository.plan;

import java.util.List;

import com.bugbycode.module.TradingPlan;

public interface PlanRepository {

	/**
	 * 添加一条交易计划
	 * @param plan
	 */
	public void insert(TradingPlan plan);
	
	/**
	 * 根据计划ID删除交易计划
	 * @param _id
	 */
	public void deleteById(String _id);
	
	/**
	 * 根据交易计划ID查询交易计划
	 * @param _id
	 * @return TradingPlan
	 */
	public TradingPlan findById(String _id);
	
	/**
	 * 根据交易对、触发价格查询交易计划
	 * @param pair
	 * @param hitPrice
	 * @return
	 */
	public TradingPlan findByPairAndHitPrice(String pair,double hitPrice);
	
	/**
	 * 根据交易对查询所有交易计划
	 * @param pair
	 * @param skip 起始记录
	 * @param limit 查询条数
	 * @return
	 */
	public List<TradingPlan> find(String pair,long skip,int limit);
	
	/**
	 * 根据交易对统计交易计划总记录数
	 * @param pair
	 * @return
	 */
	public long count(String pair);
	
	/**
	 * 查询所有交易计划
	 * @return
	 */
	public List<TradingPlan> findAll();
	
	/**
	 * 根据交易对查询所有交易计划
	 * @param pair
	 * @return
	 */
	public List<TradingPlan> find(String pair);
}
