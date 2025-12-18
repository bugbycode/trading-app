package com.bugbycode.exception;

/**
 * 下单订单异常信息类
 */
public class OrderPlaceException extends RuntimeException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 7325516636423755320L;

	private final String title;
	
	public OrderPlaceException(String title, String message) {
		super(message);
		this.title = title;
	}
	
	public OrderPlaceException(String title, String message, Throwable cause) {
		super(message, cause);
		this.title = title;
	}

	public String getTitle() {
		return title;
	}
	
	
}
