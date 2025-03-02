package com.bugbycode.webapp.filter;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONObject;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.filter.OncePerRequestFilter;

import com.bugbycode.config.AppConfig;
import com.util.RequestUtil;
import com.util.StringUtil;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * reCAPTCHA v2 身份验证
 */
public class ReCaptchaFilter extends OncePerRequestFilter {

	private final Logger logger = LogManager.getLogger(ReCaptchaFilter.class);

	private final String site_verify_url = "https://www.google.com/recaptcha/api/siteverify";
	
	private RestTemplate restTemplate;
	
	public ReCaptchaFilter(RestTemplate restTemplate) {
		this.restTemplate = restTemplate;
	}
	
	@SuppressWarnings("null")
	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
			throws ServletException, IOException {
		
		String method = request.getMethod();
		String url = request.getRequestURI();
		
		if("POST".equals(method) && url.endsWith("/login")) {

	        String cha_response = request.getParameter("cha_response");
	        
			String clientIp = RequestUtil.parseClientIp(request);
			
			logger.info("Login IP address: {}", clientIp);
			
			if(StringUtil.isEmpty(cha_response)) {
				responseFaild(response);
				return;
			} else {
				HttpHeaders headers = new HttpHeaders();
				HttpEntity<String> entity = new HttpEntity<>(headers);
				
				Map<String,Object> vars = new HashMap<String,Object>();
				
				String sv_url = site_verify_url + "?secret=" + AppConfig.RECAPTCHA_SECRET + "&response=" + cha_response;
				
				ResponseEntity<String> result = restTemplate.exchange(sv_url, HttpMethod.POST, entity, String.class, vars);
				
				HttpStatus status = HttpStatus.resolve(result.getStatusCode().value());
				
				if(status == HttpStatus.OK) {
					
					JSONObject json = new JSONObject(result.getBody());
					
					logger.info(json.toString());
					
					if(json.has("success")) {
						boolean success = json.getBoolean("success");
						if(!success) {
							responseFaild(response);
						}
					} else {
						responseFaild(response);
						return;
					}
				} else {
					responseFaild(response);
					return;
				}
			}
		}
		filterChain.doFilter(request, response);
	}
	
	private void responseFaild(HttpServletResponse response) throws IOException {
		responseFaild(response, "人机身份验证失败");
	}
	
	private void responseFaild(HttpServletResponse response,String message) throws IOException {
		response.setContentType("application/json;charset=UTF-8");
        response.setStatus(HttpServletResponse.SC_OK);
        PrintWriter out = response.getWriter();
        Map<String,Object> map = new HashMap<String, Object>();
        map.put("status", HttpServletResponse.SC_UNAUTHORIZED);// 设置为 401 未授权
        map.put("message", message);
        JSONObject json = new JSONObject(map);
        out.write(json.toString());
        out.flush();
        out.close();
	}
}
