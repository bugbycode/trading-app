package com.bugbycode.binance.module.fundingInfo;

import org.json.JSONObject;

/**
 * 资金费率
 */
public class FundingInfo {
	
	private String symbol;
	
	private String adjustedFundingRateCap;
	
	private String adjustedFundingRateFloor;
	
	private int fundingIntervalHours;
	
	private boolean disclaimer;

	public String getSymbol() {
		return symbol;
	}

	public void setSymbol(String symbol) {
		this.symbol = symbol;
	}

	public String getAdjustedFundingRateCap() {
		return adjustedFundingRateCap;
	}

	public void setAdjustedFundingRateCap(String adjustedFundingRateCap) {
		this.adjustedFundingRateCap = adjustedFundingRateCap;
	}

	public String getAdjustedFundingRateFloor() {
		return adjustedFundingRateFloor;
	}

	public void setAdjustedFundingRateFloor(String adjustedFundingRateFloor) {
		this.adjustedFundingRateFloor = adjustedFundingRateFloor;
	}

	public int getFundingIntervalHours() {
		return fundingIntervalHours;
	}

	public void setFundingIntervalHours(int fundingIntervalHours) {
		this.fundingIntervalHours = fundingIntervalHours;
	}

	public boolean isDisclaimer() {
		return disclaimer;
	}

	public void setDisclaimer(boolean disclaimer) {
		this.disclaimer = disclaimer;
	}

	public static FundingInfo parse(JSONObject o) {
		FundingInfo info = new FundingInfo();
		info.setSymbol(o.getString("symbol"));
		info.setAdjustedFundingRateCap(o.getString("adjustedFundingRateCap"));
		info.setAdjustedFundingRateFloor(o.getString("adjustedFundingRateFloor"));
		info.setDisclaimer(o.getBoolean("disclaimer"));
		info.setFundingIntervalHours(o.getInt("fundingIntervalHours"));
		return info;
	}

	@Override
	public String toString() {
		return "FundingInfo [symbol=" + symbol + ", adjustedFundingRateCap=" + adjustedFundingRateCap
				+ ", adjustedFundingRateFloor=" + adjustedFundingRateFloor + ", fundingIntervalHours="
				+ fundingIntervalHours + ", disclaimer=" + disclaimer + "]";
	}
}
