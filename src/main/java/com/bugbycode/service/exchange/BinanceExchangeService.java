package com.bugbycode.service.exchange;

import java.util.List;
import java.util.Set;

import com.bugbycode.binance.module.eoptions.EoptionContracts;
import com.bugbycode.module.binance.SymbolExchangeInfo;

public interface BinanceExchangeService {

	/**
	 * 获取所有永续合约交易对信息
	 * @return
	 */
	public Set<SymbolExchangeInfo> exchangeInfo();
	
	/**
	 * 获取所有期权交易合约底层资产信息
	 * @return
	 */
	public List<EoptionContracts> eOptionsExchangeInfo();
	
	/**
	 * 获取所有期权交易对信息
	 * @return
	 */
	public List<SymbolExchangeInfo> eOptionsExchangeInfoSymbol();
}
