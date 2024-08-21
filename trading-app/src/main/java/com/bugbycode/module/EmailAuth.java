package com.bugbycode.module;

import java.io.Serializable;

/**
 * 发件人认证信息
 */
public class EmailAuth implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private String user;//账号
	
	private String password; //密码
	
	public EmailAuth(String user, String password) {
		this.user = user;
		this.password = password;
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
	
}
