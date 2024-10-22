package com.bugbycode.config;

import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.authentication.AuthenticationEventPublisher;
import org.springframework.security.authentication.DefaultAuthenticationEventPublisher;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

import com.bugbycode.handler.LoginFailHandler;
import com.bugbycode.handler.LoginSuccessHandler;
import com.bugbycode.repository.user.UserRepository;
import com.bugbycode.service.user.impl.UserDetailsManager;
import com.util.MD5Util;

@Order(0)
@Configuration
@EnableWebSecurity
public class SecurityConfig {
	
	private final Logger logger = LogManager.getLogger(SecurityConfig.class);

	@Bean
	public UserDetailsService userDetailsService(UserRepository userRepository,PasswordEncoder passwordEncoder) {
		return new UserDetailsManager(userRepository,passwordEncoder);
	}
	
    @Bean
    @ConditionalOnMissingBean(AuthenticationEventPublisher.class)
    public DefaultAuthenticationEventPublisher defaultAuthenticationEventPublisher(ApplicationEventPublisher delegate) { 
        return new DefaultAuthenticationEventPublisher(delegate);
    }
    
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception{
    	
    	http.cors(cors -> {
    		//解决跨域问题
    		cors.configurationSource(request -> {
                var corsConfig = new org.springframework.web.cors.CorsConfiguration();
                corsConfig.setAllowedOrigins(List.of("http://localhost:5173"));  // 允许的来源
                corsConfig.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
                corsConfig.setAllowedHeaders(List.of("*"));
                corsConfig.setAllowCredentials(true);  // 允许携带凭证（如 Cookies）
                return corsConfig;
    		});
    	})
    	
    	.anonymous(any -> any.disable())
    	
    	.csrf(csrf -> csrf.disable())
    	
    	.authorizeHttpRequests(authorize -> authorize
    			
    			.requestMatchers("/user/userInfo","/shape/**","/pairs/getPairs").hasAnyRole("LOGIN")
    			
    	        .anyRequest().authenticated()
    	        
    			)
    	.formLogin((form) -> {
    		form.successHandler(new LoginSuccessHandler())
    		.failureHandler(new LoginFailHandler())
    		.permitAll();
    	}).logout(Customizer.withDefaults());
    	
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
