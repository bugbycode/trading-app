package com.bugbycode.module;

public class ReversalPoint {

	private Klines current;
	
	private Klines parent;

	public ReversalPoint(Klines current, Klines parent) {
		this.current = current;
		this.parent = parent;
	}

	public Klines getCurrent() {
		return current;
	}

	public void setCurrent(Klines current) {
		this.current = current;
	}

	public Klines getParent() {
		return parent;
	}

	public void setParent(Klines parent) {
		this.parent = parent;
	}
	
	public Klines getMaxKlines() {
		return current.getHighPriceDoubleValue() > parent.getHighPriceDoubleValue() ? current : parent; 
	}
	
	public Klines getMinKlines() {
		return current.getLowPriceDoubleValue() < parent.getLowPriceDoubleValue() ? current : parent; 
	}
	
	public double getMaxPrice() {
		return getMaxKlines().getHighPriceDoubleValue();
	}
	
	public double getMinPrice() {
		return getMinKlines().getLowPriceDoubleValue();
	}
}
