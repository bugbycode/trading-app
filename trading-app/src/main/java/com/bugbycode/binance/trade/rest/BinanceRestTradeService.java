package com.bugbycode.binance.trade.rest;

import java.math.BigDecimal;
import java.util.List;

import com.bugbycode.module.binance.Balance;
import com.bugbycode.module.binance.BinanceOrderInfo;
import com.bugbycode.module.binance.Leverage;
import com.bugbycode.module.binance.MarginType;
import com.bugbycode.module.binance.Result;
import com.bugbycode.module.binance.SymbolConfig;
import com.bugbycode.module.binance.WorkingType;
import com.bugbycode.module.trading.PositionSide;
import com.bugbycode.module.trading.Side;
import com.bugbycode.module.trading.Type;

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
	 * 查询当前挂单 (USER_DATA)
	 * 
	 * @param binanceApiKey
	 * @param binanceSecretKey
	 * @param symbol 交易对
	 * @param orderId 系统订单号 orderId 与 origClientOrderId 中的一个为必填参数
	 * @param origClientOrderId 用户自定义的订单号
	 * @return 查询的订单如果已经成交或取消，将返回报错 "Order does not exist."
	 */
	public BinanceOrderInfo openOrder(String binanceApiKey,String binanceSecretKey,String symbol,long orderId,String origClientOrderId);
	
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
	
	/**
	 * 下单 (TRADE)
	 * @param binanceApiKey
	 * @param binanceSecretKey
	 * @param symbol 交易对
	 * @param side 	买卖方向 SELL, BUY
	 * @param ps 持仓方向，单向持仓模式下非必填，默认且仅可填BOTH;在双向持仓模式下必填,且仅可选择 LONG 或 SHORT
	 * @param type 订单类型 LIMIT, MARKET, STOP, TAKE_PROFIT, STOP_MARKET, TAKE_PROFIT_MARKET, TRAILING_STOP_MARKET
	 * @param newClientOrderId 	用户自定义的订单号，不可以重复出现在挂单中。如空缺系统会自动赋值。必须满足正则规则 ^[\.A-Z\:/a-z0-9_-]{1,36}$
	 * @param quantity 下单数量 使用closePosition不支持此参数。
	 * @param price 委托价格 type为LIMIT时必填
	 * @param stopPrice 触发价, 仅 STOP, STOP_MARKET, TAKE_PROFIT, TAKE_PROFIT_MARKET 需要此参数 创建止盈止损订单时使用
	 * @param closePosition 触发stopPrice后是否全部平仓 仅支持STOP_MARKET和TAKE_PROFIT_MARKET；不与quantity合用；自带只平仓效果，不与reduceOnly 合用 止盈止损订单时使用
	 * @param workingType stopPrice 触发类型: MARK_PRICE(标记价格), CONTRACT_PRICE(合约最新价). 默认 CONTRACT_PRICE
	 * @return
	 */
	public BinanceOrderInfo orderPost(String binanceApiKey,String binanceSecretKey,String symbol,Side side,PositionSide ps,Type type,String newClientOrderId,
			BigDecimal quantity,BigDecimal price,BigDecimal stopPrice,Boolean closePosition,WorkingType workingType);
	
	/**
	 * 撤销订单
	 * @param binanceApiKey
	 * @param binanceSecretKey
	 * @param symbol 交易对
	 * @param orderId 系统订单号
	 * @param origClientOrderId 用户自定义的订单号
	 * @return
	 */
	public BinanceOrderInfo orderDelete(String binanceApiKey,String binanceSecretKey,String symbol,long orderId,String origClientOrderId);
	
	/**
	 * 账户余额V3 (USER_DATA)
	 * @param binanceApiKey
	 * @param binanceSecretKey
	 */
	public List<Balance> balance_v3(String binanceApiKey,String binanceSecretKey);
	
	/**
	 * 交易对配置 (USER_DATA) 查询交易对上的基础配置
	 * @param binanceApiKey
	 * @param binanceSecretKey
	 * @param symbol
	 * @return
	 */
	public List<SymbolConfig> getSymbolConfig(String binanceApiKey,String binanceSecretKey,String symbol);
	
	/**
	 * 交易对配置 (USER_DATA) 查询交易对上的基础配置
	 * @param binanceApiKey
	 * @param binanceSecretKey
	 * @param symbol
	 * @return
	 */
	public SymbolConfig getSymbolConfigBySymbol(String binanceApiKey,String binanceSecretKey,String symbol);
	
}
