package com.bugbycode.module.binance;

import org.springframework.data.annotation.Id;

/**
 * 币安交易订单信息
 */
public class BinanceOrderInfo {

	@Id
	private String id;
	
	private long orderId;//订单编号
	
	private String symbol;//交易对
	
	private String status;//订单状态
	
	private String clientOrderId;// 用户自定义的订单号
	
	private String price;//委托价格
	
	private String avgPrice;//平均成交价
	
	private String origQty;//原始委托数量
	
	private String executedQty;//成交量
	
	private String cumQty;//
	
	private String cumQuote;//成交金额
	
	private String timeInForce;//有效方法 订单TIF
	
	private String type;// 订单类型 LIMIT, MARKET, STOP, TAKE_PROFIT, STOP_MARKET, TAKE_PROFIT_MARKET, TRAILING_STOP_MARKET
	
	private boolean reduceOnly;//仅减仓 true, false; 非双开模式下默认false；双开模式下不接受此参数； 使用closePosition不支持此参数。
	
	private boolean closePosition;//true, false；触发后全部平仓，仅支持STOP_MARKET和TAKE_PROFIT_MARKET；不与quantity合用；自带只平仓效果，不与reduceOnly 合用
	
	private String side;//买卖方向 SELL, BUY
	
	private String positionSide;//持仓方向，单向持仓模式下非必填，默认且仅可填BOTH;在双向持仓模式下必填,且仅可选择 LONG 或 SHORT
	
	private String stopPrice;// 触发价，对`TRAILING_STOP_MARKET`无效  仅 STOP, STOP_MARKET, TAKE_PROFIT, TAKE_PROFIT_MARKET 需要此参数
	
	private String workingType;//stopPrice 触发类型: MARK_PRICE(标记价格), CONTRACT_PRICE(合约最新价). 默认 CONTRACT_PRICE
	
	private boolean priceProtect;//条件单触发保护："TRUE","FALSE", 默认"FALSE". 仅 STOP, STOP_MARKET, TAKE_PROFIT, TAKE_PROFIT_MARKET 需要此参数
	
	private String origType;// 触发前订单类型 以什么方式下单 如： 市价（MARKET）
	
	private String priceMatch;//盘口价格下单模式 OPPONENT/ OPPONENT_5/ OPPONENT_10/ OPPONENT_20/QUEUE/ QUEUE_5/ QUEUE_10/ QUEUE_20；不能与price同时传
	
	private String selfTradePreventionMode;//订单自成交保护模式 NONE / EXPIRE_TAKER/ EXPIRE_MAKER/ EXPIRE_BOTH； 默认NONE
	
	private long goodTillDate; //订单TIF为GTD时的自动取消时间 TIF为GTD时订单的自动取消时间， 当timeInforce为GTD时必传；传入的时间戳仅保留秒级精度，毫秒级部分会被自动忽略，时间戳需大于当前时间+600s且小于253402300799000
	
	private long time;//订单时间
	
	private long updateTime;// 更新时间
	
	private String activatePrice;// 跟踪止损激活价格, 仅`TRAILING_STOP_MARKET` 订单返回此字段
	
	private String priceRate;// 跟踪止损回调比例, 仅`TRAILING_STOP_MARKET` 订单返回此字段
	
	private String requestData;//下单请求数据
	
	private String responseData;//下单响应数据

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public long getOrderId() {
		return orderId;
	}

	public void setOrderId(long orderId) {
		this.orderId = orderId;
	}

	public String getSymbol() {
		return symbol;
	}

	public void setSymbol(String symbol) {
		this.symbol = symbol;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getClientOrderId() {
		return clientOrderId;
	}

	public void setClientOrderId(String clientOrderId) {
		this.clientOrderId = clientOrderId;
	}

	public String getPrice() {
		return price;
	}

	public void setPrice(String price) {
		this.price = price;
	}

	public String getAvgPrice() {
		return avgPrice;
	}

	public void setAvgPrice(String avgPrice) {
		this.avgPrice = avgPrice;
	}

	public String getOrigQty() {
		return origQty;
	}

	public void setOrigQty(String origQty) {
		this.origQty = origQty;
	}

	public String getExecutedQty() {
		return executedQty;
	}

	public void setExecutedQty(String executedQty) {
		this.executedQty = executedQty;
	}

	public String getCumQty() {
		return cumQty;
	}

	public void setCumQty(String cumQty) {
		this.cumQty = cumQty;
	}

	public String getCumQuote() {
		return cumQuote;
	}

	public void setCumQuote(String cumQuote) {
		this.cumQuote = cumQuote;
	}

	public String getTimeInForce() {
		return timeInForce;
	}

	public void setTimeInForce(String timeInForce) {
		this.timeInForce = timeInForce;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public boolean isReduceOnly() {
		return reduceOnly;
	}

	public void setReduceOnly(boolean reduceOnly) {
		this.reduceOnly = reduceOnly;
	}

	public boolean isClosePosition() {
		return closePosition;
	}

	public void setClosePosition(boolean closePosition) {
		this.closePosition = closePosition;
	}

	public String getSide() {
		return side;
	}

	public void setSide(String side) {
		this.side = side;
	}

	public String getPositionSide() {
		return positionSide;
	}

	public void setPositionSide(String positionSide) {
		this.positionSide = positionSide;
	}

	public String getStopPrice() {
		return stopPrice;
	}

	public void setStopPrice(String stopPrice) {
		this.stopPrice = stopPrice;
	}

	public String getWorkingType() {
		return workingType;
	}

	public void setWorkingType(String workingType) {
		this.workingType = workingType;
	}

	public boolean isPriceProtect() {
		return priceProtect;
	}

	public void setPriceProtect(boolean priceProtect) {
		this.priceProtect = priceProtect;
	}

	public String getOrigType() {
		return origType;
	}

	public void setOrigType(String origType) {
		this.origType = origType;
	}

	public String getPriceMatch() {
		return priceMatch;
	}

	public void setPriceMatch(String priceMatch) {
		this.priceMatch = priceMatch;
	}

	public String getSelfTradePreventionMode() {
		return selfTradePreventionMode;
	}

	public void setSelfTradePreventionMode(String selfTradePreventionMode) {
		this.selfTradePreventionMode = selfTradePreventionMode;
	}

	public long getGoodTillDate() {
		return goodTillDate;
	}

	public void setGoodTillDate(long goodTillDate) {
		this.goodTillDate = goodTillDate;
	}

	public long getTime() {
		return time;
	}

	public void setTime(long time) {
		this.time = time;
	}

	public long getUpdateTime() {
		return updateTime;
	}

	public void setUpdateTime(long updateTime) {
		this.updateTime = updateTime;
	}

	public String getActivatePrice() {
		return activatePrice;
	}

	public void setActivatePrice(String activatePrice) {
		this.activatePrice = activatePrice;
	}

	public String getPriceRate() {
		return priceRate;
	}

	public void setPriceRate(String priceRate) {
		this.priceRate = priceRate;
	}

	public String getRequestData() {
		return requestData;
	}

	public void setRequestData(String requestData) {
		this.requestData = requestData;
	}

	public String getResponseData() {
		return responseData;
	}

	public void setResponseData(String responseData) {
		this.responseData = responseData;
	}
	
}
