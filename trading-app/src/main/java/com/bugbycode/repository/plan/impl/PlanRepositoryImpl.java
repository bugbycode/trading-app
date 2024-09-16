package com.bugbycode.repository.plan.impl;

import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;

import com.bugbycode.module.TradingPlan;
import com.bugbycode.repository.plan.PlanRepository;
import com.util.StringUtil;

import jakarta.annotation.Resource;

@Repository("planRepository")
public class PlanRepositoryImpl implements PlanRepository {

	private final Logger logger = LogManager.getLogger(PlanRepositoryImpl.class);
	
	@Resource
	private MongoOperations template;
	
	@Override
	public void insert(TradingPlan plan) {
		TradingPlan tmp = findByPairAndOpeningPrice(plan.getPair(), plan.getOpeningPrice());
		if(tmp != null) { 
			deleteById(tmp.getId());
		}
		template.insert(plan);
	}

	@Override
	public void deleteById(String _id) {
		template.remove(Query.query(Criteria.where("_id").is(_id)), TradingPlan.class);
	}

	@Override
	public TradingPlan findById(String _id) {
		return template.findById(_id, TradingPlan.class);
	}

	@Override
	public TradingPlan findByPairAndOpeningPrice(String pair, double openingPrice) {
		
		Query query = Query.query(Criteria.where("pair").is(pair).and("openingPrice").is(openingPrice));
		
		return template.findOne(query, TradingPlan.class);
	}

	@Override
	public List<TradingPlan> find(String pair,long skip,int limit) {
		Query query = null;
		if(StringUtil.isNotEmpty(pair)) {
			query = Query.query(Criteria.where("pair").is(pair));
		} else {
			query = new Query();
		}
		
		query.skip(skip);
		query.limit(limit);
		
		return template.find(query, TradingPlan.class);
	}

	@Override
	public long count(String pair) {
		Query query = null;
		if(StringUtil.isNotEmpty(pair)) {
			query = Query.query(Criteria.where("pair").is(pair));
		} else {
			query = new Query();
		}
		return template.count(query, TradingPlan.class);
	}

	@Override
	public List<TradingPlan> findAll() {
		return template.findAll(TradingPlan.class);
	}

	@Override
	public List<TradingPlan> find(String pair) {
		return template.find(Query.query(Criteria.where("pair").is(pair)), TradingPlan.class);
	}

}
