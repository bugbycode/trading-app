package com.bugbycode.trading_app.init;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import com.bugbycode.config.AppConfig;
import com.bugbycode.trading_app.pool.WorkTaskPool;

@Component
@Configuration
public class InitConfig implements ApplicationRunner {
	
	@Value("${binance.baseUrl.websocket}")
	private String websocketBaseUrl;
	
	@Value("${binance.baseUrl.websocketApi}")
	private String websocketApiBaseUrl;
	
	@Value("${binance.baseUrl.rest}")
	private String restBaseUrl; 
	
	@Override
	public void run(ApplicationArguments args) throws Exception {
		AppConfig.WEBSOCKET_API_URL = websocketApiBaseUrl;
		AppConfig.WEBSOCKET_URL = websocketBaseUrl;
		AppConfig.REST_BASE_URL = restBaseUrl;
	}

	@Bean("restTemplate")
	public RestTemplate restTemplate() {
		return new RestTemplate();
	}

	@Bean
	public WorkTaskPool workTaskPool(){
		return new WorkTaskPool("SyncTaskPool", 10);
	}

	@Bean
	public WorkTaskPool analysisWorkTaskPool(){
		return new WorkTaskPool("AnalysisTaskPool", 1);
	}
	
	@Bean
	public WorkTaskPool emailWorkTaskPool() {
		return new WorkTaskPool("EmailWorkTaskPool", 1);
	}
}
