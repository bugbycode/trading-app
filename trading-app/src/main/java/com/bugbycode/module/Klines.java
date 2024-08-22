package com.bugbycode.module;

import com.util.DateFormatUtil;
import com.util.PriceUtil;

public class Klines {
	
	private String pair;//交易对
	
	private long starTime; //开盘时间
	
	private double openPrice;//开盘价
	
	private double highPrice;//最高价
	
	private double lowPrice;//最低价
	
	private double closePrice;//收盘价
	
	private double ema7;
	
	private double ema25;
	
	private double ema99;
	
	private long endTime;
	
	private int decimalNum = 2;

	public Klines(String pair,long starTime, double openPrice, double highPrice, double lowPrice, 
			double closePrice, long endTime,int decimalNum) {
		this.pair = pair;
		this.starTime = starTime;
		this.openPrice = openPrice;
		this.highPrice = highPrice;
		this.lowPrice = lowPrice;
		this.closePrice = closePrice;
		this.endTime = endTime;
		this.decimalNum = decimalNum;
	}
	
	public String getPair() {
		return pair;
	}

	public long getStarTime() {
		return starTime;
	}

	public double getOpenPrice() {
		return openPrice;
	}

	public double getHighPrice() {
		return highPrice;
	}

	public double getLowPrice() {
		return lowPrice;
	}

	public double getClosePrice() {
		return closePrice;
	}

	public long getEndTime() {
		return endTime;
	}

	public int getDecimalNum() {
		return decimalNum;
	}

	/**
	 * 阳线
	 * @return
	 */
	public boolean isRise() {
		return closePrice >= openPrice;
	}
	
	/**
	 * 阴线
	 * @return
	 */
	public boolean isFall() {
		return !isRise();
	}
	
	public double getEma7() {
		return ema7;
	}

	public void setEma7(double ema7) {
		this.ema7 = ema7;
	}

	public double getEma25() {
		return ema25;
	}

	public void setEma25(double ema25) {
		this.ema25 = ema25;
	}

	public double getEma99() {
		return ema99;
	}

	public void setEma99(double ema99) {
		this.ema99 = ema99;
	}

	/**
	 * 是否出现上引线
	 * 
	 * @return
	 */
	public boolean isUplead() {
		double leadlen = 0;//上引线长度
		double bodylen = 0;//主体长度
		if(this.isRise()) {//阳线
			leadlen = this.getHighPrice() - this.getClosePrice();
			bodylen = this.getClosePrice() - this.getOpenPrice();
		} else if(this.isFall()) {//阴线
			leadlen = this.getHighPrice() - this.getOpenPrice();
			bodylen = this.getOpenPrice() - this.getClosePrice();
		}
		return (bodylen == 0 && leadlen > 0) || (bodylen > 0 && leadlen / bodylen > 0.85) ;
	}
	
	/**
	 * 是否出现下引线
	 * 
	 * @return
	 */
	public boolean isDownlead() {
		double leadlen = 0;//下引线长度
		double bodylen = 0;//主体长度
		if(this.isRise()) {//阳线
			leadlen = this.getOpenPrice() - this.getLowPrice();
			bodylen = this.getClosePrice() - this.getOpenPrice();
		} else if(this.isFall()) {//阴线
			leadlen = this.getClosePrice() - this.getLowPrice();
			bodylen = this.getOpenPrice() - this.getClosePrice();
		}
		return (bodylen == 0 && leadlen > 0) || (bodylen > 0 && leadlen / bodylen > 0.85) ;
	}

	/**
	 * 价格波动幅度百分比
	 * 
	 * @return
	 */
	public double getPriceFluctuationPercentage() {
		if(this.isFall()) {
			return ((this.getHighPrice() - this.getLowPrice()) / this.getHighPrice()) * 100;
		} else {
			return ((this.getHighPrice() - this.getLowPrice()) / this.getLowPrice()) * 100;
		}
	}
	
	@Override
	public String toString() {
		return String.format("交易对：%s，开盘时间：%s，开盘价：%s，最高价：%s，最低价：%s，收盘价：%s，收盘时间：%s，价格波动幅度：%s%%", 
				pair,DateFormatUtil.format(starTime),getOpenPrice(),getHighPrice()
				,getLowPrice(),getClosePrice(),DateFormatUtil.format(endTime),PriceUtil.formatDoubleDecimal(getPriceFluctuationPercentage(), 2));
	}
}
