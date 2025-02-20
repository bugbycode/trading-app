package com.util;

import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.util.CollectionUtils;

import com.bugbycode.module.EMAType;
import com.bugbycode.module.FibCode;
import com.bugbycode.module.FibInfo;
import com.bugbycode.module.FibLevel;
import com.bugbycode.module.Klines;
import com.bugbycode.module.QuotationMode;
import com.bugbycode.module.SortType;
import com.bugbycode.module.trading.PositionSide;

/**
 * 斐波那契回撤计算工具类V3
 */
public class FibUtil_v3 {

	private final Logger logger = LogManager.getLogger(FibUtil_v3.class);
	
	private List<Klines> list;
	
	private Klines afterFlag;
	
	private Klines secondFibAfterFlag;
	
	private Klines thirdFibAfterFlag;

	public FibUtil_v3(List<Klines> list) {
		this.list = new ArrayList<Klines>();
		this.list.addAll(list);
	}
	
	/**
	 * 获取斐波那契回撤信息
	 * 
	 * @return
	 */
	public FibInfo getFibInfo() {
		KlinesComparator kc = new KlinesComparator(SortType.ASC);
		this.list.sort(kc);
		
		PriceUtil.calculateEMAArray(this.list, EMAType.EMA7);
		PriceUtil.calculateEMAArray(this.list, EMAType.EMA25);
		PriceUtil.calculateEMAArray(this.list, EMAType.EMA99);
		
		FibInfo fib = null;
		
		if(CollectionUtils.isEmpty(this.list)) {
			throw new RuntimeException("Klines data is empty.");
		}
		
		Klines last = PriceUtil.getLastKlines(list);
		
		PositionSide ps = getPositionSide(last);
		
		//标志性k线
		Klines firstFlag = null;
		Klines secondFlag = null;
		Klines thirdFlag = null;
		
		for(int index = this.list.size() - 1;index >= 0;index--) {
			if(index - 4 < 0) {
				break;
			}
			Klines k = this.list.get(index);
			/*Klines k1 = this.list.get(index - 1);
			Klines k2 = this.list.get(index - 2);
			Klines k3 = this.list.get(index - 3);
			Klines k4 = this.list.get(index - 4);*/
			//做多情况
			if(ps == PositionSide.LONG) {
				//先寻找第一个标志性k线
				if(firstFlag == null) {
					if(verifyHigh(k) /*&& verifyHigh(k1) && verifyHigh(k2)
							&& verifyHigh(k3) && verifyHigh(k4)*/) {
						firstFlag = k;
						logger.debug(firstFlag);
					}
				} else if(secondFlag == null) {//寻找第二个标志性k线
					if(verifyLow(k) /*&& verifyLow(k1) && verifyLow(k2)
							&& verifyLow(k3) && verifyLow(k4)*/) {
						secondFlag = k;
						logger.debug(secondFlag);
					}
				} else if(thirdFlag == null) {//寻找第三个标志性k线
					if(verifyHigh(k) /*&& verifyHigh(k1) && verifyHigh(k2)
							&& verifyHigh(k3) && verifyHigh(k4)*/) {
						thirdFlag = k;
						logger.debug(thirdFlag);
					}
				}
				
				if(thirdFlag != null) {
					break;
				}
				
			} else 
			//做空情况
			if(ps == PositionSide.SHORT) {
				//先寻找第一个标志性k线
				if(firstFlag == null) {
					if(verifyLow(k) /*&& verifyLow(k1) && verifyLow(k2)
							&& verifyLow(k3) && verifyLow(k4)*/) {
						firstFlag = k;
						logger.debug(firstFlag);
					}
				} else if(secondFlag == null) {//寻找第二个标志性k线
					if(verifyHigh(k) /*&& verifyHigh(k1) && verifyHigh(k2)
							&& verifyHigh(k3) && verifyHigh(k4)*/) {
						secondFlag = k;
						logger.debug(secondFlag);
					}
				} else if(thirdFlag == null) {//寻找第三个标志性k线
					if(verifyLow(k) /*&& verifyLow(k1) && verifyLow(k2)
							&& verifyLow(k3) && verifyLow(k4)*/) {
						thirdFlag = k;
						logger.debug(thirdFlag);
					}
				}
				
				if(thirdFlag != null) {
					break;
				}
			}
		}
		
		if(thirdFlag != null) {
			List<Klines> fisrtSubList = PriceUtil.subList(thirdFlag, firstFlag, list);
			//做多情况
			if(ps == PositionSide.LONG) {
				//寻找斐波那契回撤起始点（低点）
				Klines fibStartKlines = PriceUtil.getMinPriceKLine(fisrtSubList);
				//终端（高点）
				Klines fibEndKlines = PriceUtil.getMaxPriceKLine(PriceUtil.subList(fibStartKlines, last, list));
				fib = new FibInfo(fibStartKlines.getLowPriceDoubleValue(), fibEndKlines.getHighPriceDoubleValue(), last.getDecimalNum(), FibLevel.LEVEL_1);
			} else 
			//做空情况
			if(ps == PositionSide.SHORT) {
				//寻找斐波那契回撤起始点（高点）
				Klines fibStartKlines = PriceUtil.getMaxPriceKLine(fisrtSubList);
				//终端（低点）
				Klines fibEndKlines = PriceUtil.getMinPriceKLine(PriceUtil.subList(fibStartKlines, last, list));
				fib = new FibInfo(fibStartKlines.getHighPriceDoubleValue(), fibEndKlines.getLowPriceDoubleValue(), last.getDecimalNum(), FibLevel.LEVEL_1);
			}
		}
		
		this.afterFlag = firstFlag;
		
		return fib;
	}
	
