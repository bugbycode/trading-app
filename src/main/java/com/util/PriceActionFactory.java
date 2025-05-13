package com.util;

import java.util.ArrayList;
import java.util.List;

import org.springframework.util.CollectionUtils;

import com.bugbycode.module.FibInfo;
import com.bugbycode.module.FibLevel;
import com.bugbycode.module.Klines;
import com.bugbycode.module.QuotationMode;
import com.bugbycode.module.SortType;
import com.bugbycode.module.trading.PositionSide;

/**
 * 价格行为指标
 */
public class PriceActionFactory {

	private List<Klines> list;
	
	private List<Double> takeProfits;//止盈点
	
	private List<Double> openPrices;//开仓点
	
	private FibInfo fibInfo;
	
	//行情走势
	private PositionSide ps = PositionSide.DEFAULT;
	
	public PriceActionFactory(List<Klines> list) {
		this.takeProfits = new ArrayList<Double>();
		this.openPrices = new ArrayList<Double>();
		this.list = new ArrayList<Klines>();
		if(!CollectionUtils.isEmpty(list)) {
			this.list.addAll(list);
			this.init();
		}
	}
	
	private void init() {
		if(CollectionUtils.isEmpty(list) || list.size() < 2) {
			return;
		}
		
		KlinesComparator kc = new KlinesComparator(SortType.ASC);
		this.list.sort(kc);
		
		if(PriceUtil.isRise_v3(list)) {
			ps = PositionSide.LONG;
		} else if(PriceUtil.isFall_v3(list)) {
			ps = PositionSide.SHORT;
		}
		
		if(ps == PositionSide.DEFAULT) {
			return;
		}
		
		int index = list.size() - 1;
		Klines current = list.get(index);
		Klines parent = list.get(index - 1);
		//设置开仓点和止盈点
		if(ps == PositionSide.LONG) {
			//多头开仓点
			if(current.isRise()) {
				addOpenPrice(current.getBodyHighPriceDoubleValue());
			}
			addOpenPrice(current.getBodyLowPriceDoubleValue());
			addOpenPrice(current.getLowPriceDoubleValue());
			if(parent.isFall() && parent.getBodyHighPriceDoubleValue() < current.getBodyHighPriceDoubleValue()) {
				addOpenPrice(parent.getBodyHighPriceDoubleValue());
			}
			openPrices.sort(new PriceComparator(SortType.DESC));
			//多头止盈点
			addTakeProfits(current.getBodyHighPriceDoubleValue());
			addTakeProfits(current.getHighPriceDoubleValue());
			
			takeProfits.sort(new PriceComparator(SortType.ASC));
			
			fibInfo = new FibInfo(current.getLowPriceDoubleValue(), current.getHighPriceDoubleValue(), current.getDecimalNum(), FibLevel.LEVEL_1);
			fibInfo.setPaf(this);
		} else if(ps == PositionSide.SHORT) {
			//空头开仓点
			if(current.isFall()) {
				addOpenPrice(current.getBodyLowPriceDoubleValue());
			}
			addOpenPrice(current.getBodyHighPriceDoubleValue());
			addOpenPrice(current.getHighPriceDoubleValue());
			
			if(parent.isRise() && parent.getBodyLowPriceDoubleValue() > current.getBodyLowPriceDoubleValue()) {
				addOpenPrice(parent.getBodyLowPriceDoubleValue());
			}
			
			openPrices.sort(new PriceComparator(SortType.ASC));
			
			//空头止盈点
			addTakeProfits(current.getBodyLowPriceDoubleValue());
			addTakeProfits(current.getLowPriceDoubleValue());
			
			takeProfits.sort(new PriceComparator(SortType.DESC));
			
			fibInfo = new FibInfo(current.getHighPriceDoubleValue(), current.getLowPriceDoubleValue(), current.getDecimalNum(), FibLevel.LEVEL_1);
			fibInfo.setPaf(this);
		}
	}
	
	public List<Double> getTakeProfits() {
		return takeProfits;
	}

	public List<Double> getOpenPrices() {
		return openPrices;
	}
	
	public PositionSide getPositionSide() {
		return ps;
	}

	private void addOpenPrice(double price) {
		if(!PriceUtil.contains(openPrices, price)) {
			openPrices.add(price);
		}
	}
	
	private void addTakeProfits(double price) {
		//if(!PriceUtil.contains(takeProfits, price)) {
			takeProfits.add(price);
		//}
	}
	
	/**
	 * 获取止盈点位
	 * @param price 当前价格
	 * @param profit 用户盈利预期
	 * @param profitLimit 用户止盈百分比限制
	 * @return
	 */
	public double getTakeProfit(double price, double profit, double profitLimit) {
		double result = 0;
		
		QuotationMode qm = fibInfo.getQuotationMode();
		
		double percent_first = PriceUtil.getPercent(price, takeProfits.get(0), qm);
		double percent_second = PriceUtil.getPercent(price, takeProfits.get(1), qm);
		
		if(percent_first >= profit && percent_first <= profitLimit) {
			result = takeProfits.get(0);
		} else if(percent_second >= profit && percent_second <= profitLimit) {
			result = takeProfits.get(1);
		}
		
		if(result == 0) {
			if(percent_first >= profit) {
				result = takeProfits.get(0);
			} else if(percent_second >= profit) {
				result = takeProfits.get(1);
			} else {
				result = takeProfits.get(1);
			}
		}
		
		return result;
	}
	
	public double getTakeProfit() {
		return this.takeProfits.get(1);
	}
	
	public FibInfo getFibInfo() {
		return this.fibInfo;
	}
}
