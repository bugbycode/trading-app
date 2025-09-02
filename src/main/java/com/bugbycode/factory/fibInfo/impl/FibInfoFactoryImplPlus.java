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
 * 斐波那契回指标撤接口实现类 新增更高的高点和更低的低点逻辑判断
 */
public class FibInfoFactoryImplPlus implements FibInfoFactory {

	private List<Klines> list;
	
	private List<Klines> fibAfterKlines;
	
	private FibInfo fibInfo;
	
	private List<Klines> list_15m;//十五分钟级别k线 用于补充回撤之后的k线信息
	
	private Klines start = null;
	
	private Klines end = null;
	
	private List<OpenPrice> openPrices;
	
	private Klines firstPoint = null;
	
	private Klines secondPoint = null;
	
	public FibInfoFactoryImplPlus(List<Klines> list, List<Klines> list_15m) {
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
		if(CollectionUtils.isEmpty(list) || list.size() < 99 || CollectionUtils.isEmpty(list_15m)) {
			return;
		}
		
		KlinesComparator kc = new KlinesComparator(SortType.ASC);
		this.list.sort(kc);
		
		PriceUtil.calculateMACD(list);
		PriceUtil.calculateEMA_7_25_99(list);
		
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
			this.fibInfo = new FibInfo(start.getHighPriceDoubleValue(), end.getLowPriceDoubleValue(), start.getDecimalNum(), FibLevel.LEVEL_1);
		} else if(ps == PositionSide.LONG) {
			start = PriceUtil.getMinPriceKLine(firstSubList);
			startAfterFlag = PriceUtil.getAfterKlines(start, firstSubList);
			if(startAfterFlag == null) {
				startAfterFlag = start;
			}
			secondSubList = PriceUtil.subList(startAfterFlag, third, list);
			end = PriceUtil.getMaxPriceKLine(secondSubList);
			this.fibInfo = new FibInfo(start.getLowPriceDoubleValue(), end.getHighPriceDoubleValue(), start.getDecimalNum(), FibLevel.LEVEL_1);
		}
		
		if(this.fibInfo == null) {
			return;
		}

		QuotationMode mode = this.fibInfo.getQuotationMode();
		
		Klines macdFirst = null;
		Klines macdSecond = null;
		Klines macdThird = null;
		Klines macdFouth = null;
		
		//判断行情走势
		for(int index = list.size() - 1; index > 0; index--) {
			Klines current = list.get(index);
			if(mode == QuotationMode.LONG) { // high - low - high - low
				if(macdFouth == null) {
					if(verifyHighMacd(current)) {
						macdFouth = current;
					}
				} else if(macdThird == null) {
					if(verifyLowMacd(current)) {
						macdThird = current;
					}
				} else if(macdSecond == null) {
					if(verifyHighMacd(current)) {
						macdSecond = current;
					}
				} else if(macdFirst == null) {
					if(verifyLowMacd(current)) {
						macdFirst = current;
						break;
					}
				}
			} else { // low - high - low - high
				if(macdFouth == null) {
					if(verifyLowMacd(current)) {
						macdFouth = current;
					}
				} else if(macdThird == null) {
					if(verifyHighMacd(current)) {
						macdThird = current;
					}
				} else if(macdSecond == null) {
					if(verifyLowMacd(current)) {
						macdSecond = current;
					}
				} else if(macdFirst == null) {
					if(verifyHighMacd(current)) {
						macdFirst = current;
						break;
					}
				}
			}
		}
		
		if(macdFirst == null || macdSecond == null || macdThird == null || macdFouth == null) {
			return;
		}
		
		List<Klines> firstMacdSubList = PriceUtil.subList(macdFirst, macdSecond, list);
		List<Klines> secondMacdSubList = PriceUtil.subList(macdThird, macdFouth, list);
		if(mode == QuotationMode.LONG) {
			firstPoint = PriceUtil.getMaxPriceKLine(firstMacdSubList);
			secondPoint = PriceUtil.getMaxPriceKLine(secondMacdSubList);
		} else {
			firstPoint = PriceUtil.getMinPriceKLine(firstMacdSubList);
			secondPoint = PriceUtil.getMinPriceKLine(secondMacdSubList);
		}
		
		if(firstPoint == null || secondPoint == null) {
			return;
		}
		
		//获取开仓点位
		
		Klines fibAfterFlag = PriceUtil.getAfterKlines(end, this.list);
		
		if(fibAfterFlag != null) {
			this.fibAfterKlines.addAll(PriceUtil.subList(fibAfterFlag, this.list));
		}
		
