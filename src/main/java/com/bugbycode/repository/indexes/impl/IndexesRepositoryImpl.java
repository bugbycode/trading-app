package com.bugbycode.repository.indexes.impl;

import org.bson.Document;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.stereotype.Repository;

import com.bugbycode.repository.indexes.IndexesRepository;
import com.mongodb.client.ListIndexesIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.model.Indexes;

import jakarta.annotation.Resource;

@Repository("indexesRepository")
public class IndexesRepositoryImpl implements IndexesRepository{

	@Resource
	private MongoOperations template;
	
	@Override
	public boolean isEmptyIndexes(String collectionName) {
		MongoCursor<Document> it = getIndexes(collectionName).iterator();
		int num = 0;
		while(it.hasNext()) {
			Document doc = it.next();
			String name = doc.getString("name");
			if("_id_".equals(name)) {
				continue;
			}
			num++;
		}
		return num == 0;
	}

	@Override
	public void createKlinesIndexes(String collectionName) {
		if(isEmptyIndexes(collectionName)) {
			MongoCollection<Document> c = template.getCollection(collectionName);
			c.createIndex(Indexes.ascending("pair", "interval", "startTime"));
		}
	}

	@Override
	public ListIndexesIterable<Document> getIndexes(String collectionName) {
		return template.getCollection(collectionName).listIndexes();
	}

	@Override
	public void dropAllIndexes(String collectionName) {
		template.getCollection(collectionName).dropIndexes();
	}

}
