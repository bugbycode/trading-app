package com.util;

import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.util.CollectionUtils;

import com.bugbycode.module.FibInfo;
import com.bugbycode.module.FibLevel;
import com.bugbycode.module.Klines;
import com.bugbycode.module.SortType;
import com.bugbycode.module.trading.PositionSide;

/**
 * 斐波那契回撤工具类 以BB %B值为参考计算
 */
public class FibUtil_v5 {

	private final Logger logger = LogManager.getLogger(FibUtil_v5.class);
	
	private List<Klines> list;
	
	private FibInfo fibInfo;
	
	private List<Klines> fibAfterKlines;
	
	public FibUtil_v5(List<Klines> list) {
		this.list = new ArrayList<Klines>();
		if(!CollectionUtils.isEmpty(list)) {
			this.list.addAll(list);
			this.init();
		}
	}

	private void init() {
		if(CollectionUtils.isEmpty(this.list)) {
			return;
		}
		
		KlinesComparator kc = new KlinesComparator(SortType.ASC);
		this.list.sort(kc);
		
		PriceUtil.calculateAllBBPercentB(list);
		
		PositionSide ps = PositionSide.DEFAULT;
		
		Klines first_index = null;
		Klines second_index = null;
		
		for(int index = list.size() - 1; index > 0; index--) {
			Klines current = list.get(index);
			Klines next = list.get(index - 1);
			if(second_index == null) {
				if(PriceUtil.isHigh(current, next)) {
					second_index = current;
					ps = PositionSide.LONG;
				} else if(PriceUtil.isLow(current, next)) {
					second_index = current;
					ps = PositionSide.SHORT;
				}
			}
			
			if(first_index == null && second_index != null) {
				if(ps == PositionSide.LONG && PriceUtil.isLow(current, next)) {
					first_index = next;
					break;
				} else if(ps == PositionSide.SHORT && PriceUtil.isHigh(current, next)) {
					first_index = next;
					break;
				}
			}
		}
		
		if(first_index == null || second_index == null) {
			return;
		}
		
		List<Klines> subFirstList = PriceUtil.subList(first_index, second_index, list);
		
		if(CollectionUtils.isEmpty(subFirstList)) {
			return;
		}
		
		Klines start = null;
		Klines end = null;
		
		if(ps == PositionSide.LONG) {//多头
			//寻找高低点之间最低点
			start = PriceUtil.getMinPriceKLine(subFirstList);
			List<Klines> subSecondList = PriceUtil.subList(start, list);
			//寻找终点（高点）
			end = PriceUtil.getMaxPriceKLine(subSecondList);
			
			fibInfo = new FibInfo(start.getLowPriceDoubleValue(), end.getHighPriceDoubleValue(), start.getDecimalNum(), FibLevel.LEVEL_1);
			
		} else if(ps == PositionSide.SHORT) {//空头
			//寻找高低点之间最高点
			start = PriceUtil.getMaxPriceKLine(subFirstList);
			List<Klines> subSecondList = PriceUtil.subList(start, list);
			//寻找终点（低点）
			end = PriceUtil.getMinPriceKLine(subSecondList);
			
			fibInfo = new FibInfo(start.getHighPriceDoubleValue(), end.getLowPriceDoubleValue(), start.getDecimalNum(), FibLevel.LEVEL_1);
			
		}

		logger.debug(fibInfo);
		
		if(fibInfo == null) {
			return;
		}
		
		Klines last = PriceUtil.getAfterKlines(end, list);
		if(last == null) {
			return;
		}
		
		this.fibAfterKlines = PriceUtil.subList(last, list);
		
		fibInfo.setFibAfterKlines(fibAfterKlines);
	}

	public FibInfo getFibInfo() {
		return fibInfo;
	}

	public List<Klines> getFibAfterKlines() {
		return fibAfterKlines;
	}
}
