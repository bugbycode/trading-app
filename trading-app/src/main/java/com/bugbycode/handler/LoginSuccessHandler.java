package com.bugbycode.handler;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.json.JSONObject;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;

import com.bugbycode.module.user.User;
import com.bugbycode.repository.email.EmailRepository;
import com.bugbycode.trading_app.pool.WorkTaskPool;
import com.bugbycode.trading_app.task.email.SendMailTask;
import com.util.DateFormatUtil;
import com.util.RequestUtil;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * 登录成功逻辑处理
 */
public class LoginSuccessHandler implements AuthenticationSuccessHandler {
	
	private WorkTaskPool emailWorkTaskPool;
	
	private EmailRepository emailRepository;
	
	public LoginSuccessHandler(WorkTaskPool emailWorkTaskPool, EmailRepository emailRepository) {
		this.emailWorkTaskPool = emailWorkTaskPool;
		this.emailRepository = emailRepository;
	}
	
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
        
        String clientIp = RequestUtil.parseClientIp(request);
		
		User user =  (User)SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		
		Date date = new Date();
		
		String dateStr = DateFormatUtil.format(date);
		
		String subject = String.format("登录告警【%s】-【%s】 %s", user.getUsername(), clientIp, dateStr);
		String text = String.format("您的账号：%s，在 %s 时成功登录永续合约分析平台，来源地址：%s", user.getUsername(), dateStr, clientIp);
		
		this.emailWorkTaskPool.add(new SendMailTask(subject, text, user.getUsername(), emailRepository));
	}

}
