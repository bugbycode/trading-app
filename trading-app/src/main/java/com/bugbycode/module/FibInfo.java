package com.bugbycode.module;

import com.util.PriceUtil;

public class FibInfo {

	private double low;
	
	private double high;
	
	private double fib0 = 0;
	
	private double fib236 = 0.236;
	
	private double fib382 = 0.382;
	
	private double fib5 = 0.5;
	
	private double fib618 = 0.618;
	
	private double fib66 = 0.66;
	
	private double fib786 = 0.786;
	
	private double fib1 = 1;
	
	private int decimalPoint;

	public FibInfo(Klines kline1, Klines kline2,int decimalPoint) {
		if(kline1.getStarTime() < kline2.getStarTime()) {
			this.high = kline1.getLowPrice();
			this.low = kline2.getHighPrice();
		} else {
			this.high = kline2.getHighPrice();
			this.low = kline1.getLowPrice();
		}
		this.decimalPoint = decimalPoint;
	}

	public double getEndPrice() {
		return low;
	}

	public double getStartPrice() {
		return high;
	}

	public double getFib0() {
		return calculateFibonacciRetracement(low,high,fib0);
	}

	public double getFib236() {
		return calculateFibonacciRetracement(low,high,fib236);
	}

	public double getFib382() {
		return calculateFibonacciRetracement(low,high,fib382);
	}

	public double getFib5() {
		return calculateFibonacciRetracement(low,high,fib5);
	}

	public double getFib618() {
		return calculateFibonacciRetracement(low,high,fib618);
	}

	public double getFib66() {
		return calculateFibonacciRetracement(low,high,fib66);
	}

	public double getFib786() {
		return calculateFibonacciRetracement(low,high,fib786);
	}

	public double getFib1() {
		return calculateFibonacciRetracement(low,high,fib1);
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

	@Override
	public String toString() {
		return String.format("0(%s), 0.236(%s), 0.382(%s), 0.5(%s), 0.618(%s), 0.66(%s), 0.786(%s), 1(%s)", 
				PriceUtil.formatDoubleDecimal(getFib0(), decimalPoint),
				PriceUtil.formatDoubleDecimal(getFib236(), decimalPoint),
				PriceUtil.formatDoubleDecimal(getFib382(), decimalPoint),
				PriceUtil.formatDoubleDecimal(getFib5(), decimalPoint),
				PriceUtil.formatDoubleDecimal(getFib618(), decimalPoint),
				PriceUtil.formatDoubleDecimal(getFib66(), decimalPoint),
				PriceUtil.formatDoubleDecimal(getFib786(), decimalPoint),
				PriceUtil.formatDoubleDecimal(getFib1(), decimalPoint));
	}
    
    
}
