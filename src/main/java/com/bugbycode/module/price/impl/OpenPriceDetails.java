package com.bugbycode.module.price.impl;

import com.bugbycode.module.AutoClosePosition;
import com.bugbycode.module.DualSidePositionStatus;
import com.bugbycode.module.FibCode;
import com.bugbycode.module.FibInfo;
import com.bugbycode.module.TradeStyle;
import com.bugbycode.module.binance.AutoTrade;
import com.bugbycode.module.binance.AutoTradeType;
import com.bugbycode.module.binance.PriceInfo;
import com.bugbycode.module.price.OpenPrice;

public class OpenPriceDetails implements OpenPrice {

	private FibCode code;
	
	private double price;
	
	private double stopLossLimit;
	
	private double takeProfit;//止盈价
	
	private FibInfo fibInfo;

	private FibInfo childFibInfo;
	
	private AutoTradeType autoTradeType = AutoTradeType.DEFAULT;
	
	private boolean resetStopLoss = true;
	
	private AutoTrade autoTrade = AutoTrade.OPEN;
	
	private AutoClosePosition autoClosePosition = AutoClosePosition.OPEN;
	
	private DualSidePositionStatus dualSidePositionStatus = DualSidePositionStatus.CLOSE;
	
	private TradeStyle tradeStyle = TradeStyle.CONSERVATIVE;
	
	/**
	 * 开仓价格信息
	 * @param code 所处的回撤点
	 * @param price 开仓价
	 */
	public OpenPriceDetails(FibCode code, double price) {
		this.code = code;
		this.price = price;
		this.stopLossLimit = -1;
	}
	
	/**
	 * 开仓价格信息
	 * @param code 所处的回撤点
	 * @param price 开仓价
	 * @param stopLoss 最佳止损点
	 */
	public OpenPriceDetails(FibCode code, double price, double stopLossLimit) {
		this.code = code;
		this.price = price;
		this.stopLossLimit = stopLossLimit;
	}
	
	/**
	 * 开仓价格信息
	 * @param code 所处的回撤点
	 * @param price 开仓价
	 * @param stopLoss 最佳止损
	 * @param autoTradeType 参考指标
	 */
	public OpenPriceDetails(FibCode code, double price, double stopLossLimit, AutoTradeType autoTradeType) {
		this.code = code;
		this.price = price;
		this.stopLossLimit = stopLossLimit;
		this.autoTradeType = autoTradeType;
		this.takeProfit = -1;
	}
	
	/**
	 * 开仓价格信息
	 * @param code 所处的回撤点
	 * @param price 开仓价
	 * @param stopLoss 最佳止损点
	 * @param takeProfit 止盈价
	 */
	public OpenPriceDetails(FibCode code, double price, double stopLossLimit,
			double takeProfit) {
		this.code = code;
		this.price = price;
		this.stopLossLimit = stopLossLimit;
		this.takeProfit = takeProfit;
	}
	
	/**
	 * 开仓价格信息
	 * @param code 所处的回撤点
	 * @param price 开仓价
	 * @param stopLoss 最佳止损点
	 * @param takeProfit 止盈价
	 * @param autoTradeType 参考指标
	 */
	public OpenPriceDetails(FibCode code, double price, double stopLossLimit,
			double takeProfit, AutoTradeType autoTradeType) {
		this.code = code;
		this.price = price;
		this.stopLossLimit = stopLossLimit;
		this.takeProfit = takeProfit;
		this.autoTradeType = autoTradeType;
	}
	
	/**
	 * 开仓价格信息
	 * @param code 所处的回撤点
	 * @param price 开仓价
	 * @param stopLoss 最佳止损点
	 * @param takeProfit 止盈价
	 * @param autoTradeType 参考指标
	 * @param fibInfo 回撤信息
	 */
	public OpenPriceDetails(FibCode code, double price, double stopLossLimit,
			double takeProfit, AutoTradeType autoTradeType,
			FibInfo fibInfo) {
		this.code = code;
		this.price = price;
		this.stopLossLimit = stopLossLimit;
		this.takeProfit = takeProfit;
		this.autoTradeType = autoTradeType;
		this.fibInfo = fibInfo;
	}

	@Override
	public double getPrice() {
		return price;
	}

	@Override
	public FibCode getCode() {
		return code;
	}

	@Override
	public String toString() {
		return "OpenPrice: " + code.getValue() + "(" + price + "), StopLossLimit: " + stopLossLimit + 
				", takeProfit: " + takeProfit + ", tradeStyle: " + this.tradeStyle;
	}

	@Override
	public double getStopLossLimit() {
		return this.stopLossLimit;
	}

	public double getTakeProfit() {
		return takeProfit;
	}

	@Override
	public FibInfo getFibInfo() {
		return fibInfo;
	}

	@Override
	public AutoTradeType getAutoTradeType() {
		return autoTradeType;
	}

	public boolean isResetStopLoss() {
		return resetStopLoss;
	}

	public void setResetStopLoss(boolean resetStopLoss) {
		this.resetStopLoss = resetStopLoss;
	}

	public AutoTrade getAutoTrade() {
		return autoTrade;
	}

	public void setAutoTrade(AutoTrade autoTrade) {
		this.autoTrade = autoTrade;
	}

	public AutoClosePosition getAutoClosePosition() {
		return autoClosePosition;
	}

	public void setAutoClosePosition(AutoClosePosition autoClosePosition) {
		this.autoClosePosition = autoClosePosition;
	}

	public DualSidePositionStatus getDualSidePositionStatus() {
		return dualSidePositionStatus;
	}

	public void setDualSidePositionStatus(DualSidePositionStatus dualSidePositionStatus) {
		this.dualSidePositionStatus = dualSidePositionStatus;
	}
	
	public TradeStyle getTradeStyle() {
		return tradeStyle;
	}

	public void setTradeStyle(TradeStyle tradeStyle) {
		this.tradeStyle = tradeStyle;
	}

	public FibInfo getChildFibInfo() {
		return childFibInfo;
	}

	@Override
	public void setChildFibInfo(FibInfo childFibInfo) {
		this.childFibInfo = childFibInfo;
	}

	public FibCode getChildFibCode(PriceInfo priceInfo) {
		FibCode fibCode = FibCode.FIB0;
		if(this.childFibInfo != null && priceInfo != null) {
			fibCode = this.childFibInfo.getFibCode(priceInfo.getPriceDoubleValue());
		}
		return fibCode;
	}
	
	@Override
	public boolean verifyOpen(PriceInfo priceInfo) {
		boolean result = false;
		FibCode code = getChildFibCode(priceInfo);
		if(code.lte(FibCode.FIB382) || this.autoTradeType == AutoTradeType.FENCE_SITTER) {
			result = true;
		}
		return result;
	}
}
