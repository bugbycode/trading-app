package com.bugbycode.module.config;

import org.springframework.data.annotation.Id;

/**
 * tradingview参数配置信息
 */
public class TradingViewConfig {

	@Id
	private String id;
	
	private String owner;//用户名
	 
	private String symbol;//交易对
	
	private String inerval;//时间级别

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getOwner() {
		return owner;
	}

	public void setOwner(String owner) {
		this.owner = owner;
	}

	public String getSymbol() {
		return symbol;
	}

	public void setSymbol(String symbol) {
		this.symbol = symbol;
	}

	public String getInerval() {
		return inerval;
	}

	public void setInerval(String inerval) {
		this.inerval = inerval;
	}
}
