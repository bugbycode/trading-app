package com.bugbycode.trading_app;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bson.BsonDocument;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.bugbycode.config.AppConfig;
import com.bugbycode.module.Inerval;
import com.bugbycode.repository.indexes.IndexesRepository;
import com.mongodb.BasicDBObject;
import com.mongodb.client.ListIndexesIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.util.StringUtil;

import jakarta.annotation.Resource;

@SpringBootTest
public class IndexTest {

	private final Logger logger = LogManager.getLogger(IndexTest.class);
	
	private String pair = "BTCUSDT";
	
	private Inerval inerval = Inerval.INERVAL_15M;
	
	@Autowired
	private IndexesRepository indexesRepository;
	
	@BeforeAll
	public void befor() {
		AppConfig.DEBUG = true;
	}
	
	@Test
	public void testCreate() {
		String collectionName = StringUtil.formatCollectionName(pair, inerval);
		indexesRepository.createKlinesIndexes(collectionName);
	}
	
	@Test
	public void testQuery() {
		String collectionName = StringUtil.formatCollectionName(pair, inerval);
		ListIndexesIterable<Document> listIndexes = indexesRepository.getIndexes(collectionName);
		MongoCursor<Document> it = listIndexes.iterator();
		while(it.hasNext()) {
			Document doc = it.next();
			logger.info(doc.toJson());
		}
	}
	
	@Test
	public void testDrop() {
		String collectionName = StringUtil.formatCollectionName(pair, inerval);
		indexesRepository.dropAllIndexes(collectionName);
	}
}
