package com.bugbycode.module.open_interest;

import org.springframework.data.annotation.Id;

import com.util.DateFormatUtil;

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
	
	private long tradeNumber;//每分钟成交笔数
	
	private double v_24h;//24小时成交量
	
	private double q_24h;//24小时成交额
	
	private int tradeNumberIndex = 0;//市场活跃度排名索引，值越小热度越高
	
	public OpenInterestHist() {
		
	}

	public OpenInterestHist(String id, String symbol, String sumOpenInterest, String sumOpenInterestValue,
			long timestamp, long tradeNumber, double v_24h, double q_24h) {
		this.id = id;
		this.symbol = symbol;
		this.sumOpenInterest = sumOpenInterest;
		this.sumOpenInterestValue = sumOpenInterestValue;
		this.timestamp = timestamp;
		this.tradeNumber = tradeNumber;
		this.v_24h = v_24h;
		this.q_24h = q_24h;
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

	public long getTradeNumber() {
		return tradeNumber;
	}

	public void setTradeNumber(long tradeNumber) {
		this.tradeNumber = tradeNumber;
	}

	public double getV_24h() {
		return v_24h;
	}

	public void setV_24h(double v_24h) {
		this.v_24h = v_24h;
	}

	public double getQ_24h() {
		return q_24h;
	}

	public void setQ_24h(double q_24h) {
		this.q_24h = q_24h;
	}

	public int getTradeNumberIndex() {
		return tradeNumberIndex;
	}

	public void setTradeNumberIndex(int tradeNumberIndex) {
		this.tradeNumberIndex = tradeNumberIndex;
	}

	@Override
	public String toString() {
		return "交易对：" + this.getSymbol() + ", 持仓总数量：" + sumOpenInterest
				+ ", 持仓总价值：" + sumOpenInterestValue + ", 更新时间：" + DateFormatUtil.format(timestamp) + ", 每分钟成交笔数：" + tradeNumber
				+ ", 24小时成交量：" + v_24h + ", 24小时成交额：" + q_24h + ", 热度排名索引：" + tradeNumberIndex;
	}
	
	
}
