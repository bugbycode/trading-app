package com.bugbycode.module;

import com.util.PriceUtil;

public class FibInfo {

	private double startPrice;
	
	private double endPrice;
	
	private int decimalPoint;
	
	private FibLevel level;
	
	/**
	 * 止盈点位 V7
	 * @param code 开仓时所处的斐波那契回撤点位
	 * @return 止盈的斐波那契回撤点位
	 */
	public FibCode getTakeProfit_v7(FibCode code) {
		FibCode takeProfit = FibCode.FIB382;
		if(level == FibLevel.LEVEL_4) {
			if(code == FibCode.FIB4_618) {//4.618 -> 2.618
				takeProfit = FibCode.FIB2_618;
			} else if(code == FibCode.FIB2_618) { // 2.618 -> 2
				takeProfit = FibCode.FIB2;
			} else if(code == FibCode.FIB2) { // 2 -> 1.618
				takeProfit = FibCode.FIB1_618;
			} else if(code == FibCode.FIB1_618) { // 1.618 -> 1
				takeProfit = FibCode.FIB1;
			} else if(code == FibCode.FIB1) { // 1 -> 0.618
				takeProfit = FibCode.FIB618;
			} else if(code == FibCode.FIB786) { // 0.786 -> 0.5
				takeProfit = FibCode.FIB5;
			} else if(code == FibCode.FIB618 || code == FibCode.FIB66) { // 0.618 -> 0.382
				takeProfit = FibCode.FIB382;
			} else if(code == FibCode.FIB5) { // 0.5 -> 0.382
				takeProfit = FibCode.FIB382;
			} else if(code == FibCode.FIB382) { // 0.382 -> 0.236
				takeProfit = FibCode.FIB236;
			}
		} else if(level == FibLevel.LEVEL_3) {
			if(code == FibCode.FIB4_618) {//4.618 -> 2.618
				takeProfit = FibCode.FIB2_618;
			} else if(code == FibCode.FIB2_618) { // 2.618 -> 2
				takeProfit = FibCode.FIB2;
			} else if(code == FibCode.FIB2) { // 2 -> 1.618
				takeProfit = FibCode.FIB1_618;
			} else if(code == FibCode.FIB1_618) { // 1.618 -> 1
				takeProfit = FibCode.FIB1;
			} else if(code == FibCode.FIB1) { // 1 -> 0.618
				takeProfit = FibCode.FIB618;
			} else if(code == FibCode.FIB786) { // 0.786 -> 0.5
				takeProfit = FibCode.FIB5;
			} else if(code == FibCode.FIB618 || code == FibCode.FIB66) { // 0.618 -> 0.382
				takeProfit = FibCode.FIB382;
			} else if(code == FibCode.FIB5) { // 0.5 -> 0.382
				takeProfit = FibCode.FIB382;
			} else if(code == FibCode.FIB382) { // 0.382 -> 0.236
				takeProfit = FibCode.FIB236;
			}
		} else {
			if(code == FibCode.FIB4_618) {//4.618 -> 2.618
				takeProfit = FibCode.FIB2_618;
			} else if(code == FibCode.FIB2_618) { // 2.618 -> 2
				takeProfit = FibCode.FIB2;
			} else if(code == FibCode.FIB2) { // 2 -> 1.618
				takeProfit = FibCode.FIB1_618;
			} else if(code == FibCode.FIB1_618) { // 1.618 -> 1
				takeProfit = FibCode.FIB1;
			} else if(code == FibCode.FIB1) { // 1 -> 0.786
				takeProfit = FibCode.FIB786;
			} else if(code == FibCode.FIB786) { // 0.786 -> 0.618
				takeProfit = FibCode.FIB618;
			} else if(code == FibCode.FIB618 || code == FibCode.FIB66) { // 0.618 -> 0.5
				takeProfit = FibCode.FIB5;
			} else if(code == FibCode.FIB5) { // 0.5 -> 0.382
				takeProfit = FibCode.FIB382;
			} else if(code == FibCode.FIB382) { // 0.382 -> 0.236
				takeProfit = FibCode.FIB236;
			}
		}
		
		return takeProfit;
	}
	
	/**
	 * 止盈点位 V6
	 * @param code 开仓时所处的斐波那契回撤点位
	 * @return 止盈的斐波那契回撤点位
	 */
	public FibCode getTakeProfit_v6(FibCode code) {
		FibCode takeProfit = FibCode.FIB382;
		if(code == FibCode.FIB4_618) {//4.618 -> 2.618
			takeProfit = FibCode.FIB2_618;
		} else if(code == FibCode.FIB2_618) { // 2.618 -> 2
			takeProfit = FibCode.FIB2;
		} else if(code == FibCode.FIB2) { // 2 -> 1.618
			takeProfit = FibCode.FIB1_618;
		} else if(code == FibCode.FIB1_618) { // 1.618 -> 1
			takeProfit = FibCode.FIB1;
		} else if(code == FibCode.FIB1) { // 1 -> 0.618
			takeProfit = FibCode.FIB618;
		} else if(code == FibCode.FIB786) { // 0.786 -> 0.5
			takeProfit = FibCode.FIB5;
		}
		return takeProfit;
	}
	
