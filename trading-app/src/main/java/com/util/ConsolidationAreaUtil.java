package com.util;

import java.util.ArrayList;
import java.util.List;

import org.springframework.util.CollectionUtils;

import com.bugbycode.module.Klines;
import com.bugbycode.module.area.ConsolidationArea;

/**
 * 盘整区工具类
 */
public class ConsolidationAreaUtil {

	/**
	 * K线信息
	 */
	private List<Klines> list;
	
	private int parentOffset = 0;
	
	public ConsolidationAreaUtil(List<Klines> list) {
		if(!CollectionUtils.isEmpty(list)) {
			this.list = list;
			this.parentOffset = list.size();
		}
	}
	
	/**
	 * 获取盘整区
	 * @return
	 */
	public ConsolidationArea getConsolidationArea() {
		List<Klines> data = new ArrayList<Klines>();
		ConsolidationArea area = new ConsolidationArea();
		int offset = 0;
		for(offset = this.parentOffset - 1; offset > 0; offset--) {
			Klines current = list.get(offset);
			Klines parent = list.get(offset - 1);
			if(data.isEmpty()) {
				if(verifyArea(current,parent)) {
					data.add(current);
					data.add(parent);
					area.init(data);
				}
			} else if(area.contains(current)) {
				area.addKlines(current);
			} else {
				break;
			}
		}
		
		for(int off = offset;off < this.parentOffset; off++) {
			area.addKlines(list.get(off));
		}
		
		this.parentOffset = offset + 1;
		
		return area;
	}
	
	/**
	 * 校验两根k线是否形成盘整区
	 * @param current 当前k线
	 * @param parent 前一根k线
	 * @return
	 */
	public boolean verifyArea(Klines current,Klines parent) {
		return current.getBodyHighPriceDoubleValue() <= parent.getHighPriceDoubleValue() 
				&& current.getBodyLowPriceDoubleValue() >= parent.getLowPriceDoubleValue();
	}
}
