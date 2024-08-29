package com.bugbycode.module;

/**
 * 谐波模式
 */
public enum XABCD {
	
	/**
	 * 1. Gartley形态又称为加特利形态
	 * B点回撤:
	 * B点应回撤 XA 的 61.8% 到 78.6%。
	 * C点回撤:
	 * C点应回撤 AB 的 38.2% 到 88.6%。
	 * D点扩展:
	 * D点应扩展 XA 的 127.2% 到 161.8%。
	 */
	Gartley("Gartley","加特利形态",new double[] {0.618, 0.786},new double[] {0.382, 0.886},new double[] {1.272, 1.618}),
	
	/**
	 * 2. Bat形态又称为蝙蝠形态
	 * B点回撤:
	 * B点应回撤 XA 的 38.2% 到 50.0%。
	 * C点回撤:
	 * C点应回撤 AB 的 38.2% 到 88.6%。
	 * D点扩展:
	 * D点应扩展 XA 的 161.8% 到 261.8%。
	 */
	Bat("Bat","蝙蝠形态",new double[] {0.382, 0.50},new double[] {0.382, 0.886},new double[] {1.618, 2.618}),
	
	/**
	 * 3. Crab形态 蟹形态
	 * B点回撤:
	 * B点应回撤 XA 的 38.2% 到 61.8%。
	 * C点回撤:
	 * C点应回撤 AB 的 38.2% 到 61.8%。
	 * D点扩展:
	 * D点应扩展 XA 的 224.0% 到 361.8%。
	 */
	Crab("Crab","螃蟹形态",new double[] {0.382, 0.618},new double[] {0.382, 0.618},new double[] {2.24, 3.618}),
	
	/**
	 * 4. Butterfly形态 蝴蝶形态
	 * B点回撤:
	 * B点应回撤 XA 的 78.6%。
	 * C点回撤:
	 * C点应回撤 AB 的 38.2% 到 88.6%。
	 * D点扩展:
	 * D点应扩展 XA 的 161.8% 到 261.8%。
	 */
	Butterfly("Butterfly","蝴蝶形态",new double[] {0.786, 0.786},new double[] {0.382, 0.886},new double[] {1.618, 2.618}),
	
	/**
	 * 5. Deep Crab形态 深蟹形态
	 * B点回撤:
	 * B点应回撤 XA 的 38.2% 到 61.8%。
	 * C点回撤:
	 * C点应回撤 AB 的 38.2% 到 88.6%。
	 * D点扩展:
	 * D点应扩展 XA 的 161.8% 到 361.8%。
	 */
	Deep_Crab("Deep Crab","深蟹形态",new double[] {0.382, 0.618},new double[] {0.382, 0.886},new double[] {1.618, 3.618}),
	;

	private String name;//形态名称
	
	private String label;//形态描述
	
	private final double[] bRetracementRange;//
	
	private final double[] cRetracementRange;//
	
	private final double[] dExtensionRange;//
	
	/**
	 * 
	 * @param name 形态名称
	 * @param label 形态描述
	 * @param bRetracementRange B点回撤信息 XA
	 * @param cRetracementRange C点回撤信息 AB
	 * @param dExtensionRange D点回撤信息 XA
	 */
	XABCD(String name, String label, double[] bRetracementRange, double[] cRetracementRange, double[] dExtensionRange) {
		this.name = name;
		this.label = label;
		this.bRetracementRange = bRetracementRange;
		this.cRetracementRange = cRetracementRange;
		this.dExtensionRange = dExtensionRange;
	}

	public String getName() {
		return name;
	}

	public String getLabel() {
		return label;
	}

	/**
	 * 检查B点价格是否满足当前形态回撤
	 * @param fib XA斐波那契回撤信息
	 * @param price B点价格
	 * @return
	 */
	public boolean checkBRetracementRange(FibInfo xaFib,double price) {
		return checkRange(xaFib,price,bRetracementRange);
	}
	
	/**
	 * 检查C点价格是否满足当前形态回撤
	 * @param fib AB斐波那契回撤信息
	 * @param price C点价格
	 * @return
	 */
	public boolean checkCRetracementRange(FibInfo abFib,double price) {
		return checkRange(abFib,price,cRetracementRange);
	}
	
	/**
	 * 检查D点价格是否满足当前形态回撤
	 * @param fib XA斐波那契回撤信息
	 * @param price D点价格
	 * @return
	 */
	public boolean checkDExtensionRange(FibInfo xaFib,double price) {
		return checkRange(xaFib,price,dExtensionRange);
	}
	
	private boolean checkRange(FibInfo fib,double price,double[] range) {
		boolean result = false;
		
		QuotationMode qm = fib.getQuotationMode();
		
		switch (qm) {
		case LONG:
			
			result = (price <= fib.getFibValue(range[0]) && price >= fib.getFibValue(range[1]));
			break;

		default:
			
			result = (price >= fib.getFibValue(range[0]) && price <= fib.getFibValue(range[1]));
			break;
		}
		
		return result;
	}
	
	public String toString() {
		StringBuffer buffer = new StringBuffer();
		buffer.append(this.getName() + "(" + this.getLabel() + "): ");
		buffer.append(String.format("XA回撤%s到%s（B点）", bRetracementRange[0],bRetracementRange[1]));
		buffer.append("，");
		buffer.append(String.format("BC回撤%s到%s（C点）", cRetracementRange[0],cRetracementRange[1]));
		buffer.append("，");
		buffer.append(String.format("CD扩展%s到%s（D点）", dExtensionRange[0],dExtensionRange[1]));
		return buffer.toString();
	}
}
