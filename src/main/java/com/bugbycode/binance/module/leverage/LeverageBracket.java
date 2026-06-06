package com.bugbycode.binance.module.leverage;

import java.util.List;

/**
 * 杠杆分层标准
 */
public class LeverageBracket {
	
	private String symbol;
	
	private double notionalCoef;
	
	private List<LeverageBracketInfo> brackets;

	public LeverageBracket(String symbol, List<LeverageBracketInfo> brackets) {
		this.symbol = symbol;
		this.brackets = brackets;
	}

	public String getSymbol() {
		return symbol;
	}

	public void setSymbol(String symbol) {
		this.symbol = symbol;
	}

	public double getNotionalCoef() {
		return notionalCoef;
	}

	public void setNotionalCoef(double notionalCoef) {
		this.notionalCoef = notionalCoef;
	}

	public List<LeverageBracketInfo> getBrackets() {
		return brackets;
	}

	public void setBrackets(List<LeverageBracketInfo> brackets) {
		this.brackets = brackets;
	}

	@Override
	public String toString() {
		return "LeverageBracket [symbol=" + symbol + ", notionalCoef=" + notionalCoef + ", brackets=" + brackets + "]";
	}

}
