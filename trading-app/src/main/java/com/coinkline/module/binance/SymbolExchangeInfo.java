package com.coinkline.module.binance;

import java.math.BigDecimal;

import org.springframework.data.annotation.Id;

import com.util.PriceUtil;

/**
 * 交易对交易规则
 */
public class SymbolExchangeInfo {

	@Id
	private String id;
	
	private String symbol;
	
	private double lot_stepSize;// 市价订单最小数量间隔
	
	private double lot_minQty;//数量下限, 最小数量
	
	private double lot_maxQty;//数量上限, 最大数量
	
	private double lot_market_stepSize;// 市价订单最小价格间隔
	
	private double lot_market_minQty;//数量下限, 最小数量
	
	private double lot_market_maxQty;//数量上限, 最大数量
	
	private double min_notional;//最小名义价值

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getSymbol() {
		return symbol;
	}

	public void setSymbol(String symbol) {
		this.symbol = symbol;
	}

	public double getLot_stepSize() {
		return lot_stepSize;
	}

	public void setLot_stepSize(double lot_stepSize) {
		this.lot_stepSize = lot_stepSize;
	}

	public double getLot_minQty() {
		return lot_minQty;
	}

	public void setLot_minQty(double lot_minQty) {
		this.lot_minQty = lot_minQty;
	}

	public double getLot_maxQty() {
		return lot_maxQty;
	}

	public void setLot_maxQty(double lot_maxQty) {
		this.lot_maxQty = lot_maxQty;
	}

	public double getLot_market_stepSize() {
		return lot_market_stepSize;
	}

	public void setLot_market_stepSize(double lot_market_stepSize) {
		this.lot_market_stepSize = lot_market_stepSize;
	}

	public double getLot_market_minQty() {
		return lot_market_minQty;
	}

	public void setLot_market_minQty(double lot_market_minQty) {
		this.lot_market_minQty = lot_market_minQty;
	}

	public double getLot_market_maxQty() {
		return lot_market_maxQty;
	}

	public void setLot_market_maxQty(double lot_market_maxQty) {
		this.lot_market_maxQty = lot_market_maxQty;
	}

	public double getMin_notional() {
		return min_notional;
	}

	public void setMin_notional(double min_notional) {
		this.min_notional = min_notional;
	}
	
	/**
	 * 限价单最小下单数量
	 * @param currentPrice 当前价格
	 * @return
	 */
	public double getMinQuantity(double currentPrice) {
		double baseQuantity = this.min_notional / currentPrice;
		
		double remainder = baseQuantity % this.lot_stepSize;
		
		double quantity = baseQuantity - remainder;
		
		if(remainder > 0) {
			quantity += this.lot_stepSize;
		}
		
		return quantity;
	}
	
	/**
	 * 市价单最小下单数量
	 * @param currentPrice 当前价格
	 * @return
	 */
	public String getMarketMinQuantity(double currentPrice) {
		double baseQuantity = this.min_notional / currentPrice;
		
		double remainder = baseQuantity % this.lot_market_stepSize;
		
		double quantity = baseQuantity - remainder;
		
		if(remainder > 0) {
			quantity += this.lot_market_stepSize;
		}
		int decimalPoint = new BigDecimal(String.valueOf(Double.valueOf(this.lot_market_stepSize))).scale();
		
		return PriceUtil.formatDoubleDecimal(quantity, decimalPoint);
	}
}
