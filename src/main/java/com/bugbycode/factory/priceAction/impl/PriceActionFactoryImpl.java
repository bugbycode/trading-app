package com.bugbycode.factory.priceAction.impl;

import java.util.ArrayList;
import java.util.List;

import org.springframework.util.CollectionUtils;

import com.bugbycode.factory.priceAction.PriceActionFactory;
import com.bugbycode.module.FibCode;
import com.bugbycode.module.FibInfo;
import com.bugbycode.module.FibLevel;
import com.bugbycode.module.Klines;
import com.bugbycode.module.MarketSentiment;
import com.bugbycode.module.QuotationMode;
import com.bugbycode.module.SortType;
import com.bugbycode.module.TradeTrend;
import com.bugbycode.module.binance.AutoTradeType;
import com.bugbycode.module.price.OpenPrice;
import com.bugbycode.module.price.impl.OpenPriceDetails;
import com.bugbycode.module.trading.PositionSide;
import com.util.KlinesComparator;
import com.util.PriceUtil;

/**
 * 价格行为指标接口实现类
 */
public class PriceActionFactoryImpl implements PriceActionFactory{
	
	private List<Klines> list;
	
	private List<Klines> list_trend;
	
	List<Klines> list_hit;
	
	private List<Klines> fibAfterKlines;
	
	private FibInfo fibInfo;
	
	private List<Klines> list_15m;//十五分钟级别k线 用于补充回撤之后的k线信息
	
	private Klines start = null;
	
	private Klines end = null;
	
	private List<OpenPrice> openPrices;
	
	private TradeTrend tradeTrend = TradeTrend.FOLLOW;
	
	public PriceActionFactoryImpl(List<Klines> list_trend, List<Klines> list, List<Klines> list_hit, List<Klines> list_15m) {
		this.tradeTrend = TradeTrend.FOLLOW;
		this.list = new ArrayList<Klines>();
		this.list_trend = new ArrayList<Klines>();
		this.list_hit = new ArrayList<Klines>();
		this.list_15m = new ArrayList<Klines>();
		this.openPrices = new ArrayList<OpenPrice>();
		this.fibAfterKlines = new ArrayList<Klines>();
		if(!CollectionUtils.isEmpty(list_trend)) {
			this.list_trend.addAll(list_trend);
		}
		if(!CollectionUtils.isEmpty(list_hit)) {
			this.list_hit.addAll(list_hit);
		}
		if(!CollectionUtils.isEmpty(list_15m)) {
			this.list_15m.addAll(list_15m);
		}
		if(!CollectionUtils.isEmpty(list)) {
			this.list.addAll(list);
			this.init();
		}
	}
	
	public PriceActionFactoryImpl(List<Klines> list_trend, List<Klines> list, List<Klines> list_hit, List<Klines> list_15m,
			TradeTrend tradeTrend) {
		this.tradeTrend = tradeTrend;
		this.list = new ArrayList<Klines>();
		this.list_trend = new ArrayList<Klines>();
		this.list_hit = new ArrayList<Klines>();
		this.list_15m = new ArrayList<Klines>();
		this.openPrices = new ArrayList<OpenPrice>();
		this.fibAfterKlines = new ArrayList<Klines>();
		if(!CollectionUtils.isEmpty(list_trend)) {
			this.list_trend.addAll(list_trend);
		}
		if(!CollectionUtils.isEmpty(list_hit)) {
			this.list_hit.addAll(list_hit);
		}
		if(!CollectionUtils.isEmpty(list_15m)) {
			this.list_15m.addAll(list_15m);
		}
		if(!CollectionUtils.isEmpty(list)) {
			this.list.addAll(list);
			this.init();
		}
	}
	
	private void init() {
		if(list_trend.size() < 99 || list.size() < 99 || CollectionUtils.isEmpty(list_hit) || CollectionUtils.isEmpty(list_15m)) {
			return;
		}
		
		KlinesComparator kc = new KlinesComparator(SortType.ASC);
		this.list.sort(kc);
		this.list_trend.sort(kc);
		this.list_hit.sort(kc);
		this.list_15m.sort(kc);
		
		PriceUtil.calculateMACD(list);
		PriceUtil.calculateMACD(list_trend);
		
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
		}
		
