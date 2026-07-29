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
	
	public OrderPlaceException(String title, String message, Type type) {
		super(message);
		this.title = title;
		this.type = type;
	}
	
	public OrderPlaceException(String title, String message, Type type, Throwable cause) {
		super(message, cause);
		this.title = title;
		this.type = type;
	}

	public String getTitle() {
		return title;
	}

	public Type getType() {
		return type;
	}
	
}
