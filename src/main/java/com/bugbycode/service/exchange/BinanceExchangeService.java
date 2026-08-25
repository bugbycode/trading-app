package com.bugbycode.service.exchange;

import java.util.List;

import com.bugbycode.binance.module.eoptions.EoptionContracts;
import com.bugbycode.module.binance.SymbolExchangeInfo;

public interface BinanceExchangeService {

	/**
	 * 获取所有永续合约交易对信息
	 * @return
	 */
	public List<SymbolExchangeInfo> exchangeInfo();
	
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
	
	/**
	 * 获取所有比本位永续合约交易对信息
	 * @return
	 */
	public List<SymbolExchangeInfo> exchangeInfoUsd();
	
	/**
	 * 校验标的资产是否上市币本位合约
	 * @param baseAsset 标的资产 如：BTC、ETH
	 * @return
	 */
	public boolean verifyCoin(String baseAsset);
}
