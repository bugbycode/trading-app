package com.bugbycode.trading_app;

import java.util.List;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.bugbycode.module.FibInfo;
import com.bugbycode.module.Inerval;
import com.bugbycode.module.Klines;
import com.bugbycode.repository.klines.KlinesRepository;
import com.bugbycode.service.exchange.BinanceExchangeService;
import com.bugbycode.service.klines.KlinesService;
import com.util.PriceUtil;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest
public class TestEmaFibInfo {

    private final Logger logger = LogManager.getLogger(TestEmaFibInfo.class);

    @Autowired
    private KlinesService klinesService;

    @Autowired
    private KlinesRepository klinesRepository;
    
    @Autowired
    private BinanceExchangeService binanceExchangeService;

    @Before
	public void befor() {
		
		System.setProperty("https.proxyHost", "localhost");
		System.setProperty("https.proxyPort", "50000");
	}
    
    @Test
    public void testEmaFibInfo(){
        String pair = "IOTXUSDT";
        List<Klines> list = klinesRepository.findLastKlinesByPair(pair, Inerval.INERVAL_15M.getDescption(), 1500);

        FibInfo fibInfo = PriceUtil.getFibInfoForEma(list);

        logger.info(fibInfo);
    }
    
    @Test
    public void testExchangePair() {
    	Set<String> set = binanceExchangeService.exchangeInfo();
    	logger.info(set);
    }
}
