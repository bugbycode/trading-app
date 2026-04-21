package com.bugbycode.factory.area.impl;

import java.util.ArrayList;
import java.util.List;

import org.springframework.util.CollectionUtils;

import com.bugbycode.factory.area.AreaFactory;
import com.bugbycode.factory.fibInfo.FibInfoFactory;
import com.bugbycode.factory.fibInfo.impl.FibInfoFactoryImpl_v4;
import com.bugbycode.module.FibCode;
import com.bugbycode.module.FibInfo;
import com.bugbycode.module.Klines;
import com.bugbycode.module.QuotationMode;
import com.bugbycode.module.SortType;
import com.bugbycode.module.price.OpenPrice;
import com.bugbycode.module.price.impl.OpenPriceDetails;
import com.bugbycode.module.trading.PositionSide;
import com.util.KlinesComparator;
import com.util.PriceUtil;

/**
 * 盘整区网格交易
 */
public class AreaFactoryImpl_v4 implements AreaFactory {

	private List<Klines> list;
	
	private List<Klines> list_hit;
	
	private List<Klines> list_15m;
	
	private List<Klines> fibAfterKlines;
	
	private PositionSide ps;
	
	private List<OpenPrice> openPrices;
	
	public AreaFactoryImpl_v4(List<Klines> list, List<Klines> list_hit, List<Klines> list_15m) {
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

		FibInfoFactory factory = new FibInfoFactoryImpl_v4(list, list, list_15m);
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
				firstTakeProfit = hitCodeValue + PriceUtil.formatDoubleDecimalValue(d * 0.618, current.getDecimalNum());
				secondTakeProfit = hitCodeValue + PriceUtil.formatDoubleDecimalValue(d * 0.786, current.getDecimalNum());
				addPrices(new OpenPriceDetails(hitCode, current.getBodyHighPriceDoubleValue(), current.getLowPriceDoubleValue(), firstTakeProfit, secondTakeProfit));
				break;
			} else if((hitCode = getIsBreachFibCode(fibInfo, QuotationMode.SHORT, current)) != null) {
				last = current;
				this.ps = PositionSide.SHORT;
				takeProfitCode = getTakeProfitCode(fibInfo, QuotationMode.SHORT, hitCode);
				takeProfitValue = fibInfo.getFibValue(takeProfitCode);
				hitCodeValue = fibInfo.getFibValue(hitCode);
				d = hitCodeValue - takeProfitValue;
				firstTakeProfit = hitCodeValue - PriceUtil.formatDoubleDecimalValue(d * 0.618, current.getDecimalNum());
				secondTakeProfit = hitCodeValue - PriceUtil.formatDoubleDecimalValue(d * 0.786, current.getDecimalNum());
				addPrices(new OpenPriceDetails(hitCode, current.getBodyLowPriceDoubleValue(), current.getHighPriceDoubleValue(), firstTakeProfit, secondTakeProfit));
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
		if((fibInfo.isLong() && user_mode == QuotationMode.LONG) || (fibInfo.isShort() || user_mode == QuotationMode.SHORT)) {
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