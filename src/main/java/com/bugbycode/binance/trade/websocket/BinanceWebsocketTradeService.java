package com.bugbycode.binance.trade.websocket;

import java.math.BigDecimal;
import java.util.List;

import com.bugbycode.binance.module.order_cancel.OrderCancelResult;
import com.bugbycode.module.binance.Balance;
import com.bugbycode.module.binance.BinanceOrderInfo;
import com.bugbycode.module.binance.CallbackRateEnabled;
import com.bugbycode.module.binance.PriceInfo;
import com.bugbycode.module.binance.ProfitOrderEnabled;
import com.bugbycode.module.binance.WorkingType;
import com.bugbycode.module.trading.PositionSide;
import com.bugbycode.module.trading.Side;
import com.bugbycode.module.trading.Type;

/**
 * 币安交易管理接口 websocket
 */
public interface BinanceWebsocketTradeService {

	/**
	 * 账户余额V2 (USER_DATA)
	 * @param apiKey
	 * @param secretKey
	 */
	public List<Balance> balance_v2(String apiKey,String secretKey);
	
	/**
	 * 获取当前价格
	 * @param symbol 交易对
	 * @return
	 */
	public PriceInfo getPrice(String symbol);
	
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
	 * @param activationPrice 追踪止损激活价格，仅TRAILING_STOP_MARKET 需要此参数, 默认为下单当前市场价格(支持不同workingType)
	 * @param callbackRate 追踪止损回调比例，可取值范围[0.1, 10],其中 1代表1% ,仅TRAILING_STOP_MARKET 需要此参数
	 * @return
	 */
	public BinanceOrderInfo order_place(String binanceApiKey,String binanceSecretKey, String symbol,Side side,PositionSide ps,Type type,String newClientOrderId,
			BigDecimal quantity,BigDecimal price,BigDecimal stopPrice,Boolean closePosition,WorkingType workingType,
			BigDecimal activationPrice, BigDecimal callbackRate);
	
	/**
	 * 获取可下单余额
	 * @param apiKey
	 * @param secretKey
	 * @param asset 资产类型 如：USDT、BTC
	 * @return
	 */
	public String availableBalance(String apiKey, String secretKey, String asset);
	
	/**
	 * 合约市价开仓
	 * @param binanceApiKey
	 * @param binanceSecretKey
	 * @param symbol 交易对
	 * @param ps 持仓方向 LONG/SHORT
	 * @param quantity 委托数量
	 * @param stopLoss 止损价
	 * @param takeProfit 止盈价
	 * @param callbackRateEnabled 是否启用追踪止损
	 * @param activationPrice 追踪止损触发价
	 * @param callbackRate 追踪止损回调比例，可取值范围[0.1, 10],其中 1代表1% ,仅TRAILING_STOP_MARKET 需要此参数
	 * @param profitOrderEnabled 是否启用止盈订单
	 * @return
	 */
	public List<BinanceOrderInfo> tradeMarket(String binanceApiKey,String binanceSecretKey,String symbol,PositionSide ps,
			BigDecimal quantity,BigDecimal stopLoss,BigDecimal takeProfit, CallbackRateEnabled callbackRateEnabled, 
			BigDecimal activationPrice, BigDecimal callbackRate, ProfitOrderEnabled profitOrderEnabled);
	
	/**
	 * 获取市价单最少下单数量
	 * @param symbol 交易对
	 * @return
	 */
	public String getMarketMinQuantity(String symbol);
	
	/**
	 * 撤销订单
	 * @param binanceApiKey
	 * @param binanceSecretKey
	 * @param symbol 交易对
	 * @param orderId 订单编号
	 * @return 返回撤销结果
	 */
	public OrderCancelResult orderCancel(String binanceApiKey, String binanceSecretKey, String symbol, long orderId);
}
