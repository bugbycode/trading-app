package com.bugbycode.service.user;

import java.util.List;

import com.bugbycode.module.user.ChildApiKeyConfig;
import com.util.page.SearchResult;

public interface ChildApiKeyConfigService {

	/**
	 * 添加子账号API配置信息
	 * @param cfg
	 * @return
	 */
	public String insert(ChildApiKeyConfig cfg);
	
	/**
	 * 根据ID修改一条记录
	 * @param cfg
	 */
	public void updateById(ChildApiKeyConfig cfg);
	
	/**
	 * 根据ID删除一条记录
	 * @param id
	 */
	public void deleteById(String id);
	
	/**
	 * 根据ID查询一条记录
	 * @param id
	 * @return
	 */
	public ChildApiKeyConfig findById(String id);
	
	/**
	 * 根据ApiKey查询一条记录
	 * @param binanceApiKey
	 * @return
	 */
	public ChildApiKeyConfig findByApiKey(String binanceApiKey);
	
	/**
	 * 根据母账号信息查询所有关联的记录
	 * @param username 母账号
	 * @return
	 */
	public List<ChildApiKeyConfig> findByUsername(String username);
	
	/**
	 * 自定义条件分页查询
	 * @param username 母账号
	 * @param keyword 关键词（匹配email或binanceApiKey）
	 * @param skip
	 * @param limit
	 * @return
	 */
	public SearchResult<ChildApiKeyConfig> query(String username, String keyword,long skip,int limit);
	
}
