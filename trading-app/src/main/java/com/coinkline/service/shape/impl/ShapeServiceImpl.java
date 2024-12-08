package com.coinkline.service.shape.impl;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.coinkline.module.ShapeInfo;
import com.coinkline.repository.shape.ShapeRepository;
import com.coinkline.service.shape.ShapeService;
import com.util.page.Page;
import com.util.page.SearchResult;

@Service("shapeService")
public class ShapeServiceImpl implements ShapeService{

	@Autowired
	private ShapeRepository shapeRepository;
	
	@Override
	public SearchResult<ShapeInfo> query(String owner, String symbol, long skip, int limit) {
		long totalCount = shapeRepository.count(owner, symbol);
		Page page = new Page(totalCount, skip, limit);
		List<ShapeInfo> list = new ArrayList<ShapeInfo>();
		if(totalCount > 0) {
			list = shapeRepository.query(owner, symbol, skip, limit);
		}
		return new SearchResult<ShapeInfo>(list, page);
	}

}
