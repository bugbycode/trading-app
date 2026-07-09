package com.bugbycode.factory.area.impl;

import java.util.ArrayList;
import java.util.List;

import org.springframework.util.CollectionUtils;

import com.bugbycode.factory.area.AreaFactory;
import com.bugbycode.module.FibCode;
import com.bugbycode.module.FibInfo;
import com.bugbycode.module.Klines;
import com.bugbycode.module.SortType;
import com.bugbycode.module.binance.AutoTradeType;
import com.bugbycode.module.price.OpenPrice;
import com.bugbycode.module.price.impl.OpenPriceDetails;
import com.bugbycode.module.trading.PositionSide;
import com.util.KlinesComparator;
import com.util.PriceUtil;

/**
 * 盘整区交易
 */
public class AreaFactoryImpl implements AreaFactory {

	private List<Klines> list;
	
	private List<Klines> list_15m;
	
	private List<Klines> fibAfterKlines;
	
	private PositionSide ps;
	
	private List<OpenPrice> openPrices;
	
	private List<FibInfo> fibInfoList;
	
	public AreaFactoryImpl(List<Klines> list, List<Klines> list_15m) {
		this.ps = PositionSide.DEFAULT;
		this.list = new ArrayList<Klines>();
		this.list_15m = new ArrayList<Klines>();
		this.fibAfterKlines = new ArrayList<Klines>();
		this.openPrices = new ArrayList<OpenPrice>();
		this.fibInfoList = new ArrayList<FibInfo>();
		if(!CollectionUtils.isEmpty(list)) {
			this.list.addAll(list);
		}
		if(!CollectionUtils.isEmpty(list_15m)) {
			this.list_15m.addAll(list_15m);
		}
		
		this.init();
	}
	
