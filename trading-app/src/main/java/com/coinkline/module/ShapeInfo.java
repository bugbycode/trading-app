package com.coinkline.module;

import org.springframework.data.annotation.Id;

import com.coinkline.module.shape.ShapeType;
import com.util.DateFormatUtil;

/**
 * 图纸信息类
 */
public class ShapeInfo {

	@Id
	private String id;//mongodb数据库记录唯一标识
	
	private String owner;//创建者
	
	private String shape;//图纸名称
	
	private String symbol;//交易对
	
	private String points;//绘图坐标信息 [{price:0.1,time:1701698287},...]
	
	private String properties;//图纸渲染属性 如：颜色、用户是否可编辑、描述信息等构成的json
	
	private String price = "0";//图纸创建时最新价格

	private int longOrShortType;//做多或做空 水平射线使用 0：空 1：多
	
	private long createTime; //创建时间
	
	private long updateTime; //修改时间
	
	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getOwner() {
		return owner;
	}

	public void setOwner(String owner) {
		this.owner = owner;
	}

	public String getShape() {
		return shape;
	}

	public void setShape(String shape) {
		this.shape = shape;
	}

	public String getSymbol() {
		return symbol;
	}

	public void setSymbol(String symbol) {
		this.symbol = symbol;
	}

	public String getPoints() {
		return points;
	}

	public void setPoints(String points) {
		this.points = points;
	}

	public String getProperties() {
		return properties;
	}

	public void setProperties(String properties) {
		this.properties = properties;
	}
	
	public ShapeType getShapeType() {
		return ShapeType.resolve(this.shape);
	}
	
	public String getShapeTypeMemo() {
		return getShapeType().getMemo();
	}

	public String getPrice() {
		return price;
	}

	public void setPrice(String price) {
		this.price = price;
	}
	
	public double getPriceDoubleValue() {
		return Double.valueOf(price);
	}

	public int getLongOrShortType() {
		return longOrShortType;
	}

	public void setLongOrShortType(int longOrShortType) {
		this.longOrShortType = longOrShortType;
	}

	public long getCreateTime() {
		return createTime;
	}

	public void setCreateTime(long createTime) {
		this.createTime = createTime;
	}

	public long getUpdateTime() {
		return updateTime;
	}

	public void setUpdateTime(long updateTime) {
		this.updateTime = updateTime;
	}
	
	public String getFormatCreateTime() {
		return createTime == 0 ? "" : DateFormatUtil.format(createTime);
	}
	
	public String getFormatUpdateTime() {
		return updateTime == 0 ? "" : DateFormatUtil.format(updateTime);
	}
}
