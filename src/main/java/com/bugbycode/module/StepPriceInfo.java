package com.bugbycode.module;

import com.util.PriceUtil;
import com.util.StringUtil;

public class StepPriceInfo {

	private String hitPrice;//当前所处的标志性价格 比如BTC价格为 68900.10 则当前所处标志性价格应为 68000.00
	
	private String stepPrice;//盘整区价格步长 比如BTC价格为 68900.10 则盘整区价格步长应为 1000.00
	
	private int decimalNum;

	public StepPriceInfo(String hitPrice, String stepPrice, int decimalNum) {
		this.hitPrice = hitPrice;
		this.stepPrice = stepPrice;
		this.decimalNum = decimalNum;
	}

	public String getHitPrice() {
		return StringUtil.isEmpty(hitPrice) ? "0" : PriceUtil.formatDoubleDecimal(Double.valueOf(hitPrice), decimalNum);
	}
	
	public double getHitPriceDoubleValue() {
		return Double.valueOf(getHitPrice());
	}

	public void setHitPrice(String hitPrice) {
		this.hitPrice = hitPrice;
	}

	public String getStepPrice() {
		return StringUtil.isEmpty(stepPrice) ? "0" : PriceUtil.formatDoubleDecimal(Double.valueOf(stepPrice), decimalNum);
	}

	public void setStepPrice(String stepPrice) {
		this.stepPrice = stepPrice;
	}
	
	public String getHighPrice() {
		return PriceUtil.formatDoubleDecimal(Double.valueOf(getHitPrice()) + Double.valueOf(getStepPrice()), decimalNum);
	}
	
	public String getLowPrice() {
		return PriceUtil.formatDoubleDecimal(Double.valueOf(getHitPrice()) - Double.valueOf(getStepPrice()), decimalNum);
	}
	
	public String getNextHighPrice() {
		return PriceUtil.formatDoubleDecimal(Double.valueOf(getHighPrice()) + Double.valueOf(getStepPrice()), decimalNum);
	}
	
	public String getNextLowPrice() {
		return PriceUtil.formatDoubleDecimal(Double.valueOf(getLowPrice()) - Double.valueOf(getStepPrice()), decimalNum);
	}
	
	public double getHighPriceDoubleValue() {
		return Double.valueOf(getHighPrice());
	}
	
	public double getLowPriceDoubleValue() {
		return Double.valueOf(getLowPrice());
	}
	
	public double getNextHighPriceDoubleValue() {
		return Double.valueOf(getNextHighPrice());
	}
	
	public double getNextLowPriceDoubleValue() {
		return Double.valueOf(getNextLowPrice());
	}
}
