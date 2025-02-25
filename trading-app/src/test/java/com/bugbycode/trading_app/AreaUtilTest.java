package com.bugbycode.trading_app;

import java.util.Date;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.bugbycode.module.Inerval;
import com.bugbycode.module.Klines;
import com.bugbycode.module.area.ConsolidationArea;
import com.bugbycode.repository.klines.KlinesRepository;
import com.bugbycode.service.klines.KlinesService;
import com.util.ConsolidationAreaUtil;
import com.util.DateFormatUtil;
import com.util.PriceUtil;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest
public class AreaUtilTest {

    private final Logger logger = LogManager.getLogger(AreaUtilTest.class);

    @Autowired
    private KlinesService klinesService;

    @Autowired
    private KlinesRepository klinesRepository;

    @Test
    public void testConsolidationArea(){
    	String pair = "BNBUSDT";
    	List<Klines> list = klinesRepository.findLastKlinesByPair(pair, Inerval.INERVAL_1D.getDescption(), 100);
        //logger.info(list);
    	ConsolidationAreaUtil cau = new ConsolidationAreaUtil(list);
        /*
        ConsolidationArea area = null;
        while(!(area = cau.getConsolidationArea()).isEmpty() ){
            logger.info(area);
        } */

        ConsolidationArea area = cau.getConsolidationArea();

        logger.info(area);
        /*
        //盘整区之后开始时间
		Date startTime = DateFormatUtil.getStartTimeBySetDay(new Date(area.getEndKlinesStartTime()), 1);
		
        //盘整区之后的所有15分钟级别k线信息
		List<Klines> list_15m = klinesRepository.findByPairAndGtStartTime(pair, startTime.getTime(), Inerval.INERVAL_15M.getDescption());
		//当天所有15分钟级别k线信息
		List<Klines> list_today_15m = PriceUtil.getTodayKlines(list_15m);
        logger.info(list_15m);
        logger.info(list_today_15m); */
    }
}
