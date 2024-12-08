package com.coinkline.repository.high_low_hitprice.impl;

import java.util.List;

import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;

import com.coinkline.module.HighOrLowHitPrice;
import com.coinkline.repository.high_low_hitprice.HighOrLowHitPriceRepository;

import jakarta.annotation.Resource;

@Repository("highOrLowHitPriceRepository")
public class HighOrLowHitPriceRepositoryImpl implements HighOrLowHitPriceRepository {

    @Resource
	private MongoOperations template;
	
	@Override
	public void insert(HighOrLowHitPrice price) {
		HighOrLowHitPrice tmp = findByPrice(price.getPair(), price.getPrice());
		if(tmp == null) {
			template.insert(price);
		}
	}

	@Override
	public List<HighOrLowHitPrice> find(String pair) {
		return template.find(Query.query(Criteria.where("pair").is(pair)), HighOrLowHitPrice.class);
	}

	@Override
	public void remove(String pair, long time) {
		template.remove(Query.query(Criteria.where("pair").is(pair).and("createTime").lt(time)), HighOrLowHitPrice.class);
	}

	@Override
	public HighOrLowHitPrice findByPrice(String pair, double price) {
		return template.findOne(Query.query(Criteria.where("pair").is(pair).and("price").is(price)), HighOrLowHitPrice.class);
	}

}
