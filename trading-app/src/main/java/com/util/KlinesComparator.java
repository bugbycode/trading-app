package com.util;

import java.util.Comparator;

import com.bugbycode.module.Klines;

/**
 * K线排序 开盘时间（升序）
 */
public class KlinesComparator implements Comparator<Klines> {

	@Override
	public int compare(Klines k0, Klines k1) {
		int result = 0;
		if(k0.getStarTime() < k1.getStarTime()) {
			result = -1;
		} else if(k0.getStarTime() == k1.getStarTime()) {
			result = 0;
		} else if(k0.getStarTime() > k1.getStarTime()) {
			result = 1;
		}
		return result;
	}

}
