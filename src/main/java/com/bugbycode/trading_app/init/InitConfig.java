package com.bugbycode.trading_app.init;

import java.io.IOException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.client.ResponseErrorHandler;
import org.springframework.web.client.RestTemplate;

import com.bugbycode.config.AppConfig;
import com.bugbycode.trading_app.pool.WorkTaskPool;
import com.bugbycode.websocket.trading.endpoint.TradingWebSocketClientEndpoint;

@Component
@Configuration
public class InitConfig implements ApplicationRunner {
	
	@Value("${binance.baseUrl.websocket}")
	private String websocketBaseUrl;
	
	@Value("${binance.baseUrl.websocketApi}")
	private String websocketApiBaseUrl;
	
	@Value("${binance.baseUrl.rest}")
	private String restBaseUrl; 
	
	@Value("${google.recaptcha.secret}")
	private String recapt_secret;
	
	@Override
	public void run(ApplicationArguments args) throws Exception {
		AppConfig.WEBSOCKET_API_URL = websocketApiBaseUrl;
		AppConfig.WEBSOCKET_URL = websocketBaseUrl;
		AppConfig.REST_BASE_URL = restBaseUrl;
		AppConfig.RECAPTCHA_SECRET = recapt_secret;
	}

	@Bean("restTemplate")
	public RestTemplate restTemplate() {
		RestTemplate restTemplate = new RestTemplate();
		restTemplate.setErrorHandler(new HttpResponseErrorHandler());
		return restTemplate;
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
	
	@Bean
	public WorkTaskPool tradingTaskPool() {
		return new WorkTaskPool("TradingTaskPool", 1);
	}
	
	@Bean
	public TradingWebSocketClientEndpoint websocketApi() {
		return new TradingWebSocketClientEndpoint(websocketApiBaseUrl);
	}
	
	private class HttpResponseErrorHandler implements ResponseErrorHandler{
		
		private final Logger logger = LogManager.getLogger(HttpResponseErrorHandler.class);
		
		@Override
		public boolean hasError(ClientHttpResponse response) throws IOException {
			return response.getStatusCode().value() == HttpStatus.Series.CLIENT_ERROR.value() 
		               || response.getStatusCode().value() == HttpStatus.Series.SERVER_ERROR.value();
		}
		
		@Override
		public void handleError(ClientHttpResponse response) throws IOException {
			logger.error("Error response received with status code: " + response.getStatusCode());
		}
	}
}
