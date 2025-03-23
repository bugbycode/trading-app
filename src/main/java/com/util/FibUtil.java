package com.util;

import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.util.CollectionUtils;

import com.bugbycode.module.EMAType;
import com.bugbycode.module.FibInfo;
import com.bugbycode.module.FibLevel;
import com.bugbycode.module.Klines;
import com.bugbycode.module.SortType;
import com.bugbycode.module.trading.PositionSide;

/**
 * 斐波那契回撤计算工具类
 */
public class FibUtil {

	private final Logger logger = LogManager.getLogger(FibUtil.class);
	
	private List<Klines> list;
	
	private Klines afterFlag;
	
	private Klines fibStartKlines;
	
	private Klines fibEndKlines;

	public FibUtil(List<Klines> list) {
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
				fibStartKlines = PriceUtil.getMinPriceKLine(fisrtSubList);
				//终端（高点）
				fibEndKlines = PriceUtil.getMaxPriceKLine(PriceUtil.subList(fibStartKlines, last, list));
				
				fib = new FibInfo(fibStartKlines.getLowPriceDoubleValue(), fibEndKlines.getHighPriceDoubleValue(), last.getDecimalNum(), getFibLevel(fibStartKlines, fibEndKlines));
				
				this.afterFlag = fibEndKlines;
				
			} else 
			//做空情况
			if(ps == PositionSide.SHORT) {
				//寻找斐波那契回撤起始点（高点）
				fibStartKlines = PriceUtil.getMaxPriceKLine(fisrtSubList);
				//终端（低点）
				fibEndKlines = PriceUtil.getMinPriceKLine(PriceUtil.subList(fibStartKlines, last, list));
				
				fib = new FibInfo(fibStartKlines.getHighPriceDoubleValue(), fibEndKlines.getLowPriceDoubleValue(), last.getDecimalNum(), getFibLevel(fibStartKlines, fibEndKlines));
				
				this.afterFlag = fibEndKlines;
				
			}
		}
		
		return fib;
	}
	
	/**
	 * 获取斐波那契回撤之后的所有k线信息
	 * 
	 * @return
	 */
	public List<Klines> getFibAfterKlines() {
		List<Klines> afterList = new ArrayList<Klines>();
		if(this.afterFlag != null) {
			afterList = PriceUtil.subList(afterFlag, list);
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
		return ema7 > ema25;
	}
	
	/**
	 * 校验低点
	 * @param k
	 * @return
	 */
	private boolean verifyLow(Klines k) {
		double ema7 = k.getEma7();
		double ema25 = k.getEma25();
		return ema7 < ema25;
	}
	
	private FibLevel getFibLevel(Klines fibStartKlines, Klines fibEndKlines) {
		
		FibLevel level = FibLevel.LEVEL_1;
		
		double start_ema25 = fibStartKlines.getEma25();
		double start_ema99 = fibStartKlines.getEma99();
		double end_ema25 = fibStartKlines.getEma25();
		double end_ema99 = fibStartKlines.getEma99();
		
		if((start_ema25 < start_ema99 && end_ema25 > end_ema99) || (start_ema25 > start_ema99 && end_ema25 < end_ema99)) {
			level = FibLevel.LEVEL_1;
		} else if(start_ema25 > start_ema99 && end_ema25 > end_ema99) {
			level = FibLevel.LEVEL_2;
		} else if(start_ema25 < start_ema99 && end_ema25 < end_ema99) {
			level = FibLevel.LEVEL_3;
		}
		
		return level;
	}

	public Klines getFibStartKlines() {
		return fibStartKlines;
	}

	public Klines getFibEndKlines() {
		return fibEndKlines;
	}
	
}
