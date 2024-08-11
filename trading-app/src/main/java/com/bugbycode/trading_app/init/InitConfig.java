package com.bugbycode.trading_app.init;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

import com.bugbycode.config.AppConfig;

@Component
@Configuration
public class InitConfig implements ApplicationRunner {

	@Value("${binance.baseUrl.rest}")
	private String restBaseUrl;
	
	@Value("${binance.pair}")
	private String pairs;
	
	@Value("${email.auth.user}")
	private String emailUserName;//发件人
	
	@Value("${email.auth.password}")
	private String emailPassword;//密码
	
	@Value("${email.smtp.host}")
	private String smtpHost;//服务器
	
	@Value("${email.smtp.port}")
	private int smtpPort;//端口
	
	@Value("${email.recipient}")
	private String recipient;//收件人
	
	@Override
	public void run(ApplicationArguments args) throws Exception {
		AppConfig.REST_BASE_URL = restBaseUrl;
		AppConfig.PAIRS = pairs;
		AppConfig.EMAIL_USDRNAME = emailUserName;
		AppConfig.EMAIL_PASSWORD = emailPassword;
		AppConfig.SMTP_HOST = smtpHost;
		AppConfig.SMTP_PORT = smtpPort;
		AppConfig.RECIPIENT = recipient;
	}

}
