package com.bugbycode.trading_app.task.openInterest;

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
import com.bugbycode.module.open_interest.OpenInterestHist;
import com.bugbycode.repository.openInterest.OpenInterestHistRepository;
import com.bugbycode.service.exchange.BinanceExchangeService;

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
	
	/**
	 * 同步任务 每4分钟执行一次
	 * 
	 * @throws Exception
	 */
	@Scheduled(cron = "30 2/4 * * * ?")
	public void executeTask() {
		
		Set<String> symbolSet = binanceExchangeService.exchangeInfo();
		
		for(String symbol : symbolSet) {
			
			try {
				
				UriComponentsBuilder uriBuilder = UriComponentsBuilder.fromUriString(AppConfig.REST_BASE_URL + "/futures/data/openInterestHist")
						.queryParam("symbol", symbol)
						.queryParam("period", Inerval.INERVAL_1D.getDescption())
						.queryParam("limit", 1);
				
				String url = uriBuilder.toUriString();
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
					
					openInterestHistRepository.save(oih);
					
				});
				
			} catch (Exception e) {
				logger.error("同步历史合约持仓量信息时出现异常", e);
			}
		}
	}
}
