package com.coinkline.repository.high_low_hitprice;

import java.util.List;

import com.coinkline.module.HighOrLowHitPrice;

public interface HighOrLowHitPriceRepository {

	/**
	 * 添加一条数据
	 * @param price
	 */
	public void insert(HighOrLowHitPrice price);
	
	/**
	 * 根据交易对查询一条数据
	 * @param pair
	 * @return
	 */
	public List<HighOrLowHitPrice> find(String pair);
	
	/**
	 * 根据交易对和时间删除在time之前的所有数据
	 * @param pair
	 * @param time
	 */
	public void remove(String pair,long time);
	
	/**
	 * 根据价格查找
	 * @param pair
	 * @param price
	 * @return
	 */
	public HighOrLowHitPrice findByPrice(String pair,double price);
}
