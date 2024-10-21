package com.bugbycode.module;

import org.springframework.data.annotation.Id;

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
	
	
}
