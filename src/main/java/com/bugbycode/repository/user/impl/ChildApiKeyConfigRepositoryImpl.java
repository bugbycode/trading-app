package com.bugbycode.repository.user.impl;

import java.util.List;

import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Repository;

import com.bugbycode.module.user.ChildApiKeyConfig;
import com.bugbycode.repository.user.ChildApiKeyConfigRepository;
import com.util.StringUtil;

import jakarta.annotation.Resource;

@Repository("childApiKeyConfigRepository")
public class ChildApiKeyConfigRepositoryImpl implements ChildApiKeyConfigRepository {

	@Resource
	private MongoOperations template;
	
	@Override
	public String insert(ChildApiKeyConfig cfg) {
		ChildApiKeyConfig result = null;
		String apiKey = cfg.getBinanceApiKey();
		ChildApiKeyConfig tmp = findByApiKey(apiKey);
		if(tmp == null && StringUtil.isNotEmpty(apiKey)) {
			result = template.insert(cfg);
		}
		return result == null ? null : result.getId();
	}

	@Override
	public void updateById(ChildApiKeyConfig cfg) {
		
		Update update = new Update();
		
		update.set("email", cfg.getEmail());
		update.set("binanceApiKey", cfg.getBinanceApiKey());
		update.set("binanceSecretKey", cfg.getBinanceSecretKey());
		update.set("updateTime", cfg.getUpdateTime());
		
		template.updateMulti(Query.query(Criteria.where("id").is(cfg.getId())), update, ChildApiKeyConfig.class);
	}

	@Override
	public void deleteById(String id) {
		template.remove(Query.query(Criteria.where("id").is(id)), ChildApiKeyConfig.class);
	}

	@Override
	public ChildApiKeyConfig findById(String id) {
		return template.findOne(Query.query(Criteria.where("id").is(id)), ChildApiKeyConfig.class);
	}

	@Override
	public ChildApiKeyConfig findByApiKey(String binanceApiKey) {
		return template.findOne(Query.query(Criteria.where("binanceApiKey").is(binanceApiKey)), ChildApiKeyConfig.class);
	}

	@Override
	public List<ChildApiKeyConfig> findByUsername(String username) {
		return template.find(Query.query(Criteria.where("username").is(username)), ChildApiKeyConfig.class);
	}

	@Override
	public List<ChildApiKeyConfig> find(String username, String keyword, long skip, int limit) {
		Criteria c = Criteria.where("username").is(username);
		
		if(StringUtil.isNotEmpty(keyword)) {
			
			c.andOperator(
					new Criteria().orOperator(
							Criteria.where("email").regex(keyword, "i"),
							Criteria.where("binanceApiKey").regex(keyword, "i")
					)
			);
			
		}
		
		Query q = Query.query(c);
		
		q.with(Sort.by(Sort.Direction.DESC,"createTime"));
		
		q.skip(skip);
		q.limit(limit);
		
		return template.find(q, ChildApiKeyConfig.class);
	}

	@Override
	public long count(String username, String keyword) {
		Criteria c = Criteria.where("username").is(username);

		if(StringUtil.isNotEmpty(keyword)) {
			
			c.andOperator(
					new Criteria().orOperator(
							Criteria.where("email").regex(keyword, "i"),
							Criteria.where("binanceApiKey").regex(keyword, "i")
					)
			);
			
		}
		
		Query q = Query.query(c);
		
		return template.count(q, ChildApiKeyConfig.class);
	}

}