		Klines fibEnd = null;
		for(int index = fibAfterKlines.size() - 1; index > 1; index--) {
			Klines current = fibAfterKlines.get(index);
			Klines parent = fibAfterKlines.get(index - 1);
			Klines next = fibAfterKlines.get(index - 2);
			if((mode == QuotationMode.LONG && PriceUtil.verifyPowerful_v10(current, parent, next)) 
					|| (mode == QuotationMode.SHORT && PriceUtil.verifyDecliningPrice_v10(current, parent, next))) {
				fibEnd = current;
				break;
			}
		}
		
		if(fibEnd != null) {
			FibCode openCode = FibCode.FIB0;
			List<Klines> points_sub_list = PriceUtil.subList(fibAfterFlag, fibEnd, fibAfterKlines);
			MarketSentiment ms = new MarketSentiment(points_sub_list);
			if(ms.isNotEmpty()) {
				if(mode == QuotationMode.LONG) {
					Klines low = ms.getLow();
					Klines lowBody = ms.getLowBody();
					openCode = this.fibInfo.getFibCode(low.getLowPriceDoubleValue());
					if(openCode.gte(FibCode.FIB1) || isAth()) {
						addPrices(new OpenPriceDetails(openCode, low.getLowPriceDoubleValue()));
						addPrices(new OpenPriceDetails(openCode, lowBody.getBodyLowPriceDoubleValue()));
					}
				} else {
					Klines high = ms.getHigh();
					Klines highBody = ms.getHighBody();
					openCode = this.fibInfo.getFibCode(high.getHighPriceDoubleValue());
					
					if(openCode.gte(FibCode.FIB1) || isAtl()) {
						addPrices(new OpenPriceDetails(openCode, high.getHighPriceDoubleValue()));
						addPrices(new OpenPriceDetails(openCode, highBody.getBodyHighPriceDoubleValue()));
					}
				}
			}
			
			this.fibAfterKlines.clear();

			fibAfterFlag = PriceUtil.getAfterKlines(fibEnd, this.list_15m);
			
			if(fibAfterFlag != null) {
				
				this.fibAfterKlines.addAll(PriceUtil.subList(fibAfterFlag, this.list_15m));
				this.fibInfo.setFibAfterKlines(fibAfterKlines);
			}
			
		}

		if(mode == QuotationMode.LONG) {
			this.openPrices.sort(new PriceComparator(SortType.DESC));
		} else {
			this.openPrices.sort(new PriceComparator(SortType.ASC));
		}
		
		
		this.resetFibLevel();
	}
	
	private PositionSide getPositionSide() {
		PositionSide ps = PositionSide.DEFAULT;
		Klines last = PriceUtil.getLastKlines(list);
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
		return k.getEma7() > k.getEma25() && k.getEma25() > 0 && k.getMacd() > 0;
	}
	
	private boolean verifyLow(Klines k) {
		return k.getEma7() < k.getEma25() && k.getEma25() > 0 && k.getMacd() < 0;
	}
	
	private boolean verifyHighMacd(Klines k) {
		return k.getMacd() > 0;
	}
	
	private boolean verifyLowMacd(Klines k) {
		return k.getMacd() < 0;
	}
	
	private FibCode getFibCode() {
		FibCode result = FibCode.FIB1;
		if(this.fibInfo != null) {
			
			if(!CollectionUtils.isEmpty(openPrices)) {
				double price = openPrices.get(0).getPrice();
				result = this.fibInfo.getFibCode(price);
			}
			
			if(result == FibCode.FIB66) {
				result = FibCode.FIB618;
			} else if(result == FibCode.FIB0) {
				result = FibCode.FIB236;
			}
		}
		return result;
	}
	
	private void resetFibLevel() {
		if(this.fibInfo != null) {
			FibCode levelFibCode = getFibCode();
			FibLevel level = FibLevel.valueOf(levelFibCode);
			this.fibInfo = new FibInfo(this.fibInfo.getFibValue(FibCode.FIB1), this.fibInfo.getFibValue(FibCode.FIB0), this.fibInfo.getDecimalPoint(), level);
			this.fibInfo.setFibAfterKlines(fibAfterKlines);
		}
	}
	
	private void addPrices(OpenPrice price) {
		if(!PriceUtil.contains(openPrices, price)) {
			openPrices.add(price);
		}
	}
	
	/**
	 * 是否出现更高的高点
	 * @return
	 */
	private boolean isAth() {
		return !(firstPoint == null || secondPoint == null) && secondPoint.getHighPriceDoubleValue() >= firstPoint.getHighPriceDoubleValue();
	}
	
	/**
	 * 是否出现更低的低点
	 * @return
	 */
	private boolean isAtl() {
		return !(firstPoint == null || secondPoint == null) && secondPoint.getLowPriceDoubleValue() <= firstPoint.getLowPriceDoubleValue();
	}
}
