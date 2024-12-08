package com.coinkline.module.area;

import java.util.ArrayList;
import java.util.List;

import org.springframework.util.CollectionUtils;

import com.coinkline.module.Klines;
import com.coinkline.module.SortType;
import com.util.DateFormatUtil;
import com.util.KlinesComparator;
import com.util.PriceUtil;

/**
 * 盘整区实体信息类
 */
public class ConsolidationArea {
	
	//盘整区内所有k线信息
	private List<Klines> list;

	public ConsolidationArea() {
		
	}
	
	public boolean isEmpty() {
		return CollectionUtils.isEmpty(this.list);
	}
	
	/**
	 * 往盘整区中添加k线信息
	 * 
	 * @param klines
	 */
	public void addKlines(Klines klines) {
		if(contains(klines) && !PriceUtil.contains(list, klines)) {
			this.list.add(klines);
			this.sort();
		}
	}
	
	/**
	 * 判断k线是否属于当前盘整区
	 * @param klines
	 * @return
	 */
	public boolean contains(Klines klines) {
		return !isEmpty() &&  klines.getBodyHighPriceDoubleValue() <= getHighPriceDoubleValue()
				&& klines.getBodyLowPriceDoubleValue() >= getLowPriceDoubleValue();
	}
	
	/**
	 * 获取盘整区所有k线信息
	 * @return
	 */
	public List<Klines> getAllAreaKlines(){
		List<Klines> data = new ArrayList<Klines>();
		data.addAll(list);
		return data;
	}
	
	/**
	 * 初始化盘整区
	 */
	public void init(List<Klines> list) {
		if(isEmpty()) {
			this.list = list;
			this.sort();
		}
	}
	
	private void sort() {
		//按k线开盘时间正向排序
		KlinesComparator kc = new KlinesComparator(SortType.ASC);
		this.list.sort(kc);
	}
	
	/**
	 * 获取盘整区内最高价格k线信息
	 * @return
	 */
	public Klines getHighKlines() {
		if(CollectionUtils.isEmpty(list)) {
			throw new RuntimeException("未初始化盘整区k线信息");
		}
		return PriceUtil.getMaxPriceKLine(list);
	}
	
	/**
	 * 获取盘整区内最低价格k线信息
	 * @return
	 */
	public Klines getLowKlines() {
		if(CollectionUtils.isEmpty(list)) {
			throw new RuntimeException("未初始化盘整区k线信息");
		}
		return PriceUtil.getMinPriceKLine(list);
	}
	
	/**
	 * 最高价
	 * @return
	 */
	public String getHighPrice() {
		Klines high = getHighKlines();
		if(high == null) {
			throw new RuntimeException("无法获取最高价格k线信息");
		}
		return high.getHighPrice();
	}
	
	/**
	 * 最高价
	 * @return
	 */
	public double getHighPriceDoubleValue() {
		return Double.valueOf(getHighPrice());
	}
	
	/**
	 * 最低价
	 * @return
	 */
	public String getLowPrice() {
		Klines low = getLowKlines();
		if(low == null) {
			throw new RuntimeException("无法获取最高价格k线信息");
		}
		return low.getLowPrice();
	}
	
	/**
	 * 最低价
	 * @return
	 */
	public double getLowPriceDoubleValue() {
		return Double.valueOf(getLowPrice());
	}
	
	/**
	 * 最高价k线起始时间
	 * @return
	 */
	public long getHighKlinesStartTime() {
		return getHighKlines().getStartTime();
	}
	
	/**
	 * 最低价k线起始时间
	 * @return
	 */
	public long getLowKlinesStartTime() {
		return getLowKlines().getStartTime();
	}
	
	/**
	 * 盘整区起始k线开盘时间
	 * @return
	 */
	public long getStartKlinesStartTime() {
		if(CollectionUtils.isEmpty(list)) {
			throw new RuntimeException("未初始化盘整区k线信息");
		}
		return list.get(0).getStartTime();
	}
	
	/**
	 * 盘整区结束k线开盘时间
	 * @return
	 */
	public long getEndKlinesStartTime() {
		if(CollectionUtils.isEmpty(list)) {
			throw new RuntimeException("未初始化盘整区k线信息");
		}
		return list.get(list.size() - 1).getStartTime();
	}
	
	@Override
	public String toString() {
		return "交易对：" + getHighKlines().getPair() + "，最高价：" + getHighPrice() + "，最低价：" + getLowPrice() + 
				"，最高价K线开盘时间：" + DateFormatUtil.format(getHighKlinesStartTime()) + 
				"，最低价K线开盘时间：" + DateFormatUtil.format(getLowKlinesStartTime()) + 
				"，盘整区起始开盘时间：" + DateFormatUtil.format(getStartKlinesStartTime()) + 
				"，盘整区结束开盘时间：" + DateFormatUtil.format(getEndKlinesStartTime());
	}
}
