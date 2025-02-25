package com.bugbycode.trading_app;

import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.bugbycode.module.HighOrLowHitPrice;
import com.bugbycode.repository.high_low_hitprice.HighOrLowHitPriceRepository;
import com.util.PriceUtil;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest
public class TestHitPriceApp {

    private final Logger logger = LogManager.getLogger(TestHitPriceApp.class);

    @Autowired
    private HighOrLowHitPriceRepository highOrLowHitPriceRepository;

    @Test
    public void testQueryPrice(){
        List<HighOrLowHitPrice>  list = highOrLowHitPriceRepository.find("SOLUSDT");
        logger.info(list);
        logger.info(PriceUtil.getMax(list));
        logger.info(PriceUtil.getMin(list));
    }
}
