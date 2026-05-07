package com.bugbycode.factory.area.impl;

import java.util.ArrayList;
import java.util.List;

import org.springframework.util.CollectionUtils;

import com.bugbycode.factory.area.AreaFactory;
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
 * 盘整区网格交易
 */
public class AreaFactoryImpl_v2 implements AreaFactory {

	private List<Klines> list;
	
	private List<Klines> list_hit;
	
	private List<Klines> list_15m;
	
	private List<Klines> fibAfterKlines;
	
	private PositionSide ps;
	
	private List<OpenPrice> openPrices;
	
	public AreaFactoryImpl_v2(List<Klines> list, List<Klines> list_hit, List<Klines> list_15m) {
		this.ps = PositionSide.DEFAULT;
		this.list = new ArrayList<Klines>();
		this.list_hit = new ArrayList<Klines>();
		this.list_15m = new ArrayList<Klines>();
		this.fibAfterKlines = new ArrayList<Klines>();
		this.openPrices = new ArrayList<OpenPrice>();
		if(!CollectionUtils.isEmpty(list)) {
			this.list.addAll(list);
		}
		if(!CollectionUtils.isEmpty(list_hit)) {
			this.list_hit.addAll(list_hit);
		}
		if(!CollectionUtils.isEmpty(list_15m)) {
			this.list_15m.addAll(list_15m);
		}
		
		this.init();
	}
	
	private void init() {
		
		if(list.size() < 99 || CollectionUtils.isEmpty(this.list_hit) || CollectionUtils.isEmpty(this.list_15m)) {
			return;
		}
		
		KlinesComparator kc = new KlinesComparator(SortType.ASC);
		this.list.sort(kc);
		this.list_hit.sort(kc);
		this.list_15m.sort(kc);

		FibInfoFactory factory = new AreaFibInfoFactory(list, list, list_15m);
		
		if(!(factory.isLong() || factory.isShort())) {
			return;
		}
		
		FibInfo fibInfo = factory.getFibInfo();
		
		Klines end = PriceUtil.getAfterKlines(factory.getEnd(), list_15m);
		if(end == null) {
			return;
		}
		
		FibCode hitCode = null;
		FibCode takeProfitCode = null;
		Klines last = null;
		double takeProfitValue = 0;
		double hitCodeValue = 0;
		double d = 0;
		double firstTakeProfit = 0;
		double secondTakeProfit = 0;
		
		for(int index = list_hit.size() - 1; index > 0; index--) {
			Klines current = list_hit.get(index);
			if((hitCode = getIsBreachFibCode(fibInfo, QuotationMode.LONG, current)) != null) {
				last = current;
				this.ps = PositionSide.LONG;
				takeProfitCode = getTakeProfitCode(fibInfo, QuotationMode.LONG, hitCode);
				takeProfitValue = fibInfo.getFibValue(takeProfitCode);
				hitCodeValue = fibInfo.getFibValue(hitCode);
				d = takeProfitValue - hitCodeValue;
				firstTakeProfit = PriceUtil.formatDoubleDecimalValue(hitCodeValue + d * 0.618, current.getDecimalNum());
				secondTakeProfit = PriceUtil.formatDoubleDecimalValue(hitCodeValue + d * 0.786, current.getDecimalNum());
				addPrices(new OpenPriceDetails(hitCode, current.getBodyHighPriceDoubleValue(), current.getLowPriceDoubleValue(), firstTakeProfit, secondTakeProfit, AutoTradeType.AREA_INDEX));
				break;
			} else if((hitCode = getIsBreachFibCode(fibInfo, QuotationMode.SHORT, current)) != null) {
				last = current;
				this.ps = PositionSide.SHORT;
				takeProfitCode = getTakeProfitCode(fibInfo, QuotationMode.SHORT, hitCode);
				takeProfitValue = fibInfo.getFibValue(takeProfitCode);
				hitCodeValue = fibInfo.getFibValue(hitCode);
				d = hitCodeValue - takeProfitValue;
				firstTakeProfit = PriceUtil.formatDoubleDecimalValue(hitCodeValue - d * 0.618, current.getDecimalNum());
				secondTakeProfit = PriceUtil.formatDoubleDecimalValue(hitCodeValue - d * 0.786, current.getDecimalNum());
				addPrices(new OpenPriceDetails(hitCode, current.getBodyLowPriceDoubleValue(), current.getHighPriceDoubleValue(), firstTakeProfit, secondTakeProfit, AutoTradeType.AREA_INDEX));
				break;
			}
			
			if(current.lte(end)) {
				break;
			}
		}
		
		Klines fibAfterFlag = PriceUtil.getAfterKlines(last, this.list_15m);
		if(fibAfterFlag != null) {
			this.fibAfterKlines.addAll(PriceUtil.subList(fibAfterFlag, this.list_15m));
		}
	}
	
