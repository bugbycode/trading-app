package com.bugbycode.module.shape;

/**
 * 图纸类型
 */
public enum ShapeType {

	/**
	 * 趋势线
	 */
	TREND_LINE("LineToolTrendLine","trend_line","趋势线"),
	
	/**
	 * 射线
	 */
	LINE_TOOL_RAY("LineToolRay","ray","射线"),
	
	/**
	 * 平行通道
	 */
	LineToolParallelChannel("LineToolParallelChannel","trend_line","平行通道"),
	
	/**
	 * 水平射线
	 */
	LineToolHorzRay("LineToolHorzRay","horizontal_ray","水平射线"),
	
	/**
	 * 三角形
	 */
	LineToolTrianglePattern("LineToolTrianglePattern","triangle_pattern","三角形"),
	
	/**
	 * 盘整区
	 */
	LineToolRectangle("LineToolRectangle","rectangle","盘整区"),
	
	/**
	 * 斐波那契回撤
	 */
	LineToolFibRetracement("LineToolFibRetracement","fib_retracement","斐波那契回撤"),
	
	/**
	 * XABCD
	 */
	LineTool5PointsPattern("LineTool5PointsPattern","xabcd_pattern","XABCD"),
	
	/**
	 * 做多交易计划
	 */
	LineToolRiskRewardLong("LineToolRiskRewardLong","long_position","做多交易计划"),
	
	/**
	 * 做空交易计划
	 */
	LineToolRiskRewardShort("LineToolRiskRewardShort","short_position","做空交易计划"),
	
	/**
	 * 成交量密集分布
	 */
	LineToolFixedRangeVolumeProfile("LineToolFixedRangeVolumeProfile","fixed_range_volume_profile","成交量密集分布"),
	
	UNKNOWN("Unknown","unknown","未知类型"),
	;
	
	private String name;
	
	private String shape; 
	
	private String memo;
	
	ShapeType(String name, String shape, String memo) {
		this.name = name;
		this.shape = shape;
		this.memo = memo;
	}

	public String getName() {
		return name;
	}

	public String getShape() {
		return shape;
	}

	public String getMemo() {
		return memo;
	}
	
	public static ShapeType resolve(String name) {
		ShapeType[] arr = values();
		for(ShapeType type : arr) {
			if(type.getName().equals(name)) {
				return type;
			}
		}
		return UNKNOWN;
	}
}
