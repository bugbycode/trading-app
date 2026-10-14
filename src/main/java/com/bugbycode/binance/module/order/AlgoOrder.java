package com.bugbycode.binance.module.order;

/**
 * Algo订单（条件订单）
 */
public class AlgoOrder {
	
	private long algoId;//订单编号
	
	private String clientAlgoId;//
	
	private String algoType;//CONDITIONAL
	
	private String orderType;//TAKE_PROFIT 订单类型
	
	private String symbol;//BNBUSDT
	
	private String side;//SELL
	
	private String positionSide;//BOTH
	
	private String timeInForce;//GTC
	
	private String quantity;//0.01
	
	private String algoStatus;//CANCELED
	
	private String actualOrderId;//未触发时为空字符串；触发后为 orderId。
	
	private String actualPrice;//未触发时为 0；成交或部分成交后为平均成交价。
	
	private String actualType;//仅在触发后返回的可选字段。
	
	private String actualQty;//仅在成交或部分成交后返回的可选字段。
	
	private String triggerPrice;//750.000 条件单触发价
	
	private String price;//750.000
	
	private String icebergQuantity;//null
	
	private String tpTriggerPrice;//0.00
	
	private String tpPrice;//0.00
	
	private String slTriggerPrice;//
	
	private String slPrice;//
	
	private String tpOrderType;//
	
	private String selfTradePreventionMode;//EXPIRE_MAKER
	
	private String workingType;//CONTRACT_PRICE
	
	private String priceMatch;//NONE
	
	private boolean closePosition;//false
	
	private boolean priceProtect;//false
	
	private boolean reduceOnly;//false
	
	private long createTime;//
	
	private long updateTime;//
	
	private long triggerTime;//
	
	private long goodTillDate;//

	public long getAlgoId() {
		return algoId;
	}

	public void setAlgoId(long algoId) {
		this.algoId = algoId;
	}

	public String getClientAlgoId() {
		return clientAlgoId;
	}

	public void setClientAlgoId(String clientAlgoId) {
		this.clientAlgoId = clientAlgoId;
	}

	public String getAlgoType() {
		return algoType;
	}

	public void setAlgoType(String algoType) {
		this.algoType = algoType;
	}

	public String getOrderType() {
		return orderType;
	}

	public void setOrderType(String orderType) {
		this.orderType = orderType;
	}

	public String getSymbol() {
		return symbol;
	}

	public void setSymbol(String symbol) {
		this.symbol = symbol;
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

	public String getTimeInForce() {
		return timeInForce;
	}

	public void setTimeInForce(String timeInForce) {
		this.timeInForce = timeInForce;
	}

	public String getQuantity() {
		return quantity;
	}

	public void setQuantity(String quantity) {
		this.quantity = quantity;
	}

	public String getAlgoStatus() {
		return algoStatus;
	}

	public void setAlgoStatus(String algoStatus) {
		this.algoStatus = algoStatus;
	}

	public String getActualOrderId() {
		return actualOrderId;
	}

	public void setActualOrderId(String actualOrderId) {
		this.actualOrderId = actualOrderId;
	}

	public String getActualPrice() {
		return actualPrice;
	}

	public void setActualPrice(String actualPrice) {
		this.actualPrice = actualPrice;
	}

	public String getActualType() {
		return actualType;
	}

	public void setActualType(String actualType) {
		this.actualType = actualType;
	}

	public String getActualQty() {
		return actualQty;
	}

	public void setActualQty(String actualQty) {
		this.actualQty = actualQty;
	}

	public String getTriggerPrice() {
		return triggerPrice;
	}

	public void setTriggerPrice(String triggerPrice) {
		this.triggerPrice = triggerPrice;
	}

	public String getPrice() {
		return price;
	}

	public void setPrice(String price) {
		this.price = price;
	}

	public String getIcebergQuantity() {
		return icebergQuantity;
	}

	public void setIcebergQuantity(String icebergQuantity) {
		this.icebergQuantity = icebergQuantity;
	}

	public String getTpTriggerPrice() {
		return tpTriggerPrice;
	}

	public void setTpTriggerPrice(String tpTriggerPrice) {
		this.tpTriggerPrice = tpTriggerPrice;
	}

	public String getTpPrice() {
		return tpPrice;
	}

	public void setTpPrice(String tpPrice) {
		this.tpPrice = tpPrice;
	}

	public String getSlTriggerPrice() {
		return slTriggerPrice;
	}

	public void setSlTriggerPrice(String slTriggerPrice) {
		this.slTriggerPrice = slTriggerPrice;
	}

	public String getSlPrice() {
		return slPrice;
	}

	public void setSlPrice(String slPrice) {
		this.slPrice = slPrice;
	}

	public String getTpOrderType() {
		return tpOrderType;
	}

	public void setTpOrderType(String tpOrderType) {
		this.tpOrderType = tpOrderType;
	}

	public String getSelfTradePreventionMode() {
		return selfTradePreventionMode;
	}

	public void setSelfTradePreventionMode(String selfTradePreventionMode) {
		this.selfTradePreventionMode = selfTradePreventionMode;
	}

	public String getWorkingType() {
		return workingType;
	}

	public void setWorkingType(String workingType) {
		this.workingType = workingType;
	}

	public String getPriceMatch() {
		return priceMatch;
	}

	public void setPriceMatch(String priceMatch) {
		this.priceMatch = priceMatch;
	}

	public boolean isClosePosition() {
		return closePosition;
	}

	public void setClosePosition(boolean closePosition) {
		this.closePosition = closePosition;
	}

	public boolean isPriceProtect() {
		return priceProtect;
	}

	public void setPriceProtect(boolean priceProtect) {
		this.priceProtect = priceProtect;
	}

	public boolean isReduceOnly() {
		return reduceOnly;
	}

	public void setReduceOnly(boolean reduceOnly) {
		this.reduceOnly = reduceOnly;
	}

	public long getCreateTime() {
		return createTime;
	}

	public void setCreateTime(long createTime) {
		this.createTime = createTime;
	}

	public long getUpdateTime() {
		return updateTime;
	}

	public void setUpdateTime(long updateTime) {
		this.updateTime = updateTime;
	}

	public long getTriggerTime() {
		return triggerTime;
	}

	public void setTriggerTime(long triggerTime) {
		this.triggerTime = triggerTime;
	}

	public long getGoodTillDate() {
		return goodTillDate;
	}

	public void setGoodTillDate(long goodTillDate) {
		this.goodTillDate = goodTillDate;
	}
	
}