		if(!CollectionUtils.isEmpty(fibAfterKlines)) {
			
			MarketSentiment ms = new MarketSentiment(fibAfterKlines);
			double openCodeValue = mode == QuotationMode.LONG ? ms.getLowPrice() : ms.getHighPrice();
			double fib0Value = fibInfo.getFibValue(FibCode.FIB0);
			FibCode openCode = fibInfo.getFibCode(openCodeValue);
			
			if(openCode == FibCode.FIB0) {
				return;
			}
			
			Klines fibAfterHit = PriceUtil.getAfterKlines(end, list_hit);
			if(fibAfterHit == null) {
				return;
			}
			
			List<Klines> data = new ArrayList<Klines>();
			for(int index = list_hit.size() - 1; index >= 0; index--) {
				Klines current = list_hit.get(index);
				if((mode == QuotationMode.LONG && current.isFall()) 
						|| (mode == QuotationMode.SHORT && current.isRise())) {
					data.add(current);
				}
				if(current.lte(fibAfterHit)) {
					break;
				}
			}
			
			ms = new MarketSentiment(data);
			
			if(ms.isEmpty()) {
				return;
			}
			
			Klines openKlines = mode == QuotationMode.LONG ? ms.getMinBodyHigh() : ms.getMaxBodyLow();
			
			//次级回撤 用来计算止盈点位
			FibInfo childFibInfo = new FibInfo(fib0Value, openCodeValue, fibInfo.getDecimalPoint(), FibLevel.LEVEL_1);
			
			double openPriceValue = openKlines.getOpenPriceDoubleValue();
			FibCode hitCode = childFibInfo.getFibCode(openPriceValue);
			
			FibCode takeProfitCode = FibCode.FIB618;
			
			if(tradeTrend == TradeTrend.FOLLOW) {
				if(hitCode.lte(FibCode.FIB236)) {// 0 ~ 0.236 - 0.618
					takeProfitCode = FibCode.FIB618;
				} else if(hitCode == FibCode.FIB382) {// 0.382 - 0.786
					takeProfitCode = FibCode.FIB786;
				} else if(hitCode == FibCode.FIB5) { //0.5 - 0.786
					takeProfitCode = FibCode.FIB786;
				}
			} else {
				if(hitCode == FibCode.FIB0) {// 0 - 0.382
					takeProfitCode = FibCode.FIB382;
				} else if(hitCode == FibCode.FIB236) {// 0.236 - 0.5
					takeProfitCode = FibCode.FIB5;
				} else if(hitCode == FibCode.FIB382) {// 0.382 - 0.618
					takeProfitCode = FibCode.FIB618;
				}
			}
			
			double firstTakeProfit = childFibInfo.getFibValue(takeProfitCode);
			double secondTakeProfit = childFibInfo.getFibValue(takeProfitCode);
			
			
			
			FibInfo stopLossFibInfo = new FibInfo(openPriceValue, secondTakeProfit, fibInfo.getDecimalPoint(), FibLevel.LEVEL_1);
			double stopLossValue = stopLossFibInfo.getFibValue(FibCode.FIB1_272);
			
			addPrices(new OpenPriceDetails(openCode, openPriceValue, stopLossValue, firstTakeProfit, secondTakeProfit, AutoTradeType.PRICE_ACTION, fibInfo));
			
			this.fibAfterKlines = new ArrayList<Klines>();
			
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
		if(tradeTrend == TradeTrend.FOLLOW) {
			return k.getDea() > 0;
		} else {
			return k.getDea() < 0;
		}
	}
	
	private boolean verifyShort(Klines k) {
		if(tradeTrend == TradeTrend.FOLLOW) {
			return k.getDea() < 0;
		} else {
			return k.getDea() > 0;
		}
	}
	
	private boolean verifyHigh(Klines k) {
		return k.getDea() > 0 && k.getMacd() > 0;
	}
	
	private boolean verifyLow(Klines k) {
		return k.getDea() < 0 && k.getMacd() < 0;
	}
	
	private void addPrices(OpenPrice price) {
		if(!PriceUtil.contains(openPrices, price) && price.getCode().gte(FibCode.FIB236)) {
			openPrices.add(price);
		}
	}
	
	@Override
	public FibInfo getFibInfo() {
		return fibInfo;
	}

	@Override
	public List<Klines> getFibAfterKlines() {
		return fibAfterKlines;
	}

	@Override
	public List<OpenPrice> getOpenPrices() {
		return openPrices;
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
	
}