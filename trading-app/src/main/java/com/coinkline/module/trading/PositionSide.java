package com.coinkline.module.trading;

/**
 * 持仓方向，单向持仓模式下非必填，默认且仅可填BOTH;在双向持仓模式下必填,且仅可选择 LONG 或 SHORT
 */
public enum PositionSide {

	DEFAULT("BOTH"),
	LONG("LONG"),
	SHORT("SHORT");

	private String positionSide;
	
	PositionSide(String positionSide) {
		this.positionSide = positionSide;
	}
	
	public String value() {
		return this.positionSide;
	}
	
}
