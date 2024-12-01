package com.bugbycode.service.klines;

import java.util.Date;
import java.util.List;

import com.bugbycode.module.FibCode;
import com.bugbycode.module.FibInfo;
import com.bugbycode.module.Klines;
import com.bugbycode.module.QUERY_SPLIT;
import com.bugbycode.module.QuotationMode;
import com.bugbycode.module.ShapeInfo;
import com.bugbycode.module.binance.AutoTradeType;
import com.bugbycode.module.result.DeclineAndStrength;
import com.bugbycode.module.trading.PositionSide;

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
	@Deprecated
	public void futuresEMAMonitor(List<Klines> klinesList);

	/**
	 * 监控k线涨跌
	 * @param klinesList
	 */
	public void futuresRiseAndFall(List<Klines> klinesList);

	/**
	 * EMA指标判断涨跌
	 * @param klinesList
	 */
	public void futuresEmaRiseAndFall(List<Klines> klinesList);
	
	/**
	 * 发送邮件
	 * @param subject 主题
	 * @param text 内容
	 * @param recEmail 收件人信息
	 */
	public void sendEmail(String subject,String text,String recEmail);
	
	/**
	 * 水平射线绘图分析
	 * @param klines 当前k线信息
	 * @param info 绘图信息
	 */
	public void horizontalRay(Klines klines,ShapeInfo info);
	
	/**
	 * 盘整区（矩形）绘图分析
	 * @param klines 当前k线信息
	 * @param info 绘图信息
	 */
	public void rectangle(Klines klines,ShapeInfo info);
	
	/**
	 * 射线绘图分析
	 * @param klines 当前k线信息
	 * @param info 绘图信息
	 */
	public void ray(Klines klines,ShapeInfo info);
	
	/**
	 * 平行通道绘图分析
	 * @param klines 当前k线信息
	 * @param info 绘图信息
	 */
	public void parallelChannel(Klines klines,ShapeInfo info);
	
	/**
	 * 三角形绘图分析
	 * @param klines 当前k线信息
	 * @param info 绘图信息
	 */
	public void trianglePattern(Klines klines,ShapeInfo info);
	
	/**
	 * 做多交易计划
	 * @param klines 当前k线信息
	 * @param info 绘图信息
	 */
	public void riskRewardLong(Klines klines,ShapeInfo info);
	
	/**
	 * 做空交易计划
	 * @param klines 当前k线信息
	 * @param info 绘图信息
	 */
	public void riskRewardShort(Klines klines,ShapeInfo info);
	
	/**
	 * 斐波那契回撤绘图分析
	 * @param klines
	 * @param info
	 */
	public void fibRetracement(Klines klines,ShapeInfo info);
	
	/**
	 * 检查是否出现颓势或强势价格行为
	 * @param klinesList
	 */
	public void declineAndStrengthCheck(List<Klines> klinesList);
	
	/**
	 * 验证是否出现颓势或强势价格行为
	 * @param klinesList
	 * @return
	 */
	public DeclineAndStrength<Boolean,QuotationMode> verifyDeclineAndStrength(List<Klines> klinesList);
	
	/**
	 * 平仓
	 * @param klines 当前k线
	 */
	public void closeOrder(Klines klines);
	
	/**
     * 校验K线是否出现重复或缺失
     * @param list k线集合
     * @return true 表示没有重复或缺失 false 表示出现重复或缺失k线
     */
    public boolean checkData(List<Klines> list);
    
    /**
     * 市价交易
     * @param pair 交易对
     * @param ps 持仓方向 LONG / SHORT 
     * @param offset 当前所处斐波那契回撤点位索引
     * @param fibInfo 斐波那契回撤点位信息
     * @param autoTradeType 自动交易参考指标
     */
    public void marketPalce(String pair,PositionSide ps,int offset, FibInfo fibInfo, AutoTradeType autoTradeType);
}
