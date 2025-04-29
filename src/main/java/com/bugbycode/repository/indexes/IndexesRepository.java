package com.bugbycode.repository.indexes;

import org.bson.Document;

import com.mongodb.client.ListIndexesIterable;

public interface IndexesRepository {

	/**
	 * 检查集合中是否存在索引
	 * @param collectionName
	 * @return
	 */
	public boolean isEmptyIndexes(String collectionName);
	
	/**
	 * 为k线集合创建索引
	 * @param collectionName
	 */
	public void createKlinesIndexes(String collectionName);
	
	/**
	 * 获取所有索引信息
	 * @param collectionName
	 * @return
	 */
	public ListIndexesIterable<Document> getIndexes(String collectionName);
	
	/**
	 * 删除所有索引
	 * @param collectionName
	 */
	public void dropAllIndexes(String collectionName);
}
