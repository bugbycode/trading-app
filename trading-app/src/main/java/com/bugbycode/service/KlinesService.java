package com.bugbycode.service;

import java.util.Date;
import java.util.List;

import com.bugbycode.module.Klines;

public interface KlinesService {
	
	/**
	 * 根据时间段、时间级别获取k线信息
	 * @param pair 交易对 如：BTCUSDT
	 * @param startTime 起始时间戳
	 * @param endTime 结束时间戳
	 * @param interval 时间级别 参考 com.bugbycode.config.AppConfig.java
	 * @return
	 */
	public List<Klines> continuousKlines(String pair,long startTime,long endTime,String interval);
	

	/**
	 * 查询日线级别K线信息
	 * @param pair 交易对
	 * @param now 时间
	 * @param limit k线数量
	 * @return
	 */
	public List<Klines> continuousKlines1Day(String pair,Date now,int limit);
	
	/**
	 * 查询5分钟级别K线信息
	 * @param pair 交易对
	 * @param now 时间
	 * @param limit k线数量
	 * @return
	 */
	public List<Klines> continuousKlines5M(String pair,Date now,int limit);
}
