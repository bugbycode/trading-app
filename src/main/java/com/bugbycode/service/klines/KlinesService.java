package com.bugbycode.service.klines;

import java.util.Date;
import java.util.List;

import com.bugbycode.module.FibInfo;
import com.bugbycode.module.Inerval;
import com.bugbycode.module.Klines;
import com.bugbycode.module.QUERY_SPLIT;
import com.bugbycode.module.ShapeInfo;
import com.bugbycode.module.binance.AutoTradeType;
import com.bugbycode.module.trading.PositionSide;
import com.bugbycode.module.user.User;

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
	public List<Klines> continuousKlines(String pair,long startTime,long endTime,Inerval interval,QUERY_SPLIT split);
	

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
	 * @param afterLowKlines 回撤之后的最低k线
	 * @param klinesList_hit 最近时间段内部分k线信息
	 * @return 
	 */
	public boolean openLong(FibInfo fibInfo,Klines afterLowKlines,List<Klines> klinesList_hit);
	
	/**
	 * 合约做空
	 * @param fibInfo 斐波那契回撤参考信息
	 * @param afterHighKlines 回撤之后的最高k线
	 * @param klinesList_hit 最近时间段内部分k线信息
	 * @return 
	 */
	public boolean openShort(FibInfo fibInfo,Klines afterHighKlines,List<Klines> klinesList_hit);
	
	/**
	 * 指数均线监控
	 * @param klinesList
	 */
	public void futuresEmaRiseAndFallMonitor(List<Klines> list_15m);
	
	/**
	 * 斐波那契回撤点位监控
	 * 
	 * @param list_15m
	 */
	public void futuresFibMonitor(List<Klines> list_15m);
	
	/**
	 * 盘整区监控
	 * @param list_1h
	 * @param list_15m
	 */
	public void consolidationAreaMonitor(List<Klines> list_1h, List<Klines> list_15m);
	
	/**
	 * 价格行为监控
	 * @param list_15m
	 */
	public void futuresPriceAction(List<Klines> list_15m);
	
	/**
	 * 发送邮件
	 * @param user 发件人信息（SMTP配置）
	 * @param subject 主题
	 * @param text 内容
	 * @param recEmail 收件人信息
	 */
	public void sendEmail(User user,String subject,String text,String recEmail);
	
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
     * 校验K线是否出现重复或缺失
     * @param list k线集合
     * @return true 表示没有重复或缺失 false 表示出现重复或缺失k线
     */
    public boolean checkData(List<Klines> list);
    
    /**
     * 市价交易
     * @param pair 交易对
     * @param ps 持仓方向 LONG / SHORT 
     * @param stopLossDoubleValue 止损价 fibInfo为null时使用
     * @param takeProfitDoubleValue 止盈价 fibInfo为null时使用
     * @param offset 当前所处斐波那契回撤点位索引
     * @param fibInfo 斐波那契回撤点位信息
     * @param autoTradeType 自动交易参考指标
     * @param decimalNum 价格小数点个数
     */
    public void marketPlace(String pair,PositionSide ps, double stopLossDoubleValue, double takeProfitDoubleValue, int offset, 
    		FibInfo fibInfo, AutoTradeType autoTradeType, int decimalNum);
    
    /**
     * 获取最新k线收盘价
     * @param pair 交易对
     * @param inerval 时间级别
     * @return
     */
    public String getClosePrice(String pair,Inerval inerval);
    
    /**
     * 校验日线级别k线并更新到最新
     * @param list
     * @return 有更新则返回true 否则返回false
     */
    public boolean verifyUpdateDayKlines(List<Klines> list);
    
    /**
     * 量价分析
     * @param list
     */
    public void volumeMonitor(List<Klines> list);
    
    /**
     * 筹码密集区分布
     * @param klines
     * @param info
     */
    public void fixedRangeValumeProfile(Klines klines,ShapeInfo info);
}
