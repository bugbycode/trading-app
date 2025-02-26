package com.util;

import java.util.Comparator;

import com.bugbycode.module.Klines;
import com.bugbycode.module.SortType;

/**
 * K线排序
 */
public class KlinesComparator implements Comparator<Klines> {

	private SortType type;
	
	public KlinesComparator(SortType type) {
		this.type = type;
	}
	
	@Override
	public int compare(Klines k0, Klines k1) {
		int result = 0;
		if(type == SortType.DESC) {
			if(k0.getStartTime() > k1.getStartTime()) {
				result = -1;
			} else if(k0.getStartTime() == k1.getStartTime()) {
				result = 0;
			} else if(k0.getStartTime() < k1.getStartTime()) {
				result = 1;
			}
		} else {
			if(k0.getStartTime() < k1.getStartTime()) {
				result = -1;
			} else if(k0.getStartTime() == k1.getStartTime()) {
				result = 0;
			} else if(k0.getStartTime() > k1.getStartTime()) {
				result = 1;
			}
		}
		
		return result;
	}

}
