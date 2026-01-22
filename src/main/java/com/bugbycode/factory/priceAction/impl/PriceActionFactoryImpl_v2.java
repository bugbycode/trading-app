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
import com.bugbycode.module.PriceActionInfo;
import com.bugbycode.module.PriceActionType;
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
public class PriceActionFactoryImpl_v2 implements PriceActionFactory{
	
	private List<Klines> list;
	
	private List<Klines> fibAfterKlines;
	
	private FibInfo fibInfo;
	
	private List<Klines> list_15m;//十五分钟级别k线 用于补充回撤之后的k线信息
	
	private Klines start = null;
	
	private Klines end = null;
	
	private List<OpenPrice> openPrices;
	
	public PriceActionFactoryImpl_v2(List<Klines> list, List<Klines> list_15m) {
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
		//PriceUtil.calculateMACD(list);
		PriceUtil.calculateDeltaAndCvd(list);
		
		this.openPrices.clear();
		this.fibAfterKlines.clear();
		
		PositionSide ps = getPositionSide();
		if(ps == PositionSide.DEFAULT) {
			return;
		}
		
		Klines third = null;
		Klines second = null;
		Klines first = null;
		PriceActionInfo info = null;
		
		for(int index = list.size() - 1; index > 1; index--) {
			Klines current = list.get(index);
			Klines parent = list.get(index - 1);
			Klines next = list.get(index - 2);
			if(ps == PositionSide.LONG) {// low - high - low
				if(third == null) {
					if(verifyLow(current, parent)) {
						third = current;
						info = new PriceActionInfo(current, parent, next, null);
					}
				} else if(second == null) {
					if(verifyHigh(current, parent)) {
						second = current;
					}
				} else if(first == null) {
					if(verifyLow(current, parent)) {
						first = current;
						break;
					}
				}
			} else if(ps == PositionSide.SHORT) { // high - low - high
				if(third == null) {
					if(verifyHigh(current, parent)) {
						third = current;
						info = new PriceActionInfo(current, parent, next, null);
					}
				} else if(second == null) {
					if(verifyLow(current, parent)) {
						second = current;
					}
				} else if(first == null) {
					if(verifyHigh(current, parent)) {
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
		
		if(info != null) {
			Klines current = info.getCurrent();
			Klines parent = info.getParent();
			Klines next = info.getNext();
			
			List<Klines> data = new ArrayList<Klines>();
			MarketSentiment ms = null;
			PriceActionType type = null;
			
			if(mode == QuotationMode.LONG) {
				
				if(PriceUtil.isPutInto_v2(current, parent, next) || PriceUtil.isPutInto_v3(current, parent, next)) {
					type = PriceActionType.DEFAULT;
					if(current.getBodyLowPriceDoubleValue() < parent.getLowPriceDoubleValue()) {
						type = PriceActionType.BACK;
					}
				} else if(PriceUtil.verifyDecliningPrice_v22(current, parent, next)) {
					type = PriceActionType.DECL_POWER;
				}
				
				info = new PriceActionInfo(current, parent, next, type);

				type = info.getType();
				data.add(info.getCurrent());
				data.add(info.getParent());
				ms = new MarketSentiment(data);
				
				addPrices(new OpenPriceDetails(fibInfo.getFibCode(ms.getHighPrice()), ms.getHighPrice()));
				addPrices(new OpenPriceDetails(fibInfo.getFibCode(ms.getMaxBodyHighPrice()), ms.getMaxBodyHighPrice()));
				
				if(type == PriceActionType.DEFAULT || type == PriceActionType.BACK) {
					addPrices(new OpenPriceDetails(fibInfo.getFibCode(info.getParent().getBodyLowPriceDoubleValue()), info.getParent().getBodyLowPriceDoubleValue()));
				}
				
				if(type == PriceActionType.BACK) {
					addPrices(new OpenPriceDetails(fibInfo.getFibCode(info.getParent().getLowPriceDoubleValue()), info.getParent().getLowPriceDoubleValue()));
				}
				
				if(type == PriceActionType.DECL_POWER) {
					addPrices(new OpenPriceDetails(fibInfo.getFibCode(info.getCurrent().getClosePriceDoubleValue()), info.getCurrent().getClosePriceDoubleValue()));
				}
				
			} else {
				
				if(PriceUtil.isBullishSwallowing_v2(current, parent, next) || PriceUtil.isBullishSwallowing_v3(current, parent, next)) {
					type = PriceActionType.DEFAULT;
					if(current.getBodyHighPriceDoubleValue() > parent.getHighPriceDoubleValue()) {
						type = PriceActionType.BACK;
					}
				} else if(PriceUtil.verifyPowerful_v22(current, parent, next)) {
					type = PriceActionType.DECL_POWER;
				}
				
				info = new PriceActionInfo(current, parent, next, type);
				
				type = info.getType();
				data.add(info.getCurrent());
				data.add(info.getParent());
				ms = new MarketSentiment(data);
				
				addPrices(new OpenPriceDetails(fibInfo.getFibCode(ms.getLowPrice()), ms.getLowPrice()));
				addPrices(new OpenPriceDetails(fibInfo.getFibCode(ms.getMinBodyLowPrice()), ms.getMinBodyLowPrice()));
				
				if(type == PriceActionType.DEFAULT || type == PriceActionType.BACK) {
					addPrices(new OpenPriceDetails(fibInfo.getFibCode(info.getParent().getBodyHighPriceDoubleValue()), info.getParent().getBodyHighPriceDoubleValue()));
				}
				
				if(type == PriceActionType.BACK) {
					addPrices(new OpenPriceDetails(fibInfo.getFibCode(info.getParent().getHighPriceDoubleValue()), info.getParent().getHighPriceDoubleValue()));
				}
				
				if(type == PriceActionType.DECL_POWER) {
					addPrices(new OpenPriceDetails(fibInfo.getFibCode(info.getCurrent().getClosePriceDoubleValue()), info.getCurrent().getClosePriceDoubleValue()));
				}
			}
			
			if(type == null) {
				addPrices(new OpenPriceDetails(fibInfo.getFibCode(info.getCurrent().getClosePriceDoubleValue()), info.getCurrent().getClosePriceDoubleValue()));
			}
			
			Klines fibAfterFlag = PriceUtil.getAfterKlines(third, this.list_15m);
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
		for(int index = list.size() - 1; index > 0; index--) {
			Klines current = list.get(index);
			Klines parent = list.get(index - 1);
			if(verifyLong(current, parent)) {
				ps = PositionSide.LONG;
			} else if(verifyShort(current, parent)) {
				ps = PositionSide.SHORT;
			}
			if(ps != PositionSide.DEFAULT) {
				break;
			}
		}
		return ps;
	}
	
	private boolean verifyLong(Klines current, Klines parent) {
		return verifyLow(current, parent);
	}
	
	private boolean verifyShort(Klines current, Klines parent) {
		return verifyHigh(current, parent);
	}
	
	private boolean verifyHigh(Klines current, Klines parent) {
		return current.getDelta() < 0 && parent.getDelta() >= 0;
	}
	
	private boolean verifyLow(Klines current, Klines parent) {
		return current.getDelta() > 0 && parent.getDelta() <= 0;
	}
	
	private void addPrices(OpenPrice price) {
		if(fibInfo != null && FibCode.FIB4_618.gt(fibInfo.getFibCode(price.getPrice()))) {
			if(!PriceUtil.contains(openPrices, price)) {
				openPrices.add(price);
			}
		}
	}
}