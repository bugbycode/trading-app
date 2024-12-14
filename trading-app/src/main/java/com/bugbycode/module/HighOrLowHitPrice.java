package com.bugbycode.module;

import org.springframework.data.annotation.Id;

import com.util.DateFormatUtil;

/**
 * 高低点价格命中纪录
 */
public class HighOrLowHitPrice {

    @Id
    private String id;

    //交易对
    private String pair;

    //命中价格
    private double price;

    //发生的时间
    private long createTime;
    
	public HighOrLowHitPrice() {
		
	}

	public HighOrLowHitPrice(String pair, double price, long createTime) {
		this.pair = pair;
		this.price = price;
		this.createTime = createTime;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getPair() {
		return pair;
	}

	public void setPair(String pair) {
		this.pair = pair;
	}

	public double getPrice() {
		return price;
	}

	public void setPrice(double price) {
		this.price = price;
	}

	public long getCreateTime() {
		return createTime;
	}

	public void setCreateTime(long createTime) {
		this.createTime = createTime;
	}

	@Override
	public String toString() {
		return "交易对：" + pair + ", 价格：" + price + ", 时间：" + DateFormatUtil.format(createTime);
	}
	
}
