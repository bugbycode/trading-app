package com.bugbycode.trading_app;

import java.util.Date;
import java.util.List;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import com.bugbycode.config.AppConfig;
import com.bugbycode.module.Inerval;
import com.bugbycode.module.Klines;
import com.bugbycode.module.binance.SymbolExchangeInfo;
import com.bugbycode.module.open_interest.OpenInterestHist;
import com.bugbycode.repository.klines.KlinesRepository;
import com.bugbycode.repository.openInterest.OpenInterestHistRepository;
import com.bugbycode.service.exchange.BinanceExchangeService;
import com.util.PriceUtil;
import com.util.StringUtil;

@SpringBootTest
public class OpenInterestHistTest {
	
	private final Logger logger = LogManager.getLogger(OpenInterestHistTest.class);
	
	@Autowired
	private RestTemplate restTemplate;

	@Autowired
	private OpenInterestHistRepository openInterestHistRepository;
	
	@Autowired
	private BinanceExchangeService binanceExchangeService;
	
	@Autowired
	private KlinesRepository klinesRepository;

	@BeforeEach
	public void befor() {
		AppConfig.DEBUG = true;
		System.setProperty("https.proxyHost", "localhost");
		System.setProperty("https.proxyPort", "50000");
	}
	
	@Test
	public void testData() {
		String symbol = "币安人生USDT";
		UriComponentsBuilder uriBuilder = UriComponentsBuilder.fromUriString(AppConfig.REST_BASE_URL + "/futures/data/openInterestHist")
				.queryParam("symbol", symbol)
				.queryParam("period", Inerval.INERVAL_15M.getDescption())
				.queryParam("limit", 1);
		
		String url = uriBuilder.toUriString();
		url = StringUtil.urlDecoder(url);
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
		String symbol = "币安人生USDT";
		OpenInterestHist o = openInterestHistRepository.findOneBySymbol(symbol);
		logger.info(o);
	}

	
	@Test
	public void testSync() {
		/*Set<SymbolExchangeInfo> symbolSet = binanceExchangeService.exchangeInfo();
		
		for(SymbolExchangeInfo info : symbolSet) {
			
			String symbol = info.getSymbol();
			
			try {
				
				UriComponentsBuilder uriBuilder = UriComponentsBuilder.fromUriString(AppConfig.REST_BASE_URL + "/futures/data/openInterestHist")
						.queryParam("symbol", symbol)
						.queryParam("period", Inerval.INERVAL_15M.getDescption())
						.queryParam("limit", 1);
				
				String url = StringUtil.urlDecoder(uriBuilder.toUriString());
				//
				String result = restTemplate.getForObject(url, String.class);
				
				JSONArray jsonArray = new JSONArray(result);
				
				jsonArray.forEach(item -> {
					
					JSONObject json = (JSONObject) item;
					
					OpenInterestHist oih = new OpenInterestHist();
					oih.setSymbol(json.getString("symbol"));
					oih.setSumOpenInterest(json.getString("sumOpenInterest"));
					oih.setSumOpenInterestValue(json.getString("sumOpenInterestValue"));
					oih.setTimestamp(json.getLong("timestamp"));
					
					List<Klines> list_15m = klinesRepository.findLastKlinesByPair(symbol, Inerval.INERVAL_15M, 1);
					Klines last = PriceUtil.getLastKlines(list_15m);
					if(last != null) {
						oih.setTradeNumber(last.getN() / 15);
					}
					
					openInterestHistRepository.save(oih);
					
				});
				
			} catch (Exception e) {
				logger.error("同步历史合约持仓量信息时出现异常", e);
			}
		}*/
		
		List<SymbolExchangeInfo> list = binanceExchangeService.eOptionsExchangeInfoSymbol();
		for(SymbolExchangeInfo info : list) {
			try {
				OpenInterestHist dbOih = openInterestHistRepository.findOneBySymbol(info.getUnderlying());
				if(dbOih == null) {
					logger.info("{} - {}持仓量信息未入库", info.getUnderlying(), info.getSymbol());
					continue;
				}
				OpenInterestHist oih = new OpenInterestHist();
				oih.setSymbol(info.getSymbol());
				oih.setSumOpenInterest(dbOih.getSumOpenInterest());
				oih.setSumOpenInterestValue(dbOih.getSumOpenInterestValue());
				oih.setTimestamp(dbOih.getTimestamp());
				openInterestHistRepository.save(oih);
			} catch (Exception e) {
				logger.error("同步期权历史合约持仓量信息时出现异常", e);
			}
		}
	}
}
