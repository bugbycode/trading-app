package com.bugbycode.module;

import org.springframework.data.annotation.Id;

import com.util.DateFormatUtil;

public class Klines {
	
	@Id
	private String id;//mongodb数据库唯一标识

	private String pair;//交易对
	
	private long startTime; //开盘时间
	
	private String openPrice;//开盘价
	
	private String highPrice;//最高价
	
	private String lowPrice;//最低价
	
	private String closePrice;//收盘价

	private String interval;//时间级别
	
	private double ema7;
	
	private double ema25;
	
	private double ema99;
	
	private long endTime;
	
	private int decimalNum = 2;

	public Klines(String pair,long startTime, String openPrice, String highPrice, String lowPrice, 
			String closePrice, long endTime,String interval,int decimalNum) {
		this.pair = pair;
		this.startTime = startTime;
		this.openPrice = openPrice;
		this.highPrice = highPrice;
		this.lowPrice = lowPrice;
		this.closePrice = closePrice;
		this.endTime = endTime;
		this.decimalNum = decimalNum;
		this.interval = interval;
	}
	
	public String getId(){
		return this.id;
	}

	public String getPair() {
		return pair;
	}

	public long getStartTime() {
		return startTime;
	}

	public String getOpenPrice() {
		return openPrice;
	}
	
	public double getOpenPriceDoubleValue() {
		return Double.valueOf(openPrice);
	}

	public String getHighPrice() {
		return highPrice;
	}

	public double getHighPriceDoubleValue() {
		return Double.valueOf(highPrice);
	}
	
	public String getLowPrice() {
		return lowPrice;
	}
	
	public double getLowPriceDoubleValue() {
		return Double.valueOf(lowPrice);
	}

	public String getClosePrice() {
		return closePrice;
	}
	
	public double getClosePriceDoubleValue() {
		return Double.valueOf(closePrice);
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
		Double c = Double.valueOf(closePrice);
		Double o = Double.valueOf(openPrice);
		return c >= o;
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

	public String getInterval(){
		return this.interval;
	}
	
	public Inerval getInervalType() {
		return Inerval.resolve(this.interval);
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
			leadlen = Double.valueOf(this.getHighPrice()) - Double.valueOf(this.getClosePrice());
			bodylen = Double.valueOf(this.getClosePrice()) - Double.valueOf(this.getOpenPrice());
		} else if(this.isFall()) {//阴线
			leadlen = Double.valueOf(this.getHighPrice()) - Double.valueOf(this.getOpenPrice());
			bodylen = Double.valueOf(this.getOpenPrice()) - Double.valueOf(this.getClosePrice());
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
			leadlen = Double.valueOf(this.getOpenPrice()) - Double.valueOf(this.getLowPrice());
			bodylen = Double.valueOf(this.getClosePrice()) - Double.valueOf(this.getOpenPrice());
		} else if(this.isFall()) {//阴线
			leadlen = Double.valueOf(this.getClosePrice()) - Double.valueOf(this.getLowPrice());
			bodylen = Double.valueOf(this.getOpenPrice()) - Double.valueOf(this.getClosePrice());
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
			return ((Double.valueOf(this.getHighPrice()) - Double.valueOf(this.getLowPrice())) / Double.valueOf(this.getHighPrice())) * 100;
		} else {
			return ((Double.valueOf(this.getHighPrice()) - Double.valueOf(this.getLowPrice())) / Double.valueOf(this.getLowPrice())) * 100;
		}
	}
	
	/**
     * 判断是否为同一根k线
     * @param klines0
     * @return
     */
    public boolean isEquals(Klines klines0) {
    	return klines0 != null && klines0.getStartTime() == this.getStartTime();
    }
	
    /**
     * 判断是否包含
     * @param klines0
     * @return
     */
    public boolean isContains(Klines klines0) {
    	return klines0 != null && klines0.getPair().equals(this.getPair())
    			&& this.getStartTime() <= klines0.getStartTime() && this.getEndTime() > klines0.getStartTime();
    }
    
    /**
     * 获取k线实体部分最高价
     * @return
     */
    public String getBodyHighPrice() {
    	String bodyHighPrice = null;
    	if(this.isRise()) {
    		bodyHighPrice = this.getClosePrice();
    	} else {
    		bodyHighPrice = this.getOpenPrice();
    	}
    	return bodyHighPrice;
    }
    
    /**
     * 获取k线实体部分最高价
     * @return
     */
    public double getBodyHighPriceDoubleValue() {
    	return Double.valueOf(getBodyHighPrice());
    }
    
    /**
     * 获取k线实体部分最低价
     * @return
     */
    public String getBodyLowPrice() {
    	String bodyLowPrice = null;
    	if(this.isRise()) {
    		bodyLowPrice = this.getOpenPrice();
    	} else {
    		bodyLowPrice = this.getClosePrice();
    	}
    	return bodyLowPrice;
    }
    
    /**
     * 获取k线实体部分最低价
     * @return
     */
    public double getBodyLowPriceDoubleValue() {
    	return Double.valueOf(getBodyLowPrice());
    }
	
	@Override
	public String toString() {
		return String.format("交易对：%s，开盘时间：%s，开盘价：%s，最高价：%s，最低价：%s，收盘价：%s，收盘时间：%s，时间级别：%s", 
				pair,DateFormatUtil.format(startTime),getOpenPrice(),getHighPrice()
				,getLowPrice(),getClosePrice(),DateFormatUtil.format(endTime),this.interval);
	}
}