	/**
	 * 获取二级斐波那契回撤信息
	 * @return
	 */
	public FibInfo getSecondFibInfo(FibInfo firstFibInfo) {
		FibInfo fibInfo = null;
		if(firstFibInfo != null) {
			
			double startPrice = firstFibInfo.getFibValue(FibCode.FIB0);
			double endPrice = 0;
			List<Klines> fibAfterKlines = getFibAfterKlines();
			//一级斐波那契回撤行情模式 LONG/SHORT
			QuotationMode qm = firstFibInfo.getQuotationMode();
			if(qm == QuotationMode.LONG) {//多头 找低点
				Klines lowKlines = PriceUtil.getMinPriceKLine(fibAfterKlines);
				endPrice = lowKlines.getLowPriceDoubleValue();
				secondFibAfterFlag = lowKlines;
			} else {//空头 找高点
				Klines highKlines = PriceUtil.getMaxPriceKLine(fibAfterKlines);
				endPrice = highKlines.getHighPriceDoubleValue();
				secondFibAfterFlag = highKlines;
			}
			
			fibInfo = new FibInfo(startPrice, endPrice, firstFibInfo.getDecimalPoint(), FibLevel.LEVEL_1);
		}
		return fibInfo;
	}
	
	/**
	 * 获取第三级斐波那契回撤信息
	 * @param secondFibInfo
	 * @return
	 */
	public FibInfo getThirdFibInfo(FibInfo secondFibInfo) {
		FibInfo fibInfo = null;
		if(secondFibInfo != null) {
			double startPrice = secondFibInfo.getFibValue(FibCode.FIB0);
			double endPrice = 0;
			List<Klines> fibAfterKlines = getSecondFibAfterKlines();
			//二级斐波那契回撤行情模式 LONG/SHORT
			QuotationMode qm = secondFibInfo.getQuotationMode();
			if(qm == QuotationMode.LONG) {//多头 找低点
				Klines lowKlines = PriceUtil.getMinPriceKLine(fibAfterKlines);
				endPrice = lowKlines.getLowPriceDoubleValue();
				thirdFibAfterFlag = lowKlines;
			} else {//空头 找高点
				Klines highKlines = PriceUtil.getMaxPriceKLine(fibAfterKlines);
				endPrice = highKlines.getHighPriceDoubleValue();
				thirdFibAfterFlag = highKlines;
			}
			
			fibInfo = new FibInfo(startPrice, endPrice, secondFibInfo.getDecimalPoint(), FibLevel.LEVEL_1);
		}
		return fibInfo;
	}
	
