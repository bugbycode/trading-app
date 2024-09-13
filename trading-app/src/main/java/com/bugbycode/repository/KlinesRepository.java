package com.bugbycode.repository;

import java.util.List;

import com.bugbycode.module.Klines;

public interface KlinesRepository {

    /**
     * 添加一条K线信息
     * 
     * @param klines k线信息
     */
    public void insert(Klines klines);

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
}
