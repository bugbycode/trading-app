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
import com.bugbycode.module.PriceActionInfo_v2;
import com.bugbycode.module.PriceActionType_v2;
import com.bugbycode.module.QuotationMode;
import com.bugbycode.module.SortType;
import com.bugbycode.module.price.OpenPrice;
import com.bugbycode.module.price.impl.OpenPriceDetails;
import com.bugbycode.module.trading.PositionSide;
import com.util.KlinesComparator;
import com.util.PriceComparator;
import com.util.PriceUtil;

/**
 * 价格行为指标接口实现类
 */
public class PriceActionFactoryImpl_v3 implements PriceActionFactory{
	
	private List<Klines> list;
	
	private List<Klines> fibAfterKlines;
	
	private FibInfo fibInfo;
	
	private List<Klines> list_15m;//十五分钟级别k线 用于补充回撤之后的k线信息
	
	private Klines start = null;
	
	private Klines end = null;
	
	private List<OpenPrice> openPrices;
	
	public PriceActionFactoryImpl_v3(List<Klines> list, List<Klines> list_15m) {
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
		return fibInfo != null && fibInfo.getQuotationMode() == QuotationMode.SHORT;
	}

	@Override
	public boolean isShort() {
		return fibInfo != null && fibInfo.getQuotationMode() == QuotationMode.LONG;
	}