	private void init() {
		
		if(CollectionUtils.isEmpty(list) || CollectionUtils.isEmpty(this.list_15m)) {
			return;
		}

		this.list.sort(new KlinesComparator(SortType.ASC));
		this.list_15m.sort(new KlinesComparator(SortType.ASC));
		
		PriceUtil.calculateMACD(list);
		
		Klines third = null;
		Klines second = null;
		Klines first = null;
		
		int reset_index = 0;
		int start_index = list.size() - 1;
		
		for(int index = start_index; index > 0; index--) {
			Klines current = list.get(index);
			if(this.ps == PositionSide.DEFAULT) {
				this.ps = getPositionSide(current);
			}
			if(this.ps == PositionSide.DEFAULT) {
				return;
			}
			
			if(ps == PositionSide.SHORT) {//low - high - low
				if(third == null) {
					if(verifyLow(current)) {
						third = current;
					}
				} else if(second == null) {
					if(verifyHigh(current)) {
						second = current;
						reset_index = index;
					}
				} else if(first == null) {
					if(verifyLow(current)) {
						first = current;
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
						reset_index = index;
					}
				} else if(first == null) {
					if(verifyHigh(current)) {
						first = current;
					}
				}
			}
			
			if(first == null || second == null || third == null) {
				continue;
			}
			
			List<Klines> firstSubList = PriceUtil.subList(first, second, list);
			
			List<Klines> secondSubList = null;
			Klines startAfterFlag = null;
			Klines start = null;
			Klines end = null;
			FibInfo fibInfo = null;
			
			if(ps == PositionSide.SHORT) {
				start = PriceUtil.getMaxPriceKLine(firstSubList);
				startAfterFlag = PriceUtil.getAfterKlines(start, firstSubList);
				if(startAfterFlag == null) {
					startAfterFlag = start;
				}
				secondSubList = PriceUtil.subList(startAfterFlag, third, list);
				end = PriceUtil.getMinPriceKLine(secondSubList);
				fibInfo = new FibInfo(start.getHighPriceDoubleValue(), end.getLowPriceDoubleValue(), start.getDecimalNum());
			} else if(ps == PositionSide.LONG) {
				start = PriceUtil.getMinPriceKLine(firstSubList);
				startAfterFlag = PriceUtil.getAfterKlines(start, firstSubList);
				if(startAfterFlag == null) {
					startAfterFlag = start;
				}
				secondSubList = PriceUtil.subList(startAfterFlag, third, list);
				end = PriceUtil.getMaxPriceKLine(secondSubList);
				fibInfo = new FibInfo(start.getLowPriceDoubleValue(), end.getHighPriceDoubleValue(), start.getDecimalNum());
			}
			
			Klines fibAfterKline = PriceUtil.getAfterKlines(end, this.list_15m);
			if(fibAfterKline != null) {
				fibInfo.setFibAfterKlines(PriceUtil.subList(fibAfterKline, this.list_15m));
			}
			
			index = reset_index;//重置循环起始索引
			this.ps = PositionSide.DEFAULT;
			
			fibInfoList.add(fibInfo);
			
			third = null;
			second = null;
			first = null;
			
			if(fibInfoList.size() == 3) {
				break;
			}
		}
		
		//开始获取开仓点
		for(FibInfo fibInfo : fibInfoList) {
			double fib1Value = fibInfo.getFibValue(FibCode.FIB1);
			FibCode takeProfitCode = FibCode.FIB5;
			List<Klines> fibAfterList = fibInfo.getFibAfterKlines();
			
			if(CollectionUtils.isEmpty(fibAfterList)) {
				continue;
			}
			
			Klines hitKlines = null;
			for(int index = fibAfterList.size() - 1; index >= 0; index--) {
				Klines current = fibAfterList.get(index);
				if(PriceUtil.isBreachLong(current, fib1Value)) {
					hitKlines = current;
					this.ps = PositionSide.LONG;
					break;
				} else if(PriceUtil.isBreachShort(current, fib1Value)) {
					hitKlines = current;
					this.ps = PositionSide.SHORT;
					break;
				}
			}
			
			if(hitKlines == null) {
				continue;
			}
			
			if(!((fibInfo.isLong() && isLong()) || (fibInfo.isShort() && isShort()))) {
				takeProfitCode = FibCode.FIB1_618;
			}
			
			double hitPrice = isLong() ? hitKlines.getBodyHighPriceDoubleValue() : hitKlines.getBodyLowPriceDoubleValue();
			double firstTakeProfit = fibInfo.getFibValue(takeProfitCode);
			double secondTakeProfit = fibInfo.getFibValue(takeProfitCode);
			double stopLossLimit = isLong() ? hitKlines.getLowPriceDoubleValue() : hitKlines.getHighPriceDoubleValue();
			
			FibInfo stopLossFibInfo = new FibInfo(fib1Value, firstTakeProfit, fibInfo.getDecimalPoint());
			double stopLoss1_272 = stopLossFibInfo.getFibValue(FibCode.FIB1_272);
			
			stopLossLimit = isLong() ? PriceUtil.getMaxPrice(stopLossLimit, stopLoss1_272) : PriceUtil.getMinPrice(stopLossLimit, stopLoss1_272);
			
			addPrices(new OpenPriceDetails(FibCode.FIB1, hitPrice, stopLossLimit, firstTakeProfit, secondTakeProfit, AutoTradeType.AREA_INDEX, fibInfo));
			
			this.fibAfterKlines = fibInfo.getFibAfterKlines();
			
			break;
		}
	}
	
	private void addPrices(OpenPrice price) {
		if(!PriceUtil.contains(openPrices, price) && price.getCode().gte(FibCode.FIB236)) {
			openPrices.add(price);
		}
	}
	
	@Override
	public List<OpenPrice> getOpenPrices() {
		return this.openPrices;
	}

	@Override
	public List<Klines> getFibAfterKlines() {
		return this.fibAfterKlines;
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
	public List<FibInfo> getFibInfoList() {
		return this.fibInfoList;
	}

	private PositionSide getPositionSide(Klines last) {
		PositionSide ps = PositionSide.DEFAULT;
		if(verifyShort(last)) {
			ps = PositionSide.SHORT;
		} else if(verifyLong(last)) {
			ps = PositionSide.LONG;
		}
		
		return ps;
	}
	
	private boolean verifyLong(Klines k) {
		return k.getMacd() > 0; 
	}
	
	private boolean verifyShort(Klines k) {
		return  k.getMacd() < 0;
	}
	
	private boolean verifyHigh(Klines k) {
		return k.getMacd() > 0;
	}
	
	private boolean verifyLow(Klines k) {
		return k.getMacd() < 0;
	}
}