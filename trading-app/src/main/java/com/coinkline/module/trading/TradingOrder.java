package com.coinkline.module.trading;

import org.springframework.data.annotation.Id;

import com.coinkline.module.LongOrShortType;
import com.util.DateFormatUtil;

/**
 * 交易订单
 */
public class TradingOrder {

	@Id
	private String id;
	
	private String pair;//交易对
	
	private double accountSize;//持仓金额
	
	private double coinSize;//持仓数量
	
	private double openPrice;//开仓价
	
	private double takeProfit;//止盈价
	
	private double stopLoss;//止损价
	
	private double closePrice;//平仓价
	
	private long openTime;//开仓时间
	
	private long closeTime;//平仓时间
	
	private int longOrShort;//仓位方向 多：1 空：0
	
	private double pnl;//盈亏金额

	public TradingOrder() {
		
	}
	
	/**
	 * @param pair 交易对
	 * @param openPrice 开仓价
	 * @param takeProfit 止盈
	 * @param stopLoss 止损
	 * @param openTime 开仓时间
	 * @param longOrShort 仓位方向 1:多 0:空
	 * @param accountSize 持仓金额
	 * @param coinSize 持仓数量
	 */
	public TradingOrder(String pair, double openPrice, double takeProfit, double stopLoss, long openTime,
			int longOrShort,double accountSize,double coinSize) {
		this.pair = pair;
		this.openPrice = openPrice;
		this.takeProfit = takeProfit;
		this.stopLoss = stopLoss;
		this.openTime = openTime;
		this.longOrShort = longOrShort;
		this.accountSize = accountSize;
		this.coinSize = coinSize;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getPair() {
		return pair;
	}

	public void setPair(String pair) {
		this.pair = pair;
	}

	public double getOpenPrice() {
		return openPrice;
	}

	public void setOpenPrice(double openPrice) {
		this.openPrice = openPrice;
	}

	public double getTakeProfit() {
		return takeProfit;
	}

	public void setTakeProfit(double takeProfit) {
		this.takeProfit = takeProfit;
	}

	public double getStopLoss() {
		return stopLoss;
	}

	public void setStopLoss(double stopLoss) {
		this.stopLoss = stopLoss;
	}

	public double getClosePrice() {
		return closePrice;
	}

	public void setClosePrice(double closePrice) {
		this.closePrice = closePrice;
	}

	public long getOpenTime() {
		return openTime;
	}

	public void setOpenTime(long openTime) {
		this.openTime = openTime;
	}

	public long getCloseTime() {
		return closeTime;
	}

	public void setCloseTime(long closeTime) {
		this.closeTime = closeTime;
	}

	public int getLongOrShort() {
		return longOrShort;
	}

	public void setLongOrShort(int longOrShort) {
		this.longOrShort = longOrShort;
	}

	public double getPnl() {
		return pnl;
	}

	public void setPnl(double pnl) {
		this.pnl = pnl;
	}

	public double getAccountSize() {
		return accountSize;
	}

	public void setAccountSize(double accountSize) {
		this.accountSize = accountSize;
	}

	public double getCoinSize() {
		return coinSize;
	}

	public void setCoinSize(double coinSize) {
		this.coinSize = coinSize;
	}

	public String toJsonString() {
		return "{\"id\":\"" + id + "\", \"pair\":\"" + pair + "\", \"accountSize\":\"" + accountSize + "\", \"coinSize\":\"" + coinSize
				+ "\", \"openPrice\":\"" + openPrice + "\", \"takeProfit\":\"" + takeProfit + "\", \"stopLoss\":\"" + stopLoss + "\", \"closePrice\":\""
				+ closePrice + "\", \"openTime\":\"" + openTime + "\", \"closeTime\":\"" + closeTime + "\", \"longOrShort\":\"" + longOrShort
				+ "\", \"pnl\":\"" + pnl + "\"}";
	}

	@Override
	public String toString() {
		return "交易对：" + pair + ", 仓位金额：" + accountSize + ", 持仓数量：" + coinSize
				+ ", 开仓价格：" + openPrice + ", 止盈价：" + takeProfit + ", 止损价：" + stopLoss + ", 平仓价："
				+ closePrice + ", 开仓时间：" + DateFormatUtil.format(openTime) + ", 平仓时间：" + (closeTime == 0 ? "" : DateFormatUtil.format(closeTime)) + 
				", 持仓方向：" + LongOrShortType.resolve(longOrShort).getMemo()
				+ ", 盈亏金额：" + pnl;
	}
	
}
