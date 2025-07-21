package com.util;

import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.util.CollectionUtils;

import com.bugbycode.module.FibCode;
import com.bugbycode.module.FibInfo;
import com.bugbycode.module.FibLevel;
import com.bugbycode.module.Klines;
import com.bugbycode.module.MarketSentiment;
import com.bugbycode.module.QuotationMode;
import com.bugbycode.module.SortType;
import com.bugbycode.module.trading.PositionSide;

/**
 * 价格行为指标
 */
public class PriceActionFactory {
	
	private final Logger logger = LogManager.getLogger(PriceActionFactory.class);

	private List<Klines> list;
	
	private FibInfo fibInfo;
	
	private List<Klines> fibAfterKlines;
	
	private List<Double> openPrices;
	
	private Klines start = null;
	
	private Klines end = null;
	
	public PriceActionFactory(List<Klines> list) {
		this.list = new ArrayList<Klines>();
		this.openPrices = new ArrayList<Double>();
		this.fibAfterKlines = new ArrayList<Klines>();
		if(!CollectionUtils.isEmpty(list)) {
			this.list.addAll(list);
			this.init(PositionSide.DEFAULT);
		}
	}
	
	private void init(PositionSide loadPs) {
		if(CollectionUtils.isEmpty(list) || list.size() < 99) {
			return;
		}
		
		this.fibAfterKlines.clear();
		
		KlinesComparator kc = new KlinesComparator(SortType.ASC);
		this.list.sort(kc);
		
		PriceUtil.calculateEMA_7_25_99(list);
		PriceUtil.calculateMACD(list);
		
		PositionSide ps = PositionSide.DEFAULT;
		if(loadPs == PositionSide.DEFAULT) {
			ps = getPositionSide();
		}
		
		Klines first = null;
		Klines second = null;
		Klines third = null;
		
		for(int index = list.size() - 1; index > 0; index--) {
			Klines current = list.get(index);
			if(ps == PositionSide.LONG) {//low - high - low
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
				secondSubList = PriceUtil.subList(startAfter, list);
				end = PriceUtil.getMinPriceKLine(secondSubList);
				fibInfo = new FibInfo(start.getHighPriceDoubleValue(), end.getLowPriceDoubleValue(), start.getDecimalNum(), FibLevel.LEVEL_1);
			}
		} else if(ps == PositionSide.SHORT) {
			start = PriceUtil.getMinPriceKLine(firstSubList);
			startAfter = PriceUtil.getAfterKlines(start, list);
			if(startAfter != null) {
				secondSubList = PriceUtil.subList(start, list);
				end = PriceUtil.getMaxPriceKLine(secondSubList);
				fibInfo = new FibInfo(start.getLowPriceDoubleValue(), end.getHighPriceDoubleValue(), start.getDecimalNum(), FibLevel.LEVEL_1);
			}
		}
		
		if(fibInfo == null) {
			return;
		}
		
		Klines fibAfterFlag = PriceUtil.getAfterKlines(end, secondSubList);
		if(fibAfterFlag != null) {
			this.fibAfterKlines.addAll(PriceUtil.subList(fibAfterFlag, list));
		}
		
		List<Klines> sub_for_start_list = PriceUtil.subList(start, list);
		List<MarketSentiment> msList = new ArrayList<MarketSentiment>();
		QuotationMode mode = fibInfo.getQuotationMode();
		if(!CollectionUtils.isEmpty(sub_for_start_list)) {
			for(int index = sub_for_start_list.size() - 1; index > 2; index--) {
				Klines current = sub_for_start_list.get(index);
				Klines parent = sub_for_start_list.get(index - 1);
				Klines next = sub_for_start_list.get(index - 2);
				if((mode == QuotationMode.LONG && PriceUtil.verifyDecliningPrice_v10(current, parent, next))
						|| (mode == QuotationMode.SHORT && PriceUtil.verifyPowerful_v10(current, parent, next))
							) {
					msList.add(new MarketSentiment(current));
				}
			}
		}
		
		/*
		//处理市场情绪价格信息 START========================================
		List<MarketSentiment> msList = new ArrayList<MarketSentiment>();
		QuotationMode mode = fibInfo.getQuotationMode();
		List<Klines> fibSubList = PriceUtil.subList(start, list);
		for(int index = fibSubList.size() - 1; index > 3; index--) {
			Klines last = fibSubList.get(index);
			Klines k0 = fibSubList.get(index - 1);
			Klines k1 = fibSubList.get(index - 2);
			Klines k2 = fibSubList.get(index - 3);
			if((mode == QuotationMode.LONG && PriceUtil.isGreedyBuy(last, k0, k1, k2)) //寻找疯狂购买的市场情绪
				|| (mode == QuotationMode.SHORT && PriceUtil.isPanicSell(last, k0, k1, k2)) //寻找恐慌抛售的市场情绪
					) {
				msList.add(getMarketSentiment(last, k0, k1, k2));
			}
		}
		
		//处理市场情绪价格信息 END ========================================
		
		Klines release = null;
		//处理放量上涨或放量下跌价格信息 START ==============================
		for(int index = list.size() - 1;index > 1; index--) {
			Klines k0 = list.get(index);
			Klines k1 = list.get(index - 1);
			if(k0.lt(start)) {
				break;
			}
			if(mode == QuotationMode.LONG) {//寻找放量上涨
				if(PriceUtil.isRise_v3(k0, k1) && PriceUtil.isRelease(k0, k1)) { //上涨
					if(release == null || release.getHighPriceDoubleValue() < k0.getHighPriceDoubleValue()) {
						release = k0;
					}
				}
			} else if(mode == QuotationMode.SHORT) {//寻找放量下跌
				if(PriceUtil.isFall_v3(k0, k1) && PriceUtil.isRelease(k0, k1)) {//下跌
					if(release == null || release.getLowPriceDoubleValue() > k0.getLowPriceDoubleValue()) {
						release = k0;
					}
				}
			}
		}
		//logger.info(release);
		if(release != null) {
			MarketSentiment releaseMs = new MarketSentiment(release);
			msList.add(releaseMs);
		}
		//处理放量上涨或放量下跌价格信息 END ==============================
		*/
		//开始处理开仓点位
		MarketSentiment high = PriceUtil.getMaxMarketSentiment(msList);
		MarketSentiment low = PriceUtil.getMinMarketSentiment(msList);
		
		if(mode == QuotationMode.LONG && high != null /*&& !last_1h.isEquals(high.getHigh())*/) {//高点做空
			/*
			addPrices(high.getHighPrice());
			addPrices(high.getBodyHighPrice());
			addPrices(high.getBodyLowPrice());
			*/
			addPrices(high.getBodyLowPrice());
			this.openPrices.sort(new PriceComparator(SortType.ASC));
			
		} else if(mode == QuotationMode.SHORT && low != null /*&& !last_1h.isEquals(low.getLow())*/){//低点做多
			/*addPrices(low.getLowPrice());
			addPrices(low.getBodyLowPrice());
			addPrices(low.getBodyHighPrice());*/
			
			addPrices(low.getBodyHighPrice());
			this.openPrices.sort(new PriceComparator(SortType.DESC));
		}
		
		logger.debug(this.openPrices);
		
		if(loadPs == PositionSide.DEFAULT) {
			if(mode == QuotationMode.SHORT && !isLong()) {
				init(PositionSide.SHORT);
			} else if(mode == QuotationMode.LONG && !isShort()) {
				init(PositionSide.LONG);
			}
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
	
	private boolean verifyShort(Klines k) {
		return k.getEma7() < k.getEma25() && k.getEma25() > 0;
	}
	
	private boolean verifyLong(Klines k) {
		return k.getEma7() > k.getEma25() && k.getEma25() > 0;
	}
	
	private boolean verifyHigh(Klines k) {
		return k.getEma7() > k.getEma25() && k.getEma25() > 0;
	}
	
	private boolean verifyLow(Klines k) {
		return k.getEma7() < k.getEma25() && k.getEma25() > 0;
	}
	
	public boolean verifyOpen(List<Klines> list) {
		boolean result = false;
		if(!(CollectionUtils.isEmpty(list) || fibInfo == null)) {
			Klines last = PriceUtil.getLastKlines(list);
			double closePrice = last.getClosePriceDoubleValue();
			double fibPrice = fibInfo.getFibValue(FibCode.FIB382);
			QuotationMode mode = fibInfo.getQuotationMode();
			for(int index = 0; index < openPrices.size(); index++) {
				double price = openPrices.get(index);
				if(mode == QuotationMode.SHORT && PriceUtil.isLong_v2(price, list) && closePrice < fibPrice) {
					Klines afterLowKlines  = PriceUtil.getMinPriceKLine(fibAfterKlines);
					if(!PriceUtil.isObsoleteLong(afterLowKlines, openPrices, index)) {
						result = true;
					}
				} else if(mode == QuotationMode.LONG && PriceUtil.isShort_v2(price, list) && closePrice > fibPrice) {
					Klines afterHighKlines  = PriceUtil.getMaxPriceKLine(fibAfterKlines);
					if(!PriceUtil.isObsoleteShort(afterHighKlines, openPrices, index)) {
						result = true;
					}
				}
			}
		}
		return result;
	}
	
	public FibInfo getFibInfo() {
		return this.fibInfo;
	}
	
	public void addPrices(double price) {
		if(!PriceUtil.contains(openPrices, price)) {
			openPrices.add(price);
		}
	}

	public List<Double> getOpenPrices() {
		return openPrices;
	}

	public List<Klines> getFibAfterKlines() {
		return fibAfterKlines;
	}
	
	public MarketSentiment getMarketSentiment(Klines last, Klines k0, Klines k1, Klines k2) {
		List<Klines> data = new ArrayList<>();
		data.add(last);
		data.add(k0);
		data.add(k1);
		data.add(k2);
		return new MarketSentiment(data);
	}
	
	public boolean isLong() {
		return fibInfo != null && fibInfo.getQuotationMode() == QuotationMode.SHORT && start.getEma99() < end.getEma99();
	}
	
	public boolean isShort() {
		return fibInfo != null && fibInfo.getQuotationMode() == QuotationMode.LONG && start.getEma99() > end.getEma99();
	}
}