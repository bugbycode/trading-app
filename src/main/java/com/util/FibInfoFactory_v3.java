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
public class FibInfoFactory_v3 {

	private List<Klines> list;
	
	private List<Klines> fibAfterKlines;
	
	private FibInfo fibInfo;
	
	private List<Klines> list_15m;//十五分钟级别k线 用于补充回撤之后的k线信息
	
	private Klines start = null;
	
	private Klines end = null;
	
	private List<Double> openPrices;
	
	public FibInfoFactory_v3(List<Klines> list, List<Klines> list_15m) {
		this.list = new ArrayList<Klines>();
		this.list_15m = new ArrayList<Klines>();
		this.openPrices = new ArrayList<Double>();
		this.fibAfterKlines = new ArrayList<Klines>();
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
		
		this.openPrices.clear();
		this.fibAfterKlines.clear();
		
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
			if(startAfterFlag == null) {
				startAfterFlag = start;
			}
			secondSubList = PriceUtil.subList(startAfterFlag, list);
			end = PriceUtil.getMinPriceKLine(secondSubList);
			this.fibInfo = new FibInfo(start.getHighPriceDoubleValue(), end.getLowPriceDoubleValue(), start.getDecimalNum(), FibLevel.LEVEL_1);
		} else if(ps == PositionSide.LONG) {
			start = PriceUtil.getMinPriceKLine(firstSubList);
			startAfterFlag = PriceUtil.getAfterKlines(start, firstSubList);
			if(startAfterFlag == null) {
				startAfterFlag = start;
			}
			secondSubList = PriceUtil.subList(startAfterFlag, list);
			end = PriceUtil.getMaxPriceKLine(secondSubList);
			this.fibInfo = new FibInfo(start.getLowPriceDoubleValue(), end.getHighPriceDoubleValue(), start.getDecimalNum(), FibLevel.LEVEL_1);
		}
		
		if(this.fibInfo == null) {
			return;
		}

		QuotationMode mode = this.fibInfo.getQuotationMode();
		
		/*
		Klines fibAfterFlag = PriceUtil.getAfterKlines(end, this.list_15m);
		if(fibAfterFlag != null) {
			this.fibAfterKlines = PriceUtil.subList(fibAfterFlag, this.list_15m);
			this.fibInfo.setFibAfterKlines(fibAfterKlines);
		}*/
		
		Klines fibAfterFlag = PriceUtil.getAfterKlines(end, this.list);
		if(fibAfterFlag != null) {
			this.fibAfterKlines.addAll(PriceUtil.subList(fibAfterFlag, this.list));
		}
		
		Klines fibEndFlag = null;
		
		if(!CollectionUtils.isEmpty(this.fibAfterKlines)) {
			for(int index = this.fibAfterKlines.size() - 1; index > 1; index--) {
				Klines current = this.fibAfterKlines.get(index);
				Klines parent = this.fibAfterKlines.get(index - 1);
				if((mode == QuotationMode.LONG && PriceUtil.isRise_v3(current, parent))
						|| (mode == QuotationMode.SHORT && PriceUtil.isFall_v3(current, parent))) {
					fibEndFlag = current;
					break;
				}
			}
			
			this.fibAfterKlines.clear();
			
			if(fibEndFlag != null) {
				List<Klines> openSubList = PriceUtil.subList(fibAfterFlag, fibEndFlag, this.list);
				if(!CollectionUtils.isEmpty(openSubList)) {
					if(mode == QuotationMode.LONG) {//高点做空
						Klines low = PriceUtil.getMinPriceKLine(openSubList);
						Klines lowBody = PriceUtil.getMinBodyLowPriceKLine(openSubList);
						addPrices(low.getLowPriceDoubleValue());
						addPrices(lowBody.getBodyLowPriceDoubleValue());
					} else {
						Klines high = PriceUtil.getMaxPriceKLine(openSubList);
						Klines highBody = PriceUtil.getMaxBodyHighPriceKLine(openSubList);
						addPrices(high.getHighPriceDoubleValue());
						addPrices(highBody.getBodyHighPriceDoubleValue());
					}
				}
				fibAfterFlag = PriceUtil.getAfterKlines(fibEndFlag, this.list_15m);
				if(fibAfterFlag != null) {
					this.fibAfterKlines.addAll(PriceUtil.subList(fibAfterFlag, this.list_15m));
					this.fibInfo.setFibAfterKlines(this.fibAfterKlines);
				}
			}
		}
		
		if(mode == QuotationMode.LONG) {
			this.openPrices.sort(new PriceComparator(SortType.DESC));
		} else {
			this.openPrices.sort(new PriceComparator(SortType.ASC));
		}
		
		this.resetFibLevel();
		
		if(!loadParent) {
			if((mode == QuotationMode.LONG && !isLong()) || (mode == QuotationMode.SHORT && !isShort())) {
				this.init(true);
			}
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
			return k.getEma25() > k.getEma99() && k.getEma99() > 0;
		}
	}
	
	private boolean verifyShort(Klines k, boolean loadParent) {
		if(loadParent) {
			return k.getEma7() > k.getEma25() && k.getEma25() > 0;
		} else {
			return k.getEma25() < k.getEma99() && k.getEma99() > 0;
		}
	}
	
	private boolean verifyHigh(Klines k) {
		return k.getEma7() > k.getEma25() && k.getEma25() > 0 && k.getMacd() > 0;
	}
	
	private boolean verifyLow(Klines k) {
		return k.getEma7() < k.getEma25() && k.getEma25() > 0 && k.getMacd() < 0;
	}
	
	public FibInfo getFibInfo() {
		return this.fibInfo;
	}
	
	public List<Klines> getFibAfterKlines() {
		return fibAfterKlines;
	}
	
	public boolean isLong() {
		boolean result = false;
		if(fibInfo != null) {
			double start_ema99 = start.getEma99();
			double end_ema99 = end.getEma99();
			if(fibInfo.getQuotationMode() == QuotationMode.LONG && start_ema99 <= end_ema99 && start_ema99 > 0 && end_ema99 > 0) {
				result = true;
			}
		}
		return result;
	}
	
	public boolean isShort() {
		boolean result = false;
		if(fibInfo != null) {
			double start_ema99 = start.getEma99();
			double end_ema99 = end.getEma99();
			if(fibInfo.getQuotationMode() == QuotationMode.SHORT && start_ema99 >= end_ema99 && start_ema99 > 0 && end_ema99 > 0) {
				result = true;
			}
		}
		return result;
	}
	
	private FibCode getFibCode() {
		FibCode result = FibCode.FIB1;
		if(this.fibInfo != null) {
			
			if(!CollectionUtils.isEmpty(openPrices)) {
				double price = openPrices.get(0);
				result = this.fibInfo.getFibCode(price);
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
			this.fibInfo.setFibAfterKlines(fibAfterKlines);
		}
	}
	
	public void addPrices(double price) {
		if(!PriceUtil.contains(openPrices, price)) {
			openPrices.add(price);
		}
	}

	public List<Double> getOpenPrices() {
		return openPrices;
	}
}