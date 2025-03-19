package com.bugbycode.module.open_interest;

import org.springframework.data.annotation.Id;

/**
 * 历史合约持仓量
 */
public class OpenInterestHist {
	
	@Id
	private String id;
	
	private String symbol;//交易对
	
	private String sumOpenInterest;//持仓总数量
	
	private String sumOpenInterestValue;//持仓总价值
	
	private long timestamp;
	
	public OpenInterestHist() {
		
	}

	public OpenInterestHist(String id, String symbol, String sumOpenInterest, String sumOpenInterestValue,
			long timestamp) {
		this.id = id;
		this.symbol = symbol;
		this.sumOpenInterest = sumOpenInterest;
		this.sumOpenInterestValue = sumOpenInterestValue;
		this.timestamp = timestamp;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getSymbol() {
		return symbol;
	}

	public void setSymbol(String symbol) {
		this.symbol = symbol;
	}

	public String getSumOpenInterest() {
		return sumOpenInterest;
	}

	public void setSumOpenInterest(String sumOpenInterest) {
		this.sumOpenInterest = sumOpenInterest;
	}

	public String getSumOpenInterestValue() {
		return sumOpenInterestValue;
	}

	public void setSumOpenInterestValue(String sumOpenInterestValue) {
		this.sumOpenInterestValue = sumOpenInterestValue;
	}

	public long getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(long timestamp) {
		this.timestamp = timestamp;
	}
	
	
}
