package com.util;

/**
 * 直线函数工具类
 */
public class StraightLineUtil {
	
	private double a = 0;
	
	private double a2 = 0;
	
	private double b = 0;

	private double b2 = 0;//另一条平行线常数b
	
	/**
	 * 初始化一条直线
	 * @param x1 第一个横坐标
	 * @param y1 第一个纵坐标
	 * @param x2 第二个横坐标
	 * @param y2 第二个纵坐标
	 */
	public StraightLineUtil(long x1, double y1, long x2, double y2) {
		calculateLine(x1,y1,x2,y2);
	}
	
	/**
	 * 初始化平行通道信息
	 * @param x1 第一个横坐标
	 * @param y1 第一个纵坐标
	 * @param x2 第二个横坐标
	 * @param y2 第二个纵坐标
	 * @param x3 另一条平行线横坐标
	 * @param y3 另一条平行线纵坐标
	 */
	public StraightLineUtil(long x1, double y1, long x2, double y2, long x3, double y3) {
		calculateLine(x1,y1,x2,y2);
		b2 = getParallelBValue(x3, y3);
	}
	
	/**
	 * 初始化两条直线 AC和BD
	 * @param xa AC第一个横坐标
	 * @param ya AC第一个纵坐标
	 * @param xb BD第一个横坐标
	 * @param yb BD第一个纵坐标
	 * @param xc AC第二个横坐标
	 * @param yc AC第二个纵坐标
	 * @param xd BD第二个横坐标
	 * @param yd BD第二个纵坐标
	 */
	public StraightLineUtil(long xa,double ya,long xb,double yb,long xc,double yc,long xd,double yd) {
		//初始化AC
		calculateLine(xa,ya,xc,yc);
		//初始化BD
		calculateLine2(xb,yb,xd,yd);
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
	 * 根据两点计算斜率以及常量b（第二条直线）
	 * @param x3
	 * @param y3
	 * @param x4
	 * @param y4
	 */
	private void calculateLine2(long x3, double y3, long x4, double y4) {
		// 计算斜率 a
        a2 = (y4 - y3) / (x4 - x3);
        
        // 计算截距 b
        b2 = y3 - a2 * x3;
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
	 * 验证某个点是否为此方程的解（第二条直线）
	 * @param x
	 * @param y
	 * @return
	 */
	public boolean isPointOnLine2(long x, double y) {
        // 计算方程的 y 值
        double calculatedY = a2 * x + b2;
        
        // 检查计算的 y 值是否与给定的 y 值相等
        return calculatedY == y;
    }
	
	/**
	 * 验证某个点是否为此方程的解（另一条平行线）
	 * @param x
	 * @param y
	 * @return
	 */
	public boolean isPointOnLineParallel(long x, double y) {
		// 计算方程的 y 值
        double calculatedY = a * x + b2;
        
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
	
	/**
	 * 根据X计算Y （第二条平行线）
	 * @param x
	 * @return
	 */
	public double calculateLineYvalueForb2(long x) {
		return a * x + b2;
	}
	
	/**
	 * 根据X计算Y （第二条直线）
	 * @param x
	 * @return
	 */
	public double calculateLine2Yvalue(long x) {
		return a2 * x + b2;
	}

	/**
	 * 求另一条平行线的常数b
	 * @param x 另一条平行线已知横坐标
	 * @param y 另一条平行线已知纵坐标
	 * @return
	 */
	private double getParallelBValue(long x,double y) {
		//y = ax+b 则有 b = y - ax
		return y - a * x;
	}
	
	/**
	 * 两个条直线是否相交
	 * @return
	 */
	public boolean isIntersect() {
		return a != a2;
	}
	
	/**
	 * 两条直线相交时横坐标的值
	 * @return
	 */
	public long getIntersectXValue() {
		if(!isIntersect()) {
			throw new RuntimeException("两条平行线没有交点");
		}
		return (long) ((b2 - b) / (a - a2));
	}
}
