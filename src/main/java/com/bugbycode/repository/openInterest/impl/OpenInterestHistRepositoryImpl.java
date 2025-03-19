package com.bugbycode.repository.openInterest.impl;

import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Repository;

import com.bugbycode.module.open_interest.OpenInterestHist;
import com.bugbycode.repository.openInterest.OpenInterestHistRepository;

import jakarta.annotation.Resource;

@Repository("openInterestHistRepository")
public class OpenInterestHistRepositoryImpl implements OpenInterestHistRepository{

	private final Logger logger = LogManager.getLogger(OpenInterestHistRepositoryImpl.class);
	
	@Resource
	private MongoOperations template;
	
	@Override
	public List<OpenInterestHist> query() {
		return template.findAll(OpenInterestHist.class);
	}

	@Override
	public OpenInterestHist findOneBySymbol(String symbol) {
		return template.findOne(Query.query(Criteria.where("symbol").is(symbol)), OpenInterestHist.class);
	}

	@Override
	public void save(OpenInterestHist oih) {
		try {
			String symbol = oih.getSymbol();
			OpenInterestHist tmp = findOneBySymbol(symbol);
			if(tmp == null) {
				template.insert(tmp);
			} else {
				
				Update update = new Update();
				
				update.set("sumOpenInterest", oih.getSumOpenInterest());
				update.set("sumOpenInterestValue", oih.getSumOpenInterestValue());
				update.set("timestamp", oih.getTimestamp());
				
				template.updateFirst(Query.query(Criteria.where("symbol").is(symbol)), update, OpenInterestHist.class);
			}
		} catch (Exception e) {
			logger.error("保存历史合约持仓量信息时出现异常", e);
		}
	}

	@Override
	public void remove(String id) {
		try {
			template.remove(Query.query(Criteria.where("id").is(id)), OpenInterestHist.class);
		} catch (Exception e) {
			logger.error("根据ID删除历史合约持仓量信息时出现异常", e);
		}
	}

}
