package com.util;

import java.util.ArrayList;
import java.util.List;

import org.springframework.util.CollectionUtils;

import com.bugbycode.module.FibCode;
import com.bugbycode.module.FibInfo;
import com.bugbycode.module.FibLevel;
import com.bugbycode.module.Klines;
import com.bugbycode.module.QuotationMode;
import com.bugbycode.module.SortType;
import com.bugbycode.module.trading.PositionSide;

/**
 * 价格行为指标
 */
public class PriceActionFactory {

	private List<Klines> list;
	
	private FibInfo fibInfo;
	
	public PriceActionFactory(List<Klines> list) {
		this.list = new ArrayList<Klines>();
		if(!CollectionUtils.isEmpty(list)) {
			this.list.addAll(list);
			this.init();
		}
	}
	
	private void init() {
		if(CollectionUtils.isEmpty(list) || list.size() < 99) {
			return;
		}
		
		KlinesComparator kc = new KlinesComparator(SortType.ASC);
		this.list.sort(kc);
		
		PriceUtil.calculateEMA_7_25_99(list);
		
		Klines last = PriceUtil.getLastKlines(list);
		
		PositionSide ps = getPositionSide(last);
		
		if(ps == PositionSide.DEFAULT) {
			return;
		}
		
		Klines first = null;
		Klines second = null;
		for(int index = list.size() - 1; index > 0; index--) {
			Klines current = list.get(index);
			if(ps == PositionSide.LONG) {
				if(second == null) {
					if(verifyHigh(current)) {
						second = current;
					}
				} else if(first == null) {
					if(verifyLow(current)) {
						first = current;
						break;
					}
				}
			} else if(ps == PositionSide.SHORT) {
				if(second == null) {
					if(verifyLow(current)) {
						second = current;
					}
				} else if(first == null) {
					if(verifyHigh(current)) {
						first = current;
						break;
					}
				}
			}
		}
		
		if(first == null || second == null) {
			return;
		}
		
		List<Klines> firstSubList = PriceUtil.subList(first, second, list);
		List<Klines> secondSubList = null;
		Klines start = null;
		Klines end = null;
		Klines startAfter = null;
		if(ps == PositionSide.LONG) {
			start = PriceUtil.getMaxPriceKLine(firstSubList);
			startAfter = PriceUtil.getAfterKlines(start, list);
			secondSubList = PriceUtil.subList(startAfter, list);
			end = PriceUtil.getMinPriceKLine(secondSubList);
			fibInfo = new FibInfo(start.getHighPriceDoubleValue(), end.getLowPriceDoubleValue(), start.getDecimalNum(), FibLevel.LEVEL_1);
		} else if(ps == PositionSide.SHORT) {
			start = PriceUtil.getMinPriceKLine(firstSubList);
			startAfter = PriceUtil.getAfterKlines(start, list);
			secondSubList = PriceUtil.subList(startAfter, list);
			end = PriceUtil.getMaxPriceKLine(secondSubList);
			fibInfo = new FibInfo(start.getLowPriceDoubleValue(), end.getHighPriceDoubleValue(), start.getDecimalNum(), FibLevel.LEVEL_1);
		}
	}
	
	public boolean verifyOpen(List<Klines> list) {
		boolean result = false;
		if(!(CollectionUtils.isEmpty(list) || fibInfo == null)) {
			PriceUtil.calculateEMA_7_25_99(list);
			QuotationMode qm = fibInfo.getQuotationMode();
			Klines last = PriceUtil.getLastKlines(list);
			double ema7 = last.getEma7();
			double ema25 = last.getEma25();
			double closePrice = last.getClosePriceDoubleValue();
			double fib382Price = fibInfo.getFibValue(FibCode.FIB382);
			if(qm == QuotationMode.SHORT && ema7 > ema25 && closePrice < fib382Price && PriceUtil.isBreachLong(last, ema25)) {//做多
				result = true;
			} else if(qm == QuotationMode.LONG && ema7 < ema25 && closePrice > fib382Price && PriceUtil.isBreachShort(last, ema25)){//做空
				result = true;
			}
		}
		return result;
	}
	
	private PositionSide getPositionSide(Klines k) {
		PositionSide ps = PositionSide.DEFAULT;
		if(verifyLong(k)) {
			ps = PositionSide.LONG;
		} else if(verifyShort(k)) {
			ps = PositionSide.SHORT;
		}
		return ps;
	}
	
	private boolean verifyLong(Klines k) {
		return k.getEma25() < k.getEma99();
	}
	
	private boolean verifyShort(Klines k) {
		return k.getEma25() > k.getEma99();
	}
	
	private boolean verifyHigh(Klines k) {
		return k.getEma7() > k.getEma25() && k.getEma25() > k.getEma99();
	}
	
	private boolean verifyLow(Klines k) {
		return k.getEma7() < k.getEma25() && k.getEma25() < k.getEma99();
	}
	
	public FibInfo getFibInfo() {
		return this.fibInfo;
	}
	
}
