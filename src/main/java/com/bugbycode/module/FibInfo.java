package com.bugbycode.module;

import com.util.PriceUtil;

public class FibInfo {

	private double startPrice;
	
	private double endPrice;
	
	private int decimalPoint;
	
	private FibLevel level;
	
	/**
	 * 价格行为止盈点位
	 * @param price 当前价格
	 * @param profit 用户盈利预期
	 * @param profitLimit 用户止盈百分比限制
	 * @return
	 */
	public FibCode getDeclineAndStrengthTakeProfit(double price, double profit, double profitLimit) {
		
		FibCode result = null;
		
		QuotationMode qm = this.getQuotationMode() == QuotationMode.LONG ? QuotationMode.SHORT : QuotationMode.LONG;
		
		double percent_382 = PriceUtil.getPercent(price, this.getFibValue(FibCode.FIB382), qm);
		double percent_5 = PriceUtil.getPercent(price, this.getFibValue(FibCode.FIB5), qm);
		double percent_618 = PriceUtil.getPercent(price, this.getFibValue(FibCode.FIB618), qm);
		
		if(percent_382 >= profit && percent_382 <= profitLimit) {
			result = FibCode.FIB382;
		} else if(percent_5 >= profit && percent_5 <= profitLimit) {
			result = FibCode.FIB5;
		} else if(percent_618 >= profit && percent_618 <= profitLimit) {
			result = FibCode.FIB618;
		}
		
		if(result == null) {
			if(percent_382 >= profit) {
				result = FibCode.FIB382;
			} else if(percent_5 >= profit) {
				result = FibCode.FIB5;
			} else if(percent_618 >= profit) {
				result = FibCode.FIB618;
			} else {
				result = FibCode.FIB5;
			}
		}
		
		return result;
	}
	
	/**
	 * 止盈点位 V3 (保守的交易风格) </br>
	 * 
	 * 1、当【盈利百分比】大于【用户止盈百分比限制】则止盈点位为下一个回撤点位 </br>
	 * 2、当下一个回撤点位止盈时【盈利百分比】小于【用户盈利预期】时则止盈点位保持不变 </br>
	 * 
	 * @param code 开仓时所处的斐波那契回撤点位
	 * @param price 当前价格
	 * @param profit 用户盈利预期
	 * @param profitLimit 用户止盈百分比限制
	 * @return 止盈的斐波那契回撤点位
	 */
	public FibCode getTakeProfit_v3(FibCode code, double price, double profit, double profitLimit) {
		
		FibCode takeProfit = getTakeProfit_v2(code);
		FibCode next = getNextFibCode(code);
		
		double pricePercent = PriceUtil.getPercent(price, takeProfit, this); //价格波动幅度
		double nextPricePercent = PriceUtil.getPercent(price, next, this);;//下一个点位价格波动幅度
		
		if(PriceUtil.checkPercent(pricePercent, nextPricePercent, profit, profitLimit)) {
			takeProfit = next;
		}
		
		return takeProfit;
	}
	
	/**
	 * 止盈点位 V2
	 * @param code 开仓时所处的斐波那契回撤点位
	 * @return 止盈的斐波那契回撤点位
	 */
	public FibCode getTakeProfit_v2(FibCode code) {
		FibCode takeProfit = FibCode.FIB0;
		if(code == FibCode.FIB4_618) { // 4.618 - 2.618
			takeProfit = FibCode.FIB2_618;
		} else if(code == FibCode.FIB2_618) { // 2.618 - 1.618
			takeProfit = FibCode.FIB1_618;
		} else if(code == FibCode.FIB2 || code == FibCode.FIB1_618) { // 2/1.618 - 1
			takeProfit = FibCode.FIB1;
		} else if(code == FibCode.FIB1) { // 1 -> 0.5
			takeProfit = FibCode.FIB5;
		} else if(code == FibCode.FIB786) { // 0.786 -> 0.382
			takeProfit = FibCode.FIB382;
		} else if(code == FibCode.FIB618 || code == FibCode.FIB66) { // 0.618 -> 0.382
			takeProfit = FibCode.FIB382;
		} else if(code == FibCode.FIB5) { // 0.5 -> 0.236
			takeProfit = FibCode.FIB236;
		} else if(code == FibCode.FIB382) { // 0.382 -> 0.236
			takeProfit = FibCode.FIB236;
		}
		return takeProfit;
	}
	
	/**
	 * 
	 * @param fib1 斐波那契回撤起始价
	 * @param fib0 斐波那契回撤结束价
	 * @param decimalPoint
	 * @param level 斐波那契回撤级别 lv1 lv2 lv3 ……
	 */
	public FibInfo(double fib1,double fib0,int decimalPoint,FibLevel level) {
		this.startPrice = fib1;
		this.endPrice = fib0;
		this.decimalPoint = decimalPoint;
		this.level = level;
	}
	
	/**
	 * 
	 * @param kline1 最低价k线
	 * @param kline2 最高价k线
	 * @param decimalPoint 保留小数点个数
	 * @param level 斐波那契回撤级别 lv1 lv2 lv3 ……
	 */
	public FibInfo(Klines kline1, Klines kline2,int decimalPoint,FibLevel level) {
		if(kline1.getStartTime() < kline2.getStartTime()) {
			this.startPrice = Double.valueOf(kline1.getLowPrice());
			this.endPrice = Double.valueOf(kline2.getHighPrice());
		} else {
			this.startPrice = Double.valueOf(kline2.getHighPrice());
			this.endPrice = Double.valueOf(kline1.getLowPrice());
		}
		this.decimalPoint = decimalPoint;
		this.level = level;
	}

	public double getEndPrice() {
		return endPrice;
	}

