package com.bugbycode.factory.fenceSitter.impl;

import java.util.ArrayList;
import java.util.List;

import org.springframework.util.CollectionUtils;

import com.bugbycode.factory.fenceSitter.FenceSitterFactory;
import com.bugbycode.module.FibCode;
import com.bugbycode.module.FibInfo;
import com.bugbycode.module.FibLevel;
import com.bugbycode.module.Klines;
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
	
	private FibInfo fibInfo;
	
	private List<Klines> list_15m;//十五分钟级别k线 用于补充回撤之后的k线信息
	
	private Klines start = null;
	
	private Klines end = null;
	
	private OpenPrice openPrice;
	
	private TradeTrend tradeTrend = TradeTrend.FOLLOW;
	
	private AutoTrade autoTrade = AutoTrade.OPEN;
	
	private PositionSide ps = PositionSide.DEFAULT;
	
	public FenceSitterFactoryImpl(List<Klines> list_trend, List<Klines> list, List<Klines> list_15m) {
		this.list = new ArrayList<Klines>();
		this.list_trend = new ArrayList<Klines>();
		this.list_15m = new ArrayList<Klines>();
		if(!CollectionUtils.isEmpty(list_trend)) {
			this.list_trend.addAll(list_trend);
		}
		if(!CollectionUtils.isEmpty(list_15m)) {
			this.list_15m.addAll(list_15m);
		}
		if(!CollectionUtils.isEmpty(list)) {
			this.list.addAll(list);
			this.init(tradeTrend);
		}
	}
	
	private void init(TradeTrend tradeTrend) {
		if(list_trend.size() < 99 || list.size() < 99 || CollectionUtils.isEmpty(list_15m)) {
			return;
		}
		
		KlinesComparator kc = new KlinesComparator(SortType.ASC);
		this.list.sort(kc);
		this.list_trend.sort(kc);
		this.list_15m.sort(kc);
		
		PriceUtil.calculateMACD(list);
		PriceUtil.calculateMACD(list_trend);
		PriceUtil.calculateAllBBPercentB(list);
		
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
		
		Klines last = PriceUtil.getLastKlines(list);
		double openPriceValue = mode == QuotationMode.LONG ? last.getLowPriceDoubleValue() : last.getHighPriceDoubleValue();
		
		for(int index = list.size() - 1; index > 0; index--) {
			Klines current = list.get(index);
			double hitPrice = mode == QuotationMode.LONG ? current.getLowPriceDoubleValue() : current.getHighPriceDoubleValue();
			if(current.lte(start)) {
				break;
			}
			if((mode == QuotationMode.LONG && openPriceValue < hitPrice)
					|| (mode == QuotationMode.SHORT && openPriceValue > hitPrice)) {
				openPriceValue = hitPrice;
			}
		}
		
		Klines last_15m = PriceUtil.getLastKlines(list_15m);
		double last_15m_close = last_15m.getClosePriceDoubleValue();
		if((mode == QuotationMode.LONG && last_15m_close >= openPriceValue)
				|| (mode == QuotationMode.SHORT && last_15m_close > openPriceValue)) {
			this.ps = PositionSide.LONG;
		} else if((mode == QuotationMode.LONG && last_15m_close < openPriceValue)
				|| (mode == QuotationMode.SHORT && last_15m_close <= openPriceValue)) {
			this.ps = PositionSide.SHORT;
		}
		
		FibCode takeProfitCode = FibCode.FIB0;
		FibCode hitCode = fibInfo.getFibCode(openPriceValue);
		if(!((mode == QuotationMode.LONG && isLong()) 
				|| (mode == QuotationMode.SHORT && isShort()))) {
			takeProfitCode = getTakeProfitCode(hitCode);
		}
		
		double takeProfitValue = fibInfo.getFibValue(takeProfitCode);
		
		FibInfo stopLossFibInfo = new FibInfo(openPriceValue, takeProfitValue, last.getDecimalNum());
		double stopLossLimit = stopLossFibInfo.getFibValue(FibCode.FIB1_272);
		
		FibCode openCode = fibInfo.getFibCode_v2(openPriceValue);
		
		addPrices(new OpenPriceDetails(openCode, openPriceValue, stopLossLimit, takeProfitValue, takeProfitValue, AutoTradeType.FENCE_SITTER));
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
		return k.getDea() > 0;
	}
	
	private boolean verifyShort(Klines k) {
		return k.getDea() < 0;
	}
	
	private boolean verifyHigh(Klines k) {
		return k.getMacd() > 0 && k.getDea() > 0;
	}
	
	private boolean verifyLow(Klines k) {
		return k.getMacd() < 0 && k.getDea() < 0;
	}

	@Override
	public OpenPrice getOpenPrice() {
		return this.openPrice;
	}

	@Override
	public boolean isLong() {
		return this.ps == PositionSide.LONG;
	}
	
	@Override
	public boolean isShort() {
		return this.ps == PositionSide.SHORT;
	}

	@Override
	public boolean isClosePosition() {
		return false;
	}
	
	private void addPrices(OpenPrice price) {
		price.setAutoTrade(autoTrade);
		this.openPrice = price;
	}
	
	private FibCode getTakeProfitCode(FibCode hitCode) {
		FibCode[] codes = FibCode.values();
		int len = codes.length;
		FibCode result = codes[0];
		for(int index = 0; index < len; index++) {
			FibCode code = codes[index];
			if(code == hitCode) {
				int offset = index - 2;
				if(offset < 0) {
					offset = 0;
				}
				result = codes[offset];
			}
		}
		return result;
	}
}
