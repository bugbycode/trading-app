package com.bugbycode.repository.impl;

import java.util.List;

import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;

import com.bugbycode.module.Klines;
import com.bugbycode.repository.KlinesRepository;

import jakarta.annotation.Resource;

@Repository("klinesRepository")
public class KlinesRepositoryImpl implements KlinesRepository{

    @Resource
	private MongoOperations template;

    @Override
    public void insert(Klines klines) {
        Klines tmp = findOneByStartTime(klines.getStartTime(),klines.getPair());
        if(tmp == null) {
            template.insert(klines);
        }
    }

    @Override
    public List<Klines> findByPair(String pair) {
        return template.find(Query.query(Criteria.where("pair").is(pair))
            .with(Sort.by(Sort.Direction.ASC,"startTime")), Klines.class);
    }

    @Override
    public Klines findOneByStartTime(long startTime,String pair) {
        return template.findOne(Query.query(Criteria.where("startTime").is(startTime).and("pair").is(pair)), Klines.class);
    }

    @Override
    public void remove(long startTime,String pair) {
        template.remove(Query.query(Criteria.where("startTime").is(startTime).and("pair").is(pair)), Klines.class);
    }

    @Override
    public List<Klines> findByPairAndGtStartTime(String pair, long startTime) {
        return template.find(Query.query(Criteria.where("pair").is(pair).and("startTime").gte(startTime))
            .with(Sort.by(Sort.Direction.ASC,"startTime")), Klines.class);
    }

}
