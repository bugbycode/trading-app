package com.bugbycode.repository.user.impl;

import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Repository;

import com.bugbycode.module.FibLevel;
import com.bugbycode.module.MonitorStatus;
import com.bugbycode.module.RecvCrossUnPnlStatus;
import com.bugbycode.module.VolumeMonitorStatus;
import com.bugbycode.module.binance.AutoTrade;
import com.bugbycode.module.binance.AutoTradeType;
import com.bugbycode.module.binance.DrawTrade;
import com.bugbycode.module.user.User;
import com.bugbycode.repository.user.UserRepository;
import com.util.MD5Util;

import jakarta.annotation.Resource;

@Repository("userRepository")
public class UserRepositoryImpl implements UserRepository {

	private final Logger logger = LogManager.getLogger(UserRepositoryImpl.class);
	
	@Resource
	private MongoOperations template;
	
	@Override
	public User queryByUsername(String username) {
		try {
			return template.findOne(Query.query(Criteria.where("username").is(username)), User.class);
		} catch (Exception e) {
			logger.error(e.getLocalizedMessage(), e);
			throw new UsernameNotFoundException("用户名密码错误");
		}
	}

	@Override
	public String insert(User user) {
		user = template.insert(user);
		return user.getId();
	}

	@Override
	public void updatePassword(String username,String password) {
		Update update = new Update();
		update.set("password", MD5Util.md5(password));
		template.updateMulti(Query.query(Criteria.where("username").is(username)), update, User.class);
	}
	
	@Override
	public void updateSmtpSetting(String username,
			String smtpUser,String smtpPwd,
			String smtpUser2,String smtpPwd2,
			String smtpUser3,String smtpPwd3,
			String smtpHost,int smtpPort) {
		Update update = new Update();
		update.set("smtpUser", smtpUser);
		update.set("smtpPwd", smtpPwd);
		update.set("smtpUser2", smtpUser2);
		update.set("smtpPwd2", smtpPwd2);
		update.set("smtpUser3", smtpUser3);
		update.set("smtpPwd3", smtpPwd3);
		update.set("smtpHost", smtpHost);
		update.set("smtpPort", smtpPort);
		template.updateMulti(Query.query(Criteria.where("username").is(username)), update, User.class);
	}
	
	@Override
	public void updateSmtpIndex(String username, int smtpIndex) {
		Update update = new Update();
		update.set("smtpIndex", smtpIndex);
		template.updateMulti(Query.query(Criteria.where("username").is(username)), update, User.class);
	}

	@Override
	public void deleteByUsername(String username) {
		template.remove(Query.query(Criteria.where("username").is(username)), User.class);
	}

	@Override
	public void updateUserSubscribeInfo(String username,User user) {
		Update update = new Update();
		update.set("emaMonitor", user.getEmaMonitor());
		update.set("emaRiseAndFall", user.getEmaRiseAndFall());
		update.set("fibMonitor", user.getFibMonitor());
		update.set("highOrLowMonitor", user.getHighOrLowMonitor());
		update.set("riseAndFallMonitor", user.getRiseAndFallMonitor());
		update.set("areaMonitor", user.getAreaMonitor());
		update.set("volumeMonitor", user.getVolumeMonitor());
		update.set("monitorProfit", user.getMonitorProfit());
		update.set("tradeNumberMonitor", user.getTradeNumberMonitor());
		update.set("breakthroughMonitor", user.getBreakthroughMonitor());
		update.set("pairPolicySelected", user.getPairPolicySelected());
		template.updateMulti(Query.query(Criteria.where("username").is(username)), update, User.class);
	}

	@Override
	public List<User> queryAllUserByFibMonitor(MonitorStatus status) {
		return template.find(Query.query(Criteria.where("fibMonitor").is(status.getValue())), User.class);
	}

	@Override
	public List<User> queryAllUserByRiseAndFallMonitor(MonitorStatus status) {
		return template.find(Query.query(Criteria.where("riseAndFallMonitor").is(status.getValue())), User.class);
	}

