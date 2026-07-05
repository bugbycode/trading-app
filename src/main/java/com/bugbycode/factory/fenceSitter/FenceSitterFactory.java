package com.bugbycode.factory.fenceSitter;

import com.bugbycode.module.price.OpenPrice;

/**
 * 墙头草策略接口
 */
public interface FenceSitterFactory {

	/**
	 * 获取开仓价
	 * @return
	 */
	public OpenPrice getOpenPrice();
	
	public boolean isLong();
	
	public boolean isShort();
	
	/**
	 * 是否已到达建议平仓价
	 * @return
	 */
	public boolean isClosePosition();
}
