package com.bugbycode.factory.fenceSitter.impl;

import java.util.ArrayList;
import java.util.List;

import org.springframework.util.CollectionUtils;

import com.bugbycode.factory.fenceSitter.FenceSitterFactory;
import com.bugbycode.module.FibCode;
import com.bugbycode.module.FibInfo;
import com.bugbycode.module.Klines;
import com.bugbycode.module.MarketSentiment;
import com.bugbycode.module.QuotationMode;
import com.bugbycode.module.SortType;
import com.bugbycode.module.binance.AutoTrade;
import com.bugbycode.module.binance.AutoTradeType;
import com.bugbycode.module.price.OpenPrice;
import com.bugbycode.module.price.impl.OpenPriceDetails;
import com.bugbycode.module.trading.PositionSide;
import com.util.KlinesComparator;
import com.util.PriceUtil;

public class FenceSitterFactoryImpl implements FenceSitterFactory{

	private OpenPrice openPrice;
	
	private List<Klines> list;
	
	private List<Klines> list_15m;
	
	private PositionSide ps = PositionSide.DEFAULT;
	
	public FenceSitterFactoryImpl(List<Klines> list, List<Klines> list_15m, QuotationMode mode) {
		this.list = new ArrayList<Klines>();
		this.list_15m = new ArrayList<Klines>();
		if(!CollectionUtils.isEmpty(list)) {
			this.list.addAll(list);
		}
		if(!CollectionUtils.isEmpty(list_15m)) {
			this.list_15m.addAll(list_15m);
		}
		this.init(mode);
	}
	
	private void init(QuotationMode mode) {
		if(CollectionUtils.isEmpty(list) || CollectionUtils.isEmpty(list_15m)) {
			return;
		}
		
		KlinesComparator kc = new KlinesComparator(SortType.ASC);
		this.list.sort(kc);
		this.list_15m.sort(kc);
		
		Klines start = null;
		Klines end = null;
		for(int index = list.size() - 1; index > 0; index--) {
			Klines current = list.get(index);
			Klines parent = list.get(index - 1);
			if(start == null) {
				if((mode == QuotationMode.LONG && current.isFall())
						|| (mode == QuotationMode.SHORT && current.isRise())) {
					start = current;
				}
			}
			
			if(start == null) {
				continue;
			}
			
			if((mode == QuotationMode.LONG && parent.isRise())
					|| (mode == QuotationMode.SHORT && parent.isFall())) {
				end = current;
				break;
			}
			
		}
		
		if(start == null || end == null) {
			return;
		}
		
		List<Klines> data = PriceUtil.subList(end, start, list);
		MarketSentiment ms = new MarketSentiment(data);
		if(ms.isEmpty()) {
			return;
		}
		
		this.ps = mode == QuotationMode.LONG ? PositionSide.LONG : PositionSide.SHORT;
		
		Klines last_15m = PriceUtil.getLastKlines(list_15m);
		double last_15m_close = last_15m.getClosePriceDoubleValue();
		double openPriceValue = start.getClosePriceDoubleValue();
		double takeProfitValue = this.ps == PositionSide.LONG ? ms.getHighPrice() : ms.getLowPrice();
		
		FibInfo stopLossFibInfo = new FibInfo(openPriceValue, takeProfitValue, last_15m.getDecimalNum());
		double stopLossLimit = stopLossFibInfo.getFibValue(FibCode.FIB1_272);
		
		this.openPrice = new OpenPriceDetails(FibCode.FIB1, openPriceValue, stopLossLimit, takeProfitValue, takeProfitValue, AutoTradeType.FENCE_SITTER);
		
		if((isLong() && last_15m_close <= openPriceValue)
				|| (isShort() && last_15m_close >= openPriceValue)) {
			this.openPrice.setAutoTrade(AutoTrade.CLOSE);
		}
		
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

	@Override
	public boolean isClosePosition() {
		return false;
	}
}
