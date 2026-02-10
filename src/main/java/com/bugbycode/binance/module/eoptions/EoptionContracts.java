package com.bugbycode.binance.module.eoptions;

import org.json.JSONObject;

/**
 * 期权合约底层资产信息
 */
public class EoptionContracts {

	private String baseAsset;//标的资产
	
	private String quoteAsset;//报价资产
	
	private String underlying;//期权合约底层资产
	
	private String settleAsset;//结算资产

	public String getBaseAsset() {
		return baseAsset;
	}

	public void setBaseAsset(String baseAsset) {
		this.baseAsset = baseAsset;
	}

	public String getQuoteAsset() {
		return quoteAsset;
	}

	public void setQuoteAsset(String quoteAsset) {
		this.quoteAsset = quoteAsset;
	}

	public String getUnderlying() {
		return underlying;
	}

	public void setUnderlying(String underlying) {
		this.underlying = underlying;
	}

	public String getSettleAsset() {
		return settleAsset;
	}

	public void setSettleAsset(String settleAsset) {
		this.settleAsset = settleAsset;
	}
	
	public static EoptionContracts parse(JSONObject o) {
		EoptionContracts ec = new EoptionContracts();
		ec.setBaseAsset(o.getString("baseAsset"));
		ec.setQuoteAsset(o.getString("quoteAsset"));
		ec.setSettleAsset(o.getString("settleAsset"));
		ec.setUnderlying(o.getString("underlying"));
		return ec;
	}

	@Override
	public String toString() {
		return "EoptionContracts [baseAsset=" + baseAsset + ", quoteAsset=" + quoteAsset + ", underlying=" + underlying
				+ ", settleAsset=" + settleAsset + "]";
	}
}
