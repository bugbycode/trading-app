package com.bugbycode.repository.email;

import java.util.List;

import com.bugbycode.module.EmailAuth;

/**
 * 邮箱配置管理
 */
public interface EmailRepository {

	/**
	 * 获取所有邮箱配置信息
	 * @return
	 */
	public List<EmailAuth> query();
	
	/**
	 * 添加一条记录
	 * @param auth
	 * @return
	 */
	public EmailAuth insert(EmailAuth auth);
	
	/**
	 * 修改一条记录
	 * @param auth
	 */
	public void update(EmailAuth auth);
	
	/**
	 * 根据ID删除一条记录
	 * @param id
	 */
	public void deleteById(String id);
}
