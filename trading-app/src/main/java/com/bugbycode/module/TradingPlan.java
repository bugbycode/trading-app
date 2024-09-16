package com.bugbycode.module;

import org.springframework.data.annotation.Id;

/**
 * 交易计划实体信息类
 */
public class TradingPlan {
    
    //数据库唯一标识
	@Id
    private String id;

    //交易对 例如 BTCUSDT
    private String pair;

    //多空 1 多 0 空
    private int longOrShort;
    
    //触发价格
    private double hitPrice;

    //开仓价
    private double openingPrice;

    //止盈价
    private double takeProfitPrice;

    //止损价
    private double stopLossPrice;
    
    //计划状态 0 未触发 1 已触发
    private int status;
    
    //创建时间
    private long createTime;
    
    //执行时间
    private long execTime;

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

	public int getLongOrShort() {
		return longOrShort;
	}

	public void setLongOrShort(int longOrShort) {
		this.longOrShort = longOrShort;
		getLongOrShortType();
	}
	
	public LongOrShortType getLongOrShortType() {
		return LongOrShortType.resolve(longOrShort);
	}

	public double getHitPrice() {
		return hitPrice;
	}

	public void setHitPrice(double hitPrice) {
		this.hitPrice = hitPrice;
	}

	public double getOpeningPrice() {
		return openingPrice;
	}

	public void setOpeningPrice(double openingPrice) {
		this.openingPrice = openingPrice;
	}

	public double getTakeProfitPrice() {
		return takeProfitPrice;
	}

	public void setTakeProfitPrice(double takeProfitPrice) {
		this.takeProfitPrice = takeProfitPrice;
	}

	public double getStopLossPrice() {
		return stopLossPrice;
	}

	public void setStopLossPrice(double stopLossPrice) {
		this.stopLossPrice = stopLossPrice;
	}

	public int getStatus() {
		return status;
	}
	
	public PlanStatus getPlanStatus() {
		return PlanStatus.resolve(this.status);
	}

	public void setStatus(int status) {
		this.status = status;
		getPlanStatus();
	}

	public long getCreateTime() {
		return createTime;
	}

	public void setCreateTime(long createTime) {
		this.createTime = createTime;
	}

	public long getExecTime() {
		return execTime;
	}

	public void setExecTime(long execTime) {
		this.execTime = execTime;
	}

	@Override
	public String toString() {
		return "{id:\"" + id + "\", pair:\"" + pair + "\", longOrShort:\"" + longOrShort + "\", openingPrice:\""
				+ openingPrice + "\", takeProfitPrice:\"" + takeProfitPrice
				+ "\", stopLossPrice:\"" + stopLossPrice + "\", status:\"" + status + "\", createTime:\"" + createTime
				+ "\", execTime:\"" + execTime + "}";
	}
	
}
