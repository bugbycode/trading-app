package com.bugbycode.repository.shape.impl;

import java.util.List;

import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Repository;

import com.bugbycode.module.ShapeInfo;
import com.bugbycode.repository.shape.ShapeRepository;
import com.util.StringUtil;

import jakarta.annotation.Resource;

@Repository("shapeRepository")
public class ShapeRepositoryImpl implements ShapeRepository {

	@Resource
	private MongoOperations template;
	
	@Override
	public List<ShapeInfo> query(String owner, String symbol) {
		return template.find(Query.query(Criteria.where("owner").is(owner).and("symbol").is(symbol)), ShapeInfo.class);
	}

	@Override
	public List<ShapeInfo> queryByOwner(String owner) {
		return template.find(Query.query(Criteria.where("owner").is(owner)), ShapeInfo.class);
	}
	
	@Override
	public List<ShapeInfo> queryBySymbol(String symbol) {
		return template.find(Query.query(Criteria.where("symbol").is(symbol)), ShapeInfo.class);
	}

	@Override
	public String insert(ShapeInfo info) {
		ShapeInfo result = template.insert(info);
		return result == null ? null : result.getId();
	}

	@Override
	public void deleteById(String id) {
		template.remove(Query.query(Criteria.where("id").is(id)), ShapeInfo.class);
	}

	@Override
	public ShapeInfo queryById(String id) {
		return template.findOne(Query.query(Criteria.where("id").is(id)), ShapeInfo.class);
	}

	@Override
	public void update(ShapeInfo info) {
		template.save(info);
	}

	@Override
	public List<ShapeInfo> query() {
		return template.findAll(ShapeInfo.class);
	}

	@Override
	public void updateLongOrShortTypeById(String id, int longOrShortType) {
		Update update = new Update();
		update.set("longOrShortType", longOrShortType);
		template.updateMulti(Query.query(Criteria.where("id").is(id)), update, ShapeInfo.class);
	}

	@Override
	public List<ShapeInfo> query(String owner, String symbol, long skip, int limit) {
		Criteria c = Criteria.where("owner").is(owner);
		
		if(StringUtil.isNotEmpty(symbol)) {
			c.and("symbol").regex(symbol, "i");
		}
		
		Query q = Query.query(c);
		
		q.with(Sort.by(Sort.Direction.DESC,"createTime"));
		
		q.skip(skip);
		q.limit(limit);
		
		return template.find(q, ShapeInfo.class);
	}

	@Override
	public long count(String owner, String symbol) {
		Criteria c = Criteria.where("owner").is(owner);
		
		if(StringUtil.isNotEmpty(symbol)) {
			c.and("symbol").regex(symbol, "i");
		}
		
		Query q = Query.query(c);
		
		return template.count(q, ShapeInfo.class);
	}

}
