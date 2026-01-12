package com.bugbycode.binance.module.position;

import org.json.JSONObject;

/**
 * 持仓信息
 */
public class PositionInfo {

	/** 交易对 */
    private String symbol;

    /** 持仓方向：LONG / SHORT */
    private String positionSide;

    /** 持仓数量 */
    private String positionAmt;

    /** 开仓均价 */
    private String entryPrice;

    /** 盈亏平衡价 */
    private String breakEvenPrice;

    /** 标记价格 */
    private String markPrice;

    /** 未实现盈亏 */
    private String unRealizedProfit;

    /** 强平价格 */
    private String liquidationPrice;

    /** 逐仓保证金 */
    private String isolatedMargin;

    /** 名义价值 */
    private String notional;

    /** 保证金币种 */
    private String marginAsset;

    /** 逐仓钱包余额 */
    private String isolatedWallet;

    /** 初始保证金 */
    private String initialMargin;

    /** 维持保证金 */
    private String maintMargin;

    /** 持仓初始保证金 */
    private String positionInitialMargin;

    /** 未成交订单占用保证金 */
    private String openOrderInitialMargin;

    /** ADL 等级 */
    private int adl;

    /** 买单名义价值 */
    private String bidNotional;

    /** 卖单名义价值 */
    private String askNotional;

    /** 更新时间 */
    private long updateTime;

	public String getSymbol() {
		return symbol;
	}

	public void setSymbol(String symbol) {
		this.symbol = symbol;
	}

	public String getPositionSide() {
		return positionSide;
	}

	public void setPositionSide(String positionSide) {
		this.positionSide = positionSide;
	}

	public String getPositionAmt() {
		return positionAmt;
	}

	public void setPositionAmt(String positionAmt) {
		this.positionAmt = positionAmt;
	}

	public String getEntryPrice() {
		return entryPrice;
	}

	public void setEntryPrice(String entryPrice) {
		this.entryPrice = entryPrice;
	}

	public String getBreakEvenPrice() {
		return breakEvenPrice;
	}

	public void setBreakEvenPrice(String breakEvenPrice) {
		this.breakEvenPrice = breakEvenPrice;
	}

	public String getMarkPrice() {
		return markPrice;
	}

	public void setMarkPrice(String markPrice) {
		this.markPrice = markPrice;
	}

	public String getUnRealizedProfit() {
		return unRealizedProfit;
	}

	public void setUnRealizedProfit(String unRealizedProfit) {
		this.unRealizedProfit = unRealizedProfit;
	}

	public String getLiquidationPrice() {
		return liquidationPrice;
	}

	public void setLiquidationPrice(String liquidationPrice) {
		this.liquidationPrice = liquidationPrice;
	}

	public String getIsolatedMargin() {
		return isolatedMargin;
	}

	public void setIsolatedMargin(String isolatedMargin) {
		this.isolatedMargin = isolatedMargin;
	}

	public String getNotional() {
		return notional;
	}

	public void setNotional(String notional) {
		this.notional = notional;
	}

	public String getMarginAsset() {
		return marginAsset;
	}

	public void setMarginAsset(String marginAsset) {
		this.marginAsset = marginAsset;
	}

	public String getIsolatedWallet() {
		return isolatedWallet;
	}

	public void setIsolatedWallet(String isolatedWallet) {
		this.isolatedWallet = isolatedWallet;
	}

	public String getInitialMargin() {
		return initialMargin;
	}

	public void setInitialMargin(String initialMargin) {
		this.initialMargin = initialMargin;
	}

	public String getMaintMargin() {
		return maintMargin;
	}

	public void setMaintMargin(String maintMargin) {
		this.maintMargin = maintMargin;
	}

	public String getPositionInitialMargin() {
		return positionInitialMargin;
	}

	public void setPositionInitialMargin(String positionInitialMargin) {
		this.positionInitialMargin = positionInitialMargin;
	}

	public String getOpenOrderInitialMargin() {
		return openOrderInitialMargin;
	}

	public void setOpenOrderInitialMargin(String openOrderInitialMargin) {
		this.openOrderInitialMargin = openOrderInitialMargin;
	}

	public int getAdl() {
		return adl;
	}

	public void setAdl(int adl) {
		this.adl = adl;
	}

	public String getBidNotional() {
		return bidNotional;
	}

	public void setBidNotional(String bidNotional) {
		this.bidNotional = bidNotional;
	}

	public String getAskNotional() {
		return askNotional;
	}

	public void setAskNotional(String askNotional) {
		this.askNotional = askNotional;
	}

	public long getUpdateTime() {
		return updateTime;
	}

	public void setUpdateTime(long updateTime) {
		this.updateTime = updateTime;
	}

	@Override
	public String toString() {
		return "PositionInfo [symbol=" + symbol + ", positionSide=" + positionSide + ", positionAmt=" + positionAmt
				+ ", entryPrice=" + entryPrice + ", breakEvenPrice=" + breakEvenPrice + ", markPrice=" + markPrice
				+ ", unRealizedProfit=" + unRealizedProfit + ", liquidationPrice=" + liquidationPrice
				+ ", isolatedMargin=" + isolatedMargin + ", notional=" + notional + ", marginAsset=" + marginAsset
				+ ", isolatedWallet=" + isolatedWallet + ", initialMargin=" + initialMargin + ", maintMargin="
				+ maintMargin + ", positionInitialMargin=" + positionInitialMargin + ", openOrderInitialMargin="
				+ openOrderInitialMargin + ", adl=" + adl + ", bidNotional=" + bidNotional + ", askNotional="
				+ askNotional + ", updateTime=" + updateTime + "]";
	}
	
	public static PositionInfo parse(JSONObject o) {
		PositionInfo info = new PositionInfo();
		info.setSymbol(o.getString("symbol"));
		info.setPositionSide(o.getString("positionSide"));
		info.setPositionAmt(o.getString("positionAmt"));
		info.setEntryPrice(o.getString("entryPrice"));
		info.setBreakEvenPrice(o.getString("breakEvenPrice"));
		info.setMarkPrice(o.getString("markPrice"));
		info.setUnRealizedProfit(o.getString("unRealizedProfit"));
		info.setLiquidationPrice(o.getString("liquidationPrice"));
		info.setIsolatedMargin(o.getString("isolatedMargin"));
		info.setNotional(o.getString("notional"));
		info.setMarginAsset(o.getString("marginAsset"));
		info.setIsolatedWallet(o.getString("isolatedWallet"));
		info.setInitialMargin(o.getString("initialMargin"));
		info.setMaintMargin(o.getString("maintMargin"));
		info.setPositionInitialMargin(o.getString("positionInitialMargin"));
		info.setOpenOrderInitialMargin(o.getString("openOrderInitialMargin"));
		info.setAdl(o.getInt("adl"));
		info.setBidNotional(o.getString("bidNotional"));
		info.setAskNotional(o.getString("askNotional"));
		info.setUpdateTime(o.getLong("updateTime"));
		return info;
	}
}
