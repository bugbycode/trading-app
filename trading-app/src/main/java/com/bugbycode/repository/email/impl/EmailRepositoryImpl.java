package com.bugbycode.repository.email.impl;

import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;

import com.bugbycode.module.EmailAuth;
import com.bugbycode.repository.email.EmailRepository;

import jakarta.annotation.Resource;

@Repository("emailRepository")
public class EmailRepositoryImpl implements EmailRepository {

	private final Logger logger = LogManager.getLogger(EmailRepositoryImpl.class);
	
	@Resource
	private MongoOperations template;
	
	@Override
	public List<EmailAuth> query() {
		return template.findAll(EmailAuth.class);
	}

	@Override
	public EmailAuth insert(EmailAuth auth) {
		return template.insert(auth);
	}

	@Override
	public void update(EmailAuth auth) {
		template.save(auth);
	}

	@Override
	public void deleteById(String id) {
		template.remove(Query.query(Criteria.where("id").is(id)), EmailAuth.class);
	}

}
