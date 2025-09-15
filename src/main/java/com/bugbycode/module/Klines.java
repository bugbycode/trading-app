package com.bugbycode.module;

import org.springframework.data.annotation.Id;

import com.util.DateFormatUtil;
import com.util.StringUtil;

public class Klines {
	
	@Id
	private String id;//mongodb数据库唯一标识

	private String pair;//交易对
	
	private long startTime; //开盘时间
	
	private String openPrice;//开盘价
	
	private String highPrice;//最高价
	
	private String lowPrice;//最低价
	
	private String closePrice;//收盘价
	
	private long endTime;//收盘时间

	private String interval;//时间级别
	
	private double ema7;
	
	private double ema25;
	
	private double ema99;
	
	private double dif;
	
    private double dea;
    
    private double macd;
	
	private int decimalNum = 2;
	
	private String v;//成交量
	
	private Long n;//成交笔数
	
	private String q;//成交额
	
	private String iv;//主动买入的成交量
	
	private String iq;//主动买入的成交额
	
	private Double bbPercentB = 0.5;//BB %B
	
	private double delta;  // 当前K线的delta
	
    private double cvd;    // 当前K线的累计CVD

	public Klines(String pair,long startTime, String openPrice, String highPrice, String lowPrice, 
			String closePrice, long endTime,String interval,int decimalNum, String v, Long n, String q,
			String iv, String iq) {
		this.pair = pair;
		this.startTime = startTime;
		this.openPrice = openPrice;
		this.highPrice = highPrice;
		this.lowPrice = lowPrice;
		this.closePrice = closePrice;
		this.endTime = endTime;
		this.decimalNum = decimalNum;
		this.interval = interval;
		this.v = v;
		this.n = n;
		this.q = q;
		this.iv = iv;
		this.iq = iq;
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

	public double getDif() {
		return dif;
	}

	public void setDif(double dif) {
		this.dif = dif;
	}

	public double getDea() {
		return dea;
	}

	public void setDea(double dea) {
		this.dea = dea;
	}

	public double getMacd() {
		return macd;
	}

	public void setMacd(double macd) {
		this.macd = macd;
	}

	public String getInterval(){
		return this.interval;
	}
	
	public Inerval getInervalType() {
		return Inerval.resolve(this.interval);
	}
	
	public double getVDoubleValue() {
		return StringUtil.isEmpty(v) ? 0 : Double.valueOf(v);
	}

	public String getV() {
		return v;
	}

	public void setV(String v) {
		this.v = v;
	}

	public Long getN() {
		return n == null ? 0 : n;
	}

	public void setN(Long n) {
		this.n = n;
	}
	
	public double getQDoubleValue() {
		return StringUtil.isEmpty(q) ? 0 : Double.valueOf(q);
	}

	public String getQ() {
		return q;
	}

	public void setQ(String q) {
		this.q = q;
	}

	public double getIvDoubleValue() {
		return StringUtil.isEmpty(iv) ? 0 : Double.valueOf(iv);
	}
	
	public String getIv() {
		return iv;
	}

	public void setIv(String iv) {
		this.iv = iv;
	}
	
	public double getIqDoubleValue() {
		return StringUtil.isEmpty(iq) ? 0 : Double.valueOf(iq);
	}

	public String getIq() {
		return iq;
	}

	public void setIq(String iq) {
		this.iq = iq;
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
     * 大于 当前开盘时间比klines0的开盘时间新
     * @param klines0
     * @return
     */
    public boolean gt(Klines klines0) {
    	return klines0 != null && this.getStartTime() > klines0.getStartTime();
    }
    
    /**
     * 大于等于 当前开盘时间比klines0的开盘时间新
     * @param klines0
     * @return
     */
    public boolean gte(Klines klines0) {
    	return klines0 != null && this.getStartTime() >= klines0.getStartTime();
    }
    
    /**
     * 小于 当前开盘时间比klines0的开盘时间旧
     * @param klines0
     * @return
     */
    public boolean lt(Klines klines0) {
    	return klines0 != null && this.getStartTime() < klines0.getStartTime();
    }
    
    /**
     * 小于等于 当前开盘时间比klines0的开盘时间旧
     * @param klines0
     * @return
     */
    public boolean lte(Klines klines0) {
    	return klines0 != null && this.getStartTime() <= klines0.getStartTime();
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
    
    public double getDelta() {
		return delta;
	}

	public void setDelta(double delta) {
		this.delta = delta;
	}

	public double getCvd() {
		return cvd;
	}

	public void setCvd(double cvd) {
		this.cvd = cvd;
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
    
    public Double getBbPercentB() {
		return bbPercentB == null ? 0.5 : bbPercentB;
	}

	public void setBbPercentB(Double bbPercentB) {
		this.bbPercentB = bbPercentB;
	}

	/**
     * 校验时间级别
     * @param inerval
     * @return
     */
    public boolean verifyInterval(Inerval inerval) {
    	return inerval.getDescption().equals(this.getInterval());
    }
	
	@Override
	public String toString() {
		return String.format("交易对：%s，开盘时间：%s，开盘价：%s，最高价：%s，最低价：%s，收盘价：%s，收盘时间：%s，时间级别：%s，成交量：%s，成交额：%s，成交笔数：%s，主动买入成交量：%s，主动买入成交额：%s，delta：%s，CVD：%s", 
				pair,DateFormatUtil.format(startTime),getOpenPrice(),getHighPrice()
				,getLowPrice(),getClosePrice(),DateFormatUtil.format(endTime),this.interval, this.v, this.q, this.n, this.iv, this.iq, this.delta, this.cvd);
	}
}
