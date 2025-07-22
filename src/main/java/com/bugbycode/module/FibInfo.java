package com.bugbycode.module;

import java.util.List;

import com.util.PriceUtil;

public class FibInfo {

	private double startPrice;
	
	private double endPrice;
	
	private int decimalPoint;
	
	private FibLevel level;
	
	private List<Klines> fibAfterKlines;
	
	private FibCode endCode = FibCode.FIB4_618;
	
	public FibCode getAreaTakeProfit(double price, double profit, double profitLimit) {
		FibCode result = null;
		QuotationMode qm = this.getQuotationMode();
		double percent_5 = PriceUtil.getPercent(price, this.getFibValue(FibCode.FIB5), qm);
		double percent_618 = PriceUtil.getPercent(price, this.getFibValue(FibCode.FIB618), qm);
		if(percent_618 >= profit && percent_618 <= profitLimit) {
			result = FibCode.FIB618;
		} else if(percent_5 >= profit && percent_5 <= profitLimit) {
			result = FibCode.FIB5;
		}
		if(result == null) {
			if(percent_618 >= profit) {
				result = FibCode.FIB618;
			} else if(percent_5 >= profit) {
				result = FibCode.FIB5;
			} else {
				result = FibCode.FIB5;
			}
		}
		return result;
	}
	
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
		
		//double percent_382 = PriceUtil.getPercent(price, this.getFibValue(FibCode.FIB382), qm);
		double percent_5 = PriceUtil.getPercent(price, this.getFibValue(FibCode.FIB5), qm);
		double percent_618 = PriceUtil.getPercent(price, this.getFibValue(FibCode.FIB618), qm);
		//double percent_786 = PriceUtil.getPercent(price, this.getFibValue(FibCode.FIB786), qm);
		
		/*if(percent_382 >= profit && percent_382 <= profitLimit) {
			result = FibCode.FIB382;
		} else */if(percent_5 >= profit && percent_5 <= profitLimit) {
			result = FibCode.FIB5;
		} else if(percent_618 >= profit && percent_618 <= profitLimit) {
			result = FibCode.FIB618;
		}
		
		if(result == null) {
			/*if(percent_382 >= profit) {
				result = FibCode.FIB382;
			} else*/ if(percent_5 >= profit) {
				result = FibCode.FIB5;
			} else if(percent_618 >= profit) {
				result = FibCode.FIB618;
			} else {
				result = FibCode.FIB618;
			}
		}
		
