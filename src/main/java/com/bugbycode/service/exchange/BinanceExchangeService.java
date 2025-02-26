package com.bugbycode.service.exchange;

import java.util.Set;

public interface BinanceExchangeService {

	/**
	 * 获取所有永续合约交易对信息
	 * @return
	 */
	public Set<String> exchangeInfo();
}
