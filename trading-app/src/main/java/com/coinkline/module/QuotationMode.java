package com.coinkline.module;

/**
 * 行情走势 多头或者空头
 */
public enum QuotationMode {
	
	LONG("LONG"),
	SHORT("SHORT");

	private String label;
	
	QuotationMode(String label) {
		this.label = label;
	}

	public String getLabel() {
		return label;
	}
}
