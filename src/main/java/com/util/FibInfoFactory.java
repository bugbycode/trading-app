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

public class FibInfoFactory {

	private List<Klines> list;
	
	private List<Klines> fibAfterKlines;
	
	private FibInfo fibInfo;
	
	public FibInfoFactory(List<Klines> list) {
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
		
		PositionSide ps = getPositionSide();
		
		Klines third = null;
		Klines second = null;
		Klines first = null;
		
		for(int index = list.size() - 1; index > 0; index--) {
			Klines current = list.get(index);
			if(ps == PositionSide.SHORT) {//low - high - low
				if(third == null) {
					if(verifyLow(current)) {
						third = current;
					}
				} else if(second == null) {
					if(verifyHigh(current)) {
						second = current;
					}
				} else if(first == null) {
					if(verifyLow(current)) {
						first = current;
						break;
					}
				}
			} else if(ps == PositionSide.LONG) { // high - low - high
				if(third == null) {
					if(verifyHigh(current)) {
						third = current;
					}
				} else if(second == null) {
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
		
		if(first == null || second == null || third == null) {
			return;
		}
		
		List<Klines> firstSubList = PriceUtil.subList(first, second, list);
		List<Klines> secondSubList = null;
		Klines start = null;
		Klines end = null;
		Klines startAfterFlag = null;
		if(ps == PositionSide.SHORT) {
			start = PriceUtil.getMaxPriceKLine(firstSubList);
			startAfterFlag = PriceUtil.getAfterKlines(start, firstSubList);
			if(startAfterFlag != null) {
				secondSubList = PriceUtil.subList(startAfterFlag, list);
				end = PriceUtil.getMinPriceKLine(secondSubList);
				this.fibInfo = new FibInfo(start.getHighPriceDoubleValue(), end.getLowPriceDoubleValue(), start.getDecimalNum(), FibLevel.LEVEL_1);
			}
		} else if(ps == PositionSide.LONG) {
			start = PriceUtil.getMinPriceKLine(firstSubList);
			startAfterFlag = PriceUtil.getAfterKlines(start, firstSubList);
			if(startAfterFlag != null) {
				secondSubList = PriceUtil.subList(startAfterFlag, list);
				end = PriceUtil.getMaxPriceKLine(secondSubList);
				this.fibInfo = new FibInfo(start.getLowPriceDoubleValue(), end.getHighPriceDoubleValue(), start.getDecimalNum(), FibLevel.LEVEL_1);
			}
		}
		
		if(this.fibInfo == null) {
			return;
		}
		
		this.resetFibLevel();
		
		Klines fibAfterFlag = PriceUtil.getAfterKlines(end, secondSubList);
		if(fibAfterFlag != null) {
			this.fibAfterKlines = PriceUtil.subList(fibAfterFlag, list);
			this.fibInfo.setFibAfterKlines(fibAfterKlines);
		}
	}
	
	private PositionSide getPositionSide() {
		PositionSide ps = PositionSide.DEFAULT;
		Klines last = PriceUtil.getLastKlines(list);
		if(verifyLong(last)) {
			ps = PositionSide.LONG;
		} else if(verifyShort(last)) {
			ps = PositionSide.SHORT;
		}
		return ps;
	}
	
	private boolean verifyLong(Klines k) {
		return k.getEma25() > k.getEma99() && k.getEma99() > 0;
	}
	
	private boolean verifyShort(Klines k) {
		return k.getEma25() < k.getEma99() && k.getEma99() > 0;
	}
	
	private boolean verifyHigh(Klines k) {
		return k.getEma7() > k.getEma25();
	}
	
	private boolean verifyLow(Klines k) {
		return k.getEma7() < k.getEma25();
	}
	
	public FibInfo getFibInfo() {
		return this.fibInfo;
	}
	
	public List<Klines> getFibAfterKlines() {
		return fibAfterKlines;
	}
	
	public boolean isLong() {
		boolean result = false;
		if(fibInfo != null && fibInfo.getQuotationMode() == QuotationMode.LONG) {
			result = true;
		}
		return result;
	}
	
	public boolean isShort() {
		boolean result = false;
		if(fibInfo != null && fibInfo.getQuotationMode() == QuotationMode.SHORT) {
			result = true;
		}
		return result;
	}
	
	private FibCode getFibCode() {
		FibCode result = FibCode.FIB1;
		if(this.fibInfo != null) {
			QuotationMode mode = this.fibInfo.getQuotationMode();
			Klines last = PriceUtil.getLastKlines(list);
			double ema99 = last.getEma99();
			FibCode[] codes = FibCode.values();
			for(int index = 0; index < codes.length; index++) {
				FibCode code = codes[index];
				double fibPrice = this.fibInfo.getFibValue(code);
				if((mode == QuotationMode.LONG && fibPrice >= ema99) 
						|| (mode == QuotationMode.SHORT && fibPrice <= ema99)) {
					result = code;
					break;
				}
			}
		}
		if(result == FibCode.FIB66) {
			result = FibCode.FIB618;
		}
		return result;
	}
	
	private void resetFibLevel() {
		if(this.fibInfo != null) {
			FibCode startFibCode = getFibCode();
			FibLevel level = FibLevel.valueOf(startFibCode);
			this.fibInfo = new FibInfo(this.fibInfo.getFibValue(FibCode.FIB1), this.fibInfo.getFibValue(FibCode.FIB0), this.fibInfo.getDecimalPoint(), level);
		}
	}
}
