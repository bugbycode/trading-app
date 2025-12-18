package com.bugbycode.module.trading;

/**
 * 持仓方向，单向持仓模式下非必填，默认且仅可填BOTH;在双向持仓模式下必填,且仅可选择 LONG 或 SHORT
 */
public enum PositionSide {

	DEFAULT("BOTH", "BOTH"),
	LONG("LONG", "多头"),
	SHORT("SHORT", "空头");

	private String positionSide;
	
	private String memo;
	
	PositionSide(String positionSide, String memo) {
		this.positionSide = positionSide;
		this.memo = memo;
	}
	
	public String value() {
		return this.positionSide;
	}

	public String getMemo() {
		return memo;
	}
	
}
