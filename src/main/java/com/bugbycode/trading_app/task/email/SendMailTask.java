package com.bugbycode.trading_app.task.email;

import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.bugbycode.module.EmailAuth;
import com.bugbycode.module.Result;
import com.bugbycode.module.ResultCode;
import com.bugbycode.module.user.User;
import com.bugbycode.repository.user.UserRepository;
import com.util.EmailUtil;
import com.util.StringUtil;

/**
 * 发送邮件任务
 */
public class SendMailTask implements Runnable {

	private final Logger logger = LogManager.getLogger(SendMailTask.class);
	
	private String subject;
	
	private String text;
	
	private String recvEmail;
	
	private User user;
	
	private UserRepository userRepository;
	
	public SendMailTask(User user, String subject, String text, String recvEmail,UserRepository userRepository) {
		this.user = user;
		this.subject = subject;
		this.text = text;
		this.recvEmail = recvEmail;
		this.userRepository = userRepository;
	}

	@Override
	public void run() {
		
		if(StringUtil.isNotEmpty(subject) && StringUtil.isNotEmpty(text)) {
			
			logger.info("邮件主题：" + subject);
			logger.info("邮件内容：" + text);
			
			List<EmailAuth> authList = new ArrayList<EmailAuth>();
			
			String host = user.getSmtpHost();
			int port = user.getSmtpPort();
			
			String smtpUser = user.getSmtpUser();
			String smtpPwd = user.getSmtpPwd();
			String smtpUser2 = user.getSmtpUser2();
			String smtpPwd2 = user.getSmtpPwd2();
			String smtpUser3 = user.getSmtpUser3();
			String smtpPwd3 = user.getSmtpPwd3();
			
			if(verify(smtpUser, smtpPwd)) {
				authList.add(new EmailAuth(host, port, smtpUser, smtpPwd));
			}
			
			if(verify(smtpUser2, smtpPwd2)) {
				authList.add(new EmailAuth(host, port, smtpUser2, smtpPwd2));
			}

			if(verify(smtpUser3, smtpPwd3)) {
				authList.add(new EmailAuth(host, port, smtpUser3, smtpPwd3));
			}
			
			if(authList.isEmpty()) {
				return;
			}
			
			int smtpIndex = checkSmtpIndex(user.getSmtpIndex(), authList);
			
			EmailAuth auth = authList.get(smtpIndex++);
			
			Result<ResultCode, Exception> result = EmailUtil.send(auth, subject, text, recvEmail);
			
			switch (result.getResult()) {
			case ERROR:
				
				Exception ex = result.getErr();
				
				logger.info("邮件发送失败！失败原因：" + ex.getLocalizedMessage());
				
				break;
				
			default:
				
				logger.info("邮件发送成功！");
				
				break;
			}
			
			try {
				this.userRepository.updateSmtpIndex(user.getUsername(), checkSmtpIndex(smtpIndex, authList));
				Thread.sleep(1500);
			} catch (Exception e) {
				logger.error(e.getMessage(), e);
			}
			
		}
	}
	
	private boolean verify(String smtpUser,String smtpPwd) {
		return StringUtil.isNotEmpty(smtpUser) && StringUtil.isNotEmpty(smtpPwd);
	}

	private int checkSmtpIndex(int smtpIndex, List<EmailAuth> authList) {
		int result = smtpIndex;
		if(result == -1 || result > authList.size() - 1) {
			result = 0;
		}
		return result;
	}
}
