package com.coinkline.webapp.filter;



import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.util.MD5Util;
import com.util.StringUtil;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class LoginAuthenticationFilter extends UsernamePasswordAuthenticationFilter {

	private final AuthenticationManager authenticationManager;
	
	public LoginAuthenticationFilter(AuthenticationManager authenticationManager,AuthenticationSuccessHandler successHandler,
			AuthenticationFailureHandler failureHandler) {
		this.authenticationManager = authenticationManager;
		super.setAuthenticationSuccessHandler(successHandler);
		super.setAuthenticationFailureHandler(failureHandler);
	}
	
	@Override
	public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response)
			throws AuthenticationException {
		String username = request.getParameter(SPRING_SECURITY_FORM_USERNAME_KEY);
		String password = request.getParameter(SPRING_SECURITY_FORM_PASSWORD_KEY);
		
		if(StringUtil.isEmpty(username) || StringUtil.isEmpty(password)) {
			throw new BadCredentialsException("username or password error.");
		}
		
		UsernamePasswordAuthenticationToken token = new UsernamePasswordAuthenticationToken(username, MD5Util.md5(password));
		
		return authenticationManager.authenticate(token);
	}
}
