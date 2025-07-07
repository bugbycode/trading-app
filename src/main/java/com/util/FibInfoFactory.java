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
 * 斐波那契回撤指标 V1 按指数均线做交易
 */
public class FibInfoFactory {

	private List<Klines> list;
	
	private List<Klines> fibAfterKlines;
	
	private FibInfo fibInfo;
	
	private FibInfo fibInfo_parent;
	
	private List<Klines> list_15m;//十五分钟级别k线 用于补充回撤之后的k线信息
	
	private Klines start = null;
	
	private Klines end = null;
	
	public FibInfoFactory(List<Klines> list, List<Klines> list_15m) {
		this.list = new ArrayList<Klines>();
		this.list_15m = new ArrayList<Klines>();
		if(!CollectionUtils.isEmpty(list_15m)) {
			this.list_15m.addAll(list_15m);
		}
		if(!CollectionUtils.isEmpty(list)) {
			this.list.addAll(list);
			this.init(false);
		}
	}

	private void init(boolean loadParent) {
		if(CollectionUtils.isEmpty(list) || list.size() < 99 || CollectionUtils.isEmpty(list_15m)) {
			return;
		}
		
		KlinesComparator kc = new KlinesComparator(SortType.ASC);
		this.list.sort(kc);
		
		PriceUtil.calculateEMA_7_25_99(list);
		PriceUtil.calculateMACD(list);
		
		PositionSide ps = getPositionSide(loadParent);
		
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
		
		List<Klines> list_parent = PriceUtil.subList(list.get(0), start, list);
		Klines third_parent = null;
		Klines second_parent = null;
		Klines first_parent = null;
		
		for(int index = list_parent.size() - 1; index > 0; index--) {
			Klines current = list_parent.get(index);
			if(ps == PositionSide.SHORT) {//low - high - low
				if(third_parent == null) {
					if(verifyLow(current)) {
						third_parent = current;
					}
				} else if(second_parent == null) {
					if(verifyHigh(current)) {
						second_parent = current;
					}
				} else if(first_parent == null) {
					if(verifyLow(current)) {
						first_parent = current;
						break;
					}
				}
			} else if(ps == PositionSide.LONG) { // high - low - high
				if(third_parent == null) {
					if(verifyHigh(current)) {
						third_parent = current;
					}
				} else if(second_parent == null) {
					if(verifyLow(current)) {
						second_parent = current;
					}
				} else if(first_parent == null) {
					if(verifyHigh(current)) {
						first_parent = current;
						break;
					}
				}
			}
		}
		
		if(!(first_parent == null || second_parent == null || third_parent == null)) {
			List<Klines> firstSubList_parent = PriceUtil.subList(first_parent, second_parent, list_parent);
			List<Klines> secondSubList_parent = null;
			Klines start_parent = null;
			Klines end_parent = null;
			Klines startAfterFlag_parent = null;
			if(ps == PositionSide.SHORT) {
				start_parent = PriceUtil.getMaxPriceKLine(firstSubList_parent);
				startAfterFlag_parent = PriceUtil.getAfterKlines(start_parent, firstSubList_parent);
				if(startAfterFlag_parent != null) {
					secondSubList_parent = PriceUtil.subList(startAfterFlag_parent, list_parent);
					end_parent = PriceUtil.getMinPriceKLine(secondSubList_parent);
					this.fibInfo_parent = new FibInfo(start_parent.getHighPriceDoubleValue(), end_parent.getLowPriceDoubleValue(), start_parent.getDecimalNum(), FibLevel.LEVEL_1);
				}
			} else if(ps == PositionSide.LONG) {
				start_parent = PriceUtil.getMinPriceKLine(firstSubList_parent);
				startAfterFlag_parent = PriceUtil.getAfterKlines(start_parent, firstSubList_parent);
				if(startAfterFlag_parent != null) {
					secondSubList_parent = PriceUtil.subList(startAfterFlag_parent, list_parent);
					end_parent = PriceUtil.getMaxPriceKLine(secondSubList_parent);
					this.fibInfo_parent = new FibInfo(start_parent.getLowPriceDoubleValue(), end_parent.getHighPriceDoubleValue(), start_parent.getDecimalNum(), FibLevel.LEVEL_1);
				}
			}
			
		}
		
		if(!loadParent) {
			QuotationMode mode = this.fibInfo.getQuotationMode();
			if((mode == QuotationMode.LONG && !isLong()) || (mode == QuotationMode.SHORT && !isShort())) {
				this.init(true);
			}
		}
		
		this.resetFibLevel();
		
		Klines fibAfterFlag = PriceUtil.getAfterKlines(end, this.list_15m);
		if(fibAfterFlag != null) {
			this.fibAfterKlines = PriceUtil.subList(fibAfterFlag, this.list_15m);
			this.fibInfo.setFibAfterKlines(fibAfterKlines);
		}
	}
	
