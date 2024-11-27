package com.bugbycode.module.binance;

public class Balance {
	
	private String accountAlias;// 账户唯一识别码
	private String asset; // 资产
	private double balance;//总余额
	private double crossWalletBalance;//全仓余额
	private double crossUnPnl;//全仓持仓未实现盈亏
	private double availableBalance;//下单可用余额
	private double maxWithdrawAmount;//最大可转出余额
	private boolean marginAvailable;//是否可用作联合保证金
	private long updateTime;//
	
	public String getAccountAlias() {
		return accountAlias;
	}
	public void setAccountAlias(String accountAlias) {
		this.accountAlias = accountAlias;
	}
	public String getAsset() {
		return asset;
	}
	public void setAsset(String asset) {
		this.asset = asset;
	}
	public double getBalance() {
		return balance;
	}
	public void setBalance(double balance) {
		this.balance = balance;
	}
	public double getCrossWalletBalance() {
		return crossWalletBalance;
	}
	public void setCrossWalletBalance(double crossWalletBalance) {
		this.crossWalletBalance = crossWalletBalance;
	}
	public double getCrossUnPnl() {
		return crossUnPnl;
	}
	public void setCrossUnPnl(double crossUnPnl) {
		this.crossUnPnl = crossUnPnl;
	}
	public double getAvailableBalance() {
		return availableBalance;
	}
	public void setAvailableBalance(double availableBalance) {
		this.availableBalance = availableBalance;
	}
	public double getMaxWithdrawAmount() {
		return maxWithdrawAmount;
	}
	public void setMaxWithdrawAmount(double maxWithdrawAmount) {
		this.maxWithdrawAmount = maxWithdrawAmount;
	}
	public boolean getMarginAvailable() {
		return marginAvailable;
	}
	public void setMarginAvailable(boolean marginAvailable) {
		this.marginAvailable = marginAvailable;
	}
	public long getUpdateTime() {
		return updateTime;
	}
	public void setUpdateTime(long updateTime) {
		this.updateTime = updateTime;
	}
	
	
}
