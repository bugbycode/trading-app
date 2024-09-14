package com.bugbycode.repository.impl;

import java.util.List;

import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;
import org.springframework.util.CollectionUtils;

import com.bugbycode.module.Klines;
import com.bugbycode.repository.KlinesRepository;

import jakarta.annotation.Resource;

@Repository("klinesRepository")
public class KlinesRepositoryImpl implements KlinesRepository{

    @Resource
	private MongoOperations template;

    @Override
    public void insert(Klines klines) {
        Klines tmp = findOneByStartTime(klines.getStartTime(),klines.getPair(),klines.getInterval());
        if(tmp == null) {
            template.insert(klines);
        }
    }

    @Override
    public List<Klines> findByPair(String pair, String interval) {
        return template.find(Query.query(Criteria.where("pair").is(pair).and("interval").is(interval))
            .with(Sort.by(Sort.Direction.ASC,"startTime")), Klines.class);
    }

    @Override
    public Klines findOneByStartTime(long startTime,String pair, String interval) {
        return template.findOne(Query.query(Criteria.where("startTime").is(startTime)
        .and("pair").is(pair).and("interval").is(interval)), Klines.class);
    }

    @Override
    public void remove(long startTime,String pair, String interval) {
        template.remove(Query.query(Criteria.where("startTime").is(startTime).and("pair").is(pair)
        .and("interval").is(interval)), Klines.class);
    }

    @Override
    public List<Klines> findByPairAndGtStartTime(String pair, long startTime, String interval) {
        return template.find(Query.query(Criteria.where("pair").is(pair).and("startTime").gte(startTime)
            .and("interval").is(interval))
            .with(Sort.by(Sort.Direction.ASC,"startTime")), Klines.class);
    }

    @Override
    public void insert(List<Klines> list) {
        if(!CollectionUtils.isEmpty(list)){
            template.insertAll(list);
        }
    }

}
