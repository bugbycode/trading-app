package com.bugbycode.repository.trading.impl;

import java.util.List;
import java.util.Map;

import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationOperation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Repository;

import com.bugbycode.module.trading.TradingOrder;
import com.bugbycode.repository.trading.OrderRepository;
import com.util.StringUtil;

import jakarta.annotation.Resource;

@Repository("orderRepository")
public class OrderRepositoryImpl implements OrderRepository {

	@Resource
	private MongoOperations template;
	
	@Override
	public String insert(TradingOrder order) {
		TradingOrder result = template.insert(order);
		return result.getId();
	}

	@Override
	public void updateById(String id, double closePrice, long closeTime, double pnl) {
		Update update = new Update();
		update.set("closePrice", closePrice);
		update.set("closeTime", closeTime);
		update.set("pnl", pnl);
		template.updateFirst(Query.query(Criteria.where("id").is(id)), update, TradingOrder.class);
	}

	@Override
	public List<TradingOrder> findAll(String pair,int closeStatus,long skip,int limit) {
		Criteria c = null;
		
		if(closeStatus == 0) {
			c = Criteria.where("closeTime").is(0);
		} else if(closeStatus == 1) {
			c = Criteria.where("closeTime").gt(0);
		} 
		
		if(StringUtil.isNotEmpty(pair)) {
			if(c == null) {
				c = Criteria.where("pair").is(pair);
			} else {
				c.and("pair").is(pair);
			}
		}
		
		Query q = null;
		
		if(c == null) {
			q = new Query();
		} else {
			q = Query.query(c);
		}
		
		if(skip >= 0) {
			q.skip(skip);
			q.limit(limit);
		}
		
		return template.find(q, TradingOrder.class);
	}

	@Override
	public long countAll(String pair,int closeStatus) {
		Criteria c = null;
		
		if(closeStatus == 0) {
			c = Criteria.where("closeTime").is(0);
		} else if(closeStatus == 1) {
			c = Criteria.where("closeTime").gt(0);
		}
		
		if(StringUtil.isNotEmpty(pair)) {
			if(c == null) {
				c = Criteria.where("pair").is(pair);
			} else {
				c.and("pair").is(pair);
			}
		}
		
		Query q = null;
		
		if(c == null) {
			q = new Query();
		} else {
			q = Query.query(c);
		}
		
		return template.count(q, TradingOrder.class);
	}

	@Override
	public long countOpening(String pair, double openPrice, double takeProfit, double stopLoss, int longOrShort) {
		return template.count(Query.query(
				Criteria.where("pair").is(pair).and("openPrice").is(openPrice).and("takeProfit").is(takeProfit)
				.and("stopLoss").is(stopLoss).and("longOrShort").is(longOrShort)
				), 
				TradingOrder.class);
	}

	@Override
	public long countPositivePnlOrders() {
		return template.count(Query.query(Criteria.where("pnl").gt(0)), TradingOrder.class);
	}

	@SuppressWarnings({ "unchecked", "rawtypes", "null" })
	@Override
	public double sumPnl() {
		Aggregation aggregation = Aggregation.newAggregation(Aggregation.group().sum("pnl").as("totalPnl"));
		AggregationResults<Map> result = template.aggregate(aggregation, TradingOrder.class,Map.class);
		Map<String,Double> map = result.getUniqueMappedResult();
		return map == null ? 0 : map.get("totalPnl");
	}

}
