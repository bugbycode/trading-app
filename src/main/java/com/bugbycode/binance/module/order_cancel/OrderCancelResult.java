package com.bugbycode.binance.module.order_cancel;

/**
 * 撤销订单返回结果
 */
public class OrderCancelResult {

	private String id;
	
	private int status;
	
	OrderCancelInfo orderCancelInfo;

	public OrderCancelResult(String id, int status, OrderCancelInfo orderCancelInfo) {
		this.id = id;
		this.status = status;
		this.orderCancelInfo = orderCancelInfo;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public int getStatus() {
		return status;
	}

	public void setStatus(int status) {
		this.status = status;
	}

	public OrderCancelInfo getOrderCancelInfo() {
		return orderCancelInfo;
	}

	public void setOrderCancelInfo(OrderCancelInfo orderCancelInfo) {
		this.orderCancelInfo = orderCancelInfo;
	}
	
	
}
