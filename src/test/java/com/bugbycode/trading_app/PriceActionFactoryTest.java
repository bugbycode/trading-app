package com.bugbycode.trading_app;

import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.util.CollectionUtils;

import com.bugbycode.config.AppConfig;
import com.bugbycode.factory.priceAction.PriceActionFactory;
import com.bugbycode.factory.priceAction.impl.PriceActionFactoryImpl;
import com.bugbycode.module.FibCode;
import com.bugbycode.module.FibInfo;
import com.bugbycode.module.Inerval;
import com.bugbycode.module.Klines;
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
		
        String pair = "ETHUSDT";
        
        List<Klines> list = klinesRepository.findLastKlinesByPair(pair, Inerval.INERVAL_1H,1500);
        
        logger.info("execute findLastKlinesByPair() 1h finish.");
        
        List<Klines> list_15m = klinesRepository.findLastKlinesByPair(pair, Inerval.INERVAL_15M,1500);
        
        logger.info("execute findLastKlinesByPair() 15m finish.");
        
        PriceActionFactory factory = new PriceActionFactoryImpl(list, list_15m);
        
        logger.info("init factory finish.");
        
        if(!(factory.isLong() || factory.isShort())) {
            return;
        }
        
        logger.info(factory);

        List<Klines> fibAfKlines = factory.getFibAfterKlines();

        if(!CollectionUtils.isEmpty(fibAfKlines)) {
            for(Klines k : fibAfKlines) {
                logger.info(k);
            }
        }

        FibInfo fibInfo = factory.getFibInfo();
        List<OpenPrice> openPrices = factory.getOpenPrices();

        for(OpenPrice price : openPrices) {
            FibCode code = fibInfo.getFibCode(price.getPrice());
            FibCode profiCodeNext = fibInfo.getPriceActionTakeProfit_nextCode(code);
            FibCode profitCode = fibInfo.getPriceActionTakeProfit_v1(code);
            logger.info("{}({}) -> {}({}) ~ {}({}), isTreade:{}", code, price.getPrice(), profiCodeNext, fibInfo.getFibValue(profiCodeNext), profitCode, fibInfo.getFibValue(profitCode), 
                PriceUtil.isTradedPriceAction(price.getPrice(), fibInfo));
        }

        logger.info(factory.getFibInfo());
        //logger.info(factory.verifyOpen(list_15m));
        //logger.info(factory.getFibAfterKlines());
    }
}