	public double getStartPrice() {
		return startPrice;
	}
	
	public double getFibValue(FibCode code) {
		return calculateFibonacciRetracement(endPrice,startPrice,code.getValue());
	}
	
	public double getFibValue(double fibValue) {
		return calculateFibonacciRetracement(endPrice,startPrice,fibValue);
	}
	
	public int getDecimalPoint() {
		return decimalPoint;
	}

	/**
	 * 计算斐波那契回撤 第一个参数小于第二个参数时表示从高到低计算，反之从低到高
	 * @param endPrice 起始价
	 * @param startPrice 最终价
	 * @param fibonacciLevel
	 * @return
	 */
    public double calculateFibonacciRetracement(double endPrice, double startPrice, double fibonacciLevel) {
        /*
    	if (fibonacciLevel < FibCode.FIB0.getValue() || fibonacciLevel > FibCode.FIB4_764.getValue()) {
            throw new IllegalArgumentException("参数不合法。");
        }*/

        // 计算斐波那契水平（从高到低）
        //return endPrice + fibonacciLevel * range;
        // 计算斐波那契水平（从低到高）
        //return startPrice - fibonacciLevel * range;
        if(endPrice > startPrice) {
        	// 计算价格范围
            double range = endPrice - startPrice;
        	return endPrice - fibonacciLevel * range;
        } else {
        	// 计算价格范围
            double range = startPrice - endPrice;
        	return endPrice + fibonacciLevel * range;
        }
    }
    
    public QuotationMode getQuotationMode() {
    	return getFibValue(FibCode.FIB0) < getFibValue(FibCode.FIB1) ? QuotationMode.SHORT : QuotationMode.LONG;
    }

	public FibLevel getLevel() {
		return level;
	}
	
	/**
	 * 校验点位是否可开仓
	 * @param code 当前回撤点
	 * @return
	 */
	public boolean verifyOpenFibCode(FibCode code) {
		QuotationMode mode = this.getQuotationMode();
		boolean result = false;
		if(level == FibLevel.LEVEL_1 
				&& code.lte(FibCode.FIB1) && code.gte(FibCode.FIB618)) {//震荡行情 1 ~ 0.618
			result = true;
		} else if(level == FibLevel.LEVEL_2 && mode == QuotationMode.LONG
				 && code.lte(FibCode.FIB1) && code.gte(FibCode.FIB618)) {//多头行情做多 1 ~ 0.618
			result = true;
		} else if(level == FibLevel.LEVEL_2 && mode == QuotationMode.SHORT
				&& code.gte(FibCode.FIB618)) { //多头行情做空 4.618 ~ 0.618
			result = true;
		} else if(level == FibLevel.LEVEL_3 && mode == QuotationMode.SHORT
				 && code.lte(FibCode.FIB1) && code.gte(FibCode.FIB618)) { //空头行情做空 1 ~ 0.618
			result = true;
		} else if(level == FibLevel.LEVEL_3 && mode == QuotationMode.LONG
				&& code.gte(FibCode.FIB618)) { //空头行情做多 4.618 ~ 0.618
			result = true;
		}
		return result;
	}
	
	/**
	 * 获取下一个点位 点位顺序为： 1 -> 0.786 -> 0.618 -> 0.5 -> 0.382 -> 0.236 -> 0
	 * @param current 当前点位
	 * @return
	 */
	public FibCode getNextFibCode(FibCode current) {
		
		FibCode result = FibCode.FIB0;
		
		if(current == FibCode.FIB1) {
			result = FibCode.FIB618;
		} else if(current == FibCode.FIB786) {
			result = FibCode.FIB5;
		} else {
			FibCode codes[] = FibCode.values();
			for(int index = 0; index < codes.length; index++) {
				FibCode code = codes[index];
				if(code == current && code != FibCode.FIB0) {
					if(code == FibCode.FIB786 || code == FibCode.FIB66) {
						result = codes[index + 2];
					} else {
						result = codes[index + 1];
					}
					break;
				}
			}
		}
		
		return result;
	}

	@Override
	public String toString() {
		
		FibCode[] codes = FibCode.values();
		
		int len = codes.length;
		
		//[Lv1] [LONG]：1(0.1361400),0.786(0.1460696),0.66(0.1519160),0.618(0.1538648),0.5(0.1593400),0.382(0.1648152),0.236(0.1715896),0(0.1825400)
		
		StringBuffer formatBufBefor = new StringBuffer();
		
		StringBuffer formatBuf = new StringBuffer();
		
		formatBufBefor.append("[");
		formatBufBefor.append(this.getLevel().getLabel());
		formatBufBefor.append("] [");
		formatBufBefor.append(this.getQuotationMode().getLabel());
		formatBufBefor.append("]: ");
		
		StringBuffer extensionBuffer = new StringBuffer();
		
		for(int offset = 0;offset < len;offset++) {
			FibCode code = codes[offset];
			
			if(code.getValue() > 1) {
				if(extensionBuffer.length() > 0) {
					extensionBuffer.append(", ");
				}
				extensionBuffer.append(code.getDescription());
				extensionBuffer.append("(");
				extensionBuffer.append(PriceUtil.formatDoubleDecimal(getFibValue(code), this.decimalPoint));
				extensionBuffer.append(")");
			} else {
				if(formatBuf.length() > 0) {
					formatBuf.append(", ");
				}
				formatBuf.append(code.getDescription());
				formatBuf.append("(");
				formatBuf.append(PriceUtil.formatDoubleDecimal(getFibValue(code), this.decimalPoint));
				formatBuf.append(")");
			}
		}
		
		return formatBufBefor.toString() + formatBuf.toString() + "\n\nExtension: " + extensionBuffer.toString();
	}
    
    
}