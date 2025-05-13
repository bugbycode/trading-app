package com.util;

import java.util.Properties;

import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import com.bugbycode.module.EmailAuth;
import com.bugbycode.module.Regex;
import com.bugbycode.module.Result;
import com.bugbycode.module.ResultCode;

public class EmailUtil {
	
	public static Result<ResultCode, Exception> send(EmailAuth auth, String subject,String text,String rec)  {
		
		ResultCode code = ResultCode.SUCCESS;
		
		Exception ex = null;
        
        try {

        	String smtpUser = auth.getSmtpUser();
        	String smtpPwd = auth.getSmtpPwd();
        	String smtpHost = auth.getHost();
        	int smtpPort = auth.getPort();
        	
        	if(!(RegexUtil.test(smtpUser, Regex.EMAIL) && StringUtil.isNotEmpty(smtpPwd)
        			&& RegexUtil.test(smtpHost, Regex.DOMAIN) && RegexUtil.test(String.valueOf(smtpPort), Regex.PORT))) {
				throw new RuntimeException("SMTP信息未配置");
			}
        	
    		Properties props = new Properties();
            props.put("mail.smtp.host", smtpHost);
            props.put("mail.smtp.port", smtpPort);
            props.put("mail.smtp.auth", true);
            props.put("mail.smtp.starttls.enable", true);
            props.put("mail.smtp.ssl.protocols", "TLSv1.2");
            
            Session session = Session.getInstance(props, new Authenticator() {
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(smtpUser, smtpPwd);
                }
            });
        	
        	MimeMessage message = new MimeMessage(session);
            message.setFrom(new InternetAddress(smtpUser, "TRADE-BOT"));
            
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
        
		return new Result<ResultCode, Exception>(code, ex);
	}
}