	private void addPrices(OpenPrice price) {
		if(!PriceUtil.contains(openPrices, price)) {
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

	private FibCode getIsBreachFibCode(FibInfo fibInfo, QuotationMode user_mode, Klines current) {
		FibCode result = null;
		FibCode[] codes = FibCode.values();
		if((fibInfo.isLong() && user_mode == QuotationMode.LONG) || (fibInfo.isShort() && user_mode == QuotationMode.SHORT)) {
			for(int index = codes.length - 1; index >= 0; index--) {
				FibCode code = codes[index];
				if(code == FibCode.FIB0) {
					continue;
				}
				double fibValue = fibInfo.getFibValue(code);
				if((user_mode == QuotationMode.LONG && PriceUtil.isBreachLong(current, fibValue)) 
						|| (user_mode == QuotationMode.SHORT && PriceUtil.isBreachShort(current, fibValue))) {
					result = code;
				}
			}
		} else {
			for(int index = 0; index < codes.length; index++) {
				FibCode code = codes[index];
				if(code == FibCode.FIB4_618) {
					continue;
				}
				double fibValue = fibInfo.getFibValue(code);
				if((user_mode == QuotationMode.LONG && PriceUtil.isBreachLong(current, fibValue)) 
						|| (user_mode == QuotationMode.SHORT && PriceUtil.isBreachShort(current, fibValue))) {
					result = code;
				}
			}
		}
		
		return result;
	}
	
	private FibCode getTakeProfitCode(FibInfo fibInfo, QuotationMode user_mode, FibCode hitCode) {
		FibCode result = hitCode;
		FibCode[] codes = FibCode.values();
		if((fibInfo.isLong() && user_mode == QuotationMode.LONG) || (fibInfo.isShort() && user_mode == QuotationMode.SHORT)) {
			for(int index = codes.length - 1; index >= 0; index--) {
				FibCode code = codes[index];
				if(code == FibCode.FIB0) {
					continue;
				}
				if(code == hitCode) {
					result = codes[index + 1];
				}
			}
		} else {
			for(int index = 0; index < codes.length; index++) {
				FibCode code = codes[index];
				if(code == FibCode.FIB4_618) {
					continue;
				}
				if(code == hitCode) {
					result = codes[index - 1];
				}
			}
		}
		return result;
	}
}

class AreaFibInfoFactory implements FibInfoFactory {
	
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
	public AreaFibInfoFactory(List<Klines> list, List<Klines> list_trend, List<Klines> list_15m) {
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
			this.fibInfo.setFibAfterKlines(fibAfterKlines);
		}
		
		if(!CollectionUtils.isEmpty(fibAfterKlines)) {
			
			MarketSentiment ms = new MarketSentiment(fibAfterKlines);
			double openCodeValue = mode == QuotationMode.LONG ? ms.getLowPrice() : ms.getHighPrice();
			FibCode openCode = fibInfo.getFibCode(openCodeValue);
			
			if(openCode.gt(FibCode.FIB0)) {
				
				double fibValue = fibInfo.getFibValue(openCode);
				
				FibCode takeProfitCode = fibInfo.getTakeProfit_v2(openCode);
				double takeProfitValue = fibInfo.getFibValue(takeProfitCode);
				
				FibInfo childFibInfo = new FibInfo(takeProfitValue, fibValue, fibInfo.getDecimalPoint(), FibLevel.LEVEL_1);
				
				double firstTakeProfit = childFibInfo.getFibValue(FibCode.FIB618);
				double secondTakeProfit = childFibInfo.getFibValue(FibCode.FIB786);
				
				FibInfo stopLossFibInfo = new FibInfo(fibValue, secondTakeProfit, fibInfo.getDecimalPoint(), FibLevel.LEVEL_1);

				double stopLossValue = stopLossFibInfo.getFibValue(FibCode.FIB1_272);
				
				addPrices(new OpenPriceDetails(openCode, fibValue, stopLossValue, firstTakeProfit, secondTakeProfit, AutoTradeType.FIB_RET, fibInfo));

			}
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
		return k.getDea() > 0;
	}
	
	private boolean verifyShort(Klines k) {
		return k.getDea() < 0;
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