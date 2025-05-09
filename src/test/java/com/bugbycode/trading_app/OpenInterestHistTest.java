package com.bugbycode.trading_app;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import com.bugbycode.config.AppConfig;
import com.bugbycode.module.Inerval;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest
public class OpenInterestHistTest {
	
	private final Logger logger = LogManager.getLogger(OpenInterestHistTest.class);
	
	@Autowired
	private RestTemplate restTemplate;

	@Before
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

}
