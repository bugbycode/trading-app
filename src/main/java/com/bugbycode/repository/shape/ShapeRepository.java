package com.bugbycode.repository.shape;

import java.util.List;

import com.bugbycode.module.ShapeInfo;

/**
 * 图纸管理CRUD
 */
public interface ShapeRepository {

	/**
	 * 根据创建者、交易对查询图纸信息
	 * @param owner 创建者
	 * @param symbol 交易对
	 * @return
	 */
	public List<ShapeInfo> query(String owner,String symbol);
	
	/**
	 * 根据创建者查询图纸信息
	 * @param owner 创建者
	 * @return
	 */
	public List<ShapeInfo> queryByOwner(String owner);
	
	/**
	 * 根据交易对查询图纸信息
	 * @param symbol 交易对
	 * @return
	 */
	public List<ShapeInfo> queryBySymbol(String symbol);
	
	/**
	 * 创建图纸
	 * @param info 图纸信息
	 * @return id 返回数据库唯一标识
	 */
	public String insert(ShapeInfo info);
	
	/**
	 * 根据ID删除图纸
	 * @param id 数据库唯一标识
	 */
	public void deleteById(String id);
	
	/**
	 * 根据ID查询一条图纸信息
	 * @param id 数据库唯一标识
	 * @return
	 */
	public ShapeInfo queryById(String id);
	
	/**
	 * 更新图纸信息
	 * @param info 图纸信息
	 */
	public void update(ShapeInfo info);
	
	/**
	 * 查询所有图纸信息
	 * @return
	 */
	public List<ShapeInfo> query();
	
	/**
	 * 修改持仓方向（水平射线使用）
	 * @param id
	 * @param longOrShortType
	 */
	public void updateLongOrShortTypeById(String id, int longOrShortType);
	
	/**
	 * 自定义条件分页查询图纸信息
	 * 
	 * @param owner 创建者
	 * @param symbol 交易对
	 * @param skip 起始记录数
	 * @param limit 每页条数
	 * @return
	 */
	public List<ShapeInfo> query(String owner,String symbol,long skip,int limit);
	
	/**
	 * 自定义条件统计总记录数
	 * 
	 * @param owner
	 * @param symbol
	 * @return
	 */
	public long count(String owner,String symbol);
	
}
