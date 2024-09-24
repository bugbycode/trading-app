package com.util;

import java.util.List;

import org.springframework.util.CollectionUtils;

import com.bugbycode.module.Klines;

public class KlinesUtil {

	private List<Klines> list = null;
	
	private int first_offset = -1;
	
	private int last_offset = -1;
	
	public KlinesUtil(List<Klines> list) {
		this.list = list;
		if(!CollectionUtils.isEmpty(list)) {
			this.resetFirstOffset();
			this.resetLastOffset();
		}
	}
	
	public Klines removeLast() {
		if(this.last_offset == -1) {
			return null;
		}
		return list.get(this.last_offset--);
	}
	
	public Klines removeFirst() {
		if(this.first_offset == list.size()) {
			return null;
		}
		return list.get(this.first_offset++);
	}
	
	public void resetLastOffset() {
		this.last_offset = list.size() - 1;
	}
	
	public void resetFirstOffset() {
		this.first_offset = 0;
	}
	
	public boolean isEmpty() {
		return this.first_offset == -1 || this.last_offset == -1;
	}
}
