package com.bugbycode.repository.impl;

import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;
import org.springframework.util.CollectionUtils;

import com.bugbycode.module.Klines;
import com.bugbycode.repository.KlinesRepository;
import com.util.KlinesComparator;
import com.util.StringUtil;

import jakarta.annotation.Resource;

@Repository("klinesRepository")
public class KlinesRepositoryImpl implements KlinesRepository{

    private final Logger logger = LogManager.getLogger(KlinesRepositoryImpl.class);

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

    @Override
    public boolean checkData(List<Klines> list) {
        boolean result = true;
        if(!CollectionUtils.isEmpty(list)){
            list.sort(new KlinesComparator());
            for(int index = 0;index < list.size();index++){
                if(index == list.size() - 1){
                    continue;
                }
                Klines current = list.get(index);
                Klines next = list.get(index + 1);
                //判断重复
                if(current.getStartTime() == next.getStartTime()){
                    logger.info("查询到重复K线信息：" + current);
                    result = false;
                    String _id = current.getId();
                    if(StringUtil.isNotEmpty(_id)){
                        remove(_id);
                        logger.info("重复k线已从数据库中删除");
                    }
                }
            }
        }
        return result;
    }

    @Override
    public void remove(String _id) {
        template.remove(Query.query(Criteria.where("_id").is(_id)), Klines.class);
    }

    @Override
    public Klines findById(String _id) {
        return template.findOne(Query.query(Criteria.where("_id").is(_id)), Klines.class);
    }

}
