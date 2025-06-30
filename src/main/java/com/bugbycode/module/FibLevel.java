package com.bugbycode.module;

/**
 * 斐波那契回撤级别
 */
public enum FibLevel {

	LEVEL_0(0,"Lv0", FibCode.FIB382, FibCode.FIB236),
	
	LEVEL_1(1,"Lv1", FibCode.FIB382, FibCode.FIB382),
	
	LEVEL_2(2,"Lv2", FibCode.FIB5, FibCode.FIB5),
	
	LEVEL_3(3,"Lv3", FibCode.FIB618, FibCode.FIB618),
	
	LEVEL_4(4,"Lv4", FibCode.FIB786, FibCode.FIB786),
	
	LEVEL_5(5,"Lv5", FibCode.FIB1, FibCode.FIB1);
	
	private int value;
	
	private String label;
	
	private FibCode levelCode;
	
	private FibCode startFibCode;
	
	FibLevel(int value, String label, FibCode startFibCode, FibCode levelCode) {
		this.value = value;
		this.label = label;
		this.startFibCode = startFibCode;
		this.levelCode = levelCode;
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
