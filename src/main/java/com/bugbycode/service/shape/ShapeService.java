package com.bugbycode.service.shape;

import com.bugbycode.module.ShapeInfo;
import com.util.page.Page;
import com.util.page.SearchResult;

public interface ShapeService {
	
	/**
	 * 自定义条件分页查询图纸信息
	 * 
	 * @param owner 创建者
	 * @param symbol 交易对
	 * @param skip 起始记录数
	 * @param limit 每页条数
	 * @return
	 */
	public SearchResult<ShapeInfo> query(String owner,String symbol,long skip,int limit);
}
