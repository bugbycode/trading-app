package com.bugbycode.module;

public class EmailAuth {
	
	private String host;
	
	private int port;
	
	private String smtpUser;
	
	private String smtpPwd;

	public EmailAuth(String host, int port, String smtpUser, String smtpPwd) {
		this.host = host;
		this.port = port;
		this.smtpUser = smtpUser;
		this.smtpPwd = smtpPwd;
	}

	public String getHost() {
		return host;
	}

	public void setHost(String host) {
		this.host = host;
	}

	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}

	public String getSmtpUser() {
		return smtpUser;
	}

	public void setSmtpUser(String smtpUser) {
		this.smtpUser = smtpUser;
	}

	public String getSmtpPwd() {
		return smtpPwd;
	}

	public void setSmtpPwd(String smtpPwd) {
		this.smtpPwd = smtpPwd;
	}
	
	
}
