package com.bugbycode.module.binance;

public class Balance {
	
	private String accountAlias;// 账户唯一识别码
	private String asset; // 资产
	private String balance;//总余额
	private String crossWalletBalance;//全仓余额
	private String crossUnPnl;//全仓持仓未实现盈亏
	private String availableBalance;//下单可用余额
	private String maxWithdrawAmount;//最大可转出余额
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
	public String getBalance() {
		return balance;
	}
	public void setBalance(String balance) {
		this.balance = balance;
	}
	public String getCrossWalletBalance() {
		return crossWalletBalance;
	}
	public void setCrossWalletBalance(String crossWalletBalance) {
		this.crossWalletBalance = crossWalletBalance;
	}
	public String getCrossUnPnl() {
		return crossUnPnl;
	}
	public void setCrossUnPnl(String crossUnPnl) {
		this.crossUnPnl = crossUnPnl;
	}
	public String getAvailableBalance() {
		return availableBalance;
	}
	public void setAvailableBalance(String availableBalance) {
		this.availableBalance = availableBalance;
	}
	public String getMaxWithdrawAmount() {
		return maxWithdrawAmount;
	}
	public void setMaxWithdrawAmount(String maxWithdrawAmount) {
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
