package com.bugbycode.service.user;

import java.util.List;

import org.springframework.security.core.userdetails.UserDetailsService;

import com.bugbycode.module.binance.AutoTrade;
import com.bugbycode.module.binance.AutoTradeType;
import com.bugbycode.module.user.User;

/**
 * 用户信息管理
 */
public interface UserService extends UserDetailsService{

	/**
	 * 获取所有订阅价格回撤邮箱账号信息
	 * @return
	 */
	public String getFibMonitorUserEmail();
	
	/**
	 * 获取所有订阅行情波动邮箱账号信息
	 * @return
	 */
	public String getRiseAndFallMonitorUserEmail();
	
	/**
	 * 获取所有订阅开仓机会邮箱账号信息
	 * @return
	 */
	public String getEmaMonitorUserEmail();
	
	/**
	 * 获取所有订阅行情异动邮箱账号信息
	 * @return
	 */
	public String getEmaRiseAndFallUserEmail();
	
	/**
	 * 获取所有订阅高低点位邮箱账号信息
	 * @return
	 */
	public String getHighOrLowMonitorUserEmail();
	
	/**
	 * 获取所有用户邮箱信息 结果使用逗号分隔
	 * @return
	 */
	public String getAllUserEmail();
	
	/**
	 * 根据自动交易启用状态查询所有关联用户信息
	 * 
	 * @param autoTrade 是否开启自动交易
	 * @param autoTradeType 自动交易参考指标
	 * @return
	 */
	public List<User> queryByAutoTrade(AutoTrade autoTrade,AutoTradeType autoTradeType);
}
