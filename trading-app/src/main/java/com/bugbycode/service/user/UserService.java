package com.bugbycode.service.user;

import com.bugbycode.module.user.User;

/**
 * 用户信息管理
 */
public interface UserService {

	/**
	 * 新建用户
	 * @param user
	 * @return
	 */
	public String insert(User user);
	
	/**
	 * 修改用户信息
	 * @param user
	 */
	public void update(User user);
	
	/**
	 * 根据用户名删除用户信息
	 * @param username
	 */
	public void deleteByUsername(String username);
}
