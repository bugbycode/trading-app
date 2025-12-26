package com.bugbycode.repository.user;

import java.util.List;

import com.bugbycode.module.user.ChildApiKeyConfig;

public interface ChildApiKeyConfigRepository {

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
	 * 根据自定义条件分页查询子账号配置
	 * @param username 母账号
	 * @param keyword 关键词
	 * @param skip
	 * @param limit
	 * @return
	 */
	public List<ChildApiKeyConfig> find(String username, String keyword, long skip, int limit);
	
	/**
	 * 根据自定义条件统计总记录数
	 * @param username 母账号
	 * @param keyword 关键词
	 * @return
	 */
	public long count(String username, String keyword);
}
