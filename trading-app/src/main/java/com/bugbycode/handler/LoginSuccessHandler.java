package com.bugbycode.handler;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;

import org.json.JSONObject;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * 登录成功逻辑处理
 */
public class LoginSuccessHandler implements AuthenticationSuccessHandler {
	
	@Override
	public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
			Authentication authentication) throws IOException, ServletException {
		
		response.setContentType("application/json;charset=UTF-8");
        response.setStatus(HttpServletResponse.SC_OK); 
        PrintWriter out = response.getWriter();
        Map<String,Object> map = new HashMap<String, Object>();
        map.put("status", HttpServletResponse.SC_OK);
        map.put("message", "login success.");
        JSONObject json = new JSONObject(map);
        out.write(json.toString());
        out.flush();
        out.close();
	}

}
