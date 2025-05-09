package com.bugbycode.trading_app;


import java.util.Date;
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
import org.springframework.util.CollectionUtils;

import com.bugbycode.config.AppConfig;
import com.bugbycode.module.FibCode;
import com.bugbycode.module.FibInfo;
import com.bugbycode.module.Inerval;
import com.bugbycode.module.Klines;
import com.bugbycode.module.QUERY_SPLIT;
import com.bugbycode.repository.klines.KlinesRepository;
import com.bugbycode.service.exchange.BinanceExchangeService;
import com.bugbycode.service.klines.KlinesService;
import com.util.ConsolidationAreaFibUtil;
import com.util.DateFormatUtil;
import com.util.EmaFibUtil;
import com.util.FibInfoFactory;
import com.util.PriceUtil;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest
public class TestKlinesService {

    private final Logger logger = LogManager.getLogger(TestKlinesService.class);

    @Autowired
    private KlinesService klinesService;

    @Autowired
    private KlinesRepository klinesRepository;

    @Autowired
    private BinanceExchangeService binanceExchangeService;

    @Before
	public void befor() {
		AppConfig.DEBUG = true;
		System.setProperty("https.proxyHost", "localhost");
		System.setProperty("https.proxyPort", "50000");
	}

    @Test
    public void testQueryPrice(){
    	String price = klinesService.getClosePrice("AEROUSDT", Inerval.INERVAL_15M);
    	logger.info(price);
    }

    @Test
    public void testQuery() {
        Date now = new Date();
        String pair = "PIPPINUSDT";
        List<Klines> klines_list_15m = klinesRepository.findLastKlinesByPair(pair, Inerval.INERVAL_15M,10);
        
        klines_list_15m.remove(PriceUtil.getLastKlines(klines_list_15m)) ;

        for(Klines k : klines_list_15m) {
            logger.info(k);
        }
        klinesService.volumeMonitor(klines_list_15m);
    }

    @Test
    public void testDelete(){
        String pair = "BNBUSDT";
        /*Date now = new Date();
        Date start = DateFormatUtil.getStartTimeBySetDay(now, -10);
        List<Klines> list = klinesRepository.findByPairAndGtStartTime(pair, start.getTime(), Inerval.INERVAL_1D.getDescption());
        for(Klines k : list){
            klinesRepository.remove(k.getId());
            logger.info("已删除：" + k);
        }*/
        
        List<Klines> list_day = klinesRepository.findLastKlinesByPair(pair, Inerval.INERVAL_1D, 1500);
        if(klinesService.verifyUpdateDayKlines(list_day)){
            list_day = klinesRepository.findLastKlinesByPair(pair, Inerval.INERVAL_1D, 1500);
            klinesService.checkData(list_day);
        } 
    }

    @Test
    public void testFibUtil() {
        String pair = "AVAUSDT";
        List<Klines> list = klinesRepository.findLastKlinesByPair(pair, Inerval.INERVAL_15M, 3000);
        
    }
    
    @Test
    public void testDeclineAndStrengthCheck() {
    	String pair = "BTCUSDT";
    	List<Klines> list = klinesRepository.findLastKlinesByPair(pair, Inerval.INERVAL_1D, 5000);
        List<Klines> list_15m = klinesRepository.findLastKlinesByPair(pair, Inerval.INERVAL_15M, 5000);

        Klines last = PriceUtil.getLastKlines(list);

        List<Klines> today_list = PriceUtil.getLastDayAfterKline(last, list_15m);

        logger.info(today_list);
        logger.info(today_list.size());
    	
    }

    @Test
    public void testDeclineAndStrengthCheck_v3() {
        String pair = "AIXBTUSDT";
    	List<Klines> list = klinesRepository.findLastKlinesByPair(pair, Inerval.INERVAL_15M, 5000);

        List<Klines> klinesList = PriceUtil.to1HFor15MKlines(list);

        
    }

    @Test
    public void testSyncKlines() {
        String pair = "BTCUSDT";
        List<Klines> list = klinesService.continuousKlines15M(pair, new Date(), 10, QUERY_SPLIT.NOT_ENDTIME);
        logger.info(list);
    }

    @Test
    public void test15mTo1h(){
        String pair = "BTCUSDT";
        List<Klines> list = klinesRepository.findLastKlinesByPair(pair, Inerval.INERVAL_15M,500);
        List<Klines> list_1h = PriceUtil.to1HFor15MKlines(list);
        //logger.info(list_1h);
        Klines lastKlines = PriceUtil.getLastKlines(list_1h);
        int minute = DateFormatUtil.getMinute(lastKlines.getEndTime());
		if(minute != 59) {
			list_1h.remove(lastKlines);
		}
        for(Klines k : list_1h) {
            logger.info(k);
        }
    }

    @Test
    public void testFibInfo(){
        String pair = "SOLVUSDT";
        List<Klines> list_15m = klinesRepository.findLastKlinesByPair(pair, Inerval.INERVAL_15M,5000);
        List<Klines> klinesList_1h = PriceUtil.to1HFor15MKlines(list_15m);
		
		Klines last = PriceUtil.getLastKlines(klinesList_1h);
		
		int minute = DateFormatUtil.getMinute(last.getEndTime());
		if(minute != 59) {
			klinesList_1h.remove(last);
		}
		
		FibInfoFactory factory_v3 = new FibInfoFactory(klinesList_1h);
		
		FibInfo fibInfo = factory_v3.getFibInfo();
		
		List<Klines> fibAfterKlines = factory_v3.getFibAfterKlines();

        logger.info(fibInfo);
    }
}
