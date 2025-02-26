package com.bugbycode.module;

import java.io.Serializable;

import org.springframework.data.annotation.Id;

/**
 * 发件人认证信息
 */
public class EmailAuth implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@Id
	private String id;
	
	private String user;//账号
	
	private String password; //密码
	
	private String host;
	
	private int port;
	
	public EmailAuth() {
		
	}

	public EmailAuth(String user, String password) {
		this.user = user;
		this.password = password;
	}

	public EmailAuth(String user, String password, String host, int port) {
		this.user = user;
		this.password = password;
		this.host = host;
		this.port = port;
	}

	public String getUser() {
		return user;
	}

	public void setUser(String user) {
		this.user = user;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
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
	
}
