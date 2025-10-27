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
 * 斐波那契回指标撤接口实现类（支撑阻力互换位作为开仓点）
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
		
		PriceUtil.calculateMACD(list);
		PriceUtil.calculateMACD(list_trend);
		
		PriceUtil.calculateEMA_7_25_99(list);
		PriceUtil.calculateEMA_7_25_99(list_trend);
		
		PriceUtil.calculateDeltaAndCvd(list);
		PriceUtil.calculateDeltaAndCvd(list_trend);
		PriceUtil.calculateDeltaAndCvd(list_15m);
		
		this.openPrices.clear();
		this.fibAfterKlines.clear();
		
		//Klines last = PriceUtil.getLastKlines(list);
		
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
		
		List<Klines> firstSubList = PriceUtil.subList(first, third, list);
		
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
			this.fibInfo = new FibInfo(start.getHighPriceDoubleValue(), end.getLowPriceDoubleValue(), start.getDecimalNum(), FibLevel.LEVEL_0);
		} else if(ps == PositionSide.LONG) {
			start = PriceUtil.getMinPriceKLine(firstSubList);
			startAfterFlag = PriceUtil.getAfterKlines(start, firstSubList);
			if(startAfterFlag == null) {
				startAfterFlag = start;
			}
			secondSubList = PriceUtil.subList(startAfterFlag, list);
			end = PriceUtil.getMaxPriceKLine(secondSubList);
			this.fibInfo = new FibInfo(start.getLowPriceDoubleValue(), end.getHighPriceDoubleValue(), start.getDecimalNum(), FibLevel.LEVEL_0);
		}
		
		if(this.fibInfo == null) {
			return;
		}
		//高频交易
		//this.fibInfo.setTradeFrequency(TradeFrequency.HIGH);

		QuotationMode mode = this.fibInfo.getQuotationMode();
		
		boolean split_price_bool = false;
		
		List<Klines> fibSubList = PriceUtil.subList(start, end, list);
		for(int index = list.size() - 1; index > 1; index--) {
			Klines current = list.get(index);
			Klines parent = list.get(index - 1);
			Klines next = list.get(index - 2);
			if(current.gt(end)) {
				continue;
			}
			
			if(((mode == QuotationMode.LONG && current.getDea() >= 0 && parent.getDea() < 0)
					|| (mode == QuotationMode.SHORT && current.getDea() <= 0 && parent.getDea() > 0)) && !split_price_bool) {
				split_price_bool = true;
				List<Klines> points = PriceUtil.subList(parent, fibSubList);
				MarketSentiment ms = new MarketSentiment(points);
				
				if(ms.isNotEmpty()) {
					if(mode == QuotationMode.LONG) {
						double lowPriceValue = ms.getLowPrice();
						double bodyLowPrice = ms.getMinBodyLowPrice();
						addPrices(new OpenPriceDetails(fibInfo.getFibCode(lowPriceValue), lowPriceValue));
						addPrices(new OpenPriceDetails(fibInfo.getFibCode(bodyLowPrice), bodyLowPrice));
					} else {
						double highPriceValue = ms.getHighPrice();
						double bodyHigPrice = ms.getMaxBodyHighPrice();
						addPrices(new OpenPriceDetails(fibInfo.getFibCode(highPriceValue), highPriceValue));
						addPrices(new OpenPriceDetails(fibInfo.getFibCode(bodyHigPrice), bodyHigPrice));
					}
				}
			}
			
			if((mode == QuotationMode.LONG && current.getDea() > 0)
					|| (mode == QuotationMode.SHORT && current.getDea() < 0)) {
				continue;
			}
			
			if(current.lt(start)) {
				break;
			}
			
			if(mode == QuotationMode.LONG) {
				if(PriceUtil.verifyPowerful_v10(current, parent, next)) {
					double openPriceValue = parent.getBodyHighPriceDoubleValue();
					OpenPrice openPrice = new OpenPriceDetails(fibInfo.getFibCode(openPriceValue), openPriceValue);
					addPrices(openPrice);
				} else if(PriceUtil.verifyDecliningPrice_v10(current, parent, next)) {
					List<Klines> sub_points = PriceUtil.subList(current, fibSubList);
					MarketSentiment ms = new MarketSentiment(sub_points);
					double openPriceValue = ms.getLowPrice();
					OpenPrice openPrice = new OpenPriceDetails(fibInfo.getFibCode(openPriceValue), openPriceValue);
					addPrices(openPrice);
				}
			} else if(mode == QuotationMode.SHORT) {
				if(PriceUtil.verifyDecliningPrice_v10(current, parent, next)) {
					double openPriceValue = parent.getBodyLowPriceDoubleValue();
					OpenPrice openPrice = new OpenPriceDetails(fibInfo.getFibCode(openPriceValue), openPriceValue);
					addPrices(openPrice);
				} else if(PriceUtil.verifyPowerful_v10(current, parent, next)) {
					List<Klines> sub_points = PriceUtil.subList(current, fibSubList);
					MarketSentiment ms = new MarketSentiment(sub_points);
					double openPriceValue = ms.getHighPrice();
					OpenPrice openPrice = new OpenPriceDetails(fibInfo.getFibCode(openPriceValue), openPriceValue);
					addPrices(openPrice);
				}
			}
		}
		
		addPrices(new OpenPriceDetails(FibCode.FIB1, fibInfo.getFibValue(FibCode.FIB1)));
		
		Klines fibAfterFlag = PriceUtil.getAfterKlines(end, this.list_15m);
		if(fibAfterFlag != null) {
			this.fibAfterKlines.addAll(PriceUtil.subList(fibAfterFlag, this.list_15m));
			this.fibInfo.setFibAfterKlines(fibAfterKlines);
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
	
	private boolean verifyLong(Klines current) {
		return current.getDea() > 0;
	}
	
	private boolean verifyShort(Klines current) {
		return current.getDea() < 0;
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
	
}