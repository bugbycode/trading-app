package com.coinkline.handler;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;

import org.json.JSONObject;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * 登录失败逻辑处理
 */
public class LoginFailHandler implements AuthenticationFailureHandler {

	@Override
	public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response,
			AuthenticationException exception) throws IOException, ServletException {
		response.setContentType("application/json;charset=UTF-8");
        response.setStatus(HttpServletResponse.SC_OK);
        PrintWriter out = response.getWriter();
        Map<String,Object> map = new HashMap<String, Object>();
        map.put("status", HttpServletResponse.SC_UNAUTHORIZED);// 设置为 401 未授权
        map.put("message", exception.getLocalizedMessage());
        JSONObject json = new JSONObject(map);
        out.write(json.toString());
        out.flush();
        out.close();
	}

}
