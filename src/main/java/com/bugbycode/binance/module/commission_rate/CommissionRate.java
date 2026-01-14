package com.bugbycode.binance.module.commission_rate;

import org.json.JSONObject;

import com.util.StringUtil;

/**
 * 用户手续费率
 */
public class CommissionRate {

	private String symbol;
	
	private String makerCommissionRate;
	
	private String takerCommissionRate;
	
	private String rpiCommissionRate;

	public String getSymbol() {
		return symbol;
	}

	public void setSymbol(String symbol) {
		this.symbol = symbol;
	}

	public String getMakerCommissionRate() {
		return makerCommissionRate;
	}

	public void setMakerCommissionRate(String makerCommissionRate) {
		this.makerCommissionRate = makerCommissionRate;
	}

	public String getTakerCommissionRate() {
		return takerCommissionRate;
	}

	public void setTakerCommissionRate(String takerCommissionRate) {
		this.takerCommissionRate = takerCommissionRate;
	}
	
	public double getTakerCommissionRateDoubleValue() {
		return StringUtil.isEmpty(takerCommissionRate) ? 0 : Double.valueOf(takerCommissionRate);
	}

	public String getRpiCommissionRate() {
		return rpiCommissionRate;
	}

	public void setRpiCommissionRate(String rpiCommissionRate) {
		this.rpiCommissionRate = rpiCommissionRate;
	}
	
	public static CommissionRate parse(JSONObject o) {
		CommissionRate c = new CommissionRate();
		c.setSymbol(o.getString("symbol"));
		c.setMakerCommissionRate(o.getString("makerCommissionRate"));
		c.setTakerCommissionRate(o.getString("takerCommissionRate"));
		c.setRpiCommissionRate(o.getString("rpiCommissionRate"));
		return c;
	}

	@Override
	public String toString() {
		return "CommissionRate [symbol=" + symbol + ", makerCommissionRate=" + makerCommissionRate
				+ ", takerCommissionRate=" + takerCommissionRate + ", rpiCommissionRate=" + rpiCommissionRate + "]";
	}
}
