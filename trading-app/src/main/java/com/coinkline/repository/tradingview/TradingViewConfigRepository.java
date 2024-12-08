package com.coinkline.repository.tradingview;

import com.coinkline.module.config.TradingViewConfig;

public interface TradingViewConfigRepository {

	/**
	 * 根据拥有者查询配置
	 * @param owner
	 * @return
	 */
	public TradingViewConfig queryByOwner(String owner);
	
	/**
	 * 保存配置信息
	 * @param config
	 */
	public TradingViewConfig save(TradingViewConfig config);
	
}
