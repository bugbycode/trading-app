package com.bugbycode.websocket.trading.handler;

import com.bugbycode.module.trading.PositionSide;
import com.bugbycode.module.trading.Side;
import com.bugbycode.websocket.trading.endpoint.TradingWebSocketClientEndpoint;

public interface APIMessageHandler {
	
	public void setClient(TradingWebSocketClientEndpoint client);

	public void handleMessage(String message);
	
	/**
	 * 以市价开仓
	 * 
	 * @param symbol 交易对 如：BTCUSDT
	 * @param side 持仓方向 SELL, BUY
	 * @param positionSide 持仓方向，单向持仓模式下非必填，默认且仅可填BOTH;在双向持仓模式下必填,且仅可选择 LONG 或 SHORT
	 * @param quantity 下单数量 如：0.1BTC
	 * @return
	 */
	public String buyOrSell(String symbol,Side side, PositionSide positionSide,double quantity);
	
	/**
	 * 创建止盈单
	 * @param symbol 交易对 如：BTCUSDT
	 * @param side SELL, BUY
	 * @param positionSide LONG 平多 或 SHORT 平空
	 * @param quantity 下单数量 如：0.1BTC
	 * @param stopPrice 触发价
	 * @return
	 */
	public String takeProfit(String symbol,Side side, PositionSide positionSide, double quantity,double stopPrice);
	
	/**
	 * 创建止损单
	 * @param symbol 交易对 如：BTCUSDT
	 * @param side SELL, BUY
	 * @param positionSide LONG 平多 或 SHORT 平空
	 * @param quantity 下单数量 如：0.1BTC
	 * @param stopPrice 触发价
	 * @return
	 */
	public String stopMarket(String symbol,Side side, PositionSide positionSide, double quantity,double stopPrice);
	
	public void notifyTask();
	
}
