package com.bugbycode.module;

public enum Regex {

	/**
	 * 邮箱
	 */
	EMAIL("^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$"),

	/**
	 * 域名
	 */
	DOMAIN("\\b(?:[a-zA-Z0-9-]{1,63}\\.)+[a-zA-Z]{2,63}\\b"),
	
	/**
	 * 端口
	 */
	PORT("\\b(6553[0-5]|655[0-2]\\d|65[0-4]\\d{2}|6[0-4]\\d{3}|[1-5]\\d{4}|\\d{1,4})\\b");
	
	private String regex;
	
	Regex(String value) {
		this.regex = value;
	}

	public String getRegex() {
		return regex;
	}
}
