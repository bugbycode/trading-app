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
	 * 根据订阅价格回撤状态查询所有用户
	 * @param fibMonitor
	 * @return
	 */
	public List<User> queryAllUserByFibMonitor(int fibMonitor);
	
	/**
	 * 根据订阅行情波动状态查询所有用户
	 * @param riseAndFallMonitor
	 * @return
	 */
	public List<User> queryAllUserByRiseAndFallMonitor(int riseAndFallMonitor);
	
	/**
	 * 根据订阅开仓机会状态查询所有用户
	 * @param emaMonitor
	 * @return
	 */
	public List<User> queryAllUserByEmaMonitor(int emaMonitor);
	
	/**
	 * 根据订阅行情异动状态查询所有用户
	 * @param emaRiseAndFall
	 * @return
	 */
	public List<User> queryAllUserByEmaRiseAndFall(int emaRiseAndFall);
	
	/**
	 * 根据订阅高低点位状态查询所有用户
	 * @param highOrLowMonitor
	 * @return
	 */
	public List<User> queryAllUserByHighOrLowMonitor(int highOrLowMonitor);
	
	/**
	 * 查询所有用户信息
	 * @return
	 */
	public List<User> queryAllUser();
	
	/**
	 * 更新币安API密钥对
	 * @param username 用户名
	 * @param binanceApiKey
	 * @param binanceSecretKey
	 */
	public void updateBinanceApiSecurity(String username,String binanceApiKey,String binanceSecretKey);
}
