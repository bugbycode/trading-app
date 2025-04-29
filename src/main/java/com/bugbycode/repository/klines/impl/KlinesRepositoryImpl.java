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
import com.bugbycode.repository.indexes.IndexesRepository;
import com.bugbycode.repository.klines.KlinesRepository;
import com.util.StringUtil;

import jakarta.annotation.Resource;

@Repository("klinesRepository")
public class KlinesRepositoryImpl implements KlinesRepository{

    @Resource
	private MongoOperations template;
    
    @Resource
    private IndexesRepository indexesRepository;

    @Override
    public void insert(Klines klines) {
        Klines tmp = findOneByStartTime(klines.getStartTime(),klines.getPair(),klines.getInervalType());
        if(tmp == null) {
        	String collectionName = StringUtil.formatCollectionName(klines);
            template.insert(klines, collectionName);
            indexesRepository.createKlinesIndexes(collectionName);
        }
    }

    @Override
    public List<Klines> findByPair(String pair, Inerval interval) {
        return template.find(Query.query(Criteria.where("pair").is(pair).and("interval").is(interval.getDescption()))
            .with(Sort.by(Sort.Direction.ASC,"startTime")), Klines.class, StringUtil.formatCollectionName(pair, interval));
    }

    @Override
    public Klines findOneByStartTime(long startTime,String pair, Inerval interval) {
        return template.findOne(Query.query(
        		Criteria.where("pair").is(pair)
        		.and("interval").is(interval.getDescption())
        		.and("startTime").is(startTime)
        		), Klines.class, StringUtil.formatCollectionName(pair, interval));
    }

    @Override
    public void remove(long startTime,String pair, Inerval interval) {
        template.remove(Query.query(
        		Criteria.where("pair").is(pair)
        		.and("interval").is(interval.getDescption())
        		.and("startTime").is(startTime)
        ), Klines.class, StringUtil.formatCollectionName(pair, interval));
    }

    @Override
    public List<Klines> findByPairAndGtStartTime(String pair, long startTime, Inerval interval) {
        return template.find(Query.query(
        		Criteria.where("pair").is(pair)
        		.and("interval").is(interval.getDescption())
        		.and("startTime").gte(startTime)
            )
            .with(Sort.by(Sort.Direction.ASC,"startTime")), Klines.class, StringUtil.formatCollectionName(pair, interval));
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
    public void remove(String _id, String collectionName) {
        template.remove(Query.query(Criteria.where("_id").is(_id)), Klines.class, collectionName);
    }

    @Override
    public Klines findById(String _id, String collectionName) {
        return template.findOne(Query.query(Criteria.where("_id").is(_id)), Klines.class, collectionName);
    }

	@Override
	public List<Klines> findByPair(String pair, Inerval interval, long skip, int limit) {
		return template.find(Query.query(Criteria.where("pair").is(pair).and("interval").is(interval.getDescption()))
	            .with(Sort.by(Sort.Direction.ASC,"startTime")).skip(skip).limit(limit), Klines.class, StringUtil.formatCollectionName(pair, interval));
	}

	@Override
	public long count(String pair, Inerval interval) {
		return template.count(Query.query(Criteria.where("pair").is(pair).and("interval").is(interval.getDescption())).with(Sort.by(Sort.Direction.ASC,"startTime")), 
				Klines.class, StringUtil.formatCollectionName(pair, interval));
	}
	
	@Override
	public boolean isEmpty(String pair,Inerval interval) {
		Klines result = template.findOne(Query.query(Criteria.where("pair").is(pair).and("interval").is(interval.getDescption())), 
				Klines.class, StringUtil.formatCollectionName(pair, interval));
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
	public List<Klines> findByTimeLimit(String pair, Inerval interval, long startTime, long endTime) {
		Criteria c = Criteria.where("pair").is(pair).and("interval").is(interval.getDescption()).and("startTime").gte(startTime).and("startTime").lte(endTime);
		Query q = Query.query(c);
		return template.find(q, Klines.class, StringUtil.formatCollectionName(pair, interval));
	}

}