	/**
	 * 获取斐波那契回撤之后的所有k线信息
	 * 
	 * @return
	 */
	public List<Klines> getFibAfterKlines() {
		List<Klines> afterList = new ArrayList<Klines>();
		if(this.afterFlag != null) {
			Klines last = PriceUtil.getAfterKlines(afterFlag, list);
			if(last != null) {
				afterList = PriceUtil.subList(last, list);
			}
		}
		return afterList;
	}
	
	/**
	 * 获取第二级斐波那契回撤之后的所有k线信息
	 * 
	 * @return
	 */
	public List<Klines> getSecondFibAfterKlines() {
		List<Klines> afterList = new ArrayList<Klines>();
		if(this.secondFibAfterFlag != null) {
			Klines last = PriceUtil.getAfterKlines(secondFibAfterFlag, list);
			if(last != null) {
				afterList = PriceUtil.subList(last, list);
			}
		}
		return afterList;
	}
	
	/**
	 * 获取第三级斐波那契回撤之后的所有k线信息
	 * 
	 * @return
	 */
	public List<Klines> getThirdFibAfterKlines() {
		List<Klines> afterList = new ArrayList<Klines>();
		if(this.thirdFibAfterFlag != null) {
			Klines last = PriceUtil.getAfterKlines(thirdFibAfterFlag, list);
			if(last != null) {
				afterList = PriceUtil.subList(last, list);
			}
		}
		return afterList;
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
		double ema7 = k.getEma7();
		double ema25 = k.getEma25();
		return ema7 < ema25;
	}
	
	/**
	 * 判断做空
	 * @param k
	 * @return
	 */
	private boolean verifyShort(Klines k) {
		double ema7 = k.getEma7();
		double ema25 = k.getEma25();
		return ema7 > ema25;
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
	 * 根据一级回撤校验是否可开仓
	 * @param firstFibInfo 一级斐波那契回撤
	 * @param currentPrice 当前价格
	 * @return
	 */
	public boolean verifyFirstFibOpen(FibInfo firstFibInfo,double currentPrice) {
		boolean flag = false;
		if(firstFibInfo != null) {
			double first_fibPrice = firstFibInfo.getFibValue(FibCode.FIB618);
			//double first_fib1Price = firstFibInfo.getFibValue(FibCode.FIB1);
			QuotationMode qm = firstFibInfo.getQuotationMode();
			if(qm == QuotationMode.LONG) {
				flag = currentPrice <= first_fibPrice;
			} else {
				flag = currentPrice >= first_fibPrice;
			}
		}
		return flag;
	}
	
	/**
	 * 根据二级回撤校验是否可开仓
	 * @param secondFibInfo 二级斐波那契回撤
	 * @param currentPrice 当前价格
	 * @return
	 */
	public boolean verifySecondFibOpen(FibInfo secondFibInfo,double currentPrice) {
		boolean flag = false;
		if(secondFibInfo != null) {
			//回撤价格
			double sec_fibPrice = secondFibInfo.getFibValue(FibCode.FIB236);
			QuotationMode qm = secondFibInfo.getQuotationMode();
			if(qm == QuotationMode.LONG) {
				flag = currentPrice >= sec_fibPrice;
			} else {
				flag = currentPrice <= sec_fibPrice;
			}
		}
		return flag;
	}

	public Klines getAfterFlag() {
		return afterFlag;
	}

	public Klines getSecondFibAfterFlag() {
		return secondFibAfterFlag;
	}

	public Klines getThirdFibAfterFlag() {
		return thirdFibAfterFlag;
	}
}
