package com.bugbycode.module;

/**
 * 斐波那契回撤级别
 */
public enum FibLevel {

	DEFAULT(-1, "DEFAULT", FibCode.FIB0, FibCode.FIB0),
	
	LEVEL_0(0, "Lv0", FibCode.FIB236, FibCode.FIB236),
	
	LEVEL_1(1, "Lv1", FibCode.FIB382, FibCode.FIB382),
	
	LEVEL_2(2, "Lv2", FibCode.FIB5, FibCode.FIB5),
	
	LEVEL_3(3, "Lv3", FibCode.FIB618, FibCode.FIB618),
	
	LEVEL_4(4, "Lv4", FibCode.FIB786, FibCode.FIB786),
	
	LEVEL_5(5, "Lv5", FibCode.FIB1_272, FibCode.FIB1_272),
	
	LEVEL_6(6, "Lv6", FibCode.FIB1_618, FibCode.FIB1_618),
	
	LEVEL_7(7, "Lv7", FibCode.FIB2, FibCode.FIB2),
	
	LEVEL_8(8, "Lv8", FibCode.FIB2_618, FibCode.FIB2_618),
	
	LEVEL_9(9, "Lv9", FibCode.FIB3_618, FibCode.FIB3_618),
	
	LEVEL_10(10, "Lv10", FibCode.FIB4_618, FibCode.FIB4_618);
	
	private int value;
	
	private String label;
	
	private FibCode levelCode;
	
	private FibCode startFibCode;
	
	FibLevel(int value, String label, FibCode startFibCode, FibCode levelCode) {
		this.label = label;
		this.startFibCode = startFibCode;
		this.levelCode = levelCode;
		this.value = value;
	}

	public int getValue() {
		return value;
	}

	public String getLabel() {
		return label;
	}

	public FibCode getLevelCode() {
		return levelCode;
	}

	public FibCode getStartFibCode() {
		return startFibCode;
	}
	
	public static FibLevel valueOf(FibCode levelFibCode) {
		FibLevel result = LEVEL_5;
		FibLevel[] levels = FibLevel.values();
		for(FibLevel level : levels) {
			if(level.getLevelCode() == levelFibCode) {
				result = level;
				break;
			}
		}
		return result;
	}
	
	public static FibLevel valueOf(int value) {
		FibLevel result = LEVEL_3;
		FibLevel[] levels = FibLevel.values();
		for(FibLevel level : levels) {
			if(level.getValue() == value) {
				result = level;
				break;
			}
		}
		return result;
	}
	
	/**
	 * 大于
	 * @param level
	 * @return
	 */
	public boolean gt(FibLevel level) {
		return this.getValue() > level.getValue();
	}
	
	/**
	 * 大于等于
	 * @param level
	 * @return
	 */
	public boolean gte(FibLevel level) {
		return this.getValue() >= level.getValue();
	}
	
	/**
	 * 小于
	 * @param level
	 * @return
	 */
	public boolean lt(FibLevel level) {
		return this.getValue() < level.getValue();
	}
	
	/**
	 * 小于等于
	 * @param level
	 * @return
	 */
	public boolean lte(FibLevel level) {
		return this.getValue() <= level.getValue();
	}
}
