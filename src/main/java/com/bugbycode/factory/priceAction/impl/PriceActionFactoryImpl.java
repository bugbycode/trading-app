package com.bugbycode.factory.priceAction.impl;

import java.util.ArrayList;
import java.util.List;

import org.springframework.util.CollectionUtils;

import com.bugbycode.factory.fibInfo.FibInfoFactory;
import com.bugbycode.factory.priceAction.PriceActionFactory;
import com.bugbycode.module.FibCode;
import com.bugbycode.module.FibInfo;
import com.bugbycode.module.FibLevel;
import com.bugbycode.module.Klines;
import com.bugbycode.module.QuotationMode;
import com.bugbycode.module.SortType;
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
	
	List<Klines> list_hit;
	
	private List<Klines> fibAfterKlines;
	
	private FibInfo fibInfo;
	
	private List<Klines> list_15m;//十五分钟级别k线 用于补充回撤之后的k线信息
	
	private Klines start = null;
	
	private Klines end = null;
	
	private PositionSide ps = PositionSide.DEFAULT;
	
	private List<OpenPrice> openPrices;
	
	public PriceActionFactoryImpl(List<Klines> list, List<Klines> list_hit, List<Klines> list_15m) {
		this.list = new ArrayList<Klines>();
		this.list_hit = new ArrayList<Klines>();
		this.list_15m = new ArrayList<Klines>();
		this.openPrices = new ArrayList<OpenPrice>();
		this.fibAfterKlines = new ArrayList<Klines>();
		if(!CollectionUtils.isEmpty(list_hit)) {
			this.list_hit.addAll(list_hit);
		}
		if(!CollectionUtils.isEmpty(list_15m)) {
			this.list_15m.addAll(list_15m);
		}
		if(!CollectionUtils.isEmpty(list)) {
			this.list.addAll(list);
			this.init(FibLevel.LEVEL_1);
		}
	}
	
	public PriceActionFactoryImpl(List<Klines> list, List<Klines> list_hit, List<Klines> list_15m, FibLevel level) {
		this.list = new ArrayList<Klines>();
		this.list_hit = new ArrayList<Klines>();
		this.list_15m = new ArrayList<Klines>();
		this.openPrices = new ArrayList<OpenPrice>();
		this.fibAfterKlines = new ArrayList<Klines>();
		if(!CollectionUtils.isEmpty(list_hit)) {
			this.list_hit.addAll(list_hit);
		}
		if(!CollectionUtils.isEmpty(list_15m)) {
			this.list_15m.addAll(list_15m);
		}
		if(!CollectionUtils.isEmpty(list)) {
			this.list.addAll(list);
			this.init(level);
		}
	}
	
	private void init(FibLevel level) {
		if(list.size() < 50 || CollectionUtils.isEmpty(list_hit) || CollectionUtils.isEmpty(list_15m)) {
			return;
		}
		
		KlinesComparator kc = new KlinesComparator(SortType.ASC);
		this.list.sort(kc);
		this.list_hit.sort(kc);
		this.list_15m.sort(kc);
		
		FibInfoFactory factory = new PriceActionFibInfoFactory(list, list, list_15m);
		this.fibInfo = factory.getFibInfo();
		
		if(!(factory.isLong() || factory.isShort())) {
			return;
		}
		
		if(level == FibLevel.LEVEL_2 || level == FibLevel.LEVEL_3) {
			factory = new PriceActionFibInfoFactory(list, list, list_15m, factory.isLong() ? PositionSide.SHORT : PositionSide.LONG);
			this.fibInfo = factory.getFibInfo();
		}
		
		if(!(factory.isLong() || factory.isShort())) {
			return;
		}
		
		if(level == FibLevel.LEVEL_3) {
			this.start = list.get(0);
			this.end = factory.getEnd();
			
			List<Klines> list_lv3 = PriceUtil.subList(this.start, this.end, list);
			factory = new PriceActionFibInfoFactory(list_lv3, list_lv3, list_15m);
			this.fibInfo = factory.getFibInfo();

			if(!(factory.isLong() || factory.isShort())) {
				return;
			}
		}
		
		this.start = factory.getStart();
		this.end = factory.getEnd();
		
		Klines fibAfter_15m = PriceUtil.getAfterKlines(end, list_15m);
		if(fibAfter_15m != null) {
			this.fibAfterKlines = PriceUtil.subList(fibAfter_15m, this.list_15m);
			this.fibInfo.setFibAfterKlines(fibAfterKlines);
		}
		

		FibCode openCode = FibCode.FIB1;
		FibCode takeProfitCode = FibCode.FIB5;
		double openCodeValue = this.fibInfo.getFibValue(openCode);
		
		Klines fibAfter_list_hit = PriceUtil.getAfterKlines(end, list_hit);
		if(fibAfter_list_hit == null) {
			return;
		}
		
		double openPriceValue = 0;
		double stopLossValue = 0;
		for(int index = list_hit.size() - 1; index >= 0; index--) {
			Klines current = list_hit.get(index);
			
			if(PriceUtil.isBreachLong(current, openCodeValue)) {
				this.ps = PositionSide.LONG;
				if(factory.isShort()) {
					takeProfitCode = FibCode.FIB2;
				}
				openPriceValue = current.getBodyHighPriceDoubleValue();
				stopLossValue = current.getLowPriceDoubleValue();
				break;
			} else if(PriceUtil.isBreachShort(current, openCodeValue)) {
				this.ps = PositionSide.SHORT;
				if(factory.isLong()) {
					takeProfitCode = FibCode.FIB2;
				}
				openPriceValue = current.getBodyLowPriceDoubleValue();
				stopLossValue = current.getHighPriceDoubleValue();
				break;
			}
			
			if(current.lte(fibAfter_list_hit)) {
				break;
			}
			
		}
		
		if(!(isLong() || isShort())) {
			return;
		}
		
		double takeProfitCodeValue = this.fibInfo.getFibValue(takeProfitCode);
		
		int decimalPoint = this.fibInfo.getDecimalPoint();
		
		FibInfo childFibInfo = new FibInfo(takeProfitCodeValue, openCodeValue, decimalPoint);
		double firstTakeProfit = childFibInfo.getFibValue(FibCode.FIB618);
		double secondTakeProfit = childFibInfo.getFibValue(FibCode.FIB786);
		
		FibInfo stopLossFibInfo = new FibInfo(openCodeValue, takeProfitCodeValue, decimalPoint);
		if(isLong()) {
			stopLossValue = PriceUtil.getMaxPrice(stopLossValue, stopLossFibInfo.getFibValue(FibCode.FIB1_272));
		} else {
			stopLossValue = PriceUtil.getMinPrice(stopLossValue, stopLossFibInfo.getFibValue(FibCode.FIB1_272));
		}
		
		addPrices(new OpenPriceDetails(openCode, openPriceValue, stopLossValue, firstTakeProfit, secondTakeProfit, AutoTradeType.PRICE_ACTION, fibInfo));
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
		return this.ps == PositionSide.LONG;
	}

	@Override
	public boolean isShort() {
		return this.ps == PositionSide.SHORT;
	}

	private void addPrices(OpenPrice price) {
		if(fibInfo != null && FibCode.FIB4_618.gt(fibInfo.getFibCode(price.getPrice()))) {
			if(!PriceUtil.contains(openPrices, price)) {
				openPrices.add(price);
			}
		}
	}
}

class PriceActionFibInfoFactory implements FibInfoFactory {
	
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
	public PriceActionFibInfoFactory(List<Klines> list, List<Klines> list_trend, List<Klines> list_15m) {
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
			this.init(PositionSide.DEFAULT);
		}
	}
	
	/**
	 * 
	 * @param list 斐波那契回撤指标参考的K线信息
	 * @param list_trend 行情走势参考的K线信息
	 * @param list_15m 十五分钟级别k线信息
	 * @param ps_mode
	 */
	public PriceActionFibInfoFactory(List<Klines> list, List<Klines> list_trend, List<Klines> list_15m, PositionSide ps_mode) {
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
			this.init(ps_mode);
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
	
	private void init(PositionSide ps_mode) {
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

		PositionSide ps = ps_mode;
		
		if(ps == PositionSide.DEFAULT) {
			ps = getPositionSide();
		}
		
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
		
		Klines fibAfterKline = PriceUtil.getAfterKlines(end, this.list_15m);
		if(fibAfterKline != null) {
			this.fibAfterKlines = PriceUtil.subList(fibAfterKline, this.list_15m);
			this.fibInfo.setFibAfterKlines(fibAfterKlines);
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