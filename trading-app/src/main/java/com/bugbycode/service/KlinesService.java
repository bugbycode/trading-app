package com.bugbycode.service;

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
	

}
