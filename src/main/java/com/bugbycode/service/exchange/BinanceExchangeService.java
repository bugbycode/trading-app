package com.bugbycode.service.exchange;

import java.util.Set;

import com.bugbycode.module.binance.SymbolExchangeInfo;

public interface BinanceExchangeService {

	/**
	 * 获取所有永续合约交易对信息
	 * @return
	 */
	public Set<SymbolExchangeInfo> exchangeInfo();
}
