package com.bugbycode.repository.collections;

import com.bugbycode.module.Inerval;

public interface CollectionsRepository {

	/**
	 * 根据交易对信息清理数据
	 * 
	 * @param pair 交易对
	 * @param interval 时间级别
	 */
	public void dropCollections(String pair, Inerval interval);
}
