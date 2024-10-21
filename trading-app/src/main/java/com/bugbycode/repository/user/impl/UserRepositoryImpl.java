package com.bugbycode.repository.user.impl;

import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;

import com.bugbycode.module.user.User;
import com.bugbycode.repository.user.UserRepository;

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
	public void update(User user) {
		template.save(user);
	}

	@Override
	public void deleteByUsername(String username) {
		template.remove(Query.query(Criteria.where("username").is(username)), User.class);
	}

}
