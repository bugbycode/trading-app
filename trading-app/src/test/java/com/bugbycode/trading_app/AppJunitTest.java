package com.bugbycode.trading_app;

import java.util.Date;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.query.Collation;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.bugbycode.module.EMAType;
import com.bugbycode.module.Inerval;
import com.bugbycode.module.Klines;
import com.bugbycode.module.QUERY_SPLIT;
import com.bugbycode.repository.klines.KlinesRepository;
import com.util.DateFormatUtil;
import com.util.PriceUtil;

import jakarta.annotation.Resource;


@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest
public class AppJunitTest {

	private final Logger logger = LogManager.getLogger(AppJunitTest.class);

	@Resource
	private KlinesRepository klinesRepository;

	@Test
	public void testMain() throws Exception {
		
	}

	@Test
	public void testMongo(){
		List<Klines> list = klinesRepository.findByPair("BTCUSDT", Inerval.INERVAL_1D.getDescption());
		logger.info(list);
	}
	
	@Test
	public void query15MKlines(){
		List<Klines> list = klinesRepository.findByPair("ETCUSDT", Inerval.INERVAL_15M.getDescption());
		PriceUtil.calculateEMAArray(list, EMAType.EMA7);
		PriceUtil.calculateEMAArray(list, EMAType.EMA25);
		PriceUtil.calculateEMAArray(list, EMAType.EMA99);
		for(Klines kl : list){
			logger.info(kl.getId() + ": " + kl);
		}
	}

	@Test
	public void testFindById(){
		Klines klines = klinesRepository.findById("66e45a7e074d3d1e99fa5edf");
		logger.info(klines);
	}
}
