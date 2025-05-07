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
		
		Klines last = PriceUtil.getLastKlines(list);
		
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
		
		QuotationMode qm = fibInfo.getQuotationMode();
		
		int searchIndex = -1;
		//寻找恐慌抛售或贪婪买入的情况
		for(int index = this.list.size() - 1; index > 2; index--) {
			Klines k0 = this.list.get(index);
			Klines k1 = this.list.get(index - 1);
			Klines k2 = this.list.get(index - 2);
			if(k0.isEquals(start)) {
				break;
			}
			if(qm == QuotationMode.SHORT && PriceUtil.isPanicSell(k0, k1, k2)) {
				searchIndex = index;
				break;
			} else if(qm == QuotationMode.LONG && PriceUtil.isGreedyBuy(k0, k1, k2)) {
				searchIndex = index;
				break;
			}
		}
		
		if(searchIndex == -1) {
			return;
		}
		
		int afterIndex = -1;
		//寻找开仓点
		for(int index = searchIndex;index < this.list.size(); index++) {
			Klines k0 = this.list.get(index);//当前k线
			Klines k1 = this.list.get(index - 1);//前一根k线
			
			double c_high = k0.getHighPriceDoubleValue();
			double n_high = k1.getHighPriceDoubleValue();
			double c_low = k0.getLowPriceDoubleValue();
			double n_low = k1.getLowPriceDoubleValue();
			double c_body_high = k0.getBodyHighPriceDoubleValue();
			double n_body_high = k1.getBodyHighPriceDoubleValue();
			double c_body_low = k0.getBodyLowPriceDoubleValue();
			double n_body_low = k1.getBodyLowPriceDoubleValue();
			
			if(qm == QuotationMode.SHORT && k0.isRise()) { //做多 寻找强势信号
				addOpenPrice(PriceUtil.getMinPrice(c_low, n_low));//最低点
				addOpenPrice(PriceUtil.getMinPrice(c_body_high, n_body_high));//实体部分高点最低价
				addOpenPrice(PriceUtil.getMinPrice(c_body_low, n_body_low));//实体部分低点最低价
				addOpenPrice(c_body_high);
				openPriceList.sort(new PriceComparator(SortType.DESC));
				afterIndex = index;
				break;
			} else if(qm == QuotationMode.LONG && k0.isFall()){//做空 寻找颓势信号
				addOpenPrice(PriceUtil.getMaxPrice(c_high, n_high));//最高点
				addOpenPrice(PriceUtil.getMaxPrice(c_body_high, n_body_high));//实体部分高点最高价
				addOpenPrice(PriceUtil.getMaxPrice(c_body_low, n_body_low));//实体部分低点最高价
				addOpenPrice(c_body_low);
				openPriceList.sort(new PriceComparator(SortType.ASC));
				afterIndex = index;
				break;
			}
		}
		
		if(afterIndex == -1) {
			return;
		}
		
		Klines afterFlag = this.list.get(afterIndex);
		
		if(afterFlag.isEquals(last)) {
			return;
		}
		
		afterFlag = this.list.get(afterIndex + 1);
		this.fibAfterKlines = PriceUtil.subList(afterFlag, list);
		
		fibInfo.setFibAfterKlines(fibAfterKlines);
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
	
	private void addOpenPrice(double price) {
		if(!PriceUtil.contains(openPriceList, price)) {
			openPriceList.add(price);
		}
	}
}
