package com.bugbycode.factory.fibInfo.impl;

import java.util.ArrayList;
import java.util.List;

import org.springframework.util.CollectionUtils;

import com.bugbycode.factory.fibInfo.FibInfoFactory;
import com.bugbycode.module.FibCode;
import com.bugbycode.module.FibInfo;
import com.bugbycode.module.FibLevel;
import com.bugbycode.module.Klines;
import com.bugbycode.module.MarketSentiment;
import com.bugbycode.module.QuotationMode;
import com.bugbycode.module.SortType;
import com.bugbycode.module.price.OpenPrice;
import com.bugbycode.module.price.impl.OpenPriceDetails;
import com.bugbycode.module.trading.PositionSide;
import com.util.KlinesComparator;
import com.util.PriceComparator;
import com.util.PriceUtil;

/**
 * 斐波那契回指标撤接口实现类
 */
public class FibInfoFactoryImpl implements FibInfoFactory {

	private List<Klines> list;
	
	private List<Klines> fibAfterKlines;
	
	private FibInfo fibInfo;
	
	private List<Klines> list_15m;//十五分钟级别k线 用于补充回撤之后的k线信息
	
	private Klines start = null;
	
	private Klines end = null;
	
	private Klines firstPoint = null;//前高或前低
	
	private List<OpenPrice> openPrices;
	
	public FibInfoFactoryImpl(List<Klines> list, List<Klines> list_15m) {
		this.list = new ArrayList<Klines>();
		this.list_15m = new ArrayList<Klines>();
		this.openPrices = new ArrayList<OpenPrice>();
		this.fibAfterKlines = new ArrayList<Klines>();
		if(!CollectionUtils.isEmpty(list_15m)) {
			this.list_15m.addAll(list_15m);
		}
		if(!CollectionUtils.isEmpty(list)) {
			this.list.addAll(list);
			this.init();
		}
	}
	
	@Override
	public boolean isLong() {
		boolean result = false;
		if(fibInfo != null && fibInfo.getQuotationMode() == QuotationMode.LONG
				&& isAth()) {
			result = true;
		}
		return result;
	}
	
	@Override
	public boolean isShort() {
		boolean result = false;
		if(fibInfo != null && fibInfo.getQuotationMode() == QuotationMode.SHORT
				&& isAtl()) {
			result = true;
		}
		return result;
	}
	
	@Override
	public FibInfo getFibInfo() {
		return this.fibInfo;
	}

	@Override
	public List<Klines> getFibAfterKlines() {
		return fibAfterKlines;
	}

	@Override
	public List<OpenPrice> getOpenPrices() {
		return openPrices;
	}
	
	/**
	 * 是否出现新高
	 * @return
	 */
	private boolean isAth() {
		return fibInfo != null && fibInfo.getQuotationMode() == QuotationMode.LONG && end.getHighPriceDoubleValue() >= firstPoint.getHighPriceDoubleValue();
	}
	
	/**
	 * 是否出现新低
	 * @return
	 */
	private boolean isAtl() {
		return fibInfo != null && fibInfo.getQuotationMode() == QuotationMode.SHORT && end.getLowPriceDoubleValue() <= firstPoint.getLowPriceDoubleValue();
	}
	
