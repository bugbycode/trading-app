package com.bugbycode.binance.trade.rest;

import java.util.List;

import com.bugbycode.module.binance.BinanceOrderInfo;
import com.bugbycode.module.binance.Leverage;
import com.bugbycode.module.binance.MarginType;
import com.bugbycode.module.binance.Result;

/**
 * 币安交易管理接口
 */
public interface BinanceRestTradeService {

	/**
	 * 更改持仓模式(TRADE)
	 * 
	 * @param binanceApiKey 
	 * @param binanceSecretKey
	 * @param dualSidePosition "true": 双向持仓模式；"false": 单向持仓模式
	 * @return
	 */
	public Result dualSidePosition(String binanceApiKey,String binanceSecretKey,boolean dualSidePosition);
	
	/**
	 * 查询持仓模式(USER_DATA) 查询用户目前在 所有symbol 合约上的持仓模式：双向持仓或单向持仓。
	 * 
	 * @param binanceApiKey 
	 * @param binanceSecretKey
	 * @return
	 */
	public boolean dualSidePosition(String binanceApiKey,String binanceSecretKey);
	
	/**
	 * 调整开仓杠杆 (TRADE) 调整用户在指定symbol合约的开仓杠杆。
	 * 
	 * @param binanceApiKey 
	 * @param binanceSecretKey
	 * @param symbol 交易对
	 * @param leverage 	目标杠杆倍数：1 到 125 整数
	 * @return
	 */
	public Leverage leverage(String binanceApiKey,String binanceSecretKey,String symbol,int leverage);
	
	/**
	 * 变换逐全仓模式 (TRADE)
	 * 
	 * @param binanceApiKey 
	 * @param binanceSecretKey
	 * @param symbol 交易对
	 * @param marginType 保证金模式 ISOLATED(逐仓), CROSSED(全仓)
	 * @return
	 */
	public Result marginType(String binanceApiKey,String binanceSecretKey,String symbol,MarginType marginType);
	
	/**
	 * 查看当前全部挂单 (USER_DATA)
	 * @param binanceApiKey
	 * @param binanceSecretKey
	 * @param symbol 交易对 不带symbol参数，会返回所有交易对的挂单
	 * @return
	 */
	public List<BinanceOrderInfo> openOrders(String binanceApiKey,String binanceSecretKey,String symbol);
	
	/**
	 * 查询所有订单(包括历史订单) (USER_DATA) </br></br>
	 * 
	 * 请注意，如果订单满足如下条件，不会被查询到：</br>
		1、订单的最终状态为 CANCELED 或者 EXPIRED 并且 订单没有任何的成交记录 并且 订单生成时间 + 3天 < 当前时间</br>
		2、订单创建时间 + 90天 < 当前时间
	 * 
	 * @param binanceApiKey
	 * @param binanceSecretKey
	 * @param symbol 交易对
	 * @param orderId 只返回此orderID及之后的订单，缺省返回最近的订单
	 * @param startTime 起始时间
	 * @param endTime 结束时间
	 * @param limit 返回的结果集数量 默认值:500 最大值:1000
	 * @return
	 */
	public List<BinanceOrderInfo> allOrders(String binanceApiKey,String binanceSecretKey,String symbol,long orderId,
			long startTime,long endTime,int limit);
	
	/**
	 * 查询订单 (USER_DATA) </br></br>
	 * 查询订单状态</br></br>
		请注意，如果订单满足如下条件，不会被查询到：</br>
		1、订单的最终状态为 CANCELED 或者 EXPIRED 并且 订单没有任何的成交记录 并且 订单生成时间 + 3天 < 当前时间</br>
		2、订单创建时间 + 90天 < 当前时间</br></br>
	 * 注意: </br>

		1、至少需要发送 orderId 与 origClientOrderId中的一个</br>
		2、orderId在symbol维度是自增的
	 * @param binanceApiKey
	 * @param binanceSecretKey
	 * @param symbol 交易对
	 * @param orderId 系统订单号
	 * @param origClientOrderId 用户自定义的订单号
	 * @return
	 */
	public BinanceOrderInfo order(String binanceApiKey,String binanceSecretKey,String symbol,long orderId,
			String origClientOrderId);
}
