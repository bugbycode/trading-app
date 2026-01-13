package com.bugbycode.binance.module.leverage;

import org.json.JSONObject;

/**
 * 杠杆分层标准信息
 */
public class LeverageBracketInfo {
	
	private int bracket;//层级
	
	private int initialLeverage;//该层允许的最高初始杠杆倍数
	
	private int notionalCap;//该层对应的名义价值上限
	
	private int notionalFloor;//该层对应的名义价值下限 
	
	private double maintMarginRatio;//该层对应的维持保证金率
	
	private double cum;//速算数

	public int getBracket() {
		return bracket;
	}

	public void setBracket(int bracket) {
		this.bracket = bracket;
	}

	public int getInitialLeverage() {
		return initialLeverage;
	}

	public void setInitialLeverage(int initialLeverage) {
		this.initialLeverage = initialLeverage;
	}

	public int getNotionalCap() {
		return notionalCap;
	}

	public void setNotionalCap(int notionalCap) {
		this.notionalCap = notionalCap;
	}

	public int getNotionalFloor() {
		return notionalFloor;
	}

	public void setNotionalFloor(int notionalFloor) {
		this.notionalFloor = notionalFloor;
	}

	public double getMaintMarginRatio() {
		return maintMarginRatio;
	}

	public void setMaintMarginRatio(double maintMarginRatio) {
		this.maintMarginRatio = maintMarginRatio;
	}

	public double getCum() {
		return cum;
	}

	public void setCum(double cum) {
		this.cum = cum;
	}
	
	public static LeverageBracketInfo parse(JSONObject o) {
		LeverageBracketInfo info = new LeverageBracketInfo();
		info.setBracket(o.getInt("bracket"));
		info.setInitialLeverage(o.getInt("initialLeverage"));
		info.setNotionalCap(o.getInt("notionalCap"));
		info.setNotionalFloor(o.getInt("notionalFloor"));
		info.setMaintMarginRatio(o.getDouble("maintMarginRatio"));
		info.setCum(o.getDouble("cum"));
		return info;
	}
	
}
