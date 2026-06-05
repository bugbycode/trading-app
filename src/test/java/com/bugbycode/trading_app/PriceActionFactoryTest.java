package com.bugbycode.trading_app;

import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.util.CollectionUtils;

import com.bugbycode.config.AppConfig;
import com.bugbycode.factory.priceAction.PriceActionFactory;
import com.bugbycode.factory.priceAction.impl.PriceActionFactoryImpl;
import com.bugbycode.module.FibCode;
import com.bugbycode.module.FibInfo;
import com.bugbycode.module.FibLevel;
import com.bugbycode.module.Inerval;
import com.bugbycode.module.Klines;
import com.bugbycode.module.QuotationMode;
import com.bugbycode.module.TradeTrend;
import com.bugbycode.module.price.OpenPrice;
import com.bugbycode.repository.klines.KlinesRepository;
import com.util.PriceUtil;

import jakarta.annotation.Resource;

@SpringBootTest
public class PriceActionFactoryTest {

	private final Logger logger = LogManager.getLogger(PriceActionFactoryTest.class);
	
	@Resource
	private KlinesRepository klinesRepository;
	
	@BeforeEach
	public void testBefore() {
		AppConfig.DEBUG = true;
	}
	
	@Test
    public void testPriceAction(){
		
		logger.info("start testPriceAction.");
		
        String pair = "BTWUSDT";
        
        //List<Klines> list_trend = klinesRepository.findLastKlinesByPair(pair, Inerval.INERVAL_1H, 1500);
        List<Klines> list = klinesRepository.findLastKlinesByPair(pair, Inerval.INERVAL_1H, 1500);
        
        logger.info("execute findLastKlinesByPair() 1h finish.");
        
        List<Klines> list_15m = klinesRepository.findLastKlinesByPair(pair, Inerval.INERVAL_15M,1500);
        
        logger.info("execute findLastKlinesByPair() 15m finish.");
        
        PriceActionFactory factory = new PriceActionFactoryImpl(list, list_15m);
        
        logger.info("init factory finish.");
        
        if(!(factory.isLong() || factory.isShort())) {
            return;
        }
        
        OpenPrice openPrice = factory.getOpenPrice();
        
        QuotationMode mode = factory.isLong() ? QuotationMode.LONG : QuotationMode.SHORT;

        logger.info("{}: {}", mode, openPrice);
    }
}
