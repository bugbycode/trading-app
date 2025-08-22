package com.util;

import java.util.Comparator;

import com.bugbycode.module.SortType;
import com.bugbycode.module.price.OpenPrice;

public class PriceComparator implements Comparator<OpenPrice> {

	private SortType type;
	
	public PriceComparator(SortType type) {
		this.type = type;
	}
	
	@Override
	public int compare(OpenPrice o1, OpenPrice o2) {
		int result = 0;
		if(this.type == SortType.ASC) {
			if(o1.getPrice() < o2.getPrice()) {
				result = -1;
			} else if(o1.getPrice() > o2.getPrice()) {
				result = 1;
			}
		} else {
			if(o1.getPrice() > o2.getPrice()) {
				result = -1;
			} else if(o1.getPrice() < o2.getPrice()) {
				result = 1;
			}
		}
		return result;
	}

}
