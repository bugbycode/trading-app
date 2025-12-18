package com.bugbycode.exception;

public class OrderCancelException extends RuntimeException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 7325516636423755320L;

	private final String title;
	
	public OrderCancelException(String title, String message) {
		super(message);
		this.title = title;
	}
	
	public OrderCancelException(String title, String message, Throwable cause) {
		super(message, cause);
		this.title = title;
	}

	public String getTitle() {
		return title;
	}
	
	
}
