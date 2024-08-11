package com.util;

public class FibonacciRetracementUtil {
	
	/**
	 * 计算斐波那契回撤 第一个参数小于第二个参数时表示从高到低计算，反之从低到高
	 * @param low 起始价
	 * @param high 最终价
	 * @param fibonacciLevel
	 * @return
	 */
    public static double calculateFibonacciRetracement(double low, double high, double fibonacciLevel) {
        
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

    public static void main(String[] args) {
        // 示例最低价和最高价
        double low = 41370;
        double high = 49027.5;

        // 示例斐波那契水平百分比
        double fibonacciLevel = 0.618;

        // 计算斐波那契回撤
        double fibonacciRetracement = calculateFibonacciRetracement(high, low, fibonacciLevel);

        // 打印结果
        System.out.println("斐波那契水平 " + fibonacciLevel + ": " + fibonacciRetracement);
    }
}
