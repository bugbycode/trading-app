package com.bugbycode.trading_app.task.openInterest;

import java.util.List;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import com.bugbycode.config.AppConfig;
import com.bugbycode.module.Inerval;
import com.bugbycode.module.Klines;
import com.bugbycode.module.open_interest.OpenInterestHist;
import com.bugbycode.repository.klines.KlinesRepository;
import com.bugbycode.repository.openInterest.OpenInterestHistRepository;
import com.bugbycode.service.exchange.BinanceExchangeService;
import com.util.PriceUtil;
import com.util.StringUtil;

/**
 * 历史合约持仓量信息同步任务
 */
@Configuration
@EnableScheduling
public class OpenInterestHistTaskSync {
	
	private final Logger logger = LogManager.getLogger(OpenInterestHistTaskSync.class);
	
	@Autowired
	private OpenInterestHistRepository openInterestHistRepository;
	
	@Autowired
	private RestTemplate restTemplate;
	
	@Autowired
	private BinanceExchangeService binanceExchangeService;
	
	@Autowired
	private KlinesRepository klinesRepository;
	
	/**
	 * 同步任务 每4分钟执行一次
	 * 
	 * @throws Exception
	 */
	@Scheduled(cron = "30 2/4 * * * ?")
	public void executeTask() {
		
		if(AppConfig.DEBUG) {
			return;
		}
		
		Set<String> symbolSet = binanceExchangeService.exchangeInfo();
		
		for(String symbol : symbolSet) {
			
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
		}
	}
}
