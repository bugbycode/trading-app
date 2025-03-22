package com.util;

import java.util.ArrayList;
import java.util.List;

import com.bugbycode.module.FibCode;
import com.bugbycode.module.FibInfo;
import com.bugbycode.module.FibLevel;
import com.bugbycode.module.Klines;
import com.bugbycode.module.QuotationMode;

/**
 * 斐波那契回撤工厂类
 */
public class FibInfoFactory {

	private List<Klines> list;
	
	private List<Klines> fibAfterKlines;
	
	public FibInfoFactory(List<Klines> list) {
		this.list = new ArrayList<Klines>();
		this.list.addAll(list);
	}
	
	public FibInfo getFibInfo() {
		FibUtil fu = new FibUtil(list);
		FibInfo current = fu.getFibInfo();
		Klines split_end = fu.getFibStartKlines();
		if(!(current == null || split_end == null || list.size() < 30)) {
			QuotationMode mode = current.getQuotationMode();
			List<Klines> parentList = PriceUtil.subList(list.get(0), fu.getFibStartKlines(), list);
			FibUtil parent_fu = new FibUtil(parentList);
			FibInfo parent_fib = parent_fu.getFibInfo();
			
			if(parent_fib != null) {
				FibLevel level = current.getLevel();
				double c_0 = current.getFibValue(FibCode.FIB0);
				double c_1 = current.getFibValue(FibCode.FIB1);
				double p_0 = parent_fib.getFibValue(FibCode.FIB0);
				double p_1 = parent_fib.getFibValue(FibCode.FIB1);
				
				if(mode == QuotationMode.LONG) {
					if(p_0 >= c_0 && p_1 <= c_1) {
						current = parent_fib;
					} else {
						if(c_0 > p_0) {//创出更高的高点
							level = FibLevel.LEVEL_2;
						} else if(c_1 < p_1) {//创出更低的低点
							level = FibLevel.LEVEL_3;
						} 
						current = new FibInfo(c_1, c_0, current.getDecimalPoint(), level);
					}
				} else if(mode == QuotationMode.SHORT) {
					if(p_1 >= c_1 && p_0 <= c_0) {
						current = parent_fib;
					} else {
						if(c_0 < p_0) {//创出更低的低点
							level = FibLevel.LEVEL_3;
						} else if(c_1 > p_1) {//创出更高的高点
							level = FibLevel.LEVEL_2;
						}
						current = new FibInfo(c_1, c_0, current.getDecimalPoint(), level);
					}
				}
			}
		}
		
		this.fibAfterKlines = fu.getFibAfterKlines();
		
		return current;
	}
	
	public List<Klines> getFibAfterKlines() {
		return this.fibAfterKlines;
	}
}
