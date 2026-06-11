package com.bugbycode.factory.fenceSitter.impl;

import java.util.ArrayList;
import java.util.List;

import org.springframework.util.CollectionUtils;

import com.bugbycode.factory.fenceSitter.FenceSitterFactory;
import com.bugbycode.module.FibCode;
import com.bugbycode.module.FibInfo;
import com.bugbycode.module.Klines;
import com.bugbycode.module.SortType;
import com.bugbycode.module.binance.AutoTradeType;
import com.bugbycode.module.price.OpenPrice;
import com.bugbycode.module.price.impl.OpenPriceDetails;
import com.bugbycode.module.trading.PositionSide;
import com.util.KlinesComparator;
import com.util.PriceUtil;

/**
 * 高频交易
 */
public class FenceSitterFactoryImpl_v2 implements FenceSitterFactory{

	private OpenPrice openPrice;
	
	private List<Klines> list;
	
	private List<Klines> list_15m;
	
	private PositionSide ps = PositionSide.DEFAULT;
	
	public FenceSitterFactoryImpl_v2(List<Klines> list, List<Klines> list_15m) {
		this.list = new ArrayList<Klines>();
		this.list_15m = new ArrayList<Klines>();
		if(!CollectionUtils.isEmpty(list_15m)) {
			this.list_15m.addAll(list_15m);
		}
		if(!CollectionUtils.isEmpty(list)) {
			this.list.addAll(list);
		}
		this.init();
	}
	
	private void init() {
		if(CollectionUtils.isEmpty(list) || list.size() < 50 || CollectionUtils.isEmpty(list_15m)) {
			return;
		}
		
		KlinesComparator kc = new KlinesComparator(SortType.ASC);
		this.list.sort(kc);
		
		PriceUtil.calculateBollingerBands(list);
		
		Klines last = PriceUtil.getLastKlines(list);
		Klines last_15m = PriceUtil.getLastKlines(list_15m);
		
		double last_15m_close = last_15m.getClosePriceDoubleValue();
		int decimalNum = last.getDecimalNum();
		double middleBand = PriceUtil.formatDoubleDecimalValue(last.getMiddleBand(), decimalNum);
		double upperBand = PriceUtil.formatDoubleDecimalValue(last.getUpperBand(), decimalNum);
		double lowerBand = PriceUtil.formatDoubleDecimalValue(last.getLowerBand(), decimalNum);
		
		if(last_15m_close < middleBand) {
			ps = PositionSide.SHORT;
		} else if(last_15m_close > middleBand) {
			ps = PositionSide.LONG;
		}
		
		if(ps == PositionSide.DEFAULT) {
			return;
		}
		
		double takeProfitValue = ps == PositionSide.LONG ? upperBand : lowerBand;
		FibInfo stopLossFibInfo = new FibInfo(middleBand, takeProfitValue, decimalNum);
		double stopLossLimit = stopLossFibInfo.getFibValue(FibCode.FIB1_272);
		
		this.openPrice = new OpenPriceDetails(FibCode.FIB1, middleBand, stopLossLimit, takeProfitValue, takeProfitValue, AutoTradeType.FENCE_SITTER);
	}

	@Override
	public OpenPrice getOpenPrice() {
		return this.openPrice;
	}

	@Override
	public boolean isLong() {
		return this.ps == PositionSide.LONG && this.openPrice != null;
	}

	@Override
	public boolean isShort() {
		return this.ps == PositionSide.SHORT && this.openPrice != null;
	}

}
