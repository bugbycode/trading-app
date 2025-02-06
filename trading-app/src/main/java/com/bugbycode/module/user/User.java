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
	
	private int fibMonitor = 0; //是否订阅斐波那契回撤监控 0：否 1：是
	
	private int riseAndFallMonitor = 0; //是否订阅涨跌幅监控 0：否 1：是
	
	private int emaMonitor = 0;//是否订阅开仓机会监控 0：否 1：是
	
	private int emaRiseAndFall = 0;//是否订阅行情异动监控 0：否 1：是
	
	private int highOrLowMonitor = 0;//是否订阅标志性高低点监控 0：否 1：是
	
	private int areaMonitor = 0;//是否订阅盘整区监控 0：否 1：是
	
	private String binanceApiKey;//币安API key
	
	private String binanceSecretKey;//币安 Secret Key
	
	private int autoTrade = 0; //是否启用自动交易 0：否 1：是
	
	private int drawTrade = 0; //是否启用画线交易 0：否 1：是
	
	private int autoTradeType = 0; //自动交易参考指标 取值参考泛型 com.bugbycode.module.binance.AutoTradeType
	
	private int baseStepSize = 1;//名义价值倍数
	
	private int leverage = 10;//杠杆倍数
	
	private double positionValue = 50;//持仓价值
	
	private double cutLoss = 3;//止损比例
	
	private double profit = 3;//收益筛选
	
	private int recvTrade = 1;//是否接收下单邮件 0：否 1：是
	
	private int recvCrossUnPnl = 1;//是否接收未实现盈亏邮件 0：否 1：是
	
	private double recvCrossUnPnlPercent = 0;//未实现盈亏阈值百分比 用于邮件通知条件
	
	private int tradeStepBack = 1;//是否交易回踩单 0：否 1：是
	
	private int tradeStyle = 0; // 交易风格 0：保守 1：激进
	
	private double profitLimit = 4;//止盈百分比限制 交易风格为保守时使用
	
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

	public int getFibMonitor() {
		return fibMonitor;
	}

	public void setFibMonitor(int fibMonitor) {
		this.fibMonitor = fibMonitor;
	}

	public int getRiseAndFallMonitor() {
		return riseAndFallMonitor;
	}

	public void setRiseAndFallMonitor(int riseAndFallMonitor) {
		this.riseAndFallMonitor = riseAndFallMonitor;
	}

	public int getEmaMonitor() {
		return emaMonitor;
	}

	public void setEmaMonitor(int emaMonitor) {
		this.emaMonitor = emaMonitor;
	}

	public int getEmaRiseAndFall() {
		return emaRiseAndFall;
	}

	public void setEmaRiseAndFall(int emaRiseAndFall) {
		this.emaRiseAndFall = emaRiseAndFall;
	}

	public int getHighOrLowMonitor() {
		return highOrLowMonitor;
	}

	public void setHighOrLowMonitor(int highOrLowMonitor) {
		this.highOrLowMonitor = highOrLowMonitor;
	}

	public int getAreaMonitor() {
		return areaMonitor;
	}

	public void setAreaMonitor(int areaMonitor) {
		this.areaMonitor = areaMonitor;
	}

	public String getBinanceApiKey() {
		return binanceApiKey;
	}

	public void setBinanceApiKey(String binanceApiKey) {
		this.binanceApiKey = binanceApiKey;
	}

	public String getBinanceSecretKey() {
		return binanceSecretKey;
	}

	public void setBinanceSecretKey(String binanceSecretKey) {
		this.binanceSecretKey = binanceSecretKey;
	}

	public int getAutoTrade() {
		return autoTrade;
	}

	public void setAutoTrade(int autoTrade) {
		this.autoTrade = autoTrade;
	}

	public int getDrawTrade() {
		return drawTrade;
	}

	public void setDrawTrade(int drawTrade) {
		this.drawTrade = drawTrade;
	}

	public int getBaseStepSize() {
		return baseStepSize;
	}

	public void setBaseStepSize(int baseStepSize) {
		this.baseStepSize = baseStepSize;
	}

	public int getLeverage() {
		return leverage;
	}

	public void setLeverage(int leverage) {
		this.leverage = leverage;
	}

	public double getPositionValue() {
		return positionValue;
	}

	public void setPositionValue(double positionValue) {
		this.positionValue = positionValue;
	}

	public double getCutLoss() {
		return cutLoss;
	}

	public void setCutLoss(double cutLoss) {
		this.cutLoss = cutLoss;
	}

	public double getProfit() {
		return profit;
	}

	public void setProfit(double profit) {
		this.profit = profit;
	}

	public int getAutoTradeType() {
		return autoTradeType;
	}

	public void setAutoTradeType(int autoTradeType) {
		this.autoTradeType = autoTradeType;
	}
	
	public int getRecvTrade() {
		return recvTrade;
	}

	public void setRecvTrade(int recvTrade) {
		this.recvTrade = recvTrade;
	}

	public int getRecvCrossUnPnl() {
		return recvCrossUnPnl;
	}

	public void setRecvCrossUnPnl(int recvCrossUnPnl) {
		this.recvCrossUnPnl = recvCrossUnPnl;
	}

	public double getRecvCrossUnPnlPercent() {
		return recvCrossUnPnlPercent;
	}

	public void setRecvCrossUnPnlPercent(double recvCrossUnPnlPercent) {
		this.recvCrossUnPnlPercent = recvCrossUnPnlPercent;
	}

	public int getTradeStepBack() {
		return tradeStepBack;
	}

	public void setTradeStepBack(int tradeStepBack) {
		this.tradeStepBack = tradeStepBack;
	}

	public int getTradeStyle() {
		return tradeStyle;
	}

	public void setTradeStyle(int tradeStyle) {
		this.tradeStyle = tradeStyle;
	}

	public double getProfitLimit() {
		return profitLimit;
	}

	public void setProfitLimit(double profitLimit) {
		this.profitLimit = profitLimit;
	}

	public void copy(User user) {
		this.setId(user.getId());
		this.setUsername(user.getUsername());
		this.setPassword(user.getPassword());
		this.setRiseAndFallMonitor(user.getRiseAndFallMonitor());
		this.setHighOrLowMonitor(user.getHighOrLowMonitor());
		this.setFibMonitor(user.getFibMonitor());
		this.setEmaRiseAndFall(user.getEmaRiseAndFall());
		this.setEmaMonitor(user.getEmaMonitor());
		this.setAreaMonitor(user.getAreaMonitor());
		this.setBinanceApiKey(user.getBinanceApiKey());
		this.setBinanceSecretKey(user.getBinanceSecretKey());
		this.setRecvTrade(user.getRecvTrade());
		this.setRecvCrossUnPnl(user.getRecvCrossUnPnl());
		this.setRecvCrossUnPnlPercent(user.getRecvCrossUnPnlPercent());
		this.setTradeStepBack(user.getTradeStepBack());
		this.setTradeStyle(user.getTradeStyle());
		this.setProfitLimit(user.getProfitLimit());
	}
	
	public void copyAIInfo(User user) {
		this.setRiseAndFallMonitor(user.getRiseAndFallMonitor());
		this.setHighOrLowMonitor(user.getHighOrLowMonitor());
		this.setFibMonitor(user.getFibMonitor());
		this.setEmaRiseAndFall(user.getEmaRiseAndFall());
		this.setEmaMonitor(user.getEmaMonitor());
		this.setAreaMonitor(user.getAreaMonitor());
		this.setRecvTrade(user.getRecvTrade());
		this.setRecvCrossUnPnl(user.getRecvCrossUnPnl());
		this.setRecvCrossUnPnlPercent(user.getRecvCrossUnPnlPercent());
		this.setTradeStepBack(user.getTradeStepBack());
		this.setTradeStyle(user.getTradeStyle());
		this.setProfitLimit(user.getProfitLimit());
	}
}
