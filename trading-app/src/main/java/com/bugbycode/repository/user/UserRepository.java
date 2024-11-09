package com.bugbycode.repository.user;

import java.util.List;

import com.bugbycode.module.user.User;

public interface UserRepository {

	/**
	 * 根据用户名查询用户信息
	 * @param username
	 * @return
	 */
	public User queryByUsername(String username);
	
	/**
	 * 新建用户信息
	 * @param user
	 * @return
	 */
	public String insert(User user);
	
	/**
	 * 修改用户密码
	 * @param username
	 * @param password
	 */
	public void updatePassword(String username,String password);
	
	/**
	 * 根据用户名删除用户信息
	 * @param username
	 */
	public void deleteByUsername(String username);
	
	/**
	 * 修改用户行情订阅设置信息
	 * @param username 用户名
	 * @param user 
	 */
	public void updateUserSubscribeInfo(String username,User user);
	
	/**
	 * 根据订阅AI分析状态查询所有用户
	 * @param subscribeAi
	 * @return
	 */
	public List<User> queryAllUserBySubscribeAi(int subscribeAi);
}
