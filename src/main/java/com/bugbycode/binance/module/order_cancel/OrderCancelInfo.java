package com.bugbycode.binance.module.order_cancel;

import java.io.Serializable;

public class OrderCancelInfo implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -8466731460504651025L;

	private String clientOrderId;

    private String cumQty;

    private String cumQuote;

    private String executedQty;

    private Long orderId;

    private String origQty;

    private String origType;

    private String price;

    private Boolean reduceOnly;

    private String side;

    private String positionSide;

    private String status;

    private String stopPrice;

    private Boolean closePosition;

    private String symbol;

    private String timeInForce;

    private String type;

    private String activatePrice;

    private String priceRate;

    private Long updateTime;

    private String workingType;

    private Boolean priceProtect;

    private String priceMatch;

    private String selfTradePreventionMode;

    private Long goodTillDate;

	public String getClientOrderId() {
		return clientOrderId;
	}

	public void setClientOrderId(String clientOrderId) {
		this.clientOrderId = clientOrderId;
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

	public String getExecutedQty() {
		return executedQty;
	}

	public void setExecutedQty(String executedQty) {
		this.executedQty = executedQty;
	}

	public Long getOrderId() {
		return orderId;
	}

	public void setOrderId(Long orderId) {
		this.orderId = orderId;
	}

	public String getOrigQty() {
		return origQty;
	}

	public void setOrigQty(String origQty) {
		this.origQty = origQty;
	}

	public String getOrigType() {
		return origType;
	}

	public void setOrigType(String origType) {
		this.origType = origType;
	}

	public String getPrice() {
		return price;
	}

	public void setPrice(String price) {
		this.price = price;
	}

	public Boolean getReduceOnly() {
		return reduceOnly;
	}

	public void setReduceOnly(Boolean reduceOnly) {
		this.reduceOnly = reduceOnly;
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

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getStopPrice() {
		return stopPrice;
	}

	public void setStopPrice(String stopPrice) {
		this.stopPrice = stopPrice;
	}

	public Boolean getClosePosition() {
		return closePosition;
	}

	public void setClosePosition(Boolean closePosition) {
		this.closePosition = closePosition;
	}

	public String getSymbol() {
		return symbol;
	}

	public void setSymbol(String symbol) {
		this.symbol = symbol;
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

	public Long getUpdateTime() {
		return updateTime;
	}

	public void setUpdateTime(Long updateTime) {
		this.updateTime = updateTime;
	}

	public String getWorkingType() {
		return workingType;
	}

	public void setWorkingType(String workingType) {
		this.workingType = workingType;
	}

	public Boolean getPriceProtect() {
		return priceProtect;
	}

	public void setPriceProtect(Boolean priceProtect) {
		this.priceProtect = priceProtect;
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

	public Long getGoodTillDate() {
		return goodTillDate;
	}

	public void setGoodTillDate(Long goodTillDate) {
		this.goodTillDate = goodTillDate;
	}
    
}
