package com.coinkline.repository.user;

import java.util.List;

import com.coinkline.module.binance.AutoTrade;
import com.coinkline.module.binance.AutoTradeType;
import com.coinkline.module.binance.DrawTrade;
import com.coinkline.module.user.User;

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
	 * 根据订阅盘整区间状态查询所有用户
	 * @param areaMonitor
	 * @return
	 */
	public List<User> queryAllUserByAreaMonitor(int areaMonitor);
	
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
	 * @param autoTrade 是否开启自动交易
	 * @param baseStepSize 名义价值倍数
	 * @param leverage 杠杆倍数
	 * @param positionValue 持仓价值
	 * @param cutLoss 止损比例
	 * @param profit 收益筛选
	 * @param autoTradeType 自动交易参考指标 取值参考泛型 com.bugbycode.module.binance.AutoTradeType
	 * @param drawTrade 是否开启画线交易
	 */
	public void updateBinanceApiSecurity(String username,String binanceApiKey,String binanceSecretKey,int autoTrade,
			int baseStepSize,int leverage,int positionValue,int cutLoss,double profit,int autoTradeType,int drawTrade);
	
	/**
	 * 根据自动交易启用状态查询所有关联用户信息
	 * 
	 * @param autoTrade
	 * @return
	 */
	public List<User> queryByAutoTrade(AutoTrade autoTrade,AutoTradeType autoTradeType);
	
	/**
	 * 根据画线交易启用状态查询所有关联用户信息
	 * @param drawTrade 是否开启画线交易
	 * @return
	 */
	public List<User> queryByDrawTrade(DrawTrade drawTrade);
}
