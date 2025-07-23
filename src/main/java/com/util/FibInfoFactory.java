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
	
	private List<Klines> list_15m;//十五分钟级别k线 用于补充回撤之后的k线信息
	
	private Klines start = null;
	
	private Klines end = null;
	
	private List<Double> openPrices;
	
	public FibInfoFactory(List<Klines> list, List<Klines> list_15m) {
		this.list = new ArrayList<Klines>();
		this.list_15m = new ArrayList<Klines>();
		this.openPrices = new ArrayList<Double>();
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

		QuotationMode mode = this.fibInfo.getQuotationMode();
		
		this.resetFibLevel();
		
		Klines fibAfterFlag = PriceUtil.getAfterKlines(end, this.list_15m);
		if(fibAfterFlag != null) {
			this.fibAfterKlines = PriceUtil.subList(fibAfterFlag, this.list_15m);
			this.fibInfo.setFibAfterKlines(fibAfterKlines);
		}
		
		List<Klines> fibSubList = PriceUtil.subList(start, end, list);
		Klines gap_start = null;
		//Klines gap_end = null;
		//寻找缺口
		if(!CollectionUtils.isEmpty(fibSubList)) {
			for(int index = 0; index < fibSubList.size() - 1; index++) {
				Klines parent = fibSubList.get(index);
				Klines current = fibSubList.get(index + 1);
				if((mode == QuotationMode.LONG && PriceUtil.verifyPowerful_v8(current, parent)) 
						|| (mode == QuotationMode.SHORT && PriceUtil.verifyDecliningPrice_v8(current, parent))) {
					if((mode == QuotationMode.LONG && current.isFall()) || (mode == QuotationMode.SHORT && current.isRise())) {
						gap_start = parent;
					} else {
						gap_start = current;
					}
					break;
				}
			}
		}
		
		if(gap_start != null) {
			List<Klines> gapSubList = PriceUtil.subList(gap_start, fibSubList);
			if(!CollectionUtils.isEmpty(gapSubList)) {
				Klines openFlag = null;
				if(mode == QuotationMode.LONG) {
					openFlag = PriceUtil.getMinPriceKLine(gapSubList);
					addPrices(openFlag.getLowPriceDoubleValue());
				} else {
					openFlag = PriceUtil.getMaxPriceKLine(gapSubList);
					addPrices(openFlag.getHighPriceDoubleValue());
				}
			}
		}
		
		addPrices(this.fibInfo.getFibValue(FibCode.FIB1));
		
		if(mode == QuotationMode.LONG) {
			this.openPrices.sort(new PriceComparator(SortType.DESC));
		} else {
			this.openPrices.sort(new PriceComparator(SortType.ASC));
		}
		
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
			QuotationMode mode = this.fibInfo.getQuotationMode();
			Klines last = PriceUtil.getLastKlines(list);
			double emaValue = last.getEma99();
			List<Klines> fibSubList = PriceUtil.subList(start, end, list);
			for(int index = 0; index < fibSubList.size(); index++) {
				Klines current = fibSubList.get(index);
				double c_ema7 = current.getEma7();
				double c_ema25 = current.getEma25();
				if(mode == QuotationMode.LONG && c_ema7 > c_ema25) {
					if(c_ema25 < emaValue) {
						emaValue = c_ema25;
					}
					break;
				} else if(mode == QuotationMode.SHORT && c_ema7 < c_ema25) {
					if(c_ema25 > emaValue) {
						emaValue = c_ema25;
					}
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
			//this.fibInfo.setEndCode(getParentCode(level.getStartFibCode()));
			//this.fibInfo.setEndCode(level.getStartFibCode());
			Klines last = PriceUtil.getLastKlines(list);
			if((isLong() && ( last.getDif() < 0 || ( last.getOpenPriceDoubleValue() <= last.getEma99() && last.getClosePriceDoubleValue() <= last.getEma99() ) )) 
					
					|| (isShort() && ( last.getDif() > 0 || ( last.getOpenPriceDoubleValue() >= last.getEma99() && last.getClosePriceDoubleValue() >= last.getEma99() ) ) )) {
				
				//this.fibInfo.setEndCode(level.getStartFibCode());
				this.fibInfo.setEndCode(getParentCode(level.getStartFibCode()));
			
			}
			/*Klines last = PriceUtil.getLastKlines(list);
			if((isLong() && last.getOpenPriceDoubleValue() <= last.getEma99() && last.getClosePriceDoubleValue() <= last.getEma99()) 
					|| (isShort() && last.getOpenPriceDoubleValue() >= last.getEma99() && last.getClosePriceDoubleValue() >= last.getEma99())) {
				this.fibInfo.setEndCode(level.getStartFibCode());
			}*/
		}
	}
	
	public FibCode getParentCode(FibCode code) {
		FibCode[] codes = FibCode.values();
		FibCode result = FibCode.FIB4_618;
		for(int index = 0; index < codes.length; index++) {
			if(code == FibCode.FIB5) {
				result = FibCode.FIB66;
			} else if(code == FibCode.FIB618) {
				result = FibCode.FIB786;
			} else if(code == codes[index] && index > 0) {
				result = codes[index - 1];
			}
		}
		
		Klines last = PriceUtil.getLastKlines(list);
		if(last != null) {
			double ema25 = last.getEma25();
			double ema99 = last.getEma99();
			if( ( (isLong() && ema25 > ema99) || (isShort() && ema25 < ema99) ) 
					&& result.lt(FibCode.FIB1) ) {
				result = FibCode.FIB1_272;
			}
		}
		return result;
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