package com.coinkline.repository.tradingview.impl;

import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;

import com.coinkline.module.config.TradingViewConfig;
import com.coinkline.repository.tradingview.TradingViewConfigRepository;

import jakarta.annotation.Resource;

@Repository("tradingViewConfigRepository")
public class TradingViewConfigRepositoryImpl implements TradingViewConfigRepository {

	@Resource
	private MongoOperations template;
	
	@Override
	public TradingViewConfig queryByOwner(String owner) {
		return template.findOne(Query.query(Criteria.where("owner").is(owner)), TradingViewConfig.class);
	}

	@Override
	public TradingViewConfig save(TradingViewConfig config) {
		TradingViewConfig cfg = queryByOwner(config.getOwner());
		if(cfg == null) {
			return template.insert(config);
		} else {
			cfg.setInerval(config.getInerval());
			cfg.setSymbol(config.getSymbol());
			return template.save(cfg);
		}
	}

}
