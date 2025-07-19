package com.bugbycode.repository.user;

import java.util.List;

import com.bugbycode.module.FibLevel;
import com.bugbycode.module.MonitorStatus;
import com.bugbycode.module.RecvCrossUnPnlStatus;
import com.bugbycode.module.VolumeMonitorStatus;
import com.bugbycode.module.binance.AutoTrade;
import com.bugbycode.module.binance.AutoTradeType;
import com.bugbycode.module.binance.DrawTrade;
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
	 * 修改SMTP配置
	 * @param username
	 * @param smtpUser
	 * @param smtpPwd
	 * @param smtpUser2
	 * @param smtpPwd2
	 * @param smtpUser3
	 * @param smtpPwd3
	 * @param smtpHost
	 * @param smtpPort
	 */
	public void updateSmtpSetting(String username,
			String smtpUser,String smtpPwd,
			String smtpUser2,String smtpPwd2,
			String smtpUser3,String smtpPwd3,
			String smtpHost,int smtpPort);
	
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
	 * @param status
	 * @return
	 */
	public List<User> queryAllUserByFibMonitor(MonitorStatus status);
	
	/**
	 * 根据订阅行情波动状态查询所有用户
	 * @param status
	 * @return
	 */
	public List<User> queryAllUserByRiseAndFallMonitor(MonitorStatus status);
	
	/**
	 * 根据订阅价格行为状态查询所有用户
	 * @param status
	 * @return
	 */
	public List<User> queryAllUserByEmaMonitor(MonitorStatus status);
	
	/**
	 * 根据订阅指数均线状态查询所有用户
	 * @param status
	 * @return
	 */
	public List<User> queryAllUserByEmaRiseAndFall(MonitorStatus status);
	
	/**
	 * 根据订阅高低点位状态查询所有用户
	 * @param status
	 * @return
	 */
	public List<User> queryAllUserByHighOrLowMonitor(MonitorStatus status);
	
	/**
	 * 根据订阅盘整区间状态查询所有用户
	 * @param status
	 * @return
	 */
	public List<User> queryAllUserByAreaMonitor(MonitorStatus status);
	
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
	 * @param recvTrade 是否接收交易通知
	 * @param recvCrossUnPnl 是否接收PNL通知
	 * @param recvCrossUnPnlPercent 未实现盈亏阈值百分比 用于邮件通知条件
	 * @param tradeStepBack 是否交易回踩单 0：否 1：是
	 * @param tradeStyle 交易风格 0：保守 1：激进
	 * @param profitLimit 止盈百分比限制 交易风格为保守时使用
	 * @param countertrendTrading 是否交易逆势单 0：否 1：是
	 * @param fibLevel 价格回撤级别
	 * @param tradeNumber 每分钟成交笔数
	 */
	public void updateBinanceApiSecurity(String username,String binanceApiKey,String binanceSecretKey,int autoTrade,
			int baseStepSize,int leverage,double positionValue,double cutLoss,double profit,int autoTradeType,int drawTrade,
			int recvTrade,int recvCrossUnPnl,double recvCrossUnPnlPercent,int tradeStepBack,int tradeStyle,double profitLimit,
			int countertrendTrading, FibLevel fibLevel, long tradeNumber);
	
	/**
	 * 根据自动交易启用状态查询所有关联用户信息
	 * 
	 * @param autoTrade
	 * @param autoTradeType
	 * @return
	 */
	public List<User> queryByAutoTrade(AutoTrade autoTrade, AutoTradeType autoTradeType);
	
	/**
	 * 根据画线交易启用状态查询所有关联用户信息
	 * @param drawTrade 是否开启画线交易
	 * @return
	 */
	public List<User> queryByDrawTrade(DrawTrade drawTrade);
	
	/**
	 * 根据接收PNL通知开关状态查询所有关联用户信息
	 * 
	 * @param status PNL通知开关状
	 * @return
	 */
	public List<User> queryByRecvCrossUnPnl(RecvCrossUnPnlStatus status);
	
	/**
	 * 根据量价监控状态查询所有关联用户信息
	 * @param status 是否开启量价监控
	 * @return
	 */
	public List<User> queryByVolumeMonitorStatus(VolumeMonitorStatus status);
	
	/**
	 * 修改smtp账号索引
	 * @param username
	 * @param smtpIndex
	 */
	public void updateSmtpIndex(String username, int smtpIndex);
}
