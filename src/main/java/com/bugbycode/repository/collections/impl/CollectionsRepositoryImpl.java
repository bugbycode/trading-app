package com.bugbycode.repository.collections.impl;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bson.Document;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.stereotype.Repository;

import com.bugbycode.module.Inerval;
import com.bugbycode.repository.collections.CollectionsRepository;
import com.mongodb.client.MongoCollection;
import com.util.StringUtil;

import jakarta.annotation.Resource;

@Repository("collectionsRepository")
public class CollectionsRepositoryImpl implements CollectionsRepository {

	private final Logger logger = LogManager.getLogger(CollectionsRepositoryImpl.class);
	
	@Resource
	private MongoOperations template;
	
	@Override
	public void dropCollections(String pair, Inerval interval) {
		String collectionName = StringUtil.formatCollectionName(pair, interval);
		MongoCollection<Document> collections = template.getCollection(collectionName);
		if(collections != null) {
			logger.info("开始删除集合{}", collectionName);
			collections.drop();
			logger.info("集合{}已被删除", collectionName);
			logger.info("已清理{}交易对{}级别的所有K线数据", pair, interval.getMemo());
		}
	}

}
