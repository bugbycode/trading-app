package com.bugbycode.service.user;

import org.springframework.security.core.userdetails.UserDetailsService;

/**
 * 用户信息管理
 */
public interface UserService extends UserDetailsService{

	/**
	 * 获取所有订阅分析的邮箱账号信息
	 * @return
	 */
	public String getSubscribeAiUserEmail();
}
