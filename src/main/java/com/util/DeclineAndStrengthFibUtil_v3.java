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
 * 价格行为斐波那契回撤工具类
 */
public class DeclineAndStrengthFibUtil_v3 {

	private final Logger logger = LogManager.getLogger(DeclineAndStrengthFibUtil_v3.class);

	private List<Klines> list;
	
	private FibInfo fibInfo;
	
	private List<Klines> fibAfterKlines;
	
	private List<Double> openPriceList = new ArrayList<Double>();
	
	public DeclineAndStrengthFibUtil_v3(List<Klines> list) {
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
		
		for(int index = this.list.size() - 1; index > 0; index--) {
			
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
		
		QuotationMode qm = fibInfo.getQuotationMode();
		
		//寻找开仓点
		for(int index = 0;index < this.list.size(); index++) {
			Klines k0 = this.list.get(index);
			if(k0.lt(end)) {
				continue;
			}
			Klines k1 = this.list.get(index - 1);
			Klines k2 = this.list.get(index - 2);
			
			double c_high = k0.getHighPriceDoubleValue();
			double n_high = k1.getHighPriceDoubleValue();
			double c_low = k0.getLowPriceDoubleValue();
			double n_low = k1.getLowPriceDoubleValue();
			double c_body_high = k0.getBodyHighPriceDoubleValue();
			double n_body_high = k1.getBodyHighPriceDoubleValue();
			double c_body_low = k0.getBodyLowPriceDoubleValue();
			double n_body_low = k1.getBodyLowPriceDoubleValue();
			
			if(qm == QuotationMode.SHORT && PriceUtil.verifyPowerful_v2(k0, k1, k2)) { //做多 寻找强势信号
				openPriceList.add(PriceUtil.getMinPrice(c_low, n_low));//最低点
				openPriceList.add(PriceUtil.getMinPrice(c_body_high, n_body_high));//实体部分高点最低价
				openPriceList.add(PriceUtil.getMinPrice(c_body_low, n_body_low));//实体部分低点最低价
				openPriceList.add(c_body_high);
				openPriceList.sort(new PriceComparator(SortType.DESC));
				break;
			} else if(qm == QuotationMode.LONG && PriceUtil.verifyDecliningPrice_v2(k0, k1, k2)){//做空 寻找颓势信号
				openPriceList.add(PriceUtil.getMaxPrice(c_high, n_high));//最高点
				openPriceList.add(PriceUtil.getMaxPrice(c_body_high, n_body_high));//实体部分高点最高价
				openPriceList.add(PriceUtil.getMaxPrice(c_body_low, n_body_low));//实体部分低点最高价
				openPriceList.add(c_body_low);
				openPriceList.sort(new PriceComparator(SortType.ASC));
				break;
			}
		}
	}
	
	public boolean verifyOpen(List<Klines> hitList) {
		boolean result = false;
		if(fibInfo != null) {
			QuotationMode qm = fibInfo.getQuotationMode();
			double fib382Price = fibInfo.getFibValue(FibCode.FIB382);
			List<Klines> afterList = getFibAfterKlines();
			Klines afterLowKlines = PriceUtil.getMinPriceKLine(afterList);
			Klines afterHighKlines = PriceUtil.getMaxPriceKLine(afterList);
			Klines last = PriceUtil.getLastKlines(hitList);
			double closePrice = last.getClosePriceDoubleValue();
			for(int index = 0; index < openPriceList.size(); index++) {
				double price = openPriceList.get(index);
				if(qm == QuotationMode.SHORT && (PriceUtil.isBreachLong(last, price) || PriceUtil.isLong_v2(price, hitList)) 
						&& !PriceUtil.isObsoleteLong(afterLowKlines, openPriceList, index)
						&& closePrice < fib382Price) {
					result = true;
				} else if(qm == QuotationMode.LONG && (PriceUtil.isBreachShort(last, price) || PriceUtil.isShort_v2(price, hitList)) 
						&& !PriceUtil.isObsoleteShort(afterHighKlines, openPriceList, index)
						&& closePrice > fib382Price) {
					result = true;
				}
			}
		}
		return result;
	}
	
	public FibInfo getFibInfo() {
		return fibInfo;
	}

	public List<Klines> getFibAfterKlines() {
		return fibAfterKlines;
	}
	
	public List<Double> getOpenPriceList() {
		return this.openPriceList;
	}
}