	private void init() {
		if(CollectionUtils.isEmpty(list) || list.size() < 99 || CollectionUtils.isEmpty(list_15m)) {
			return;
		}
		
		KlinesComparator kc = new KlinesComparator(SortType.ASC);
		this.list.sort(kc);
		
		PriceUtil.calculateMACD(list);
		
		this.openPrices.clear();
		this.fibAfterKlines.clear();
		
		PositionSide ps = getPositionSide();
		
		Klines fifth = null;
		Klines fourth = null;
		Klines third = null;
		Klines second = null;
		Klines first = null;
		
		for(int index = list.size() - 1; index > 0; index--) {
			Klines current = list.get(index);
			if(ps == PositionSide.SHORT) {//low - high - low - high - low
				if(fifth == null) {
					if(verifyLow(current)) {
						fifth = current;
					}
				} else if(fourth == null) {
					if(verifyHigh(current)) {
						fourth = current;
					}
				} else if(third == null) {
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
			} else if(ps == PositionSide.LONG) { // high - low - high - low - high
				if(fifth == null) {
					if(verifyHigh(current)) {
						fifth = current;
					}
				} else if(fourth == null) {
					if(verifyLow(current)) {
						fourth = current;
					}
				} else if(third == null) {
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
		
		if(first == null || second == null || third == null || fourth == null || fifth == null) {
			return;
		}
		
		List<Klines> firstSubList = PriceUtil.subList(third, fourth, list);
		
		List<Klines> secondSubList = null;
		
		Klines startAfterFlag = null;
		if(ps == PositionSide.SHORT) {
			start = PriceUtil.getMaxPriceKLine(firstSubList);
			startAfterFlag = PriceUtil.getAfterKlines(start, firstSubList);
			if(startAfterFlag == null) {
				startAfterFlag = start;
			}
			secondSubList = PriceUtil.subList(startAfterFlag, fifth, list);
			end = PriceUtil.getMinPriceKLine(secondSubList);
			this.fibInfo = new FibInfo(start.getHighPriceDoubleValue(), end.getLowPriceDoubleValue(), start.getDecimalNum(), FibLevel.LEVEL_1);
		} else if(ps == PositionSide.LONG) {
			start = PriceUtil.getMinPriceKLine(firstSubList);
			startAfterFlag = PriceUtil.getAfterKlines(start, firstSubList);
			if(startAfterFlag == null) {
				startAfterFlag = start;
			}
			secondSubList = PriceUtil.subList(startAfterFlag, fifth, list);
			end = PriceUtil.getMaxPriceKLine(secondSubList);
			this.fibInfo = new FibInfo(start.getLowPriceDoubleValue(), end.getHighPriceDoubleValue(), start.getDecimalNum(), FibLevel.LEVEL_1);
		}
		
		if(this.fibInfo == null) {
			return;
		}

		QuotationMode mode = this.fibInfo.getQuotationMode();

		//寻找前高或前低
		List<Klines> firstPointsSubList = PriceUtil.subList(second, fourth, list);
		if(mode == QuotationMode.LONG) {//寻找前高
			firstPoint = PriceUtil.getMaxPriceKLine(firstPointsSubList);
		} else { // 寻找前低
			firstPoint = PriceUtil.getMinPriceKLine(firstPointsSubList);
		}
		
		List<Klines> fibSubList = PriceUtil.subList(start, end, list);
		MarketSentiment ms = null;
		for(int index = fibSubList.size() - 1; index > 1; index--) {
			Klines current = fibSubList.get(index);
			Klines parent = fibSubList.get(index - 1);
			if((mode == QuotationMode.LONG && PriceUtil.verifyPowerful_v8(current, parent))
					|| (mode == QuotationMode.SHORT && PriceUtil.verifyDecliningPrice_v8(current, parent))) {
				List<Klines> sub_list = PriceUtil.subList(current, fibSubList);
				ms = new MarketSentiment(sub_list);
				break;
			}
		}
		
		addPrices(new OpenPriceDetails(FibCode.FIB1, fibInfo.getFibValue(FibCode.FIB1)));
		
		if(isAth() || isAtl()) {
			FibCode[] codes = FibCode.values();
			for(FibCode code : codes) {
				if(code.gt(FibCode.FIB1)) {
					addPrices(new OpenPriceDetails(code, fibInfo.getFibValue(code)));
				}
			}
		}
		
		//开始处理开仓点位
		if(ms != null) {
			double price = -1;
			Klines openFlag = null;
			if(mode == QuotationMode.LONG) {
				openFlag = ms.getLow();
				price = openFlag.getLowPriceDoubleValue();
				addPrices(new OpenPriceDetails(this.fibInfo.getFibCode(price), price));
			} else {
				openFlag = ms.getHigh();
				price = openFlag.getHighPriceDoubleValue();
				addPrices(new OpenPriceDetails(this.fibInfo.getFibCode(price), price));
			}
		}
		
		if(mode == QuotationMode.LONG) {
			this.openPrices.sort(new PriceComparator(SortType.DESC));
		} else {
			this.openPrices.sort(new PriceComparator(SortType.ASC));
		}
		
		Klines fibAfterFlag = PriceUtil.getAfterKlines(end, this.list_15m);
		
		if(fibAfterFlag != null) {
			
			this.fibAfterKlines.addAll(PriceUtil.subList(fibAfterFlag, this.list_15m));
			this.fibInfo.setFibAfterKlines(fibAfterKlines);
		}
		
		
		this.resetFibLevel();
	}
	
	private PositionSide getPositionSide() {
		PositionSide ps = PositionSide.DEFAULT;
		Klines last = PriceUtil.getLastKlines(list);
		if(verifyShort(last)) {
			ps = PositionSide.SHORT;
		} else if(verifyLong(last)) {
			ps = PositionSide.LONG;
		}
		return ps;
	}
	
	private boolean verifyLong(Klines current) {
		return current.getMacd() < 0;
	}
	
	private boolean verifyShort(Klines current) {
		return current.getMacd() > 0;
	}
	
	private boolean verifyHigh(Klines k) {
		return k.getMacd() > 0;
	}
	
	private boolean verifyLow(Klines k) {
		return k.getMacd() < 0;
	}
	
	private FibCode getFibCode() {
		FibCode result = FibCode.FIB1;
		if(this.fibInfo != null) {
			
			if(!CollectionUtils.isEmpty(openPrices)) {
				double price = openPrices.get(0).getPrice();
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
	
	private void addPrices(OpenPrice price) {
		if(!PriceUtil.contains(openPrices, price)) {
			openPrices.add(price);
		}
	}
}
