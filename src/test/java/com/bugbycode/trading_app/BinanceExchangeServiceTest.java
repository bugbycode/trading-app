package com.bugbycode.trading_app;

import java.util.List;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.bugbycode.binance.module.eoptions.EoptionContracts;
import com.bugbycode.config.AppConfig;
import com.bugbycode.module.Inerval;
import com.bugbycode.module.binance.ContractType;
import com.bugbycode.module.binance.SymbolExchangeInfo;
import com.bugbycode.service.exchange.BinanceExchangeService;
import com.util.CoinPairSet;

@SpringBootTest
public class BinanceExchangeServiceTest {

	private final Logger logger = LogManager.getLogger(BinanceExchangeServiceTest.class);
	
	@Autowired
    private BinanceExchangeService binanceExchangeService;

    @BeforeEach
	public void befor() {
		AppConfig.DEBUG = true;
		System.setProperty("https.proxyHost", "localhost");
		System.setProperty("https.proxyPort", "50000");
	}
    
    @Test
    public void testExchangeInfo() {
    	Set<SymbolExchangeInfo> symbols = binanceExchangeService.exchangeInfo();
    	for(SymbolExchangeInfo info : symbols) {
    		logger.info("{} - {} - {}" , info.getSymbol(), info.getBaseAsset(), info.getMarginAsset());
    	}
    	logger.info("symbol total: {}", symbols.size());
    }
    
    @Test
    public void testCoinSet() {
    	Set<SymbolExchangeInfo> symbols = binanceExchangeService.exchangeInfo();
    	for(SymbolExchangeInfo info : symbols) {
    		CoinPairSet set = new CoinPairSet(Inerval.INERVAL_15M, ContractType.PERPETUAL);
    		set.add(info);
    		logger.info(set.getStreamName());
    	}
    }
    
    @Test
    public void testEOptionsExchangeInfo() {
    	List<EoptionContracts> list = binanceExchangeService.eOptionsExchangeInfo();
    	for(EoptionContracts ec : list) {
    		logger.info(ec);
    	}
    }
    
    @Test
    public void testDecimalNum() {
    	Set<SymbolExchangeInfo> symbols = binanceExchangeService.exchangeInfo();
    	for(SymbolExchangeInfo info : symbols) {
    		logger.info("{} - {}" , info.getSymbol(), info.getTickSize());
    	}
    }
    
    @Test
    public void testEOptionsExchangeInfoSymbol() {
    	List<SymbolExchangeInfo> list = binanceExchangeService.eOptionsExchangeInfoSymbol();
    	for(SymbolExchangeInfo info : list) {
    		CoinPairSet set = new CoinPairSet(Inerval.INERVAL_15M, info.getContractType());
    		set.add(info);
    		logger.info("{} - {}", info.getUnderlying(), set.getStreamName());
    	}
    }
}
