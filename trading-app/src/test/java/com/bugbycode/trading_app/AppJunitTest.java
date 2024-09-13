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

import com.bugbycode.module.Inerval;
import com.bugbycode.module.Klines;
import com.bugbycode.module.QUERY_SPLIT;
import com.bugbycode.repository.KlinesRepository;
import com.bugbycode.service.KlinesService;

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
		Date now = new Date();
		String interval = Inerval.INERVAL_1H.getDescption();
		Klines k = new Klines("BTCUSDT", now.getTime() + 60000, 0, 0, 0, 0, now.getTime(),interval, 2);
		klinesRepository.insert(k);
		k = new Klines("BTCUSDT", now.getTime(), 0, 0, 0, 0, now.getTime(),interval, 2);
		klinesRepository.insert(k);
		//logger.info(k);
		List<Klines> list = klinesRepository.findByPairAndGtStartTime("BTCUSDT", now.getTime() - 99999,interval);
		logger.info(list);

		for(Klines kl : list){
			klinesRepository.remove(kl.getStartTime(),kl.getPair(),interval);
		}
	}
	
}
