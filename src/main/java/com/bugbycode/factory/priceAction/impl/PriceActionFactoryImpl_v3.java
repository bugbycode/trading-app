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
		List<PriceActionInfo> priceInfoList = new ArrayList<PriceActionInfo>();
		
		for(int index = list.size() - 1; index > 0; index--) {
			Klines current = list.get(index);
			Klines parent = list.get(index - 1);
			Klines next = list.get(index - 2);
			if((mode == QuotationMode.SHORT && current.getMacd() < 0) 
					|| (mode == QuotationMode.LONG && current.getMacd() > 0)) {
				break;
			}
			if(mode == QuotationMode.LONG) {
				if(PriceUtil.isPutInto_v2(current, parent, next) || PriceUtil.isPutInto_v3(current, parent, next)) {
					PriceActionType type = PriceActionType.DEFAULT;
					if(current.getBodyLowPriceDoubleValue() < parent.getLowPriceDoubleValue()) {
						type = PriceActionType.BACK;
					}
					priceInfoList.add(new PriceActionInfo(current, parent, next, type));
				} else if(PriceUtil.verifyDecliningPrice_v22(current, parent, next)) {
					priceInfoList.add(new PriceActionInfo(current, parent, next, PriceActionType.DECL_POWER));
				}
			} else {
				if(PriceUtil.isBullishSwallowing_v2(current, parent, next) || PriceUtil.isBullishSwallowing_v3(current, parent, next)) {
					PriceActionType type = PriceActionType.DEFAULT;
					if(current.getBodyHighPriceDoubleValue() > parent.getHighPriceDoubleValue()) {
						type = PriceActionType.BACK;
					}
					priceInfoList.add(new PriceActionInfo(current, parent, next, type));
				} else if(PriceUtil.verifyPowerful_v22(current, parent, next)) {
					priceInfoList.add(new PriceActionInfo(current, parent, next, PriceActionType.DECL_POWER));
				}
			}
		}
		
		if(!CollectionUtils.isEmpty(priceInfoList)) {
			Klines fibEnd = null;
			List<Klines> data = new ArrayList<Klines>();
			PriceActionInfo info = null;
			MarketSentiment ms = null;
			PriceActionType type = PriceActionType.DEFAULT;
			
			if(mode == QuotationMode.LONG) {
				
				info = PriceUtil.getMaxPriceActionInfo(priceInfoList);

				type = info.getType();
				data.add(info.getCurrent());
				data.add(info.getParent());
				ms = new MarketSentiment(data);
				
				double stopLossLimit = ms.getHighPrice();
				
				addPrices(new OpenPriceDetails(fibInfo.getFibCode(ms.getHighPrice()), ms.getHighPrice(), stopLossLimit));
				addPrices(new OpenPriceDetails(fibInfo.getFibCode(ms.getMaxBodyHighPrice()), ms.getMaxBodyHighPrice(), stopLossLimit));
				
				if(type == PriceActionType.DEFAULT || type == PriceActionType.BACK) {
					addPrices(new OpenPriceDetails(fibInfo.getFibCode(info.getParent().getBodyLowPriceDoubleValue()), info.getParent().getBodyLowPriceDoubleValue(), ms.getMaxBodyHighPrice()));
				}
				
				if(type == PriceActionType.BACK) {
					addPrices(new OpenPriceDetails(fibInfo.getFibCode(info.getParent().getLowPriceDoubleValue()), info.getParent().getLowPriceDoubleValue(), 
							info.getParent().getBodyLowPriceDoubleValue()));
				}
				
				if(type == PriceActionType.DECL_POWER) {
					addPrices(new OpenPriceDetails(fibInfo.getFibCode(info.getCurrent().getClosePriceDoubleValue()), info.getCurrent().getClosePriceDoubleValue(), ms.getMaxBodyHighPrice()));
				}
				
			} else {
				info = PriceUtil.getMinPriceActionInfo(priceInfoList);
				
				type = info.getType();
				data.add(info.getCurrent());
				data.add(info.getParent());
				ms = new MarketSentiment(data);
				
				double stopLossLimit = ms.getLowPrice();
				
				addPrices(new OpenPriceDetails(fibInfo.getFibCode(ms.getLowPrice()), ms.getLowPrice(), stopLossLimit));
				addPrices(new OpenPriceDetails(fibInfo.getFibCode(ms.getMinBodyLowPrice()), ms.getMinBodyLowPrice(), stopLossLimit));
				
				if(type == PriceActionType.DEFAULT || type == PriceActionType.BACK) {
					addPrices(new OpenPriceDetails(fibInfo.getFibCode(info.getParent().getBodyHighPriceDoubleValue()), info.getParent().getBodyHighPriceDoubleValue(), ms.getMinBodyLowPrice()));
				}
				
				if(type == PriceActionType.BACK) {
					addPrices(new OpenPriceDetails(fibInfo.getFibCode(info.getParent().getHighPriceDoubleValue()), info.getParent().getHighPriceDoubleValue(), 
							info.getParent().getBodyHighPriceDoubleValue()));
				}
				
				if(type == PriceActionType.DECL_POWER) {
					addPrices(new OpenPriceDetails(fibInfo.getFibCode(info.getCurrent().getClosePriceDoubleValue()), info.getCurrent().getClosePriceDoubleValue(), ms.getMinBodyLowPrice()));
				}
				
			}
			
			fibEnd = info.getCurrent();
			
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
		return current.getMacd() > 0;
	}
	
	private boolean verifyShort(Klines current) {
		return current.getMacd() < 0;
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
				//openPrices.add(price);
				FibCode parentCode = getParentFibCode(price.getCode());
				if(price.getCode() == FibCode.FIB0) {
					openPrices.add(new OpenPriceDetails(price.getCode(), price.getPrice()));
				} else {
					openPrices.add(new OpenPriceDetails(price.getCode(), price.getPrice(), fibInfo.getFibValue(parentCode)));
				}
			}
		}
	}
	
	private FibCode getParentFibCode(FibCode code) {
		FibCode parent = FibCode.FIB0;
		FibCode[] codes = FibCode.values();
		for(int index = 0; index < codes.length - 1; index++) { // 4.618 ~ 0
			FibCode current = codes[index];
			if(code == current) {
				parent = codes[index + 1];
				if(current == FibCode.FIB786) {
					parent = FibCode.FIB618;
				}
				break;
			}
		}
		return parent;
	}
}