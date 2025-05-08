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
	
	private List<Klines> list_15m;
	
	private List<Klines> list_1h;
	
	private FibInfo fibInfo;
	
	public EmaFibUtil(List<Klines> list_15m) {
		this.list_15m = list_15m;
		this.list_15m = new ArrayList<Klines>();
		this.list_1h = new ArrayList<>();
		if(!CollectionUtils.isEmpty(list_15m)) {
			this.list_15m.addAll(list_15m);
			this.init();
		}
	}

	private void init() {
		if(CollectionUtils.isEmpty(this.list_15m)) {
			return;
		}
		
		KlinesComparator kc = new KlinesComparator(SortType.ASC);
		this.list_15m.sort(kc);
		
		this.list_1h = PriceUtil.to1HFor15MKlines(list_15m);
		
		if(this.list_1h.size() < 99) {
			return;
		}
		
		Klines last_1h = PriceUtil.getLastKlines(this.list_1h);
		
		int minute = DateFormatUtil.getMinute(last_1h.getEndTime());
		if(minute != 59) {
			this.list_1h.remove(last_1h);
			last_1h = PriceUtil.getLastKlines(this.list_1h);
		}
		
		this.list_1h.sort(kc);
		
		PriceUtil.calculateEMA_7_25_99(this.list_1h);
		
		//最后一根k线
		Klines last_15m = PriceUtil.getLastKlines(list_15m);
		double ema25 = last_1h.getEma25();
		double ema99 = last_1h.getEma99();
		
		logger.debug("ema25: {}, ema99: {}", ema25, ema99);
		
		Klines second = null;
		Klines first = null;
		
		PositionSide ps = PositionSide.DEFAULT;
		
		if(PriceUtil.isBreachLong(last_15m, ema99) 
				|| PriceUtil.isBreachLong(last_15m, ema25) 
				|| PriceUtil.isLong_v2(ema25, list_15m)
				|| PriceUtil.isLong_v2(ema99, list_15m)) {//做多
			ps = PositionSide.LONG;
		} else if(PriceUtil.isBreachShort(last_15m, ema99) 
				|| PriceUtil.isBreachShort(last_15m, ema25)
				|| PriceUtil.isShort_v2(ema25, list_15m)
				|| PriceUtil.isShort_v2(ema99, list_15m)) {//做空
			ps = PositionSide.SHORT;
		}
		
		for(int index = this.list_1h.size() - 1; index > 0; index--) {
			Klines current = this.list_1h.get(index);
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
		
		List<Klines> subList = PriceUtil.subList(first, second, this.list_1h);
		List<Klines> subList_second = null;
		Klines start = null;
		Klines end = null;
		Klines after = null;
		if(ps == PositionSide.LONG) {//先寻找高点再寻找低点
			start = PriceUtil.getMaxPriceKLine(subList);
			after = PriceUtil.getAfterKlines(start, this.list_1h);
			if(after == null) {
				subList_second = PriceUtil.subList(start, this.list_1h);
			} else {
				subList_second = PriceUtil.subList(after, this.list_1h);
			}
			end = PriceUtil.getMinPriceKLine(subList_second);
			
			fibInfo = new FibInfo(start.getHighPriceDoubleValue(), end.getLowPriceDoubleValue(), start.getDecimalNum(), FibLevel.LEVEL_1);
		} else if(ps == PositionSide.SHORT) {//先寻找低点再寻找高点
			start = PriceUtil.getMinPriceKLine(subList);
			after = PriceUtil.getAfterKlines(start, this.list_1h);
			if(after == null) {
				subList_second = PriceUtil.subList(start, this.list_1h);
			} else {
				subList_second = PriceUtil.subList(after, this.list_1h);
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

	public boolean verifyOpen() {
		boolean result = false;
		if(this.fibInfo != null) {
			Klines last_1h = PriceUtil.getLastKlines(this.list_1h);
			Klines last_15m = PriceUtil.getLastKlines(this.list_15m);
			double ema25 = last_1h.getEma25();
			double ema99 = last_1h.getEma99();
			double closePrice = last_15m.getClosePriceDoubleValue();
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
