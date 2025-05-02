package com.util;

import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.util.CollectionUtils;

import com.bugbycode.module.FibCode;
import com.bugbycode.module.FibInfo;
import com.bugbycode.module.FibLevel;
import com.bugbycode.module.Klines;
import com.bugbycode.module.QuotationMode;
import com.bugbycode.module.SortType;
import com.bugbycode.module.trading.PositionSide;

/**
 * 指数均线斐波那契回撤工具类
 */
public class FibUtil_v4 {
	
	private final Logger logger = LogManager.getLogger(FibUtil_v4.class);

	private List<Klines> list;
	
	private List<Klines> fibAfterKlines;
	
	private List<Double> openPriceList = new ArrayList<Double>();
	
	private FibInfo fibInfo;
	
	private Klines signKlines;
	
	public FibUtil_v4(List<Klines> list) {
		this.list = new ArrayList<Klines>();
		if(!CollectionUtils.isEmpty(list)) {
			this.list.addAll(list);
			this.init();
		}
	}
	
	public void init() {
		
		if(CollectionUtils.isEmpty(this.list)) {
			return;
		}
		
		KlinesComparator kc = new KlinesComparator(SortType.ASC);
		this.list.sort(kc);
		
		PriceUtil.calculateEMA_7_25_99(list);
		PriceUtil.calculateAllBBPercentB(list);
		
		PositionSide ps = PositionSide.DEFAULT;
		
		Klines first_index = null;
		Klines second_index = null;
		
		for(int index = this.list.size() - 1; index > 0; index--) {
			Klines current = list.get(index);
			Klines next = list.get(index - 1);
			if(second_index == null) {
				double c_high = current.getHighPriceDoubleValue();
				double n_high = next.getHighPriceDoubleValue();
				double c_low = current.getLowPriceDoubleValue();
				double n_low = next.getLowPriceDoubleValue();
				double c_body_high = current.getBodyHighPriceDoubleValue();
				double n_body_high = next.getBodyHighPriceDoubleValue();
				double c_body_low = current.getBodyLowPriceDoubleValue();
				double n_body_low = next.getBodyLowPriceDoubleValue();
				if(PriceUtil.isHigh(current, next)) {
					signKlines = next;
					second_index = current;
					ps = PositionSide.LONG;
					
					openPriceList.add(PriceUtil.getMaxPrice(c_high, n_high));//最高点
					openPriceList.add(PriceUtil.getMaxPrice(c_body_high, n_body_high));//实体部分高点最高价
					openPriceList.add(PriceUtil.getMaxPrice(c_body_low, n_body_low));//实体部分低点最高价
					openPriceList.sort(new PriceComparator(SortType.ASC));
					
				} else if(PriceUtil.isLow(current, next)) {
					signKlines = next;
					second_index = current;
					ps = PositionSide.SHORT;
					
					openPriceList.add(PriceUtil.getMinPrice(c_low, n_low));//最低点
					openPriceList.add(PriceUtil.getMinPrice(c_body_high, n_body_high));//实体部分高点最低价
					openPriceList.add(PriceUtil.getMinPrice(c_body_low, n_body_low));//实体部分低点最低价
					openPriceList.sort(new PriceComparator(SortType.DESC));
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
			
			fibInfo = new FibInfo(start.getLowPriceDoubleValue(), end.getHighPriceDoubleValue(), start.getDecimalNum(), FibLevel.LEVEL_3);
			
		} else if(ps == PositionSide.SHORT) {//空头
			//寻找高低点之间最高点
			start = PriceUtil.getMaxPriceKLine(subFirstList);
			List<Klines> subSecondList = PriceUtil.subList(start, list);
			//寻找终点（低点）
			end = PriceUtil.getMinPriceKLine(subSecondList);
			
			fibInfo = new FibInfo(start.getHighPriceDoubleValue(), end.getLowPriceDoubleValue(), start.getDecimalNum(), FibLevel.LEVEL_3);
			
		}
		
		if(fibInfo == null) {
			return;
		}

		logger.debug(fibInfo);
		
		Klines last = PriceUtil.getAfterKlines(end, list);
		if(last == null) {
			return;
		}
		
		this.fibAfterKlines = PriceUtil.subList(last, list);
		
		fibInfo.setFibAfterKlines(fibAfterKlines);
	}
	
	public FibInfo getFibInfo() {
		return this.fibInfo;
	}
	
	public List<Klines> getFibAfterKlines() {
		return fibAfterKlines;
	}

	public List<Double> getOpenPriceList() {
		return openPriceList;
	}
	
	public boolean verifyOpen(List<Klines> hitList) {
		boolean result = false;
		if(fibInfo != null) {
			
			double ema25 = signKlines.getEma25();
			double ema99 = signKlines.getEma99();
			
			QuotationMode qm = fibInfo.getQuotationMode();
			double fib382Price = fibInfo.getFibValue(FibCode.FIB382);
			List<Klines> afterList = getFibAfterKlines();
			Klines afterLowKlines = PriceUtil.getMinPriceKLine(afterList);
			Klines afterHighKlines = PriceUtil.getMaxPriceKLine(afterList);
			Klines last = PriceUtil.getLastKlines(hitList);
			double closePrice = last.getClosePriceDoubleValue();
			for(int index = 0; index < openPriceList.size(); index++) {
				double price = openPriceList.get(index);
				if(ema25 > ema99 && qm == QuotationMode.SHORT && PriceUtil.isLong_v2(price, hitList)
						&& !PriceUtil.isObsoleteLong(afterLowKlines, openPriceList, index)
						&& closePrice < fib382Price) {
					result = true;
				} else if(ema25 < ema99 && qm == QuotationMode.LONG && PriceUtil.isShort_v2(price, hitList)
						&& !PriceUtil.isObsoleteShort(afterHighKlines, openPriceList, index)
						&& closePrice > fib382Price) {
					result = true;
				}
			}
		}
		return result;
	}

	public Klines getSignKlines() {
		return signKlines;
	}
	
}
