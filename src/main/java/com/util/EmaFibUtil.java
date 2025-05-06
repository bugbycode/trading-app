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
 * 指数均线回撤计算工具类
 */
public class EmaFibUtil {

	private final Logger logger = LogManager.getLogger(EmaFibUtil.class);
	
	private List<Klines> list;
	
	private FibInfo fibInfo;
	
	public EmaFibUtil(List<Klines> list) {
		this.list = list;
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
		
		PriceUtil.calculateEMA_7_25_99(list);
		
		//最后一根k线
		Klines last = PriceUtil.getLastKlines(list);
		double ema25 = last.getEma25();
		
		Klines second = null;
		Klines first = null;
		
		PositionSide ps = PositionSide.DEFAULT;
		
		if(PriceUtil.isBreachLong(last, ema25)) {//做多
			ps = PositionSide.LONG;
		} else if(PriceUtil.isBreachShort(last, ema25)) {//做空
			ps = PositionSide.SHORT;
		}
		
		for(int index = this.list.size() - 1; index > 0; index--) {
			Klines current = this.list.get(index);
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
		
		List<Klines> subList = PriceUtil.subList(first, second, list);
		List<Klines> subList_second = null;
		Klines start = null;
		Klines end = null;
		Klines after = null;
		if(ps == PositionSide.LONG) {//先寻找高点再寻找低点
			start = PriceUtil.getMaxPriceKLine(subList);
			after = PriceUtil.getAfterKlines(start, list);
			if(after == null) {
				subList_second = PriceUtil.subList(start, list);
			} else {
				subList_second = PriceUtil.subList(after, list);
			}
			end = PriceUtil.getMinPriceKLine(subList_second);
			
			fibInfo = new FibInfo(start.getHighPriceDoubleValue(), end.getLowPriceDoubleValue(), start.getDecimalNum(), FibLevel.LEVEL_1);
		} else if(ps == PositionSide.SHORT) {//先寻找低点再寻找高点
			start = PriceUtil.getMinPriceKLine(subList);
			after = PriceUtil.getAfterKlines(start, list);
			if(after == null) {
				subList_second = PriceUtil.subList(start, list);
			} else {
				subList_second = PriceUtil.subList(after, list);
			}
			end = PriceUtil.getMaxPriceKLine(subList_second);
			
			fibInfo = new FibInfo(start.getLowPriceDoubleValue(), end.getHighPriceDoubleValue(), start.getDecimalNum(), FibLevel.LEVEL_1);
		}
		
		logger.debug(fibInfo);
	}
	
	private boolean verifyHigh(Klines k) {
		return k.getEma7() > k.getEma25();
	}
	
	private boolean verifyLow(Klines k) {
		return k.getEma7() < k.getEma25();
	}
	
	public FibInfo getFibInfo() {
		return fibInfo;
	}

	public boolean verifyOpen(List<Klines> hitList) {
		boolean result = false;
		if(this.fibInfo != null) {
			Klines last = PriceUtil.getLastKlines(hitList);
			double ema25 = last.getEma25();
			double ema99 = last.getEma99();
			double closePrice = last.getClosePriceDoubleValue();
			double fib5Price = this.fibInfo.getFibValue(FibCode.FIB5);
			QuotationMode qm = this.fibInfo.getQuotationMode();
			if(qm == QuotationMode.SHORT && closePrice <= fib5Price && ema25 > ema99) {//做多
				result = true;
			} else if(qm == QuotationMode.LONG && closePrice >= fib5Price && ema25 < ema99) {//做空
				result = true;
			}
		}
		return result;
	}
}
