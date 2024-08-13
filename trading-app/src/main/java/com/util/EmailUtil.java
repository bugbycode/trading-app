package com.util;

import java.io.UnsupportedEncodingException;
import java.util.Properties;

import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import com.bugbycode.config.AppConfig;
import com.bugbycode.module.Result;
import com.bugbycode.module.ResultCode;

public class EmailUtil {
	
	public static Result<ResultCode, Exception> send(String subject,String text)  {
		
		ResultCode code = ResultCode.SUCCESS;
		Exception ex = null;
		String recipient = AppConfig.RECIPIENT;
		
		Properties props = new Properties();
        props.put("mail.smtp.host", AppConfig.SMTP_HOST);
        props.put("mail.smtp.port", AppConfig.SMTP_PORT);
        props.put("mail.smtp.auth", true);
        props.put("mail.smtp.starttls.enable", true);
        props.put("mail.smtp.ssl.protocols", "TLSv1.2");
        
        Session session = Session.getInstance(props, new Authenticator() {
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(AppConfig.EMAIL_USDRNAME, AppConfig.EMAIL_PASSWORD);
            }
        });
        
        try {
        	MimeMessage message = new MimeMessage(session);
            message.setFrom(new InternetAddress(AppConfig.EMAIL_USDRNAME,"TRADE-BOT"));
            
            int recIndex = 0;
            if(recipient.contains(",")) {
            	String[] recUser = recipient.split(",");
            	for(String rec : recUser) {
            		if(StringUtil.isNotEmpty(rec)) {
            			message.addRecipient(Message.RecipientType.TO, new InternetAddress(rec));
            			recIndex++;
            		}
            	}
            } else if(StringUtil.isNotEmpty(recipient)){
            	message.addRecipient(Message.RecipientType.TO, new InternetAddress(recipient));
            	recIndex++;
            }
            
            message.setSubject(subject);
            message.setText(text);
            
            if(recIndex > 0) {
                Transport.send(message);
            }
            
        } catch (UnsupportedEncodingException e) {
        	ex = e;
        	code = ResultCode.ERROR;
		} catch (MessagingException e) {
			ex = e;
			code = ResultCode.ERROR;
		}
        
		return new Result<ResultCode, Exception>(code, ex);
	}
}
