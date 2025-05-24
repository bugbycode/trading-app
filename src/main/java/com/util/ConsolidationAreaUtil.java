package com.util;

import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.util.CollectionUtils;

import com.bugbycode.module.Klines;
import com.bugbycode.module.ReversalPoint;
import com.bugbycode.module.SortType;

public class ConsolidationAreaUtil {

	private final Logger logger = LogManager.getLogger();
	
	private List<Klines> list;
	
	private ReversalPoint high;
	
	private ReversalPoint low;
	
	public ConsolidationAreaUtil(List<Klines> list) {
		this.list = new ArrayList<Klines>();
		if(!CollectionUtils.isEmpty(list)) {
			this.list.addAll(list);
			this.init();
		}
	}

	private void init() {
		if(CollectionUtils.isEmpty(list) || list.size() < 20) {
			return;
		}
		
		list.sort(new KlinesComparator(SortType.ASC));
		
		PriceUtil.calculateAllBBPercentB(list);
		
		List<ReversalPoint> points = new ArrayList<ReversalPoint>();
		
		for(int index = list.size() - 1; index > 1; index--) {
			Klines current = list.get(index);
			Klines parent = list.get(index - 1);
			if(!CollectionUtils.isEmpty(points)) {
				ReversalPoint point = points.get(0);
				if(verifyHigh(point) && current.getBbPercentB() < 0.5) {
					break;
				} else if(verifyLow(point) && current.getBbPercentB() > 0.5) {
					break;
				}
			}
			if(verifyHigh(current, parent) || verifyLow(current, parent)) {
				ReversalPoint point = new ReversalPoint(current, parent);
				points.add(point);
			}
		}
		
		logger.debug(points);
		
		if(CollectionUtils.isEmpty(points)) {
			return;
		}
		
		low = PriceUtil.getMinReversalPoint(points);
		high = PriceUtil.getMaxReversalPoint(points);
		
		logger.debug(low.getCurrent());
		logger.debug(low.getParent());
	}
	
	public boolean isEmpty() {
		return low == null || high == null;
	}
	
	private boolean verifyHigh(Klines current, Klines parent) {
		return parent.isRise() && current.isFall() && parent.getBbPercentB() >= 0.95;
	}
	
	private boolean verifyLow(Klines current, Klines parent) {
		return parent.isFall() && current.isRise() && parent.getBbPercentB() <= 0.05;
	}
	
	private boolean verifyHigh(ReversalPoint point) {
		return verifyHigh(point.getCurrent(), point.getParent());
	}
	
	private boolean verifyLow(ReversalPoint point) {
		return verifyLow(point.getCurrent(), point.getParent());
	}

	public ReversalPoint getHigh() {
		return high;
	}

	public ReversalPoint getLow() {
		return low;
	}
	
	public double getHighPrice() {
		return getHigh().getMaxPrice();
	}
	
	public double getLowPrice() {
		return getLow().getMinPrice();
	}

	@Override
	public String toString() {
		if(high == null || low == null) {
			return "";
		}
		return String.format("%s ~ %s", getLow().getMinKlines().getLowPrice(), getHigh().getMaxKlines().getHighPrice());
	}
}
