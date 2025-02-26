package com.bugbycode.module;

/**
 * 交易对枚举
 */
public enum CoinPairs {
	
	BTCUSDT("BTCUSDT",FibCode.FIB618),
	ETHUSDT("ETHUSDT",FibCode.FIB618),
	BNBUSDT("BNBUSDT",FibCode.FIB618),
	OTHER_COIN("OTHER_COIN",FibCode.FIB1);
	
	private String pair;
	private FibCode startFibCode;
	
	CoinPairs(String pair, FibCode startFibCode) {
		this.pair = pair;
		this.startFibCode = startFibCode;
	}

	public String getPair() {
		return pair;
	}

	public FibCode getStartFibCode() {
		return startFibCode;
	}
	
}
