package com.bugbycode.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationEventPublisher;
import org.springframework.security.authentication.DefaultAuthenticationEventPublisher;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.client.RestTemplate;

import com.bugbycode.handler.LoginFailHandler;
import com.bugbycode.handler.LoginSuccessHandler;
import com.bugbycode.webapp.filter.ReCaptchaFilter;
import com.util.MD5Util;

@Configuration
@EnableWebSecurity
public class SecurityConfig {
	
	@Autowired
	private RestTemplate restTemplate;
	
    @Bean
    @ConditionalOnMissingBean(AuthenticationEventPublisher.class)
    public DefaultAuthenticationEventPublisher defaultAuthenticationEventPublisher(ApplicationEventPublisher delegate) { 
        return new DefaultAuthenticationEventPublisher(delegate);
    }
    
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception{
    	
    	http.anonymous(any -> any.disable())
    	
    	.csrf(csrf -> csrf.disable())
    	
    	.authorizeHttpRequests(authorize -> authorize
    			
    			.requestMatchers("/shape/**",
    					"/user/**",
    					"/tradingview/**",
    					"/bot/**").hasAnyRole("LOGIN")
    	        .anyRequest().authenticated()
    	        
    			)
    	.formLogin((form) -> {
    		form.successHandler(new LoginSuccessHandler())
    		.failureHandler(new LoginFailHandler())
    		//.loginPage("/web")
    		.loginProcessingUrl("/login")
    		.permitAll();
    	}).logout(Customizer.withDefaults())
    	
    	.addFilterBefore(new ReCaptchaFilter(restTemplate), UsernamePasswordAuthenticationFilter.class)
    	;
    	
    	return http.build();
    }
    
	@Bean
	public PasswordEncoder passwordEncoder() {
		return new PasswordEncoder() {
			
			@Override
			public boolean matches(CharSequence rawPassword, String encodedPassword) {
				String md5Pwd = MD5Util.md5(rawPassword.toString());
				return md5Pwd.equals(encodedPassword);
			}
			
			@Override
			public String encode(CharSequence rawPassword) {
				return rawPassword.toString();
			}
		};
	}
}
