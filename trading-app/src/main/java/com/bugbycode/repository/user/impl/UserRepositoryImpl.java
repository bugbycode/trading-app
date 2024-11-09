package com.bugbycode.repository.user.impl;

import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Repository;

import com.bugbycode.module.user.User;
import com.bugbycode.repository.user.UserRepository;
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
	public void updateUserInfo(String username, int subscribeAi) {
		Update update = new Update();
		update.set("subscribeAi", subscribeAi);
		template.updateMulti(Query.query(Criteria.where("username").is(username)), update, User.class);
	}

	
}
