package com.util;

import java.util.Comparator;

import com.bugbycode.module.SortType;

public class PriceComparator implements Comparator<Double> {

	private SortType type;
	
	public PriceComparator(SortType type) {
		this.type = type;
	}
	
	@Override
	public int compare(Double o1, Double o2) {
		int result = 0;
		if(this.type == SortType.ASC) {
			if(o1 < o2) {
				result = -1;
			} else if(o1 > o2) {
				result = 1;
			}
		} else {
			if(o1 > o2) {
				result = -1;
			} else if(o1 < o2) {
				result = 1;
			}
		}
		return result;
	}

}
