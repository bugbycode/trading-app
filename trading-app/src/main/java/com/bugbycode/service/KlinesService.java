package com.bugbycode.service;

import java.util.Date;
import java.util.List;

import com.bugbycode.module.FibInfo;
import com.bugbycode.module.Klines;
import com.bugbycode.module.QUERY_SPLIT;

public interface KlinesService {
	
	/**
	 * 根据时间段、时间级别获取k线信息
	 * @param pair 交易对 如：BTCUSDT
	 * @param startTime 起始时间戳
	 * @param endTime 结束时间戳
	 * @param interval 时间级别 参考 com.bugbycode.module.Inerval.java
	 * @param split 用来判断是否读取所有K线 ALL 所有 NOT_ENDTIME 没有返回最后一根K线
	 * @return
	 */
	public List<Klines> continuousKlines(String pair,long startTime,long endTime,String interval,QUERY_SPLIT split);
	

	/**
	 * 查询日线级别K线信息
	 * @param pair 交易对
	 * @param now 时间
	 * @param limit k线数量
	 * @param split 用来判断是否读取所有K线 ALL 所有 NOT_ENDTIME 没有返回最后一根K线
	 * @return
	 */
	public List<Klines> continuousKlines1Day(String pair,Date now,int limit,QUERY_SPLIT split);
	
	/**
	 * 查询5分钟级别K线信息
	 * @param pair 交易对
	 * @param now 时间
	 * @param limit k线数量
	 * @param split 用来判断是否读取所有K线 ALL 所有 NOT_ENDTIME 没有返回最后一根K线
	 * @return
	 */
	public List<Klines> continuousKlines5M(String pair,Date now,int limit,QUERY_SPLIT split);
	
	/**
	 * 查询15分钟级别K线信息
	 * @param pair 交易对
	 * @param now 时间
	 * @param limit k线数量
	 * @param split 用来判断是否读取所有K线 ALL 所有 NOT_ENDTIME 没有返回最后一根K线
	 * @return
	 */
	public List<Klines> continuousKlines15M(String pair,Date now,int limit,QUERY_SPLIT split);
	
	/**
	 * 合约做多
	 * @param fibInfo 斐波那契回撤参考信息
	 * @param afterLowKlines 回撤之后的最低日线
	 * @param klinesList_hit 最近时间段内部分k线信息
	 */
	public void openLong(FibInfo fibInfo,Klines afterLowKlines,List<Klines> klinesList_hit);
	
	/**
	 * 合约做空
	 * @param fibInfo 斐波那契回撤参考信息
	 * @param afterHighKlines 回撤之后的最高日线
	 * @param klinesList_hit 最近时间段内部分k线信息
	 */
	public void openShort(FibInfo fibInfo,Klines afterHighKlines,List<Klines> klinesList_hit);
	
	
	/**
	 * 发送Fib0价格行为
	 * @param fibInfo 斐波那契回撤信息
	 * @param klinesList_hit 最近时间段内部分k线信息
	 */
	public void sendFib0Email(FibInfo fibInfo,List<Klines> klinesList_hit);
	
	/**
	 * 标志性高低点价格监控
	 * 
	 * @param klinesList
	 * @param klinesList_hit
	 */
	public void futuresHighOrLowMonitor(List<Klines> klinesList,List<Klines> klinesList_hit);
	
	/**
	 * 日线级别斐波那契回撤点位监控
	 * 
	 * @param klinesList
	 * @param klinesList_hit
	 */
	public void futuresFibMonitor(List<Klines> klinesList,List<Klines> klinesList_hit);

	/**
	 * EMA点位监控
	 * @param klinesList
	 */
	public void futuresEMAMonitor(List<Klines> klinesList);

	/**
	 * 监控k线涨跌
	 * @param klinesList
	 */
	public void futuresRiseAndFall(List<Klines> klinesList);
	
	/**
	 * 发送邮件
	 * @param subject 主题
	 * @param text 内容
	 * @param fibInfo 斐波那契回撤指标
	 */
	public void sendEmail(String subject,String text,FibInfo fibInfo);
	
}
