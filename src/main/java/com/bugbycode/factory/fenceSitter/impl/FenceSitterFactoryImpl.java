package com.bugbycode.factory.fenceSitter.impl;

import java.util.ArrayList;
import java.util.List;

import org.springframework.util.CollectionUtils;

import com.bugbycode.factory.fenceSitter.FenceSitterFactory;
import com.bugbycode.module.AutoClosePosition;
import com.bugbycode.module.DualSidePositionStatus;
import com.bugbycode.module.FibCode;
import com.bugbycode.module.FibInfo;
import com.bugbycode.module.Klines;
import com.bugbycode.module.MarketSentiment;
import com.bugbycode.module.QuotationMode;
import com.bugbycode.module.SortType;
import com.bugbycode.module.TradeTrend;
import com.bugbycode.module.binance.AutoTrade;
import com.bugbycode.module.binance.AutoTradeType;
import com.bugbycode.module.price.OpenPrice;
import com.bugbycode.module.price.impl.OpenPriceDetails;
import com.bugbycode.module.trading.PositionSide;
import com.util.KlinesComparator;
import com.util.PriceUtil;

public class FenceSitterFactoryImpl implements FenceSitterFactory{

	private List<Klines> list;
	
	private List<Klines> list_trend;
	
	private List<Klines> fibAfterKlines;
	
	private FibInfo fibInfo;
	
	private FibInfo childFibInfo;
	
	private List<Klines> list_15m;//十五分钟级别k线 用于补充回撤之后的k线信息
	
	private Klines start = null;
	
	private Klines end = null;
	
	private OpenPrice openPrice;
	
	private AutoTrade autoTrade = AutoTrade.OPEN;
	
	private AutoClosePosition autoClosePosition = AutoClosePosition.OPEN;
	
	private DualSidePositionStatus dualSidePositionStatus = DualSidePositionStatus.CLOSE;
	
	private boolean resetStopLoss = false;
	
	private final TradeTrend tradeTrend;
	
	private PositionSide ps_mode = PositionSide.DEFAULT;
	
	public FenceSitterFactoryImpl(List<Klines> list_trend, List<Klines> list, List<Klines> list_15m, TradeTrend tradeTrend, PositionSide ps_mode) {
		this.list = new ArrayList<Klines>();
		this.list_trend = new ArrayList<Klines>();
		this.list_15m = new ArrayList<Klines>();
		this.fibAfterKlines = new ArrayList<Klines>();
		if(tradeTrend == null) {
			tradeTrend = TradeTrend.FOLLOW;
		}
		this.tradeTrend = tradeTrend;
		if(ps_mode != null) {
			this.ps_mode = ps_mode; 
		}
		if(!CollectionUtils.isEmpty(list_trend)) {
			this.list_trend.addAll(list_trend);
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
		if(list_trend.size() < 99 || list.size() < 99 || CollectionUtils.isEmpty(list_15m) || this.ps_mode == PositionSide.DEFAULT) {
			return;
		}
		
		KlinesComparator kc = new KlinesComparator(SortType.ASC);
		this.list.sort(kc);
		this.list_trend.sort(kc);
		this.list_15m.sort(kc);
		
		PriceUtil.calculateMACD(list);
		PriceUtil.calculateMACD(list_trend);
		
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
					if(verifyLow_end(current)) {
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
					if(verifyHigh_end(current)) {
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
			this.fibInfo = new FibInfo(start.getHighPriceDoubleValue(), end.getLowPriceDoubleValue(), start.getDecimalNum());
		} else if(ps == PositionSide.LONG) {
			start = PriceUtil.getMinPriceKLine(firstSubList);
			startAfterFlag = PriceUtil.getAfterKlines(start, firstSubList);
			if(startAfterFlag == null) {
				startAfterFlag = start;
			}
			secondSubList = PriceUtil.subList(startAfterFlag, third, list);
			end = PriceUtil.getMaxPriceKLine(secondSubList);
			this.fibInfo = new FibInfo(start.getLowPriceDoubleValue(), end.getHighPriceDoubleValue(), start.getDecimalNum());
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
			FibCode openCode = fibInfo.getFibCode_v2(openCodeValue);
			
			double openPriceValue = 0;
			Klines hit_k = PriceUtil.getLastKlines(list);
			
			for(int index = list.size() - 1; index > 0; index--) {
				Klines current = list.get(index);
				
				if(current.lte(end)) {
					break;
				}

				double hitPrice = mode == QuotationMode.LONG ? current.getHighPriceDoubleValue() : current.getLowPriceDoubleValue();
				if(openPriceValue == 0 || 
						((mode == QuotationMode.LONG && hitPrice < openPriceValue) || (mode == QuotationMode.SHORT && hitPrice > openPriceValue))) {
					openPriceValue = hitPrice;
					hit_k = current;
				}
			}
			
			if(openPriceValue == 0) {
				return;
			}
			
			childFibInfo = new FibInfo(fib0Value, openCodeValue, fibInfo.getDecimalPoint());
			
			FibCode childHitCode = childFibInfo.getFibCode(openPriceValue);
			
			if(childHitCode.gte(FibCode.FIB382)) {
				this.autoTrade = AutoTrade.CLOSE;
			}

			double stopLossLimit = mode == QuotationMode.LONG ? hit_k.getLowPriceDoubleValue() : hit_k.getHighPriceDoubleValue();
			
			if((mode == QuotationMode.LONG && ps_mode == PositionSide.SHORT)
					|| (mode == QuotationMode.SHORT && ps_mode == PositionSide.LONG)) {
				
				double lossLen = hit_k.getHighPriceDoubleValue() - hit_k.getLowPriceDoubleValue();
				
				stopLossLimit = ps_mode == PositionSide.LONG ? openPriceValue - lossLen : openPriceValue + lossLen;
				
				stopLossLimit = PriceUtil.formatDoubleDecimalValue(stopLossLimit, fibInfo.getDecimalPoint());
			}
			
			if(this.autoTrade == AutoTrade.OPEN) {
				addPrices(new OpenPriceDetails(openCode, openPriceValue, stopLossLimit, AutoTradeType.FENCE_SITTER));
			}
			
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
		return k.getDea() > 0;
	}
	
	private boolean verifyLow(Klines k) {
		return k.getDea() < 0;
	}
	
	private boolean verifyHigh_end(Klines k) {
		return k.getDea() > 0;
	}
	
	private boolean verifyLow_end(Klines k) {
		return k.getDea() < 0;
	}
	
	private void addPrices(OpenPrice price) {
		if(price.getCode().gte(FibCode.FIB236)) {
			price.setResetStopLoss(resetStopLoss);
			price.setAutoTrade(autoTrade);
			price.setAutoClosePosition(autoClosePosition);
			price.setChildFibInfo(childFibInfo);
			price.setDualSidePositionStatus(dualSidePositionStatus);
			openPrice = price;
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
	public OpenPrice getOpenPrice() {
		return openPrice;
	}

	@Override
	public boolean isLong() {
		boolean result = false;
		if(openPrice != null && ps_mode == PositionSide.LONG) {
			result = true;
		}
		return result;
	}
	
	@Override
	public boolean isShort() {
		boolean result = false;
		if(openPrice != null && ps_mode == PositionSide.SHORT) {
			result = true;
		}
		return result;
	}
	
}