	@Override
	public List<User> queryAllUserByEmaMonitor(MonitorStatus status) {
		return template.find(Query.query(Criteria.where("emaMonitor").is(status.getValue())), User.class);
	}

	@Override
	public List<User> queryAllUserByEmaRiseAndFall(MonitorStatus status) {
		return template.find(Query.query(Criteria.where("emaRiseAndFall").is(status.getValue())), User.class);
	}

	@Override
	public List<User> queryAllUserByHighOrLowMonitor(MonitorStatus status) {
		return template.find(Query.query(Criteria.where("highOrLowMonitor").is(status.getValue())), User.class);
	}
	
	@Override
	public List<User> queryAllUserByAreaMonitor(MonitorStatus status) {
		return template.find(Query.query(Criteria.where("areaMonitor").is(status.getValue())), User.class);
	}

	@Override
	public List<User> queryAllUser() {
		return template.findAll(User.class);
	}

	@Override
	public void updateBinanceApiSecurity(String username,String binanceApiKey, String binanceSecretKey,int autoTrade,
			int baseStepSize,int leverage,double positionValue, double cutLoss,double profit,int autoTradeType,int drawTrade,
			int recvTrade,int recvCrossUnPnl,double recvCrossUnPnlPercent,int tradeStepBack,int tradeStyle,double profitLimit,
			int countertrendTrading, FibLevel fibLevel, long tradeNumber, int breakthroughTrade, double callbackRate, 
			double activationPriceRatio, int callbackRateEnabled) {
		Update update = new Update();
		update.set("binanceApiKey", binanceApiKey);
		update.set("binanceSecretKey", binanceSecretKey);
		update.set("autoTrade", autoTrade);
		update.set("baseStepSize", baseStepSize);
		update.set("leverage", leverage);
		update.set("positionValue", positionValue);
		update.set("cutLoss", cutLoss);
		update.set("profit", profit);
		update.set("autoTradeType", autoTradeType);
		update.set("drawTrade", drawTrade);
		update.set("recvTrade", recvTrade);
		update.set("recvCrossUnPnl", recvCrossUnPnl);
		update.set("recvCrossUnPnlPercent", recvCrossUnPnlPercent);
		update.set("tradeStepBack", tradeStepBack);
		update.set("tradeStyle", tradeStyle);
		update.set("profitLimit", profitLimit);
		update.set("countertrendTrading", countertrendTrading);
		update.set("fibLevel", fibLevel.getValue());
		update.set("tradeNumber", tradeNumber);
		update.set("breakthroughTrade", breakthroughTrade);
		update.set("callbackRate", callbackRate);
		update.set("activationPriceRatio", activationPriceRatio);
		update.set("callbackRateEnabled", callbackRateEnabled);
		
		template.updateMulti(Query.query(Criteria.where("username").is(username)), update, User.class);
	}

	@Override
	public List<User> queryByAutoTrade(AutoTrade autoTrade,AutoTradeType autoTradeType) {
		return template.find(
				
				Query.query(Criteria.where("autoTrade").is(autoTrade.value()).and("autoTradeType").is(autoTradeType.value())), 
				
				User.class);
	}

	@Override
	public List<User> queryByDrawTrade(DrawTrade drawTrade) {
		return template.find(
				
				Query.query(Criteria.where("drawTrade").is(drawTrade.getValue())), 
				
				User.class);
	}

	@Override
	public List<User> queryByRecvCrossUnPnl(RecvCrossUnPnlStatus status) {
		return template.find(
				
				Query.query(Criteria.where("recvCrossUnPnl").is(status.getValue())), 
				
				User.class);
	}

	@Override
	public List<User> queryByVolumeMonitorStatus(VolumeMonitorStatus status) {
		return template.find(
				
				Query.query(Criteria.where("volumeMonitor").is(status.getValue())), 
				
				User.class);
	}
	
}
