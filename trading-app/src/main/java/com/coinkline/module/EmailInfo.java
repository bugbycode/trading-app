package com.coinkline.module;

/**
 * 邮件实体信息类
 */
public class EmailInfo {
	
	private String text = "";//邮件内容
	
	private String subject = "";//邮件主题

	/**
	 * @param subject 邮件主题
	 * @param text 邮件内容
	 */
	public EmailInfo(String subject,String text) {
		this.text = text;
		this.subject = subject;
	}

	/**
	 * 邮件内容
	 * @return
	 */
	public String getText() {
		return text;
	}

	/**
	 * 邮件主题
	 * @return
	 */
	public String getSubject() {
		return subject;
	}
	
}
