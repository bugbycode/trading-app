package com.util;

import java.util.Properties;

import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import com.coinkline.config.AppConfig;
import com.coinkline.module.EmailAuth;
import com.coinkline.module.Result;
import com.coinkline.module.ResultCode;

public class EmailUtil {
	
	public static Result<ResultCode, Exception> send(String subject,String text,String rec)  {
		
		ResultCode code = ResultCode.SUCCESS;
		
		Exception ex = null;
        
        EmailAuth emailAuth = AppConfig.getEmailAuth();
        
        try {

    		Properties props = new Properties();
            props.put("mail.smtp.host", emailAuth.getHost());
            props.put("mail.smtp.port", emailAuth.getPort());
            props.put("mail.smtp.auth", true);
            props.put("mail.smtp.starttls.enable", true);
            props.put("mail.smtp.ssl.protocols", "TLSv1.2");
            
            Session session = Session.getInstance(props, new Authenticator() {
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(emailAuth.getUser(), emailAuth.getPassword());
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
