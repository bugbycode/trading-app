package com.bugbycode.repository.klines;

import java.util.List;

import com.bugbycode.module.Inerval;
import com.bugbycode.module.Klines;

public interface KlinesRepository {

    /**
     * 添加一条K线信息
     * 
     * @param klines k线信息
     */
    public void insert(Klines klines);

    /**
     * 批量添加k线信息
     * @param list
     */
    public void insert(List<Klines> list);

    /**
     * 根据交易对名称查询所有k线信息
     * @param pair 交易对
     * @param interval 时间级别
     * @return
     */
    public List<Klines> findByPair(String pair,String interval);

    /**
     * 根据开盘时间和交易对查询1条k线信息
     * @param startTime 开盘时间
     * @param pair 交易对
     * @param interval 时间级别
     * @return
     */
    public Klines findOneByStartTime(long startTime,String pair,String interval);

    /**
     * 删除一条k线信息
     * @param startTime 开盘时间
     * @param pair 交易对
     * @param interval 时间级别
     */
    public void remove(long startTime,String pair,String interval);

    /**
     * 根据交易对和起始时间查询开盘时间大于等于起始时间的所有k线信息
     * @param pair 交易对
     * @param startTime 起始时间
     * @param interval 时间级别
     * @return
     */
    public List<Klines> findByPairAndGtStartTime(String pair,long startTime,String interval);

    /**
     * 根据ID删除一条k线信息
     * @param _id
     */
    public void remove(String _id);

    /**
     * 根据ID查询一条K线信息
     * @param _id
     * @return
     */
    public Klines findById(String _id);
    
    /**
     * 根据交易对名称分页查询所有k线信息
     * @param pair 交易对
     * @param interval 时间级别
	 * @param skip 起始记录
	 * @param limit 查询条数
     * @return
     */
    public List<Klines> findByPair(String pair,String interval,long skip,int limit);
    
    /**
     * 统计总条数
     * @param pair 交易对
     * @param interval 时间级别
     * @return
     */
    public long count(String pair,String interval);
    
    /**
     * 查询最新的一部分k线信息
     * @param pair 交易对
     * @param interval 时间级别
     * @param limit 查询条数
     * @return
     */
    public List<Klines> findLastKlinesByPair(String pair,String interval,int limit);
    
    /**
     * 根据时间段查询k线信息(根据开盘时间匹配)
     * @param pair 交易对
     * @param inerval 时间级别
     * @param startTime 起始时间
     * @param endTime 结束时间
     * @return
     */
    public List<Klines> findByTimeLimit(String pair,Inerval inerval,long startTime, long endTime);
}
