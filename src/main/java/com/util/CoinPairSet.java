package com.util;

import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import com.bugbycode.module.Inerval;

public class CoinPairSet extends HashSet<String> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private Inerval inerval;
	
	private Set<String> finishPair = Collections.synchronizedSet(new HashSet<String>());
	
	public CoinPairSet(Inerval inerval) {
		this.inerval = inerval;
	}

	public Inerval getInerval() {
		return inerval;
	}

	public String getStreamName() {
		StringBuffer buffer = new StringBuffer();
		Iterator<String> it = this.iterator();
		while(it.hasNext()) {
			if(buffer.length() > 0) {
				buffer.append("/");
			}
			buffer.append(it.next().toLowerCase() + "_perpetual@continuousKline_" + inerval.getDescption());
		}
		return buffer.toString();
	}
	
	
	@Override
	public boolean add(String e) {
		if(isFull()) {
			return false;
		}
		return super.add(e);
	}

	public boolean isFull() {
		return this.size() == 10;
	}

	public boolean isEmpty() {
		return this.size() == 0;
	}
	
	@Override
	public void clear() {
		super.clear();
		this.finishPair.clear();
	}
	
	public boolean addFinishPair(String pair) {
		if(this.finishPair.contains(pair)) {
			return false;
		}
		return this.finishPair.add(pair);
	}
	
	public boolean isFinish() {
		return this.finishPair.size() == this.size();
	}
}
