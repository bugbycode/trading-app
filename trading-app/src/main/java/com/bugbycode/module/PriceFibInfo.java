package com.bugbycode.module;

/**
 * 价格回撤斐波那契信息
 */
public class PriceFibInfo {

	private String inerval;//时间级别
	
	private long lowKlinesTime;//最低价k线开盘时间
	
	private long highKlinesTime;//最高价k线开盘时间

	public String getInerval() {
		return inerval;
	}

	public void setInerval(String inerval) {
		this.inerval = inerval;
	}

	public long getLowKlinesTime() {
		return lowKlinesTime;
	}

	public void setLowKlinesTime(long lowKlinesTime) {
		this.lowKlinesTime = lowKlinesTime;
	}

	public long getHighKlinesTime() {
		return highKlinesTime;
	}

	public void setHighKlinesTime(long highKlinesTime) {
		this.highKlinesTime = highKlinesTime;
	}
}
