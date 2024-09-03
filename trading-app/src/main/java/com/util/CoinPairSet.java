package com.util;

import java.util.HashSet;
import java.util.Iterator;

import com.bugbycode.module.Inerval;

public class CoinPairSet extends HashSet<String> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public String getStreamName() {
		StringBuffer buffer = new StringBuffer();
		Iterator<String> it = this.iterator();
		while(it.hasNext()) {
			if(buffer.length() > 0) {
				buffer.append("/");
			}
			buffer.append(it.next().toLowerCase() + "_perpetual@continuousKline_" + Inerval.INERVAL_15M.getDescption());
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

	@Override
	public void clear() {
		super.clear();
	}
}
