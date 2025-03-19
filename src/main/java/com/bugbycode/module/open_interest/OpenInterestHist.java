package com.bugbycode.module.open_interest;

import org.springframework.data.annotation.Id;

import com.util.DateFormatUtil;

/**
 * 历史合约持仓量
 */
public class OpenInterestHist {
	
	@Id
	private String id;
	
	private String pair;//交易对
	
	private String sumOpenInterest;//持仓总数量
	
	private String sumOpenInterestValue;//持仓总价值
	
	private long timestamp;
	
	public OpenInterestHist() {
		
	}

	public OpenInterestHist(String id, String pair, String sumOpenInterest, String sumOpenInterestValue,
			long timestamp) {
		this.id = id;
		this.pair = pair;
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

	public String getPair() {
		return pair;
	}

	public void setPair(String pair) {
		this.pair = pair;
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

	@Override
	public String toString() {
		return "交易对：" + this.getPair() + ", 持仓总数量：" + sumOpenInterest
				+ ", 持仓总价值：" + sumOpenInterestValue + ", 更新时间：" + DateFormatUtil.format(timestamp);
	}
	
	
}
