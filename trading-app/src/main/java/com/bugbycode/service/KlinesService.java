package com.bugbycode.service;

import java.util.List;

import com.bugbycode.module.Klines;

public interface KlinesService {
	
	/**
	 * 获取180天k线信息
	 * @param pair 交易对
	 * @param restBaseUrl rest接口地址
	 * @return
	 */
	public List<Klines> continuousKlines_180d(String pair,String restBaseUrl);
	
	/**
	 * 获取最近三小时k线信息
	 * @param pair 交易对
	 * @param restBaseUrl rest接口地址
	 * @return
	 */
	public List<Klines> continuousKlines_3h(String pair,String restBaseUrl);
	
	/**
	 * 获取前一天所有15分钟级别K线信息
	 * @param pair 交易对
	 * @param restBaseUrl rest接口地址
	 * @return
	 */
	public List<Klines> continuousKlines_last_day_15m(String pair,String restBaseUrl);
	
	/**
	 * 获取最近四根十五分钟级别k线信息
	 * @param pair 交易对
	 * @param restBaseUrl rest接口地址
	 * @return
	 */
	public List<Klines> continuousKlines_last_4_x_15m(String pair,String restBaseUrl);
	
	/**
	 * 获取最近两根周线级别k线信息
	 * @param pair 交易对
	 * @param restBaseUrl rest接口地址
	 * @return
	 */
	public List<Klines> continuousKlines_last_2_x_1w(String pair,String restBaseUrl);
	
	/**
	 * 查询上一周1小时级别k线信息
	 * @param pair 交易对
	 * @param restBaseUrl rest接口地址
	 * @return
	 */
	public List<Klines> continuousKlines_lastweek_1h(String pair,String restBaseUrl);
	
	/**
	 * 获取最近7根15分钟级别k线信息
	 * @param pair
	 * @param restBaseUrl
	 * @return
	 */
	public List<Klines> continuousKlines_last_7_x_15m(String pair,String restBaseUrl);
	
	/**
	 * 获取最近10根15分钟级别k线信息
	 * @param pair
	 * @param restBaseUrl
	 * @return
	 */
	public List<Klines> continuousKlines_last_10_x_15m(String pair,String restBaseUrl);
	
	/**
	 * 获取近两天K线信息
	 * 
	 * @return
	 */
	public List<Klines> continuousKlines_last_2_x_1d(String pair,String restBaseUrl);
	
	/**
	 * 根据时间段获取最近15分钟级别k线信息
	 * @param pair
	 * @param restBaseUrl
	 * @param startTime
	 * @param endTime
	 * @return
	 */
	public List<Klines> continuousKlines_last_15m(String pair,String restBaseUrl,long startTime,long endTime);
	
	/**
	 * 根据时间段、时间级别获取k线信息
	 * @param pair 交易对 如：BTCUSDT
	 * @param restBaseUrl 请求地址
	 * @param startTime 起始时间戳
	 * @param endTime 结束时间戳
	 * @param interval 时间级别 参考 com.bugbycode.config.AppConfig.java
	 * @return
	 */
	public List<Klines> continuousKlines(String pair,String restBaseUrl,long startTime,long endTime,String interval);
	
	/**
	 * 时间级别、k线数量获取最近k线信息
	 * @param pair 交易对 如：BTCUSDT
	 * @param restBaseUrl 请求地址
	 * @param interval 时间级别 参考 com.bugbycode.config.AppConfig.java
	 * @param klinesNumber k线数量
	 * @return
	 */
	//public List<Klines> continuousKlines(String pair,String restBaseUrl,String interval,int klinesNumber);

}