	private PositionSide getPositionSide(boolean loadParent) {
		PositionSide ps = PositionSide.DEFAULT;
		Klines last = PriceUtil.getLastKlines(list);
		if(verifyLong(last, loadParent)) {
			ps = PositionSide.LONG;
		} else if(verifyShort(last, loadParent)) {
			ps = PositionSide.SHORT;
		}
		return ps;
	}
	
	private boolean verifyLong(Klines k, boolean loadParent) {
		if(loadParent) {
			return k.getEma7() < k.getEma25() && k.getEma25() > 0;
		} else {
			return k.getEma7() > k.getEma25() && k.getEma25() > 0;
		}
	}
	
	private boolean verifyShort(Klines k, boolean loadParent) {
		if(loadParent) {
			return k.getEma7() > k.getEma25() && k.getEma25() > 0;
		} else {
			return k.getEma7() < k.getEma25() && k.getEma25() > 0;
		}
	}
	
	private boolean verifyHigh(Klines k) {
		return k.getEma7() > k.getEma25() && k.getEma25() > 0;
	}
	
	private boolean verifyLow(Klines k) {
		return k.getEma7() < k.getEma25() && k.getEma25() > 0;
	}
	
	public FibInfo getFibInfo() {
		return this.fibInfo;
	}
	
	public List<Klines> getFibAfterKlines() {
		return fibAfterKlines;
	}
	
	public boolean isLong() {
		boolean result = false;
		if(fibInfo != null && fibInfo_parent != null && fibInfo.getQuotationMode() == QuotationMode.LONG
				&& fibInfo.getFibValue(FibCode.FIB0) > fibInfo_parent.getFibValue(FibCode.FIB0)) {
			result = true;
		}
		return result;
	}
	
	public boolean isShort() {
		boolean result = false;
		if(fibInfo != null && fibInfo_parent != null && fibInfo.getQuotationMode() == QuotationMode.SHORT
				&& fibInfo.getFibValue(FibCode.FIB0) < fibInfo_parent.getFibValue(FibCode.FIB0)) {
			result = true;
		}
		return result;
	}
	
	private FibCode getFibCode() {
		FibCode result = FibCode.FIB1;
		if(this.fibInfo != null) {
			QuotationMode mode = this.fibInfo.getQuotationMode();
			Klines last = PriceUtil.getLastKlines(list);
			double emaValue = last.getEma25();
			List<Klines> fibSubList = PriceUtil.subList(start, end, list);
			for(int index = 0; index < fibSubList.size(); index++) {
				Klines current = fibSubList.get(index);
				double c_ema7 = current.getEma7();
				double c_ema25 = current.getEma25();
				if(mode == QuotationMode.LONG && c_ema7 > c_ema25) {
					emaValue = c_ema25;
					break;
				} else if(mode == QuotationMode.SHORT && c_ema7 < c_ema25) {
					emaValue = c_ema25;
					break;
				}
			}
			
			FibCode[] codes = FibCode.values();
			int index = 0;
			for(index = 0; index < codes.length; index++) {
				FibCode code = codes[index];
				double fibPrice = this.fibInfo.getFibValue(code);
				if((mode == QuotationMode.LONG && fibPrice >= emaValue) 
						|| (mode == QuotationMode.SHORT && fibPrice <= emaValue)) {
					result = code;
					break;
				}
			}
			
			if(result == FibCode.FIB66) {
				result = FibCode.FIB618;
			} else if(result == FibCode.FIB0) {
				result = FibCode.FIB236;
			}
		}
		return result;
	}
	
	private void resetFibLevel() {
		if(this.fibInfo != null) {
			FibCode levelFibCode = getFibCode();
			FibLevel level = FibLevel.valueOf(levelFibCode);
			this.fibInfo = new FibInfo(this.fibInfo.getFibValue(FibCode.FIB1), this.fibInfo.getFibValue(FibCode.FIB0), this.fibInfo.getDecimalPoint(), level);
		}
	}

	public FibInfo getFibInfo_parent() {
		return fibInfo_parent;
	}
}
