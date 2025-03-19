package com.util;

import java.util.Comparator;

import com.bugbycode.module.SortType;
import com.bugbycode.module.open_interest.OpenInterestHist;

/**
 * 合约持仓量排序工具类 按照SumOpenInterestValue（持仓总价值）来排序
 */
public class OpenInterestHistComparator implements Comparator<OpenInterestHist>{

	private SortType type;
	
	public OpenInterestHistComparator(SortType type) {
		this.type = type;
	}
	
	@Override
	public int compare(OpenInterestHist o1, OpenInterestHist o2) {
		
		int result = 0;
		
		double v1 = Double.valueOf(o1.getSumOpenInterestValue());
		double v2 = Double.valueOf(o2.getSumOpenInterestValue());
		if(type == SortType.DESC) {
			if(v1 > v2) {
				result = -1;
			} else if(v1 == v2) {
				result = 0;
			} else if(v1 < v2) {
				result = 1;
			}
		} else {
			if(v1 > v2) {
				result = 1;
			} else if(v1 == v2) {
				result = 0;
			} else if(v1 < v2) {
				result = -1;
			}
		}
		return result;
	}

}
