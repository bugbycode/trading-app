package com.bugbycode.trading_app;


import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Date;
import java.util.List;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.util.CollectionUtils;

import com.bugbycode.config.AppConfig;
import com.bugbycode.factory.area.AreaFactory;
import com.bugbycode.factory.area.impl.AreaFactoryImpl;
import com.bugbycode.factory.area.impl.AreaFactoryImpl_v2;
import com.bugbycode.factory.fibInfo.FibInfoFactory;
import com.bugbycode.factory.fibInfo.impl.FibInfoFactoryImpl;
import com.bugbycode.factory.priceAction.PriceActionFactory;
import com.bugbycode.factory.priceAction.impl.PriceActionFactoryImpl;
import com.bugbycode.module.FibCode;
import com.bugbycode.module.FibInfo;
import com.bugbycode.module.FibLevel;
import com.bugbycode.module.Inerval;
import com.bugbycode.module.Klines;
import com.bugbycode.module.LongOrShortType;
import com.bugbycode.module.QUERY_SPLIT;
import com.bugbycode.module.QuotationMode;
import com.bugbycode.module.price.OpenPrice;
import com.bugbycode.repository.klines.KlinesRepository;
import com.bugbycode.service.exchange.BinanceExchangeService;
import com.bugbycode.service.klines.KlinesService;
import com.util.ConsolidationAreaFibUtil;
import com.util.DateFormatUtil;
import com.util.EmaFibUtil;
import com.util.PriceUtil;

@SpringBootTest
public class KlinesServiceTest {

    private final Logger logger = LogManager.getLogger(KlinesServiceTest.class);

    @Autowired
    private KlinesService klinesService;

    @Autowired
    private KlinesRepository klinesRepository;

    @Autowired
    private BinanceExchangeService binanceExchangeService;

    @BeforeEach
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
        String pair = "币安人生USDT";
        List<Klines> klines_list_15m = klinesRepository.findLastKlinesByPair(pair, Inerval.INERVAL_15M,20);
        
        klines_list_15m.remove(PriceUtil.getLastKlines(klines_list_15m)) ;

        for(Klines k : klines_list_15m) {
            logger.info(k);
        }
        //klinesService.volumeMonitor(klines_list_15m);
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
    public void testSyncKlines() throws UnsupportedEncodingException {
        String pair = "币安人生USDT";
        List<Klines> list = klinesService.continuousKlines15M(pair, new Date(), 1, QUERY_SPLIT.ALL);
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
        String pair = "ETHUSDT";
        //List<Klines> list_1d = klinesRepository.findLastKlinesByPair(pair, Inerval.INERVAL_1D,1500);
        //List<Klines> list_4h = klinesRepository.findLastKlinesByPair(pair, Inerval.INERVAL_4H,1500);
        List<Klines> list_1h = klinesRepository.findLastKlinesByPair(pair, Inerval.INERVAL_1H, 1500);
        List<Klines> list_15m = klinesRepository.findLastKlinesByPair(pair, Inerval.INERVAL_15M,1500);
		
        Klines last_15m = PriceUtil.getLastKlines(list_15m);
        
        //List<Klines> klines_list_1h = PriceUtil.to1HFor15MKlines(list_15m);
        
        //logger.info(klines_list_1h);
        
        FibInfoFactory factory = new FibInfoFactoryImpl(list_1h, list_1h, list_15m);
        
        if(!(factory.isLong() || factory.isShort())) {
        	return;
        }
        
		//logger.info(PriceUtil.getLastKlines(list));
		FibInfo fibInfo = factory.getFibInfo();
		//FibInfo parentFibInfo = factory.getParentFibInfo();
		//FibInfo fibInfo_parent = factory.getFibInfo_parent();

        if(fibInfo != null) {
            List<Klines> fibAfterKlines = fibInfo.getFibAfterKlines();
            if(!CollectionUtils.isEmpty(fibAfterKlines)) {
                for(Klines k : fibAfterKlines) {
                    logger.info(k);
                }
            }
            
            logger.info("============================================================");
            fibAfterKlines = factory.getFibAfterKlines();
            if(!CollectionUtils.isEmpty(fibAfterKlines)) {
                for(Klines k : fibAfterKlines) {
                    logger.info(k);
                }
            }
            
            //logger.info(parentFibInfo);
            logger.info(fibInfo);
            QuotationMode mode = fibInfo.getQuotationMode();
            if(mode == QuotationMode.LONG) {
                logger.info(factory.isLong());
            } else {
                logger.info(factory.isShort());
            }
            
            //logger.info(fibInfo.getTakeProfit_v2(FibCode.FIB786));
            //logger.info(fibInfo.getNextFibCode(FibCode.FIB786));
            //logger.info(fibInfo.getEndCode());
            //logger.info(fibInfo.getLevel().getStartFibCode());
            //logger.info(FibCode.FIB382.lte(fibInfo.getEndCode()));
            List<OpenPrice> openPrices = factory.getOpenPrices();
            for(OpenPrice price : openPrices) {
                logger.info("{} - {} ~ {}, istrade: {}, verifyOpenPrice: {}", price, fibInfo.getNextFibCode(price.getCode()), fibInfo.getTakeProfit_v2(price.getCode()), 
                		PriceUtil.isTraded(price.getCode(), fibInfo), fibInfo.verifyOpenPrice(price, last_15m.getClosePriceDoubleValue()));
            }

            //logger.info(fibInfo.getFibCode(factory.getOpenPrices().get(0)));
        }
        //logger.info(PriceUtil.isTraded(FibCode.FIB618, fibInfo));
        //logger.info(fibInfo.getNextFibCode(FibCode.FIB2));
        //logger.info(fibInfo.getTakeProfit_v2(FibCode.FIB2));
    }
    
