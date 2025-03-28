package com.bugbycode.repository.klines.impl;

import java.util.ArrayList;
import java.util.List;

import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;
import org.springframework.util.CollectionUtils;

import com.bugbycode.module.Inerval;
import com.bugbycode.module.Klines;
import com.bugbycode.repository.klines.KlinesRepository;

import jakarta.annotation.Resource;

@Repository("klinesRepository")
public class KlinesRepositoryImpl implements KlinesRepository{

    @Resource
	private MongoOperations template;

    @Override
    public void insert(Klines klines) {
        Klines tmp = findOneByStartTime(klines.getStartTime(),klines.getPair(),klines.getInervalType());
        if(tmp == null) {
            template.insert(klines);
        }
    }

    @Override
    public List<Klines> findByPair(String pair, Inerval interval) {
        return template.find(Query.query(Criteria.where("pair").is(pair).and("interval").is(interval.getDescption()))
            .with(Sort.by(Sort.Direction.ASC,"startTime")), Klines.class);
    }

    @Override
    public Klines findOneByStartTime(long startTime,String pair, Inerval interval) {
        return template.findOne(Query.query(Criteria.where("pair").is(pair)
        .and("startTime").is(startTime).and("interval").is(interval.getDescption())), Klines.class);
    }

    @Override
    public void remove(long startTime,String pair, Inerval interval) {
        template.remove(Query.query(Criteria.where("pair").is(pair).and("startTime").is(startTime)
        .and("interval").is(interval.getDescption())), Klines.class);
    }

    @Override
    public List<Klines> findByPairAndGtStartTime(String pair, long startTime, Inerval interval) {
        return template.find(Query.query(Criteria.where("pair").is(pair).and("startTime").gte(startTime)
            .and("interval").is(interval.getDescption()))
            .with(Sort.by(Sort.Direction.ASC,"startTime")), Klines.class);
    }

    @Override
    public void insert(List<Klines> list) {
        if(!CollectionUtils.isEmpty(list)){
            //template.insertAll(list);
        	for(Klines k : list) {
        		insert(k);
        	}
        }
    }

    @Override
    public void remove(String _id) {
        template.remove(Query.query(Criteria.where("_id").is(_id)), Klines.class);
    }

    @Override
    public Klines findById(String _id) {
        return template.findOne(Query.query(Criteria.where("_id").is(_id)), Klines.class);
    }

	@Override
	public List<Klines> findByPair(String pair, Inerval interval, long skip, int limit) {
		return template.find(Query.query(Criteria.where("pair").is(pair).and("interval").is(interval.getDescption()))
	            .with(Sort.by(Sort.Direction.ASC,"startTime")).skip(skip).limit(limit), Klines.class);
	}

	@Override
	public long count(String pair, Inerval interval) {
		return template.count(Query.query(Criteria.where("pair").is(pair).and("interval").is(interval.getDescption())).with(Sort.by(Sort.Direction.ASC,"startTime")), Klines.class);
	}
	
	@Override
	public boolean isEmpty(String pair,Inerval interval) {
		Klines result = template.findOne(Query.query(Criteria.where("pair").is(pair).and("interval").is(interval.getDescption())), Klines.class);
		return result == null;
	}

	@Override
	public List<Klines> findLastKlinesByPair(String pair, Inerval interval, int limit) {
		long count = count(pair, interval);
		if(count > 0) {
			return findByPair(pair, interval, count - limit, limit);
		}
		return new ArrayList<Klines>();
	}

	@Override
	public List<Klines> findByTimeLimit(String pair, Inerval inerval, long startTime, long endTime) {
		Criteria c = Criteria.where("pair").is(pair).and("interval").is(inerval.getDescption()).and("startTime").gte(startTime).and("startTime").lte(endTime);
		Query q = Query.query(c);
		return template.find(q, Klines.class);
	}

}
