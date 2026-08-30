package com.util;

import java.util.ArrayList;
import java.util.List;

import com.bugbycode.factory.fibInfo.FibInfoFactory;
import com.bugbycode.factory.priceAction.PriceActionFactory;

public class FactoryInitUtil<T> {

	private List<T> list;
	
	public FactoryInitUtil(List<T> list) {
		this.list = list;
	}
	
	public List<T> deduplicate() {
		List<T> result = new ArrayList<T>();
		if(!(list == null || list.size() == 0)) {
			for(T t : list) {
				if(!contails(t, result)) {
					result.add(t);
				}
			}
		}
		return result;
	}
	
	private boolean contails(T f, List<T> list) {
		boolean result = false;
		for(T t : list) {
			if(t instanceof FibInfoFactory && f instanceof FibInfoFactory) {
				FibInfoFactory tf = (FibInfoFactory) t;
				FibInfoFactory ff = (FibInfoFactory) f;
				if((tf.isLong() && ff.isLong()) || (tf.isShort() && ff.isShort())) {
					result = true;
					break;
				}
			} else if(t instanceof PriceActionFactory && f instanceof PriceActionFactory) {
				PriceActionFactory tf = (PriceActionFactory) t;
				PriceActionFactory ff = (PriceActionFactory) f;
				if((tf.isLong() && ff.isLong()) || (tf.isShort() && ff.isShort())) {
					result = true;
					break;
				}
			}
		}
		return result;
	}
}
