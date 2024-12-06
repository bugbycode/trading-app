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
	
	private int positionValue = 50;//持仓价值
	
	private int cutLoss = 3;//止损比例
	
	private double profit = 3;//收益筛选
	
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

	public int getPositionValue() {
		return positionValue;
	}

	public void setPositionValue(int positionValue) {
		this.positionValue = positionValue;
	}

	public int getCutLoss() {
		return cutLoss;
	}

	public void setCutLoss(int cutLoss) {
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
	}
	
	public void copyAIInfo(User user) {
		this.setRiseAndFallMonitor(user.getRiseAndFallMonitor());
		this.setHighOrLowMonitor(user.getHighOrLowMonitor());
		this.setFibMonitor(user.getFibMonitor());
		this.setEmaRiseAndFall(user.getEmaRiseAndFall());
		this.setEmaMonitor(user.getEmaMonitor());
		this.setAreaMonitor(user.getAreaMonitor());
	}
}
