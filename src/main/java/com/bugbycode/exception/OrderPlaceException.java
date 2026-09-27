package com.bugbycode.exception;

import com.bugbycode.module.trading.Type;

/**
 * 下单订单异常信息类
 */
public class OrderPlaceException extends RuntimeException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 7325516636423755320L;

	private final String title;
	
	private final Type type;
	
	private final int code;
	
	public OrderPlaceException(String title, String message, Type type) {
		super(message);
		this.title = title;
		this.type = type;
		this.code = -1000;
	}
	
	public OrderPlaceException(String title, String message, Type type, int code) {
		super(message);
		this.title = title;
		this.type = type;
		this.code = code;
	}
	
	public OrderPlaceException(String title, String message, Type type, Throwable cause) {
		super(message, cause);
		this.title = title;
		this.type = type;
		this.code = -1000;
	}

	public String getTitle() {
		return title;
	}

	public Type getType() {
		return type;
	}

	public int getCode() {
		return code;
	}
	
}
