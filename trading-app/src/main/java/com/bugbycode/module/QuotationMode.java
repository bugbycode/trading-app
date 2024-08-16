package com.bugbycode.module;

/**
 * 行情走势 多头或者空头
 */
public enum QuotationMode {
	
	LONG("Long"),
	SHORT("Short");

	private String label;
	
	QuotationMode(String label) {
		this.label = label;
	}

	public String getLabel() {
		return label;
	}
}