	private void init() {
		if(CollectionUtils.isEmpty(list) || list.size() < 50 || CollectionUtils.isEmpty(list_15m)) {
			return;
		}
		
		KlinesComparator kc = new KlinesComparator(SortType.ASC);
		this.list.sort(kc);
		this.list_15m.sort(kc);
		
		//PriceUtil.calculateBollingerBands(list);
		//PriceUtil.calculateEMA_7_25_99(list);
		PriceUtil.calculateMACD(list);
		//PriceUtil.calculateDeltaAndCvd(list);
		
		this.openPrices.clear();
		this.fibAfterKlines.clear();
		
		PositionSide ps = getPositionSide();
		if(ps == PositionSide.DEFAULT) {
			return;
		}
		
		Klines third = null;
		Klines second = null;
		Klines first = null;
		
		for(int index = list.size() - 1; index > 0; index--) {
			Klines current = list.get(index);
			if(ps == PositionSide.LONG) {// low - high - low
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
			} else if(ps == PositionSide.SHORT) { // high - low - high
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
		Klines startAfter = null;
		if(ps == PositionSide.LONG) {
			start = PriceUtil.getMaxPriceKLine(firstSubList);
			startAfter = PriceUtil.getAfterKlines(start, list);
			if(startAfter != null) {
				secondSubList = PriceUtil.subList(startAfter, third, list);
				end = PriceUtil.getMinPriceKLine(secondSubList);
				if(end != null) {
					fibInfo = new FibInfo(start.getHighPriceDoubleValue(), end.getLowPriceDoubleValue(), start.getDecimalNum(), FibLevel.LEVEL_1);
				}
			}
		} else if(ps == PositionSide.SHORT) {
			start = PriceUtil.getMinPriceKLine(firstSubList);
			startAfter = PriceUtil.getAfterKlines(start, list);
			if(startAfter != null) {
				secondSubList = PriceUtil.subList(startAfter, third, list);
				end = PriceUtil.getMaxPriceKLine(secondSubList);
				if(end != null) {
					fibInfo = new FibInfo(start.getLowPriceDoubleValue(), end.getHighPriceDoubleValue(), start.getDecimalNum(), FibLevel.LEVEL_1);
				}
			}
		}
		
		if(fibInfo == null) {
			return;
		}

		QuotationMode mode = this.fibInfo.getQuotationMode();
		List<PriceActionInfo_v2> priceInfoList = new ArrayList<PriceActionInfo_v2>();
		
		for(int index = list.size() - 1; index > 0; index--) {
			Klines current = list.get(index);
			Klines parent = list.get(index - 1);
			if((mode == QuotationMode.SHORT && current.getMacd() > 0) 
					|| (mode == QuotationMode.LONG && current.getMacd() < 0)) {
				break;
			}
			if(mode == QuotationMode.LONG) {
				if(PriceUtil.verifyDecliningPrice_v24(current, parent)) {
					priceInfoList.add(new PriceActionInfo_v2(current, parent, PriceActionType_v2.LEAD));
				}
				if(PriceUtil.verifyDecliningPrice_v25(current, parent)) {
					priceInfoList.add(new PriceActionInfo_v2(current, parent, PriceActionType_v2.RISE_OR_FALL));
				}
			} else {
				if(PriceUtil.verifyPowerful_v24(current, parent)) {
					priceInfoList.add(new PriceActionInfo_v2(current, parent, PriceActionType_v2.LEAD));
				}
				if(PriceUtil.verifyPowerful_v25(current, parent)) {
					priceInfoList.add(new PriceActionInfo_v2(current, parent, PriceActionType_v2.RISE_OR_FALL));
				}
			}
		}

		Klines fibEnd = end;
		Klines last = PriceUtil.getLastKlines(list_15m);
		double stopLossLimit = 0;
		if(mode == QuotationMode.SHORT) {
			stopLossLimit = last.getLowPriceDoubleValue();
		} else {
			stopLossLimit = last.getHighPriceDoubleValue();
		}
		
		if(!CollectionUtils.isEmpty(priceInfoList)) {
			Klines current = null;
			if(mode == QuotationMode.SHORT) {
				PriceActionInfo_v2 info = PriceUtil.getMinBodyPriceActionInfo_v2(priceInfoList);
				PriceActionType_v2 type = info.getType();
				current = info.getCurrent();
				if(type == PriceActionType_v2.LEAD) {
					addPrices(new OpenPriceDetails(fibInfo.getFibCode(current.getClosePriceDoubleValue()), current.getClosePriceDoubleValue(), stopLossLimit));
				} else {
					addPrices(new OpenPriceDetails(fibInfo.getFibCode(current.getBodyLowPriceDoubleValue()), current.getBodyLowPriceDoubleValue(), stopLossLimit));
				}
			} else {
				PriceActionInfo_v2 info = PriceUtil.getMaxBodyPriceActionInfo_v2(priceInfoList);
				PriceActionType_v2 type = info.getType();
				current = info.getCurrent();
				if(type == PriceActionType_v2.LEAD) {
					addPrices(new OpenPriceDetails(fibInfo.getFibCode(current.getClosePriceDoubleValue()), current.getClosePriceDoubleValue(), stopLossLimit));
				} else {
					addPrices(new OpenPriceDetails(fibInfo.getFibCode(current.getBodyHighPriceDoubleValue()), current.getBodyHighPriceDoubleValue(), stopLossLimit));
				}
			}
			
			
			if(current.gt(end)) {
				
				List<Klines> data = PriceUtil.subList(start, end, list);
				MarketSentiment ms = new MarketSentiment(data);
				addPrices(this.fibInfo, ms, stopLossLimit);
				
				fibEnd = current;
				
			}
			
			Klines fibAfterFlag = PriceUtil.getAfterKlines(fibEnd, this.list_15m);
			if(fibAfterFlag != null) {
				this.fibAfterKlines.addAll(PriceUtil.subList(fibAfterFlag, this.list_15m));
				this.fibInfo.setFibAfterKlines(fibAfterKlines);
			}
		}
		
		if(mode == QuotationMode.LONG) {
			this.openPrices.sort(new PriceComparator(SortType.ASC));
		} else {
			this.openPrices.sort(new PriceComparator(SortType.DESC));
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
	
	private void addPrices(OpenPrice price) {
		if(fibInfo != null && FibCode.FIB4_618.gt(fibInfo.getFibCode(price.getPrice()))) {
			if(!PriceUtil.contains(openPrices, price)) {
				openPrices.add(price);
			}
		}
	}
	
	private void addPrices(FibInfo fibInfo, MarketSentiment ms, double stopLossLimit) {
		QuotationMode mode = fibInfo.getQuotationMode();
		if(mode == QuotationMode.SHORT) {
			addPrices(new OpenPriceDetails(fibInfo.getFibCode(ms.getMinBodyLowPrice()), ms.getMinBodyLowPrice(), stopLossLimit));
			addPrices(new OpenPriceDetails(fibInfo.getFibCode(ms.getLowPrice()), ms.getLowPrice(), stopLossLimit));
		} else {
			addPrices(new OpenPriceDetails(fibInfo.getFibCode(ms.getMaxBodyHighPrice()), ms.getMaxBodyHighPrice(), stopLossLimit));
			addPrices(new OpenPriceDetails(fibInfo.getFibCode(ms.getHighPrice()), ms.getHighPrice(), stopLossLimit));
		}
	}
}