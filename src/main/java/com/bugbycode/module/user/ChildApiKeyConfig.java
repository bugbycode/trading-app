package com.bugbycode.module.user;

import org.springframework.data.annotation.Id;

import com.util.DateFormatUtil;

/**
 * 子账号API配置
 */
public class ChildApiKeyConfig {
	
	@Id
	private String id;//数据库唯一标识
	
	private String username;//母账号信息
	
	private String email;//子账号邮箱
	
	private String binanceApiKey;//币安API key
	
	private String binanceSecretKey;//币安 Secret Key
	
	private long createTime;//创建时间
	
	private long updateTime;//修改时间

	public ChildApiKeyConfig() {
		
	}

	public ChildApiKeyConfig(String username, String email, String binanceApiKey, String binanceSecretKey,
			long createTime, long updateTime) {
		this.username = username;
		this.email = email;
		this.binanceApiKey = binanceApiKey;
		this.binanceSecretKey = binanceSecretKey;
		this.createTime = createTime;
		this.updateTime = updateTime;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
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

	public long getCreateTime() {
		return createTime;
	}

	public void setCreateTime(long createTime) {
		this.createTime = createTime;
	}

	public long getUpdateTime() {
		return updateTime;
	}

	public void setUpdateTime(long updateTime) {
		this.updateTime = updateTime;
	}
	
	public String getFormatCreateTime() {
		return createTime == 0 ? "" : DateFormatUtil.format(createTime);
	}
	
	public String getFormatUpdateTime() {
		return updateTime == 0 ? "" : DateFormatUtil.format(updateTime);
	}
}