    @Test
    public void testAreaFibInfo(){
    	String pair = "ETHUSDT";
        List<Klines> list_1h = klinesRepository.findLastKlinesByPair(pair, Inerval.INERVAL_1H,500);
        List<Klines> list_15m = klinesRepository.findLastKlinesByPair(pair, Inerval.INERVAL_15M,500);
        AreaFactory factory = new AreaFactoryImpl_v2(list_1h, list_15m);
        if(!(factory.isLong() || factory.isShort())) {
        	return;
        }
        
        List<Klines> fibAfKlines = factory.getFibAfterKlines();

        if(!CollectionUtils.isEmpty(fibAfKlines)) {
            for(Klines k : fibAfKlines) {
                logger.info(k);
            }
        }
        
        List<OpenPrice> openPrices = factory.getOpenPrices();
        
        for(OpenPrice price : openPrices) {
        	logger.info("{} -> {} ~ {}, istread: {}", price, price.getFirstTakeProfit(), price.getSecondTakeProfit(), PriceUtil.isTraded(price, factory));
        }
    }

    @Test
    public void testPriceAction(){
        String pair = "BTCUSDT";
        List<Klines> list_1h = klinesRepository.findLastKlinesByPair(pair, Inerval.INERVAL_1H,500);
        List<Klines> list_15m = klinesRepository.findLastKlinesByPair(pair, Inerval.INERVAL_15M,500);
        PriceActionFactory factory = new PriceActionFactoryImpl(list_1h, list_15m);
        
        if(!(factory.isLong() || factory.isShort())) {
            return;
        }

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

    @Test
    public void testEmaFactory() {
       String pair = "IDOLUSDT";
       
    }

    @Test
    public void testPriceActionTakeProfit() {
        FibInfo fibInfo = new FibInfo(1, 2, 2, FibLevel.LEVEL_0);
        FibCode[] codes = FibCode.values();
        for(FibCode code : codes) {
            logger.info("{} - {},{}", code, fibInfo.getPriceActionTakeProfit_nextCode(code), fibInfo.getPriceActionTakeProfit_v1(code));
        }
    }

    @Test
    public void testParse(){
        String pair = "ETHUSDT";
        List<Klines> klines_list_1d = klinesRepository.findLastKlinesByPair(pair, Inerval.INERVAL_1D, 5000);
        List<Klines> list_15m = klinesRepository.findLastKlinesByPair(pair, Inerval.INERVAL_15M,5000);
        //合并成昨日的日线级别
        int hours = DateFormatUtil.getHours(new Date().getTime());
        Date lastDayStartTimeDate = DateFormatUtil.getStartTime(hours);//前一天K线起始时间 yyyy-MM-dd 08:00:00
        Date lastDayEndTimeDate = DateFormatUtil.getEndTime(hours);//前一天K线结束时间 yyyy-MM-dd 07:59:59
        List<Klines> lastDay_15m = PriceUtil.subListForBetweenStartTimeAndEndTime(list_15m, lastDayStartTimeDate.getTime(), lastDayEndTimeDate.getTime() + 1000);

        Klines lastDay = PriceUtil.getLastKlines(klines_list_1d);

        if(!CollectionUtils.isEmpty(lastDay_15m)) {

            for(Klines k : lastDay_15m) {
                logger.info(k);
            }

            Klines parseKlines_1d = PriceUtil.parse(lastDay_15m, Inerval.INERVAL_1D);

            if(!lastDay.isEquals(parseKlines_1d)) {
                klinesRepository.insert(parseKlines_1d);
            } else {
                logger.info("日线级别已是最新数据");
                logger.info(lastDay);
            }
            logger.info(parseKlines_1d);
        }

    }

    @Test
    public void testKlines(){
        String pair = "ETHUSDT";
        List<Klines> klines_list_1d = klinesRepository.findLastKlinesByPair(pair, Inerval.INERVAL_1D, 5000);
        for(Klines k : klines_list_1d) {
            logger.info(k);
        }
    }

    @Test
    public void testConsolidationAreaFib() {
        String pair = "JASMYUSDT";
        List<Klines> list_h = klinesRepository.findLastKlinesByPair(pair, Inerval.INERVAL_1H, 5000);
        List<Klines> list_15m = klinesRepository.findLastKlinesByPair(pair, Inerval.INERVAL_15M,5000);
        
        ConsolidationAreaFibUtil ca = new ConsolidationAreaFibUtil(list_h, list_15m);
        
        FibInfo fibInfo = ca.getFibInfo();
        
        if(fibInfo == null) {
            return;
        }

        List<Klines> fibAfterKlines = fibInfo.getFibAfterKlines();
        if(!CollectionUtils.isEmpty(fibAfterKlines)) {
        	for(Klines k : fibAfterKlines) {
        		logger.info(k);
        	}
        }
        
        logger.info(fibInfo);
    }

    @Test
    public void testQueryKlines() {
        Date now = new Date();
        now = DateFormatUtil.parse(DateFormatUtil.format_yyyy_mm_dd_HH_00_00(now));
        String pair = "BTCUSDT";
        List<Klines> list_4h = klinesService.continuousKlines4H(pair, now, 1500, QUERY_SPLIT.ALL);
        Klines klines_last_4h = PriceUtil.getLastKlines(list_4h);
        if(!PriceUtil.verifyKlines(klines_last_4h)) {
            list_4h.remove(klines_last_4h);
        }
        for(Klines k : list_4h) {
            logger.info(k);
            logger.info(PriceUtil.verifyKlines(k));
        }
        /*
        List<Klines> klines_list_1h = klinesRepository.findLastKlinesByPair(pair, Inerval.INERVAL_1H, 10);
        klines_list_1h.remove(klines_list_1h.get(klines_list_1h.size() - 1));
        //klines_list_1h.remove(klines_list_1h.get(klines_list_1h.size() - 1));
        logger.info(klines_list_1h);
        Klines k_4h = PriceUtil.parse1Hto4H(klines_list_1h);
        logger.info(k_4h);
        logger.info(PriceUtil.verifyKlines(k_4h)); */
    }

    @Test
    public void testMacd() {
        String pair = "BTCUSDT";
        List<Klines> list_h = klinesRepository.findLastKlinesByPair(pair, Inerval.INERVAL_1H,5000);
        PriceUtil.calculateMACD(list_h);

        for(Klines k : list_h) {
            logger.info("dea:{}", k.getDea());
            logger.info("dif:{}", k.getDif());
            logger.info("macd:{}", k.getMacd());
        }
    }
    
    @Test
    public void testDeltaAndCVD() {
        String pair = "WETUSDT";
        List<Klines> list = klinesRepository.findLastKlinesByPair(pair, Inerval.INERVAL_1H,5000);
        PriceUtil.calculateDeltaAndCvd(list);
        for(Klines k : list) {
        	logger.info("{}, {}, {}, {}, {}",k.getPair(), k.getInterval(),DateFormatUtil.format(k.getStartTime()) , k.getDelta(), k.getCvd());
        }
    }

    @Test
    public void testBand() {
    	String pair = "ETHUSDT";
        List<Klines> list = klinesRepository.findLastKlinesByPair(pair, Inerval.INERVAL_1H,5000);
        PriceUtil.calculateBollingerBands(list);
        for(Klines k : list) {
        	logger.info(k);
        }
    }
    
    @Test
    public void testVolumeMonitor() {
    	String pair = "ETHUSDT";
        //List<Klines> list_1d = klinesRepository.findLastKlinesByPair(pair, Inerval.INERVAL_1D,1500);
      //  List<Klines> list_4h = klinesRepository.findLastKlinesByPair(pair, Inerval.INERVAL_4H,1500);
       // List<Klines> list_1h = klinesRepository.findLastKlinesByPair(pair, Inerval.INERVAL_1H,1500);
        List<Klines> list_15m = klinesRepository.findLastKlinesByPair(pair, Inerval.INERVAL_15M,1500);
        klinesService.volumeMonitor(null, null, null, list_15m);
    }
    
    @Test
    public void testIsEmpty() {
    	String pair = "ETHUSDT";
    	boolean isEmpty = klinesRepository.isEmpty(pair, Inerval.INERVAL_15M);
    	logger.info(isEmpty);
    }
}
