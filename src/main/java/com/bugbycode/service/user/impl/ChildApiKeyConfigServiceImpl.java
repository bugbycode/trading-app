package com.bugbycode.service.user.impl;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

import com.bugbycode.module.user.ChildApiKeyConfig;
import com.bugbycode.repository.user.ChildApiKeyConfigRepository;
import com.bugbycode.service.user.ChildApiKeyConfigService;
import com.util.page.Page;
import com.util.page.SearchResult;

import jakarta.annotation.Resource;

@Service("childApiKeyConfigService")
public class ChildApiKeyConfigServiceImpl implements ChildApiKeyConfigService {

	@Resource
	private ChildApiKeyConfigRepository childApiKeyConfigRepository;
	
	@Override
	public String insert(ChildApiKeyConfig cfg) {
		return childApiKeyConfigRepository.insert(cfg);
	}

	@Override
	public void updateById(ChildApiKeyConfig cfg) {
		childApiKeyConfigRepository.updateById(cfg);
	}

	@Override
	public void deleteById(String id) {
		childApiKeyConfigRepository.deleteById(id);
	}

	@Override
	public ChildApiKeyConfig findById(String id) {
		return childApiKeyConfigRepository.findById(id);
	}

	@Override
	public ChildApiKeyConfig findByApiKey(String binanceApiKey) {
		return childApiKeyConfigRepository.findByApiKey(binanceApiKey);
	}

	@Override
	public List<ChildApiKeyConfig> findByUsername(String username) {
		return childApiKeyConfigRepository.findByUsername(username);
	}

	@Override
	public SearchResult<ChildApiKeyConfig> query(String username, String keyword, long skip, int limit) {
		long totalCount = childApiKeyConfigRepository.count(username, keyword);
		Page page = new Page(totalCount, skip, limit);
		List<ChildApiKeyConfig> list = new ArrayList<ChildApiKeyConfig>();
		if(totalCount > 0) {
			list = childApiKeyConfigRepository.find(username, keyword, skip, limit);
		}
		return new SearchResult<>(list, page);
	}

}
