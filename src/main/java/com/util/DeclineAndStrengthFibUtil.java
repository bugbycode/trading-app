package com.util;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.util.CollectionUtils;

import com.bugbycode.module.FibCode;
import com.bugbycode.module.FibInfo;
import com.bugbycode.module.FibLevel;
import com.bugbycode.module.Klines;
import com.bugbycode.module.QuotationMode;
import com.bugbycode.module.SortType;
import com.bugbycode.module.trading.PositionSide;

/**
 * 价格行为斐波那契回撤工具类
 */
public class DeclineAndStrengthFibUtil {
	
	private final Logger logger = LogManager.getLogger(DeclineAndStrengthFibUtil.class);

	private List<Klines> list;
	
	private FibInfo firstFibInfo;
	
	private FibInfo secondFibInfo;
	
	private List<Klines> secondAfterKlines;
	
	private Klines secondFibEndKlines;
	
	public DeclineAndStrengthFibUtil(List<Klines> list) {
		this.list = new ArrayList<Klines>();
		if(!CollectionUtils.isEmpty(list)) {
			this.list.addAll(list);
			this.init();
		}
	}
	
	public void init() {
		
		if(CollectionUtils.isEmpty(list)) {
			return;
		}
		
		PriceUtil.calculateEMA_7_25_99(list);
		
		this.list.sort(new KlinesComparator(SortType.ASC));
		
		Klines last = PriceUtil.getLastKlines(list);
		
		PositionSide ps = getPositionSide(last);
		
		Klines firstFlag = null;
		Klines secondFlag = null;
		
		for(int index = this.list.size() - 1;index >= 0;index--) {
			Klines k = this.list.get(index);
			if(ps == PositionSide.LONG) {//做多情况
				if(firstFlag == null) {
					if(verifyHigh(k)) {//寻找高点标志位
						firstFlag = k;
					}
				} else if(secondFlag == null) {//寻找低点标志位
					if(verifyLow(k)) {
						secondFlag = k;
					}
				}
			} else if(ps == PositionSide.SHORT) {//做空情况
				if(firstFlag == null) {//寻找低点标志位
					if(verifyLow(k)) {
						firstFlag = k;
					}
				} else if(secondFlag == null) {//寻找高点标志位
					if(verifyHigh(k)) {
						secondFlag = k;
					}
				}
			}
			if(secondFlag != null) {
				break;
			}
		}
		
		if(secondFlag == null) {
			return;
		}
		
		logger.debug(secondFlag);
		logger.debug(firstFlag);
		
		//一级回撤起始点所在的k线区间
		List<Klines> fisrtSubList = PriceUtil.subList(secondFlag, firstFlag, list);
		//一级回撤终点所在的k线区间
		List<Klines> secondSubList = PriceUtil.subList(firstFlag, list);
		//做多的情况
		if(ps == PositionSide.LONG) {
			//确定起始点（高点）
			Klines fibStartKlines = PriceUtil.getMaxPriceKLine(fisrtSubList);
			//确定终点（低点）
			Klines fibEndKlines = PriceUtil.getMinPriceKLine(secondSubList);
			
			this.firstFibInfo = new FibInfo(fibStartKlines.getHighPriceDoubleValue(), fibEndKlines.getLowPriceDoubleValue(), fibStartKlines.getDecimalNum(), FibLevel.LEVEL_1);
			
			//二级终点（高点）所在的k线区间
			List<Klines> endSubList = getAfterKlines(fibEndKlines, list);
			secondFibEndKlines = PriceUtil.getMaxPriceKLine(endSubList);
			if(secondFibEndKlines != null) {
				this.secondFibInfo = new FibInfo(fibEndKlines.getLowPriceDoubleValue(), secondFibEndKlines.getHighPriceDoubleValue(), fibStartKlines.getDecimalNum(), FibLevel.LEVEL_2);
				this.secondAfterKlines = getAfterKlines(secondFibEndKlines, list);
			}
			
		} else if(ps == PositionSide.SHORT) {//做空的情况
			//确定起始点（低点）
			Klines fibStartKlines = PriceUtil.getMinPriceKLine(fisrtSubList);
			//确定终点（高点）
			Klines fibEndKlines = PriceUtil.getMaxPriceKLine(secondSubList);
			
			this.firstFibInfo = new FibInfo(fibStartKlines.getLowPriceDoubleValue(), fibEndKlines.getHighPriceDoubleValue(), fibStartKlines.getDecimalNum(), FibLevel.LEVEL_1);
			
			//二级终点（低点）所在的k线区间
			List<Klines> endSubList = getAfterKlines(fibEndKlines, list);
			secondFibEndKlines = PriceUtil.getMinPriceKLine(endSubList);
			if(secondFibEndKlines != null) {
				this.secondFibInfo = new FibInfo(fibEndKlines.getHighPriceDoubleValue(), secondFibEndKlines.getLowPriceDoubleValue(), fibStartKlines.getDecimalNum(), FibLevel.LEVEL_2);
				this.secondAfterKlines = getAfterKlines(secondFibEndKlines, list);
			}
		}
	}
	
	public FibInfo getFirstFibInfo() {
		return firstFibInfo;
	}

	public FibInfo getSecondFibInfo() {
		return secondFibInfo;
	}
	
	/**
	 * 截取某根k线之后的所有k线信息并返回
	 * @param k
	 * @param data 
	 * @return
	 */
	public List<Klines> getAfterKlines(Klines k, List<Klines> data) {
		List<Klines> afterList = new ArrayList<Klines>();
		if(k != null) {
			Klines last = PriceUtil.getAfterKlines(k, data);
			if(last != null) {
				afterList = PriceUtil.subList(last, data);
			}
		}
		return afterList;
	}
	
