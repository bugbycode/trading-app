package com.bugbycode.module;

import com.util.PriceUtil;

public class FibInfo {

	private double low;
	
	private double high;
	
	private int decimalPoint;
	
	private FibLevel level;
	
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
	 * 
	 * @param kline1 最低价k线
	 * @param kline2 最高价k线
	 * @param decimalPoint 保留小数点个数
	 * @param level 斐波那契回撤级别 lv1 lv2 lv3 ……
	 */
	public FibInfo(Klines kline1, Klines kline2,int decimalPoint,FibLevel level) {
		if(kline1.getStarTime() < kline2.getStarTime()) {
			this.high = kline1.getLowPrice();
			this.low = kline2.getHighPrice();
		} else {
			this.high = kline2.getHighPrice();
			this.low = kline1.getLowPrice();
		}
		this.decimalPoint = decimalPoint;
		this.level = level;
	}

	public double getEndPrice() {
		return low;
	}

	public double getStartPrice() {
		return high;
	}
	
	public double getFibValue(FibCode code) {
		return calculateFibonacciRetracement(low,high,code.getValue());
	}
	
	public double getFibValue(double fibValue) {
		return calculateFibonacciRetracement(low,high,fibValue);
	}
	
	public int getDecimalPoint() {
		return decimalPoint;
	}

	/**
	 * 计算斐波那契回撤 第一个参数小于第二个参数时表示从高到低计算，反之从低到高
	 * @param low 起始价
	 * @param high 最终价
	 * @param fibonacciLevel
	 * @return
	 */
    public double calculateFibonacciRetracement(double low, double high, double fibonacciLevel) {
        
    	if (fibonacciLevel < 0 || fibonacciLevel > 1) {
            throw new IllegalArgumentException("参数不合法。");
        }

        // 计算斐波那契水平（从高到低）
        //return low + fibonacciLevel * range;
        // 计算斐波那契水平（从低到高）
        //return high - fibonacciLevel * range;
        if(low > high) {
        	// 计算价格范围
            double range = low - high;
        	return low - fibonacciLevel * range;
        } else {
        	// 计算价格范围
            double range = high - low;
        	return low + fibonacciLevel * range;
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
		StringBuffer formatBuf = new StringBuffer();
		Object[] arr = new Object[len];
		
		formatBuf.append("[");
		formatBuf.append(this.getLevel().getLabel());
		formatBuf.append("] [");
		formatBuf.append(this.getQuotationMode().getLabel());
		formatBuf.append("]: ");
		
		StringBuffer extensionBuffer = new StringBuffer();
		
		for(int offset = 0;offset < len;offset++) {
			FibCode code = codes[offset];
			
			if(code.getValue() > 1) {
				if(extensionBuffer.length() > 0) {
					extensionBuffer.append(", ");
					extensionBuffer.append(code.getDescription());
					extensionBuffer.append("(%s)");
					arr[offset] = PriceUtil.formatDoubleDecimal(getFibValue(code), this.decimalPoint);
				}
			} else {
				if(formatBuf.length() > 0) {
					formatBuf.append(", ");
				}
				formatBuf.append(code.getDescription());
				formatBuf.append("(%s)");
				arr[offset] = PriceUtil.formatDoubleDecimal(getFibValue(code), this.decimalPoint);
			}
		}
		
		return String.format(formatBuf.toString(), arr) + "\nExtension: " + extensionBuffer.toString();
	}
    
    
}
