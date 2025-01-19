package com.bugbycode.trading_app.task.email;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.bugbycode.module.Result;
import com.bugbycode.module.ResultCode;
import com.bugbycode.repository.email.EmailRepository;
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
	
	private EmailRepository emailRepository;
	
	public SendMailTask(String subject, String text, String recvEmail,EmailRepository emailRepository) {
		this.subject = subject;
		this.text = text;
		this.recvEmail = recvEmail;
		this.emailRepository = emailRepository;
	}

	@Override
	public void run() {
		
		if(StringUtil.isNotEmpty(subject) && StringUtil.isNotEmpty(text)) {
			
			logger.info("邮件主题：" + subject);
			logger.info("邮件内容：" + text);
			
			Result<ResultCode, Exception> result = EmailUtil.send(subject, text, recvEmail,emailRepository);
			
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
				Thread.sleep(1500);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

}
