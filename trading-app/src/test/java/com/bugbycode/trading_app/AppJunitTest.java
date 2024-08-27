package com.bugbycode.trading_app;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.bugbycode.module.FibCode;
import com.bugbycode.module.FibInfo;
import com.bugbycode.module.FibLevel;
import com.bugbycode.module.Inerval;
import com.bugbycode.module.Klines;
import com.bugbycode.module.QUERY_SPLIT;
import com.bugbycode.service.KlinesService;
import com.util.DateFormatUtil;
import com.util.PriceUtil;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest
public class AppJunitTest {

	private final Logger logger = LogManager.getLogger(AppJunitTest.class);

	@Autowired
	private KlinesService klinesService;
	
	@Test
	public void testMain() throws Exception {
		/*
		Date now = new Date();
		String pair = "BTCUSDT";
		
		//近1年日线级别k线信息
		List<Klines> klinesList_365_x_day = klinesService.continuousKlines1Day(pair, now, 4 * 365, QUERY_SPLIT.NOT_ENDTIME);
		
		if(klinesList_365_x_day.isEmpty()) {
			logger.info("无法获取" + pair + "交易对最近4年日线级别K线信息");
		}
		
		int offset = klinesList_365_x_day.size() - 1;
		
		Klines firstKlines = PriceUtil.getConsolidationAreaFirstKlines(klinesList_365_x_day);
		Klines lastKlines = klinesList_365_x_day.get(offset);
		
		//logger.info(firstKlines);
		//logger.info(lastKlines);
		
		long fibStartTime = firstKlines.getStarTime();
		long fibEndTime = lastKlines.getEndTime();
		
		//logger.info(DateFormatUtil.format(fibEndTime));
		
		List<Klines> fibKlinesList = klinesService.continuousKlines(pair, fibStartTime, fibEndTime, Inerval.INERVAL_4H.getDescption(), QUERY_SPLIT.NOT_ENDTIME);
		
		if(fibKlinesList.isEmpty()) {
			logger.info("无法获取" + pair + "交易对最近4小时级别K线信息");
		}
		
		Klines fibLowKlines = PriceUtil.getMinPriceKLine(fibKlinesList,fibStartTime, fibEndTime);
		Klines fibHightKlines = PriceUtil.getMaxPriceKLine(fibKlinesList,fibStartTime, fibEndTime);
		
		FibInfo fibInfo = new FibInfo(fibLowKlines, fibHightKlines, fibLowKlines.getDecimalNum(),FibLevel.LEVEL_1);
		logger.info(fibInfo);
		*/
		FibCode[] codes = FibCode.values();
		//FIB1 FIB786 FIB66 FIB618 FIB5 FIB382 FIB236 
		for(int offset = 0;offset < codes.length;offset++) {
			logger.info(codes[offset]);
		}
	}
	
}
