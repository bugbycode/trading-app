package com.bugbycode.repository.openInterest;

import java.util.List;

import com.bugbycode.module.open_interest.OpenInterestHist;

/**
 * 历史合约持仓量管理接口
 */
public interface OpenInterestHistRepository {

	/**
	 * 查询所有交易对合约持仓量信息
	 * @return
	 */
	public List<OpenInterestHist> query();
	
	/**
	 * 根据交易对查询合约持仓量信息
	 * @param pair
	 * @return
	 */
	public OpenInterestHist findOneByPair(String pair); 
	
	/**
	 * 添加一条合约持仓量信息
	 * @param oih
	 */
	public void save(OpenInterestHist oih);
	
	/**
	 * 根据ID删除合约持仓量信息
	 * @param id
	 */
	public void remove(String id);
}
