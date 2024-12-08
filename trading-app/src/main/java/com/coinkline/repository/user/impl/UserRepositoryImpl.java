package com.coinkline.repository.user.impl;

import java.util.List;

import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Repository;

import com.coinkline.module.binance.AutoTrade;
import com.coinkline.module.binance.AutoTradeType;
import com.coinkline.module.binance.DrawTrade;
import com.coinkline.module.user.User;
import com.coinkline.repository.user.UserRepository;
import com.util.MD5Util;

import jakarta.annotation.Resource;

@Repository("userRepository")
public class UserRepositoryImpl implements UserRepository {

	@Resource
	private MongoOperations template;
	
	@Override
	public User queryByUsername(String username) {
		return template.findOne(Query.query(Criteria.where("username").is(username)), User.class);
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
		template.updateMulti(Query.query(Criteria.where("username").is(username)), update, User.class);
	}

	@Override
	public List<User> queryAllUserByFibMonitor(int fibMonitor) {
		return template.find(Query.query(Criteria.where("fibMonitor").is(fibMonitor)), User.class);
	}

	@Override
	public List<User> queryAllUserByRiseAndFallMonitor(int riseAndFallMonitor) {
		return template.find(Query.query(Criteria.where("riseAndFallMonitor").is(riseAndFallMonitor)), User.class);
	}

	@Override
	public List<User> queryAllUserByEmaMonitor(int emaMonitor) {
		return template.find(Query.query(Criteria.where("emaMonitor").is(emaMonitor)), User.class);
	}

	@Override
	public List<User> queryAllUserByEmaRiseAndFall(int emaRiseAndFall) {
		return template.find(Query.query(Criteria.where("emaRiseAndFall").is(emaRiseAndFall)), User.class);
	}

	@Override
	public List<User> queryAllUserByHighOrLowMonitor(int highOrLowMonitor) {
		return template.find(Query.query(Criteria.where("highOrLowMonitor").is(highOrLowMonitor)), User.class);
	}
	
	@Override
	public List<User> queryAllUserByAreaMonitor(int areaMonitor) {
		return template.find(Query.query(Criteria.where("areaMonitor").is(areaMonitor)), User.class);
	}

	@Override
	public List<User> queryAllUser() {
		return template.findAll(User.class);
	}

	@Override
	public void updateBinanceApiSecurity(String username,String binanceApiKey, String binanceSecretKey,int autoTrade,
			int baseStepSize,int leverage,int positionValue, int cutLoss,double profit,int autoTradeType,int drawTrade) {
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

	
}
