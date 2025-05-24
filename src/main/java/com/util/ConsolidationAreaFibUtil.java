package com.util;

import java.util.List;

import org.springframework.util.CollectionUtils;

import com.bugbycode.module.FibInfo;
import com.bugbycode.module.FibLevel;
import com.bugbycode.module.Klines;

/**
 * 盘整区斐波那契回撤工具类
 */
public class ConsolidationAreaFibUtil {

	private ConsolidationAreaUtil areaUtil;
	
	public ConsolidationAreaFibUtil(List<Klines> list_1d) {
		this.areaUtil = new ConsolidationAreaUtil(list_1d);
	}
	
	public FibInfo getFibInfo(List<Klines> list_15m) {
		FibInfo fibInfo = null;
		if(!this.areaUtil.isEmpty() && !CollectionUtils.isEmpty(list_15m)) {
			Klines last = PriceUtil.getLastKlines(list_15m);
			if(PriceUtil.isLong_v2(this.areaUtil.getLowPrice(), list_15m)) {
				fibInfo = new FibInfo(this.areaUtil.getLowPrice(), this.areaUtil.getHighPrice(), last.getDecimalNum(), FibLevel.LEVEL_1);
			} else if(PriceUtil.isShort_v2(this.areaUtil.getHighPrice(), list_15m)) {
				fibInfo = new FibInfo(this.areaUtil.getHighPrice(), this.areaUtil.getLowPrice(), last.getDecimalNum(), FibLevel.LEVEL_1);
			}
		}
		return fibInfo;
	}
}