	/**
	 * 止盈点位 V5
	 * @param code 开仓时所处的斐波那契回撤点位
	 * @return 止盈的斐波那契回撤点位
	 */
	public FibCode getTakeProfit_v5(FibCode code) {
		FibCode takeProfit = FibCode.FIB382;
		if(code == FibCode.FIB4_618) {//4 -> 2.618
			takeProfit = FibCode.FIB2_618;
		} else if(code == FibCode.FIB2_618) { // 2.618 -> 2
			takeProfit = FibCode.FIB2;
		} else if(code == FibCode.FIB2) { // 2 -> 1.618
			takeProfit = FibCode.FIB1_618;
		} else if(code == FibCode.FIB1_618) { // 1.618 -> 1
			takeProfit = FibCode.FIB1;
		} else if(code == FibCode.FIB1) { // 1 -> 0.618
			takeProfit = FibCode.FIB618;
		}
		return takeProfit;
	}
	
	/**
	 * 止盈点位 V4
	 * @param code 开仓时所处的斐波那契回撤点位
	 * @return 止盈的斐波那契回撤点位
	 */
	public FibCode getTakeProfit_v4(FibCode code) {
		FibCode takeProfit = FibCode.FIB382;
		if(code == FibCode.FIB4_618) {
			takeProfit = FibCode.FIB1_618;
		} else if(code.gt(FibCode.FIB1)) {
			takeProfit = FibCode.FIB1;
		} else if(code.equalsValue(FibCode.FIB786) || code.equalsValue(FibCode.FIB1)) {
			takeProfit = FibCode.FIB5;
		}
		return takeProfit;
	}
	
	/**
	 * 止盈点位 V3
	 * @param code 开仓时所处的斐波那契回撤点位
	 * @return 止盈的斐波那契回撤点位
	 */
	public FibCode getTakeProfit_v3(FibCode code) {
		FibCode takeProfit = FibCode.FIB382;
		if(code == FibCode.FIB4_618) {
			takeProfit = FibCode.FIB1_618;
		} else if(code.gt(FibCode.FIB1)) {
			takeProfit = FibCode.FIB1;
		}
		return takeProfit;
	}
	
	/**
	 * 止盈点位 V2
	 * @param code 开仓时所处的斐波那契回撤点位
	 * @return 止盈的斐波那契回撤点位
	 */
	public FibCode getTakeProfit_v2(FibCode code) {
		FibCode takeProfit = FibCode.FIB382;
		if(code == FibCode.FIB4_618) {
			takeProfit = FibCode.FIB2_618;
		} else if(code.getValue() > FibCode.FIB1.getValue()) {
			takeProfit = FibCode.FIB1;
		}
		return takeProfit;
	}
	
	/**
	 * 止盈点位
	 * @param code 开仓时所处的斐波那契回撤点位
	 * @return 止盈的斐波那契回撤点位
	 */
	public FibCode getTakeProfit(FibCode code) {
		
		FibCode takeProfit;
		
		switch (level) {
		
		case LEVEL_1:
			
			switch (code) {
			
			case FIB1:
				takeProfit = FibCode.FIB786;
				break;
			case FIB786:
				takeProfit = FibCode.FIB618;
				break;
			case FIB66:
				takeProfit = FibCode.FIB5;
				break;
			case FIB618:
				takeProfit = FibCode.FIB5;
				break;
			case FIB5:
				takeProfit = FibCode.FIB382;
				break;
			case FIB382:
				takeProfit = FibCode.FIB236;
				break;
			default:
				takeProfit = FibCode.FIB382;
				break;
			}
			
			break;
			
		case LEVEL_2:
			
			switch (code) {
			
			case FIB1:
				takeProfit = FibCode.FIB786;
				break;
			case FIB786:
				takeProfit = FibCode.FIB618;
				break;
			case FIB66:
				takeProfit = FibCode.FIB5;
				break;
			case FIB618:
				takeProfit = FibCode.FIB5;
				break;
			case FIB5:
				takeProfit = FibCode.FIB382;
				break;
			case FIB382:
				takeProfit = FibCode.FIB236;
			default:
				takeProfit = FibCode.FIB382;
				break;
			}
			break;
		default:
			takeProfit = FibCode.FIB382;
			break;
		}
		
		return takeProfit;
	}
	
	/**
	 * 止盈点位
	 * @param offset
	 * @param codes
	 * @return
	 */
	public FibCode getTakeProfit(int offset,FibCode[] codes) {
		int index = offset;
		FibCode current = codes[index];
		if(this.level == FibLevel.LEVEL_1) {//1级回撤
			if(current == FibCode.FIB66 || current == FibCode.FIB786) {
				index += 2;
			} else {
				index++;
			}
		} else { // 2级 或 3级回撤
			if(current == FibCode.FIB66 || current == FibCode.FIB786) {
				index += 3;
			} else if(current == FibCode.FIB5) {//0.5 ~ 0.382
				index++;
			} else {
				index += 2;
			}
		}
		
		return codes[index];
	}
	
	/**
	 * 校验是否为回踩单开仓点位
	 * @param code
	 * @return
	 */
	public boolean verifyStepBack(FibCode code) {
		//一级0位置分别对应回撤0.382 0.5 0.618 0.786
		return code == FibCode.FIB1_618 || code == FibCode.FIB2 || code == FibCode.FIB2_618
				 || code == FibCode.FIB4_618;
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
