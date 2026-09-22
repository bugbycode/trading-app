package com.bugbycode.factory.eoption.impl;

import java.util.ArrayList;
import java.util.List;

import org.springframework.util.CollectionUtils;

import com.bugbycode.factory.eoption.EoptionFactory;
import com.bugbycode.module.AutoClosePosition;
import com.bugbycode.module.DualSidePositionStatus;
import com.bugbycode.module.FibCode;
import com.bugbycode.module.FibInfo;
import com.bugbycode.module.Klines;
import com.bugbycode.module.MarketSentiment;
import com.bugbycode.module.QuotationMode;
import com.bugbycode.module.SortType;
import com.bugbycode.module.binance.AutoTrade;
import com.bugbycode.module.binance.AutoTradeType;
import com.bugbycode.module.price.OpenPrice;
import com.bugbycode.module.price.impl.OpenPriceDetails;
import com.bugbycode.module.trading.PositionSide;
import com.util.KlinesComparator;
import com.util.PriceUtil;

/**
 * 期权交易
 */
public class EoptionFactoryImpl implements EoptionFactory {

	private List<Klines> list;
	
	private List<Klines> fibAfterKlines;
	
	private FibInfo fibInfo;
	
	private List<Klines> list_15m;//十五分钟级别k线 用于补充回撤之后的k线信息
	
	private Klines start = null;
	
	private Klines end = null;
	
	private List<OpenPrice> openPrices;
	
	private AutoTrade autoTrade = AutoTrade.OPEN;
	
	private AutoClosePosition autoClosePosition = AutoClosePosition.CLOSE;
	
	private DualSidePositionStatus dualSidePositionStatus = DualSidePositionStatus.CLOSE;
	
	public EoptionFactoryImpl(List<Klines> list, List<Klines> list_15m) {
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
	
	private void init() {
		if(list.size() < 99 || CollectionUtils.isEmpty(list_15m)) {
			return;
		}
		
		KlinesComparator kc = new KlinesComparator(SortType.ASC);
		this.list.sort(kc);
		this.list_15m.sort(kc);
		
		PriceUtil.calculateMACD(list);
		PriceUtil.calculateMACD(list_15m);
		
		PositionSide ps = getPositionSide();
		
		this.openPrices = new ArrayList<OpenPrice>();
		this.fibAfterKlines = new ArrayList<Klines>();
		
		Klines fourth = null;
		Klines third = null;
		Klines second = null;
		Klines first = null;
		
		for(int index = list.size() - 1; index > 0; index--) {
			Klines current = list.get(index);
			if(ps == PositionSide.SHORT) {//high - low - high - low
				if(fourth == null) {
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
			} else if(ps == PositionSide.LONG) { // low - high - low - high
				if(fourth == null) {
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
		
		if(first == null || second == null || third == null || fourth == null) {
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
			
			double openPriceValue = 0;
			
			for(int index = list.size() - 1; index > 0; index--) {
				Klines current = list.get(index);

				if((isLong() && current.getDea() <= 0) || (isShort() && current.getDea() >= 0)) {
					break;
				}
				
				double hitPrice = isLong() ? current.getHighPriceDoubleValue() : current.getLowPriceDoubleValue();
				if(openPriceValue == 0 || 
						((isLong() && hitPrice < openPriceValue) || (isShort() && hitPrice > openPriceValue))) {
					openPriceValue = hitPrice;
				}
			}
			
			for(int index = list.size() - 1; index > 0; index--) {
				Klines current = list.get(index);
				Klines parent = list.get(index - 1);

				if((isLong() && current.getDea() <= 0) || (isShort() && current.getDea() >= 0)) {
					break;
				}

				if((isLong() && PriceUtil.verifyPowerful_v28(current, parent)) || 
						(isShort() && PriceUtil.verifyDeclining_v28(current, parent))) {
					double closePrice = current.getClosePriceDoubleValue();
					if(openPriceValue == 0 || (isLong() && openPriceValue > closePrice)
							|| (isShort() && openPriceValue < closePrice)) {
						openPriceValue = closePrice;
					}
				}
			}
			
			for(int index = list.size() - 1; index > 0; index--) {
				Klines current = list.get(index);
				Klines parent = list.get(index - 1);

				if((isLong() && current.getDea() <= 0) || (isShort() && current.getDea() >= 0)) {
					break;
				}
				
				if((isLong() && PriceUtil.verifyPowerful_v33(current, parent)) || 
						(isShort() && PriceUtil.verifyDeclining_v33(current, parent))) {
					double closePrice = current.getClosePriceDoubleValue();
					if(openPriceValue == 0 || (isLong() && openPriceValue > closePrice)
							|| (isShort() && openPriceValue < closePrice)) {
						openPriceValue = closePrice;
					}
				}
			}
			
			if(openPriceValue == 0) {
				return;
			}
			
			FibInfo childFibInfo = new FibInfo(fib0Value, openCodeValue, fibInfo.getDecimalPoint());
			
			FibCode takeProfitCode = FibCode.FIB618;
			
			double takeProfitCodeValue = childFibInfo.getFibValue(takeProfitCode);
			
			FibInfo stopLossFibInfo = new FibInfo(openPriceValue, takeProfitCodeValue, fibInfo.getDecimalPoint());
			double stopLossLimit = stopLossFibInfo.getFibValue(FibCode.FIB1_272);
			
			addPrices(new OpenPriceDetails(openCode, openPriceValue, stopLossLimit, takeProfitCodeValue, takeProfitCodeValue, AutoTradeType.FIB_RET, fibInfo));
			
			this.fibAfterKlines = new ArrayList<Klines>();

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
	
	private void addPrices(OpenPrice price) {
		if(!PriceUtil.contains(openPrices, price) && price.getCode().gte(FibCode.FIB236)) {
			price.setAutoTrade(autoTrade);
			price.setAutoClosePosition(autoClosePosition);
			price.setDualSidePositionStatus(dualSidePositionStatus);
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