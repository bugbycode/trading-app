package com.bugbycode.module;

public enum Regex {

	/**
	 * 邮箱
	 */
	EMAIL("^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$");

	private String regex;
	
	Regex(String value) {
		this.regex = value;
	}

	public String getRegex() {
		return regex;
	}
}
