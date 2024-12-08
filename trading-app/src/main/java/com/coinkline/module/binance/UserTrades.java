package com.coinkline.module.binance;

import org.springframework.data.annotation.Id;

/**
 * 成交历史信息
 */
public class UserTrades {
	
	@Id
	private String id;
	
	private boolean buyer;
	
	private String commission;
	
	private String commissionAsset;
	
	private String tradeId;
	
	private String maker;
	
	private long orderId;
	
	private String price;
	
	private String qty;
	
	private String quoteQty;
	
	private String realizedPnl;
	
	private String side;
	
	private String positionSide;
	
	private String symbol;
	
	private long time;
	
	
}