	public List<Klines> getSecondAfterKlines() {
		return this.secondAfterKlines;
	}

	private PositionSide getPositionSide(Klines k) {
		PositionSide ps = PositionSide.DEFAULT;
		
		if(verifyShort(k)) {
			ps = PositionSide.SHORT;
		} else if(verifyLong(k)) {
			ps = PositionSide.LONG;
		}
		
		return ps;
	}
	
	/**
	 * 判断做多
	 * @param k
	 * @return
	 */
	private boolean verifyLong(Klines k) {
		double ema99 = k.getEma99();
		double ema25 = k.getEma25();
		return ema25 < ema99;
	}
	
	/**
	 * 判断做空
	 * @param k
	 * @return
	 */
	private boolean verifyShort(Klines k) {
		double ema99 = k.getEma99();
		double ema25 = k.getEma25();
		return ema25 > ema99;
	}
	
	/**
	 * 校验高点
	 * @param k
	 * @return
	 */
	private boolean verifyHigh(Klines k) {
		double ema7 = k.getEma7();
		double ema25 = k.getEma25();
		double ema99 = k.getEma99();
		return ema7 > ema25 && ema25 > ema99;
	}
	
	/**
	 * 校验低点
	 * @param k
	 * @return
	 */
	private boolean verifyLow(Klines k) {
		double ema7 = k.getEma7();
		double ema25 = k.getEma25();
		double ema99 = k.getEma99();
		return ema7 < ema25 && ema25 < ema99;
	}
	
	/**
	 * 校验是否包含ema7小于ema25的k线
	 * @param data
	 * @return
	 */
	private boolean verify_ema7_lt_ema25(List<Klines> data) {
		boolean result = false;
		if(!CollectionUtils.isEmpty(data)) {
			for(Klines k : data) {
				if(k.getEma7() < k.getEma25()) {
					result = true;
					break;
				}
			}
		}
		return result;
	}
	
	/**
	 * 校验是否包含ema7大于ema25的k线
	 * @param data
	 * @return
	 */
	private boolean verify_ema7_gt_ema25(List<Klines> data) {
		boolean result = false;
		if(!CollectionUtils.isEmpty(data)) {
			for(Klines k : data) {
				if(k.getEma7() > k.getEma25()) {
					result = true;
					break;
				}
			}
		}
		return result;
	}
	
	/**
	 * 判断是否可做多
	 * @param list_hit 参考k线
	 * @return
	 */
	public boolean isOpenLong(List<Klines> list_hit) {
		boolean result = false;
		if(!(this.firstFibInfo == null || this.secondFibInfo == null) && this.secondFibInfo.getQuotationMode() == QuotationMode.LONG) {
			
			Klines last = PriceUtil.getLastKlines(list_hit);
			double price = last.getClosePriceDoubleValue();
			
			double first_382 = this.firstFibInfo.getFibValue(FibCode.FIB382);
			
			Date secondAfterStartTime = DateFormatUtil.getStartTimeBySetSecond(DateFormatUtil.parse(DateFormatUtil.format(secondFibEndKlines.getEndTime())), 1);
			
			List<Klines> afterData = PriceUtil.subList(secondAfterStartTime.getTime(), list_hit);
			
			if(price < first_382 && !CollectionUtils.isEmpty(afterData) 
					&& afterData.size() > 4 && verify_ema7_gt_ema25(afterData)) {
				
				Klines afterLowKlines = PriceUtil.getMinPriceKLine(afterData);
				
				FibCode[] codes = FibCode.values();
				
				for(int offset = 0; offset < codes.length; offset++) {
					
					FibCode code = codes[offset];
					
					if(!(code.lte(FibCode.FIB1) && code.gte(FibCode.FIB66))) {
						continue;
					}
					
					if(PriceUtil.isLong_v2(secondFibInfo.getFibValue(code), list_hit)
							&& !PriceUtil.isObsoleteLong(secondFibInfo, afterLowKlines, codes, offset)) {
						result = true;
						break;
					}
				}
			}
			
		}
		return result;
	}
	
	/**
	 * 判断是否可做空
	 * @param list_hit 参考k线
	 * @return
	 */
	public boolean isOpenShort(List<Klines> list_hit) {
		boolean result = false;
		if(!(this.firstFibInfo == null || this.secondFibInfo == null) && this.secondFibInfo.getQuotationMode() == QuotationMode.SHORT) {
			
			Klines last = PriceUtil.getLastKlines(list_hit);
			double price = last.getClosePriceDoubleValue();
			
			double first_382 = this.firstFibInfo.getFibValue(FibCode.FIB382);
			
			Date secondAfterStartTime = DateFormatUtil.getStartTimeBySetSecond(DateFormatUtil.parse(DateFormatUtil.format(secondFibEndKlines.getEndTime())), 1);
			
			List<Klines> afterData = PriceUtil.subList(secondAfterStartTime.getTime(), list_hit);
			
			if(price > first_382 && !CollectionUtils.isEmpty(afterData) 
					&& afterData.size() > 4 && verify_ema7_lt_ema25(afterData)) {
				
				Klines afterHightKlines = PriceUtil.getMaxPriceKLine(afterData);
				
				FibCode[] codes = FibCode.values();
				
				for(int offset = 0; offset < codes.length; offset++) {
					
					FibCode code = codes[offset];
					
					if(!(code.lte(FibCode.FIB1) && code.gte(FibCode.FIB66))) {
						continue;
					}
					
					if(PriceUtil.isShort_v2(secondFibInfo.getFibValue(code), list_hit)
							&& !PriceUtil.isObsoleteShort(secondFibInfo, afterHightKlines, codes, offset)) {
						result = true;
						break;
					}
				}
			}
			
		}
		return result;
	}
}