		return result;
	}
	
	/**
	 * 指数均线指标止盈点位
	 * @param price 当前价格
	 * @param profit 用户盈利预期
	 * @param profitLimit 用户止盈百分比限制
	 * @return
	 */
	public FibCode getEmaEmaRiseAndFallTakeProfit(double price, double profit, double profitLimit) {
		
		FibCode result = null;
		
		QuotationMode qm = this.getQuotationMode() == QuotationMode.LONG ? QuotationMode.SHORT : QuotationMode.LONG;
		//double percent_5 = PriceUtil.getPercent(price, this.getFibValue(FibCode.FIB5), qm);
		double percent_618 = PriceUtil.getPercent(price, this.getFibValue(FibCode.FIB618), qm);
		double percent_786 = PriceUtil.getPercent(price, this.getFibValue(FibCode.FIB786), qm);
		//double percent_1 = PriceUtil.getPercent(price, this.getFibValue(FibCode.FIB1), qm);
		
		/*if(percent_5 >= profit && percent_5 <= profitLimit) {
			result = FibCode.FIB5;
		} else */if(percent_618 >= profit && percent_618 <= profitLimit) {
			result = FibCode.FIB618;
		} else if(percent_786 >= profit && percent_786 <= profitLimit) {
			result = FibCode.FIB786;
		}/* else if(percent_1 >= profit && percent_1 <= profitLimit) {
			result = FibCode.FIB1;
		}*/
		
		if(result == null) {
			/*if(percent_5 >= profit) {
				result = FibCode.FIB5;
			} else */if(percent_618 >= profit) {
				result = FibCode.FIB618;
			} else if(percent_786 >= profit) {
				result = FibCode.FIB786;
			}/* else if(percent_1 >= profit) {
				result = FibCode.FIB1;
			} */else {
				result = FibCode.FIB618;
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
		} else if(code == FibCode.FIB3_618) { // 3.618 - 2
			takeProfit = FibCode.FIB2;
		} else if(code == FibCode.FIB2_618) { // 2.618 - 1.618
			takeProfit = FibCode.FIB1_618;
		} else if(code == FibCode.FIB2) { // 2 - 1
			takeProfit = FibCode.FIB1;
		} else if(code == FibCode.FIB1_618) { // 1.618 - 1
			takeProfit = FibCode.FIB1;
		} else if(code == FibCode.FIB1_272) { // 1.272 - 0.618
			takeProfit = FibCode.FIB618;
		} else if(code == FibCode.FIB1) { // 1 -> 0.5
			takeProfit = FibCode.FIB5;
		} else if(code == FibCode.FIB786) { // 0.786 -> 0.382
			takeProfit = FibCode.FIB382;
		} else if(code == FibCode.FIB618 || code == FibCode.FIB66) { // 0.618 -> 0.236
			takeProfit = FibCode.FIB236;
		} else if(code == FibCode.FIB5) { // 0.5 -> 0.236
			takeProfit = FibCode.FIB236;
		} else if(code == FibCode.FIB382) { // 0.382 -> 0
			takeProfit = FibCode.FIB0;
		} else if(code == FibCode.FIB236) { // 0.236 -> 0
			takeProfit = FibCode.FIB0;
		}
		return takeProfit;
	}
	
	/**
	 * 止盈点位 V1 (用于判断是否开仓过)
	 * @param code 开仓时所处的斐波那契回撤点位
	 * @return 止盈的斐波那契回撤点位
	 */
	public FibCode getTakeProfit_v1(FibCode code) {
		FibCode takeProfit = FibCode.FIB0;
		if(code == FibCode.FIB4_618) { // 4.618 - 2.618
			takeProfit = FibCode.FIB3_618;
		} else if(code == FibCode.FIB3_618) { //3.618 - 2.618
			takeProfit = FibCode.FIB2_618;
		} else if(code == FibCode.FIB2_618) { // 2.618 - 2
			takeProfit = FibCode.FIB2;
		} else if(code == FibCode.FIB2) {// 2 - 1.618
			takeProfit = FibCode.FIB1_618;
		} else if(code == FibCode.FIB1_618) { // 1.618 - 1.272
			takeProfit = FibCode.FIB1_272;
		} else if(code == FibCode.FIB1_272) {// 1.272 - 1
			takeProfit = FibCode.FIB1;
		} else if(code == FibCode.FIB1) { // 1 -> 0.618
			takeProfit = FibCode.FIB618;
		} else if(code == FibCode.FIB786) { // 0.786 -> 0.5
			takeProfit = FibCode.FIB5;
		} else if(code == FibCode.FIB618 || code == FibCode.FIB66) { // 0.618 -> 0.382
			takeProfit = FibCode.FIB382;
		} else if(code == FibCode.FIB5) { // 0.5 -> 0.236
			takeProfit = FibCode.FIB236;
		} else if(code == FibCode.FIB382) { // 0.382 -> 0.236
			takeProfit = FibCode.FIB236;
		} else if(code == FibCode.FIB236) {// 0.236 -> 0
			takeProfit = FibCode.FIB0;
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
		return Double.valueOf(PriceUtil.formatDoubleDecimal(calculateFibonacciRetracement(endPrice,startPrice,code.getValue()), this.decimalPoint));
	}
	
	public double getFibValue(double fibValue) {
		return Double.valueOf(PriceUtil.formatDoubleDecimal(calculateFibonacciRetracement(endPrice,startPrice,fibValue), this.decimalPoint));
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
	 * 获取下一个点位 点位顺序为： 1 -> 0.786 -> 0.618 -> 0.5 -> 0.382 -> 0.236 -> 0
	 * @param current 当前点位
	 * @return
	 */
	public FibCode getNextFibCode(FibCode current) {
		
		FibCode result = FibCode.FIB0;
		
		if(current == FibCode.FIB1_272) {
			result = FibCode.FIB786;
		} else if(current == FibCode.FIB1) {
			result = FibCode.FIB618;
		} else if(current == FibCode.FIB786) {
			result = FibCode.FIB5;
		} else if(current == FibCode.FIB618 || current == FibCode.FIB66) {
			result = FibCode.FIB382;
		} else if(current == FibCode.FIB5) {
			result = FibCode.FIB382;
		} else {
			FibCode codes[] = FibCode.values();
			for(int index = 0; index < codes.length; index++) {
				FibCode code = codes[index];
				if(code == current && code != FibCode.FIB0) {
					result = codes[index + 1];
					break;
				}
			}
		}
		
		return result;
	}

	public List<Klines> getFibAfterKlines() {
		return fibAfterKlines;
	}

	public void setFibAfterKlines(List<Klines> fibAfterKlines) {
		this.fibAfterKlines = fibAfterKlines;
	}
	
	public boolean equals(FibInfo info) {
		
		return info != null && this.getFibValue(FibCode.FIB0) == info.getFibValue(FibCode.FIB0)
				 && this.getFibValue(FibCode.FIB1) == info.getFibValue(FibCode.FIB1);
	}
	
	public int getFibCodeIndex(FibCode code) {
		int index = 0;
		FibCode[] codes = FibCode.values();
		for(int offset = 0;offset < codes.length; offset++) {
			if(codes[offset] == code) {
				index = offset;
				break;
			}
		}
		return index;
	}
	
	/**
	 * 根据价格寻找所处的回撤点位
	 * @param price
	 * @return
	 */
	public FibCode getFibCode(double price) {
		FibCode code = FibCode.FIB0;
		QuotationMode mode = this.getQuotationMode();
		if(mode == QuotationMode.LONG) {
			if(price <= this.getFibValue(FibCode.FIB4_618)) {// 4.618
				code = FibCode.FIB4_618;
			} else if(price > this.getFibValue(FibCode.FIB4_618) && price <= this.getFibValue(FibCode.FIB3_618)) {// 4.618 ~ 3.618
				code = FibCode.FIB3_618;
			} else if(price > this.getFibValue(FibCode.FIB3_618) && price <= this.getFibValue(FibCode.FIB2_618)) {// 3.618 ~ 2.618
				code = FibCode.FIB2_618;
			} else if(price > this.getFibValue(FibCode.FIB2_618) && price <= this.getFibValue(FibCode.FIB2)) {// 2.618 ~ 2
				code = FibCode.FIB2;
			} else if(price > this.getFibValue(FibCode.FIB2) && price <= this.getFibValue(FibCode.FIB1_618)) {// 2 ~ 1.618
				code = FibCode.FIB1_618;
			} else if(price > this.getFibValue(FibCode.FIB1_618) && price <= this.getFibValue(FibCode.FIB1_272)) {// 1.618 ~ 1.272
				code = FibCode.FIB1_272;
			} else if(price > this.getFibValue(FibCode.FIB1_272) && price <= this.getFibValue(FibCode.FIB1)) {// 1.272 ~ 1
				code = FibCode.FIB1;
			} else if(price > this.getFibValue(FibCode.FIB1) && price <= this.getFibValue(FibCode.FIB786)) {// 1 ~ 0.786
				code = FibCode.FIB786;
			} else if(price > this.getFibValue(FibCode.FIB786) && price <= this.getFibValue(FibCode.FIB618)) {// 0.786 ~ 0.618
				code = FibCode.FIB618;
			} else if(price > this.getFibValue(FibCode.FIB618) && price <= this.getFibValue(FibCode.FIB5)) {// 0.618 ~ 0.5
				code = FibCode.FIB5;
			} else if(price > this.getFibValue(FibCode.FIB5) && price <= this.getFibValue(FibCode.FIB382)) {// 0.5 ~ 0.382
				code = FibCode.FIB382;
			} else if(price > this.getFibValue(FibCode.FIB382) && price <= this.getFibValue(FibCode.FIB236)) {// 0.382 ~ 0.236
				code = FibCode.FIB236;
			} else if(price > this.getFibValue(FibCode.FIB236) && price < this.getFibValue(FibCode.FIB0)) {// 0.236 ~ 0
				code = FibCode.FIB236;
			}
		} else {
			if(price >= this.getFibValue(FibCode.FIB4_618)) {// 4.618
				code = FibCode.FIB4_618;
			} else if(price < this.getFibValue(FibCode.FIB4_618) && price >= this.getFibValue(FibCode.FIB3_618)) {// 4.618 ~ 3.618
				code = FibCode.FIB3_618;
			} else if(price < this.getFibValue(FibCode.FIB3_618) && price >= this.getFibValue(FibCode.FIB2_618)) {// 3.618 ~ 2.618
				code = FibCode.FIB2_618;
			} else if(price < this.getFibValue(FibCode.FIB2_618) && price >= this.getFibValue(FibCode.FIB2)) {// 2.618 ~ 2
				code = FibCode.FIB2;
			} else if(price < this.getFibValue(FibCode.FIB2) && price >= this.getFibValue(FibCode.FIB1_618)) {// 2 ~ 1.618
				code = FibCode.FIB1_618;
			} else if(price < this.getFibValue(FibCode.FIB1_618) && price >= this.getFibValue(FibCode.FIB1_272)) {// 1.618 ~ 1.272
				code = FibCode.FIB1_272;
			} else if(price < this.getFibValue(FibCode.FIB1_272) && price >= this.getFibValue(FibCode.FIB1)) {// 1.272 ~ 1
				code = FibCode.FIB1;
			} else if(price < this.getFibValue(FibCode.FIB1) && price >= this.getFibValue(FibCode.FIB786)) {// 1 ~ 0.786
				code = FibCode.FIB786;
			} else if(price < this.getFibValue(FibCode.FIB786) && price >= this.getFibValue(FibCode.FIB618)) {// 0.786 ~ 0.618
				code = FibCode.FIB618;
			} else if(price < this.getFibValue(FibCode.FIB618) && price >= this.getFibValue(FibCode.FIB5)) {// 0.618 ~ 0.5
				code = FibCode.FIB5;
			} else if(price < this.getFibValue(FibCode.FIB5) && price >= this.getFibValue(FibCode.FIB382)) {// 0.5 ~ 0.382
				code = FibCode.FIB382;
			} else if(price < this.getFibValue(FibCode.FIB382) && price >= this.getFibValue(FibCode.FIB236)) {// 0.382 ~ 0.236
				code = FibCode.FIB236;
			} else if(price < this.getFibValue(FibCode.FIB236) && price > this.getFibValue(FibCode.FIB0)) {// 0.236 ~ 0
				code = FibCode.FIB236;
			}
		}
		
		return code;
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
			if(!code.isTrade() && code.gt(FibCode.FIB1)) {
				continue;
			}
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

	public FibCode getEndCode() {
		return endCode;
	}

	public void setEndCode(FibCode endCode) {
		this.endCode = endCode;
	}
    
}