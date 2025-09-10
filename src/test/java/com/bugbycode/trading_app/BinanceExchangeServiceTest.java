package com.bugbycode.trading_app;

import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.bugbycode.config.AppConfig;
import com.bugbycode.service.exchange.BinanceExchangeService;

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
    	Set<String> symbols = binanceExchangeService.exchangeInfo();
    	for(String symbol : symbols) {
    		logger.info(symbol);
    	}
    }
}
