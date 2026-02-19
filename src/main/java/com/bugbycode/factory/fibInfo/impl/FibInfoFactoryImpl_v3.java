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
 * 斐波那契回指标撤接口实现类（短线交易）
 */
public class FibInfoFactoryImpl_v3 implements FibInfoFactory {

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
	public FibInfoFactoryImpl_v3(List<Klines> list, List<Klines> list_trend, List<Klines> list_15m) {
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
		if(CollectionUtils.isEmpty(list) || list.size() < 99 || list_trend.size() < 50 || CollectionUtils.isEmpty(list_15m)) {
			return;
		}
		
		KlinesComparator kc = new KlinesComparator(SortType.ASC);
		this.list.sort(kc);
		this.list_trend.sort(kc);
		this.list_15m.sort(kc);
		
		PriceUtil.calculateMACD(list);
		PriceUtil.calculateMACD(list_trend);
		
		//PriceUtil.calculateEMA_7_25_99(list);
		//PriceUtil.calculateEMA_7_25_99(list_trend);
		
		//PriceUtil.calculateDeltaAndCvd(list);
		//PriceUtil.calculateDeltaAndCvd(list_trend);
		//PriceUtil.calculateDeltaAndCvd(list_15m);
		
		this.openPrices.clear();
		this.fibAfterKlines.clear();
		
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
		
		Klines fibEnd = end;
		Klines last = PriceUtil.getLastKlines(list_15m);
		double stopLossLimit = 0;
		if(mode == QuotationMode.LONG) {
			stopLossLimit = last.getLowPriceDoubleValue();
		} else {
			stopLossLimit = last.getHighPriceDoubleValue();
		}
		
		
		Klines fibAfterKline = PriceUtil.getAfterKlines(fibEnd, this.list_15m);
		if(fibAfterKline != null) {
			this.fibAfterKlines = PriceUtil.subList(fibAfterKline, this.list_15m);
		}

		FibCode openCode = FibCode.FIB0;
		MarketSentiment ms = new MarketSentiment(fibAfterKlines);
		if(ms.isNotEmpty()) {
			if(mode == QuotationMode.LONG) {
				openCode = fibInfo.getFibCode(ms.getLowPrice());
			} else {
				openCode = fibInfo.getFibCode(ms.getHighPrice());
			}
		}
		
		fibAfterKline = PriceUtil.getAfterKlines(fibEnd, this.list);
		
		if(openCode.gte(FibCode.FIB236)) {
			
			List<PriceActionInfo_v2> priceActionList = new ArrayList<PriceActionInfo_v2>();
			
			for(int index = list.size() - 1;index > 0; index--) {
				Klines current = list.get(index);
				Klines parent = list.get(index - 1);
				if(current.lte(end)) {
					break;
				}
				
				if(mode == QuotationMode.LONG) {
					if(PriceUtil.verifyPowerful_v24(current, parent)) {
						priceActionList.add(new PriceActionInfo_v2(current, parent, PriceActionType_v2.LEAD));
					}
					if(PriceUtil.verifyPowerful_v25(current, parent)) {
						priceActionList.add(new PriceActionInfo_v2(current, parent, PriceActionType_v2.RISE_OR_FALL));
					}
				} else {
					if(PriceUtil.verifyDecliningPrice_v24(current, parent)) {
						priceActionList.add(new PriceActionInfo_v2(current, parent, PriceActionType_v2.LEAD));
					}
					if(PriceUtil.verifyDecliningPrice_v25(current, parent)) {
						priceActionList.add(new PriceActionInfo_v2(current, parent, PriceActionType_v2.RISE_OR_FALL));
					}
				}
				
			}
			//处理价格 start
			if(!CollectionUtils.isEmpty(priceActionList)) {
				Klines current = null;
				if(mode == QuotationMode.LONG) {
					PriceActionInfo_v2 info = PriceUtil.getMinBodyPriceActionInfo_v2(priceActionList);
					PriceActionType_v2 type = info.getType();
					current = info.getCurrent();
					if(type == PriceActionType_v2.LEAD) {
						addPrices(new OpenPriceDetails(openCode, current.getClosePriceDoubleValue(), stopLossLimit));
					} else {
						addPrices(new OpenPriceDetails(openCode, current.getBodyLowPriceDoubleValue(), stopLossLimit));
					}
				} else {
					PriceActionInfo_v2 info = PriceUtil.getMaxBodyPriceActionInfo_v2(priceActionList);
					PriceActionType_v2 type = info.getType();
					current = info.getCurrent();
					if(type == PriceActionType_v2.LEAD) {
						addPrices(new OpenPriceDetails(openCode, current.getClosePriceDoubleValue(), stopLossLimit));
					} else {
						addPrices(new OpenPriceDetails(openCode, current.getBodyHighPriceDoubleValue(), stopLossLimit));
					}
				}
				List<Klines> data = PriceUtil.subList(fibAfterKline, current, list);
				ms = new MarketSentiment(data);
				addPrices(mode, openCode, ms, stopLossLimit);
				fibEnd = current;
			}
			//处理价格 end
		}
		
		if(openCode.lte(FibCode.FIB1)) {
			List<Klines> fibSubList = PriceUtil.subList(start, end, list);
			ms = new MarketSentiment(fibSubList);
			addPrices(this.fibInfo, ms, stopLossLimit);
		}
		
		fibAfterKline = PriceUtil.getAfterKlines(fibEnd, this.list_15m);
		if(fibAfterKline != null) {
			this.fibAfterKlines = PriceUtil.subList(fibAfterKline, this.list_15m);
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
		if(!PriceUtil.contains(openPrices, price) && price.getCode().gte(FibCode.FIB236)) {
			openPrices.add(price);
		}
	}
	
	private void addPrices(QuotationMode mode, FibCode openCode, MarketSentiment ms, double stopLossLimit) {
		if(mode == QuotationMode.LONG) {
			addPrices(new OpenPriceDetails(openCode, ms.getMinBodyLowPrice(), stopLossLimit));
			addPrices(new OpenPriceDetails(openCode, ms.getLowPrice(), stopLossLimit));
		} else {
			addPrices(new OpenPriceDetails(openCode, ms.getMaxBodyHighPrice(), stopLossLimit));
			addPrices(new OpenPriceDetails(openCode, ms.getHighPrice(), stopLossLimit));
		}
	}
	
	private void addPrices(FibInfo fibInfo, MarketSentiment ms, double stopLossLimit) {
		QuotationMode mode = fibInfo.getQuotationMode();
		if(mode == QuotationMode.LONG) {
			addPrices(new OpenPriceDetails(fibInfo.getFibCode(ms.getMinBodyLowPrice()), ms.getMinBodyLowPrice(), stopLossLimit));
			addPrices(new OpenPriceDetails(fibInfo.getFibCode(ms.getLowPrice()), ms.getLowPrice(), stopLossLimit));
		} else {
			addPrices(new OpenPriceDetails(fibInfo.getFibCode(ms.getMaxBodyHighPrice()), ms.getMaxBodyHighPrice(), stopLossLimit));
			addPrices(new OpenPriceDetails(fibInfo.getFibCode(ms.getHighPrice()), ms.getHighPrice(), stopLossLimit));
		}
	}
	
	public Klines getStart() {
		return start;
	}

	public Klines getEnd() {
		return end;
	}
	
}