package com.bugbycode.module.user;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.springframework.data.annotation.Id;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

/**
 * 用户信息
 */
public class User implements UserDetails {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@Id
	private String id;
	
	private String username;
	
	private String password;
	
	private int subscribeAi = 0;//是否订阅AI分析 0：否 1：是
	
	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	@Override
	public String getPassword() {
		return this.password;
	}

	@Override
	public String getUsername() {
		return this.username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	@Override
	public Collection<? extends GrantedAuthority> getAuthorities() {
		Set<GrantedAuthority> roleSet = new HashSet<GrantedAuthority>();
		roleSet.add(new SimpleGrantedAuthority("ROLE_LOGIN"));
		return roleSet;
	}

	public int getSubscribeAi() {
		return subscribeAi;
	}

	public void setSubscribeAi(int subscribeAi) {
		this.subscribeAi = subscribeAi;
	}
}
