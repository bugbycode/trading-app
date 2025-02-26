package com.bugbycode.trading_app;


import java.util.Date;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.util.CollectionUtils;

import com.bugbycode.module.FibInfo;
import com.bugbycode.module.Inerval;
import com.bugbycode.module.Klines;
import com.bugbycode.module.QUERY_SPLIT;
import com.bugbycode.repository.klines.KlinesRepository;
import com.bugbycode.service.klines.KlinesService;
import com.util.DateFormatUtil;
import com.util.FibUtil;
import com.util.FibUtil_v2;
import com.util.FibUtil_v3;
import com.util.PriceUtil;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest
public class TestKlinesService {

    private final Logger logger = LogManager.getLogger(TestKlinesService.class);

    @Autowired
    private KlinesService klinesService;

    @Autowired
    private KlinesRepository klinesRepository;

    @Before
	public void befor() {
		
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
        String pair = "BTCUSDT";
        List<Klines> klines_list_1d = klinesRepository.findLastKlinesByPair(pair, Inerval.INERVAL_1D.getDescption(),3);
        
        klines_list_1d.remove(klines_list_1d.size() - 1);
        klines_list_1d.remove(klines_list_1d.size() - 1);

        logger.info(klines_list_1d);

        if(!CollectionUtils.isEmpty(klines_list_1d)) {
            Klines last = PriceUtil.getLastKlines(klines_list_1d);
            //logger.info(last.verifyInterval(Inerval.INERVAL_15M));
            if(PriceUtil.verifyLastDay(last)){
               return; 
            }

            //获取需要更新的时间段信息
			long startTime = DateFormatUtil.parse(DateFormatUtil.format(last.getEndTime())).getTime() + 1000;
            long endTime = DateFormatUtil.getEndTime(DateFormatUtil.getHours(now.getTime())).getTime();
            logger.info(new Date(startTime));
            logger.info(new Date(endTime));
            List<Klines> list_day = klinesService.continuousKlines(last.getPair(), startTime, endTime, Inerval.INERVAL_1D.getDescption(), QUERY_SPLIT.NOT_ENDTIME);
            logger.info(list_day);
        }
        //Date start = DateFormatUtil.getTodayStartTime(now);
        //start = DateFormatUtil.parse(DateFormatUtil.format_yyyy_mm_dd_HH_mm_00(start));
        /*start = DateFormatUtil.parse(DateFormatUtil.format(start.getTime() + 150000));
        logger.info(DateFormatUtil.format(start));
        start = DateFormatUtil.parse(DateFormatUtil.format_yyyy_mm_dd_HH_mm_00(start));
        */
        /*logger.info(DateFormatUtil.format(start));
        logger.info(DateFormatUtil.verifyLastDayStartTime(start));

        logger.info(DateFormatUtil.getEndTime(DateFormatUtil.getHours(now.getTime())));*/
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
        
        List<Klines> list_day = klinesRepository.findLastKlinesByPair(pair, Inerval.INERVAL_1D.getDescption(), 1500);
        if(klinesService.verifyUpdateDayKlines(list_day)){
            list_day = klinesRepository.findLastKlinesByPair(pair, Inerval.INERVAL_1D.getDescption(), 1500);
            klinesService.checkData(list_day);
        } 
    }

    @Test
    public void testFibUtil() {
        String pair = "AVAUSDT";
        List<Klines> list = klinesRepository.findLastKlinesByPair(pair, Inerval.INERVAL_15M.getDescption(), 3000);
        FibUtil fu = new FibUtil(list);
        logger.info(fu.getFibInfo());
    }
    
    @Test
    public void testDeclineAndStrengthCheck() {
    	String pair = "BTCUSDT";
    	List<Klines> list = klinesRepository.findLastKlinesByPair(pair, Inerval.INERVAL_1D.getDescption(), 5000);
        List<Klines> list_15m = klinesRepository.findLastKlinesByPair(pair, Inerval.INERVAL_15M.getDescption(), 5000);

        Klines last = PriceUtil.getLastKlines(list);

        List<Klines> today_list = PriceUtil.getLastDayAfterKline(last, list_15m);

        logger.info(today_list);
        logger.info(today_list.size());
    	
        FibUtil_v3 fu = new FibUtil_v3(list);
        FibInfo fibInfo = fu.getFibInfo();
        FibInfo secondFibInfo = fu.getSecondFibInfo(fibInfo);
        FibInfo thirdFibInfo = fu.getThirdFibInfo(secondFibInfo);
        FibInfo fourthFibInfo = fu.getFourthFibInfo(thirdFibInfo);

        logger.info(fu.getAfterFlag());
        logger.info(fu.getSecondFibAfterFlag());
        logger.info(fu.getThirdFibAfterFlag());
        logger.info(fu.getFourthFibAfterFlag());

        logger.info(fibInfo);
        logger.info(secondFibInfo);
        logger.info(thirdFibInfo); 
        logger.info(fourthFibInfo);
    }

    @Test
    public void testDeclineAndStrengthCheck_v3() {
        String pair = "AIXBTUSDT";
    	List<Klines> list = klinesRepository.findLastKlinesByPair(pair, Inerval.INERVAL_15M.getDescption(), 5000);

        List<Klines> klinesList = PriceUtil.to1HFor15MKlines(list);

        FibUtil_v2 fu = new FibUtil_v2(klinesList);
        FibInfo fibInfo = fu.getFibInfo();
        FibInfo secondFibInfo = fu.getSecondFibInfo(fibInfo);

        logger.info(fibInfo);

        logger.info(secondFibInfo);
    }
}
