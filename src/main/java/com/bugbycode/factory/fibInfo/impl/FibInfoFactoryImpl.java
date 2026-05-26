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
import com.bugbycode.module.binance.AutoTradeType;
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
	
	private List<Klines> list_trend;
	
	private List<Klines> fibAfterKlines;
	
	private FibInfo fibInfo;
	
	private List<Klines> list_15m;//十五分钟级别k线 用于补充回撤之后的k线信息
	
	private Klines start = null;
	
	private Klines end = null;
	
	private List<OpenPrice> openPrices;
	
	/**
	 * 
	 * @param list 斐波那契回撤指标参考的K线信息
	 * @param list_trend 行情走势参考的K线信息
	 * @param list_15m 十五分钟级别k线信息
	 */
	public FibInfoFactoryImpl(List<Klines> list, List<Klines> list_trend, List<Klines> list_15m) {
		this.list = new ArrayList<Klines>();
		this.list_15m = new ArrayList<Klines>();
		this.list_trend = new ArrayList<Klines>();
		this.openPrices = new ArrayList<OpenPrice>();
		this.fibAfterKlines = new ArrayList<Klines>();
		if(!CollectionUtils.isEmpty(list_15m)) {
			this.list_15m.addAll(list_15m);
		}
		if(!CollectionUtils.isEmpty(list_trend)) {
			this.list_trend.addAll(list_trend);
		}
		if(!CollectionUtils.isEmpty(list)) {
			this.list.addAll(list);
			this.init();
		}
	}
	
	@Override
	public boolean isLong() {
		boolean result = false;
		if(fibInfo != null && fibInfo.getQuotationMode() == QuotationMode.LONG) {
			result = true;
		}
		return result;
	}
	
	@Override
	public boolean isShort() {
		boolean result = false;
		if(fibInfo != null && fibInfo.getQuotationMode() == QuotationMode.SHORT) {
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
	
	private void init() {
		if(CollectionUtils.isEmpty(list) || list.size() < 50 || list_trend.size() < 50 || CollectionUtils.isEmpty(list_15m)) {
			return;
		}
		
		KlinesComparator kc = new KlinesComparator(SortType.ASC);
		this.list.sort(kc);
		this.list_trend.sort(kc);
		this.list_15m.sort(kc);
		
		PriceUtil.calculateEMA_7_25_99(list);
		PriceUtil.calculateEMA_7_25_99(list_trend);
		
		this.openPrices = new ArrayList<OpenPrice>();
		this.fibAfterKlines = new ArrayList<Klines>();

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
		
		Klines startAfterFlag = null;
		if(ps == PositionSide.SHORT) {
			start = PriceUtil.getMaxPriceKLine(firstSubList);
			startAfterFlag = PriceUtil.getAfterKlines(start, firstSubList);
			if(startAfterFlag == null) {
				startAfterFlag = start;
			}
			secondSubList = PriceUtil.subList(startAfterFlag, third, list);
			end = PriceUtil.getMinPriceKLine(secondSubList);
			this.fibInfo = new FibInfo(start.getHighPriceDoubleValue(), end.getLowPriceDoubleValue(), start.getDecimalNum(), FibLevel.LEVEL_0);
		} else if(ps == PositionSide.LONG) {
			start = PriceUtil.getMinPriceKLine(firstSubList);
			startAfterFlag = PriceUtil.getAfterKlines(start, firstSubList);
			if(startAfterFlag == null) {
				startAfterFlag = start;
			}
			secondSubList = PriceUtil.subList(startAfterFlag, third, list);
			end = PriceUtil.getMaxPriceKLine(secondSubList);
			this.fibInfo = new FibInfo(start.getLowPriceDoubleValue(), end.getHighPriceDoubleValue(), start.getDecimalNum(), FibLevel.LEVEL_0);
		}
		
		if(this.fibInfo == null) {
			return;
		}
		
		QuotationMode mode = this.fibInfo.getQuotationMode();
		
		Klines fibAfterKline = PriceUtil.getAfterKlines(end, this.list_15m);
		if(fibAfterKline != null) {
			this.fibAfterKlines = PriceUtil.subList(fibAfterKline, this.list_15m);
			//this.fibInfo.setFibAfterKlines(fibAfterKlines);
		}
		
		if(!CollectionUtils.isEmpty(fibAfterKlines)) {
			
			MarketSentiment ms = new MarketSentiment(fibAfterKlines);
			double openCodeValue = mode == QuotationMode.LONG ? ms.getLowPrice() : ms.getHighPrice();
			double fib0Value = fibInfo.getFibValue(FibCode.FIB0);
			FibCode openCode = fibInfo.getFibCode_v2(openCodeValue);
			
			if(openCode == FibCode.FIB0) {
				return;
			}
			
			Klines fibAfterHit = PriceUtil.getAfterKlines(end, list);
			if((mode == QuotationMode.LONG && end.isFall()) || (mode == QuotationMode.SHORT && end.isRise())) {
				fibAfterHit = end;
			}
			
			if(fibAfterHit == null) {
				return;
			}
			
			List<Klines> data = PriceUtil.subList(fibAfterHit, list);
			
			ms = new MarketSentiment(data);
			
			if(ms.isEmpty()) {
				return;
			}
			
			Klines openKlines = mode == QuotationMode.LONG ? ms.getMinBodyLow() : ms.getMaxBodyHigh();
			double hitValue = mode == QuotationMode.LONG ? ms.getMinBodyLowPrice() : ms.getMaxBodyHighPrice();
			
			Klines openKlinesAfter = PriceUtil.getAfterKlines(openKlines, list_15m);
			
			if(openKlinesAfter == null) {
				return;
			}
			
			Klines hit_current = null;
			
			for(int index = list_15m.size() - 1; index > 0; index--) {
				Klines current = list_15m.get(index);
				if((mode == QuotationMode.LONG && PriceUtil.isBreachLong(current, hitValue)) 
						|| (mode == QuotationMode.SHORT && PriceUtil.isBreachShort(current, hitValue))) {
					
					hit_current = current;
					
					break;
				}
				if(current.lte(openKlinesAfter)) {
					break;
				}
			}
			
			if(hit_current == null) {
				if((mode == QuotationMode.LONG && openKlinesAfter.isRise()) || (mode == QuotationMode.SHORT && openKlinesAfter.isFall())) {
					hit_current = openKlinesAfter;
				}
			}
			
			if(hit_current != null) {
				double openPriceValue = mode == QuotationMode.LONG ? hit_current.getBodyHighPriceDoubleValue() : hit_current.getBodyLowPriceDoubleValue();
				
				//次级回撤 用来计算止盈点位
				FibInfo childFibInfo = new FibInfo(fib0Value, openCodeValue, fibInfo.getDecimalPoint());
				
				
				FibCode hitCode = childFibInfo.getFibCode(hitValue);
				
				FibCode takeProfitCode = FibCode.FIB5;
				
				if(hitCode == FibCode.FIB0) {// 0 - 0.5
					takeProfitCode = FibCode.FIB5;
				} else if(hitCode == FibCode.FIB236) { // 0.236 - 0.618
					takeProfitCode = FibCode.FIB618;
				} else if(hitCode == FibCode.FIB382) {// 0.382 - 0.618
					takeProfitCode = FibCode.FIB618;
				} else if(hitCode == FibCode.FIB5) { //0.5 - 0.786
					takeProfitCode = FibCode.FIB786;
				}
				
				double firstTakeProfit = childFibInfo.getFibValue(takeProfitCode);
				double secondTakeProfit = childFibInfo.getFibValue(takeProfitCode);
				
				
				
				FibInfo stopLossFibInfo = new FibInfo(hitValue, secondTakeProfit, fibInfo.getDecimalPoint());
				double stopLossValue = stopLossFibInfo.getFibValue(FibCode.FIB1_272);
				
				addPrices(new OpenPriceDetails(openCode, openPriceValue, stopLossValue, firstTakeProfit, secondTakeProfit, AutoTradeType.FIB_RET, fibInfo));
				
			}
			
			this.fibAfterKlines = new ArrayList<Klines>();
		}
		
		if(mode == QuotationMode.LONG) {
			this.openPrices.sort(new PriceComparator(SortType.DESC));
		} else {
			this.openPrices.sort(new PriceComparator(SortType.ASC));
		}
		
	}
	
	private PositionSide getPositionSide() {
		PositionSide ps = PositionSide.DEFAULT;
		Klines last = PriceUtil.getLastKlines(list_trend);
		
		if(verifyShort(last)) {
			ps = PositionSide.SHORT;
		} else if(verifyLong(last)) {
			ps = PositionSide.LONG;
		}
		
		return ps;
	}
	
	private boolean verifyLong(Klines k) {
		return k.getEma7() < k.getEma25() && k.getEma25() > 0; 
	}
	
	private boolean verifyShort(Klines k) {
		return k.getEma7() > k.getEma25() && k.getEma25() > 0; 
	}
	
	private boolean verifyHigh(Klines k) {
		return k.getEma7() > k.getEma25() && k.getEma25() > 0;
	}
	
	private boolean verifyLow(Klines k) {
		return k.getEma7() < k.getEma25() && k.getEma25() > 0;
	}
	
	private void addPrices(OpenPrice price) {
		if(!PriceUtil.contains(openPrices, price) && price.getCode().gte(FibCode.FIB236)) {
			openPrices.add(price);
		}
	}
	
	public Klines getStart() {
		return start;
	}

	public Klines getEnd() {
		return end;
	}
	
	@Override
	public FibCode getHitCode() {
		return null;
	}
}