package com.util;

/**
 * 直线函数工具类
 */
public class StraightLineUtil {
	
	private double a = 0;
	
	private double b = 0;

	public StraightLineUtil(long x1, double y1, long x2, double y2) {
		calculateLine(x1,y1,x2,y2);
	}
	
	/**
	 * 根据两点计算斜率以及常量b
	 * @param x1
	 * @param y1
	 * @param x2
	 * @param y2
	 */
	private void calculateLine(long x1, double y1, long x2, double y2) {
        // 计算斜率 a
        a = (y2 - y1) / (x2 - x1);
        
        // 计算截距 b
        b = y1 - a * x1;
    }
	
	/**
	 * 验证某个点是否为此方程的解
	 * @param x
	 * @param y
	 * @return
	 */
	public boolean isPointOnLine(long x, double y) {
        // 计算方程的 y 值
        double calculatedY = a * x + b;
        
        // 检查计算的 y 值是否与给定的 y 值相等
        return calculatedY == y;
    }
	
	/**
	 * 根据X计算Y
	 * @param x
	 * @return
	 */
	public double calculateLineYvalue(long x) {
		return a * x + b;
	}
}
