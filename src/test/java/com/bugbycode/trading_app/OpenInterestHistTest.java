package com.bugbycode.trading_app;

import java.util.Date;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import com.bugbycode.config.AppConfig;
import com.bugbycode.module.Inerval;
import com.bugbycode.module.open_interest.OpenInterestHist;
import com.bugbycode.repository.openInterest.OpenInterestHistRepository;

@SpringBootTest
public class OpenInterestHistTest {
	
	private final Logger logger = LogManager.getLogger(OpenInterestHistTest.class);
	
	@Autowired
	private RestTemplate restTemplate;

	@Autowired
	private OpenInterestHistRepository openInterestHistRepository;

	@BeforeAll
	public void befor() {
		AppConfig.DEBUG = true;
		System.setProperty("https.proxyHost", "localhost");
		System.setProperty("https.proxyPort", "50000");
	}
	
	@Test
	public void testData() {
		String symbol = "BTCUSDT";
		UriComponentsBuilder uriBuilder = UriComponentsBuilder.fromUriString(AppConfig.REST_BASE_URL + "/futures/data/openInterestHist")
				.queryParam("symbol", symbol)
				.queryParam("period", Inerval.INERVAL_15M.getDescption())
				.queryParam("limit", 1);
		
		String url = uriBuilder.toUriString();
		//
		String result = restTemplate.getForObject(url, String.class);
		logger.info(result);
	}

	@Test
	public void testQuery() {
		long now = new Date().getTime();
		List<OpenInterestHist> list = openInterestHistRepository.query();
		logger.info(list.size());
		for(OpenInterestHist oih : list) {
			long t = oih.getTimestamp();
			long d = (now - t) / 1000 / 60 / 60 / 24;
			if(d > 30) {
				String pair = oih.getSymbol();
				logger.info("{}交易对已超过{}天未更新数据", pair, d);
			}
			logger.info(oih);
		}
	}

	@Test
	public void testQueryOne() {
		String symbol = "SKLUSDT";
		OpenInterestHist o = openInterestHistRepository.findOneBySymbol(symbol);
		logger.info(o);
	}

}
