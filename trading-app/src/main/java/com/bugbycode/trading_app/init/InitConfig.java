package com.bugbycode.trading_app.init;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import com.bugbycode.config.AppConfig;
import com.bugbycode.module.EmailAuth;
import com.bugbycode.trading_app.pool.WorkTaskPool;
import com.bugbycode.websocket.realtime.endpoint.PerpetualWebSocketClientEndpoint;
import com.bugbycode.websocket.realtime.handler.MessageHandler;
import com.util.CoinPairSet;
import com.util.StringUtil;

@Component
@Configuration
public class InitConfig implements ApplicationRunner {

	@Value("${binance.baseUrl.rest}")
	private String restBaseUrl;
	
	@Value("${binance.baseUrl.websocket}")
	private String websocketBaseUrl; 
	
	@Value("${binance.pair}")
	private Set<String> pair;
	
	@Value("${email.auth.user}")
	private String[] emailUserNameArr;//发件人
	
	@Value("${email.auth.password}")
	private String[] emailPasswordArr;//密码
	
	@Value("${email.smtp.host}")
	private String smtpHost;//服务器
	
	@Value("${email.smtp.port}")
	private int smtpPort;//端口
	
	@Value("${email.recipient}")
	private Set<String> recipient;//收件人
	
	@Autowired
	private MessageHandler messageHandler;
	
	@Override
	public void run(ApplicationArguments args) throws Exception {
		AppConfig.REST_BASE_URL = restBaseUrl;
		AppConfig.WEBSOCKET_URL = websocketBaseUrl;
		AppConfig.PAIRS = pair;
		AppConfig.SMTP_HOST = smtpHost;
		AppConfig.SMTP_PORT = smtpPort;
		AppConfig.RECIPIENT = recipient;
		
		List<EmailAuth> emailAuthList = new ArrayList<EmailAuth>();
		
		for(int index = 0;index < emailUserNameArr.length;index++) {
			String user = emailUserNameArr[index].trim();
			String password = emailPasswordArr[index].trim();
			if(StringUtil.isEmpty(user)) {
				continue;
			}
			emailAuthList.add(new EmailAuth(user, password));
		}
		
		AppConfig.setEmailAuth(emailAuthList);
		/*
		CoinPairSet set = new CoinPairSet();
		for(String coin : pair) {
			if(set.isFull()) {
				new PerpetualWebSocketClientEndpoint(set.getStreamName(), messageHandler);
				set.clear();
			}
			set.add(coin);
		}*/
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
}
