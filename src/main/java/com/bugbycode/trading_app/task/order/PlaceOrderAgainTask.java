package com.bugbycode.trading_app.task.order;

import java.math.BigDecimal;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.bugbycode.binance.trade.websocket.BinanceWebsocketTradeService;
import com.bugbycode.exception.OrderPlaceException;
import com.bugbycode.module.PlaceOrderAgain;
import com.bugbycode.module.binance.WorkingType;
import com.bugbycode.module.trading.PositionSide;
import com.bugbycode.module.trading.Side;
import com.bugbycode.module.trading.Type;

public class PlaceOrderAgainTask implements Runnable{
	
	private final Logger logger = LogManager.getLogger(PlaceOrderAgainTask.class);

	private BinanceWebsocketTradeService binanceWebsocketTradeService;
	
	private String binanceApiKey; 
	
	private String binanceSecretKey; 
	
	private String symbol; 
	
	private Side side;
	
	private PositionSide ps; 
	
	private Type type; 
	
	private String newClientOrderId; 
	
	private BigDecimal quantity; 
	
	private BigDecimal price;
	
	private BigDecimal stopPrice; 
	
	private Boolean closePosition; 
	
	private WorkingType workingType;
	
	private BigDecimal activationPrice; 
	
	private BigDecimal callbackRate;
	
	/**
	 * 
	 * 再次下单 (TRADE)
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
	 */
	public PlaceOrderAgainTask(BinanceWebsocketTradeService binanceWebsocketTradeService, String binanceApiKey,
			String binanceSecretKey, String symbol, Side side, PositionSide ps, Type type, String newClientOrderId,
			BigDecimal quantity, BigDecimal price, BigDecimal stopPrice, Boolean closePosition, WorkingType workingType,
			BigDecimal activationPrice, BigDecimal callbackRate) {
		this.binanceWebsocketTradeService = binanceWebsocketTradeService;
		this.binanceApiKey = binanceApiKey;
		this.binanceSecretKey = binanceSecretKey;
		this.symbol = symbol;
		this.side = side;
		this.ps = ps;
		this.type = type;
		this.newClientOrderId = newClientOrderId;
		this.quantity = quantity;
		this.price = price;
		this.stopPrice = stopPrice;
		this.closePosition = closePosition;
		this.workingType = workingType;
		this.activationPrice = activationPrice;
		this.callbackRate = callbackRate;
	}

	@Override
	public void run() {
		try {
			this.binanceWebsocketTradeService.order_place(binanceApiKey, binanceSecretKey, symbol, side, ps, type, newClientOrderId, 
					quantity, price, stopPrice, closePosition, workingType, activationPrice, callbackRate, PlaceOrderAgain.CLOSE);
		} catch (Exception e) {
			String title = "下单" + symbol + "多头仓位时出现异常";
			//String message = e.getMessage();
			if(e instanceof OrderPlaceException) {
				title = ((OrderPlaceException)e).getTitle();
			}
			logger.error(title, e);
		}
	}

}
