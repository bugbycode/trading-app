package com.bugbycode.factory.fenceSitter.impl;

import java.util.ArrayList;
import java.util.List;

import org.springframework.util.CollectionUtils;

import com.bugbycode.factory.fenceSitter.FenceSitterFactory;
import com.bugbycode.module.FibCode;
import com.bugbycode.module.Klines;
import com.bugbycode.module.SortType;
import com.bugbycode.module.binance.AutoTrade;
import com.bugbycode.module.binance.AutoTradeType;
import com.bugbycode.module.price.OpenPrice;
import com.bugbycode.module.price.impl.OpenPriceDetails;
import com.bugbycode.module.trading.PositionSide;
import com.util.KDJIndicatorUtil;
import com.util.KlinesComparator;
import com.util.PriceUtil;

public class FenceSitterFactoryImpl implements FenceSitterFactory{

	private List<Klines> list;
	
	private List<Klines> list_15m;//十五分钟级别k线 用于补充回撤之后的k线信息
	
	private OpenPrice openPrice;
	
	private AutoTrade autoTrade = AutoTrade.OPEN;
	
	private PositionSide ps = PositionSide.DEFAULT;
	
	public FenceSitterFactoryImpl(List<Klines> list, List<Klines> list_15m) {
		this.list = new ArrayList<Klines>();
		this.list_15m = new ArrayList<Klines>();
		if(!CollectionUtils.isEmpty(list_15m)) {
			this.list_15m.addAll(list_15m);
		}
		if(!CollectionUtils.isEmpty(list)) {
			this.list.addAll(list);
			this.init();
		}
	}
	
	private void init() {
		if(list.size() < 99 || CollectionUtils.isEmpty(list_15m)) {
			return;
		}
		
		KlinesComparator kc = new KlinesComparator(SortType.ASC);
		this.list.sort(kc);
		this.list_15m.sort(kc);
		
		KDJIndicatorUtil.calculate(list);
		
		this.ps = getPositionSide();
		
		for(int index = list.size() - 1; index > 0; index--) {
			Klines current = list.get(index);
			Klines parent = list.get(index - 1);
			if((ps == PositionSide.LONG && parent.getK() <= parent.getD())
					|| (ps == PositionSide.SHORT && parent.getK() >= parent.getD())) {
				
				double cutLoss = 10;
				double openPriceValue = current.getClosePriceDoubleValue();
				double stopLossLimit = ps == PositionSide.LONG ? 
						PriceUtil.rectificationCutLossLongPrice_v3(openPriceValue, cutLoss) : PriceUtil.rectificationCutLossShortPrice_v3(openPriceValue, cutLoss);
				stopLossLimit = PriceUtil.formatDoubleDecimalValue(stopLossLimit, current.getDecimalNum());
				
				addPrices(new OpenPriceDetails(FibCode.FIB1, openPriceValue, stopLossLimit, AutoTradeType.FENCE_SITTER));
				break;
			}
		}
	}
	
	private PositionSide getPositionSide() {
		PositionSide ps = PositionSide.DEFAULT;
		Klines last = PriceUtil.getLastKlines(list);
		
		if(verifyShort(last)) {
			ps = PositionSide.SHORT;
		} else if(verifyLong(last)) {
			ps = PositionSide.LONG;
		}
		
		return ps;
	}
	
	private boolean verifyLong(Klines k) {
		return k.getK() > k.getD();
	}
	
	private boolean verifyShort(Klines k) {
		return k.getK() < k.getD();
	}

	@Override
	public OpenPrice getOpenPrice() {
		return this.openPrice;
	}

	@Override
	public boolean isLong() {
		return this.ps == PositionSide.LONG;
	}
	
	@Override
	public boolean isShort() {
		return this.ps == PositionSide.SHORT;
	}

	@Override
	public boolean isClosePosition() {
		return false;
	}
	
	private void addPrices(OpenPrice price) {
		price.setResetStopLoss(false);
		price.setAutoTrade(autoTrade);
		this.openPrice = price;
	}
	
}
