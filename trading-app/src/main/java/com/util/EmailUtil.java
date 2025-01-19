package com.util;

import java.util.List;
import java.util.Properties;

import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import org.springframework.util.CollectionUtils;

import com.bugbycode.config.AppConfig;
import com.bugbycode.module.EmailAuth;
import com.bugbycode.module.Result;
import com.bugbycode.module.ResultCode;
import com.bugbycode.repository.email.EmailRepository;

public class EmailUtil {
	
	public static Result<ResultCode, Exception> send(String subject,String text,String rec,EmailRepository emailRepository)  {
		
		ResultCode code = ResultCode.SUCCESS;
		
		Exception ex = null;
        
        try {
        	
        	EmailAuth emailAuth = AppConfig.getEmailAuth();
        	
        	if(emailAuth == null) {
            	
            	List<EmailAuth> emailAuthList = emailRepository.query();
            	
            	if(CollectionUtils.isEmpty(emailAuthList)) {
        			throw new RuntimeException("邮箱认证未配置");
        		} else {
        			AppConfig.setEmailAuth(emailAuthList);
        		}
            	
            	emailAuth = AppConfig.getEmailAuth();
        	}

        	String user = emailAuth.getUser();
        	String password = emailAuth.getPassword();
        	
    		Properties props = new Properties();
            props.put("mail.smtp.host", emailAuth.getHost());
            props.put("mail.smtp.port", emailAuth.getPort());
            props.put("mail.smtp.auth", true);
            props.put("mail.smtp.starttls.enable", true);
            props.put("mail.smtp.ssl.protocols", "TLSv1.2");
            
            Session session = Session.getInstance(props, new Authenticator() {
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(user, password);
                }
            });
        	
        	MimeMessage message = new MimeMessage(session);
            message.setFrom(new InternetAddress(emailAuth.getUser(),"TRADE-BOT"));
            
            int sendIndex = 0;
            if(StringUtil.isNotEmpty(rec)) {
                String regex = ",";
                if(rec.indexOf(regex) != -1) {
                	String[] recs = rec.split(regex);
                	for(String rc : recs) {
                		if(StringUtil.isNotEmpty(rc)) {
                    		message.addRecipient(Message.RecipientType.TO, new InternetAddress(rc));
                            sendIndex++;
                		}
                	}
                } else {
                    message.addRecipient(Message.RecipientType.TO, new InternetAddress(rec));
                    sendIndex++;
                }
            }
            
            message.setSubject(subject);
            message.setText(text);
            
            if(sendIndex > 0) {
                Transport.send(message);
            }
            
        } catch (Exception e) {
        	ex = e;
        	code = ResultCode.ERROR;
		}
        
        AppConfig.nexEmailAuth();
        
		return new Result<ResultCode, Exception>(code, ex);
	}
}
